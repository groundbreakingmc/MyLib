package com.github.groundbreakingmc.mylib.database.kv;

import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Disk-backed key-value store using append-only writes (Bitcask model).
 * <p>
 * Record format (alive):
 * [keyLen(4)][keyBytes][flag=0(1)][valueLen(4)][valueBytes]
 * <p>
 * Record format (tombstone):
 * [keyLen(4)][keyBytes][flag=1(1)]
 * <p>
 * Callers encode values and queue writes; one writer thread appends them, updates the index and
 * compacts the file. The index, the writes not appended yet and the cache change only under the
 * write lock, so a reader sees either a write still in the queue or the index entry it made.
 */
final class DiskRepository<K, V> implements KVRepository<K, V> {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final byte FLAG_ALIVE = 0;
    private static final byte FLAG_DELETED = 1;

    // The ratio alone would rewrite a small file after almost every batch, syncing it each time.
    private static final long MIN_RATIO_DEAD_BYTES = 64 * 1024;

    private static final System.Logger LOGGER = System.getLogger(DiskRepository.class.getName());

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    static final class Builder<K, V> implements KVRepository.Builder<K, V> {

        private Path path;
        private KeyValueEncoder<K> keyEncoder;
        private KeyValueEncoder<V> valueEncoder;
        private RepositoryConfig config = RepositoryConfig.defaults();

        @Override
        public Builder<K, V> path(@NotNull Path path) {
            this.path = path;
            return this;
        }

        @Override
        public Builder<K, V> keyEncoder(@NotNull KeyValueEncoder<K> encoder) {
            this.keyEncoder = encoder;
            return this;
        }

        @Override
        public Builder<K, V> valueEncoder(@NotNull KeyValueEncoder<V> encoder) {
            this.valueEncoder = encoder;
            return this;
        }

        @Override
        public Builder<K, V> config(@NotNull RepositoryConfig config) {
            this.config = config;
            return this;
        }

        @Override
        public KVRepository<K, V> build() {
            Objects.requireNonNull(this.path, "path must be set");
            Objects.requireNonNull(this.keyEncoder, "keyEncoder must be set");
            Objects.requireNonNull(this.valueEncoder, "valueEncoder must be set");
            return new DiskRepository<>(this.path, this.keyEncoder, this.valueEncoder, this.config);
        }
    }

    // -------------------------------------------------------------------------
    // Internal types
    // -------------------------------------------------------------------------

    private sealed interface Task permits Write, Stop {
    }

    /**
     * A save, or a delete if {@code bytes} is {@code null}. The value is encoded on the caller's
     * thread; the object stays for reads until the writer appends it.
     */
    private record Write<V>(ByteArrayKey key, @Nullable V value, byte @Nullable [] bytes) implements Task {
    }

    private enum Stop implements Task {
        INSTANCE
    }

    static final class ByteArrayKey {

        final byte[] data;
        private final int hash;

        ByteArrayKey(byte[] data) {
            this.data = data;
            this.hash = Arrays.hashCode(data);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ByteArrayKey other)) return false;
            return Arrays.equals(this.data, other.data);
        }

        @Override
        public int hashCode() {
            return this.hash;
        }
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private static final ThreadLocal<ByteBuffer> INT_BUF =
            ThreadLocal.withInitial(() -> ByteBuffer.allocate(4));

    private final Path path;
    private final KeyValueEncoder<K> keyEncoder;
    private final KeyValueEncoder<V> valueEncoder;
    private final RepositoryConfig config;

    // Replaced by compaction under the write lock; the file lock lasts until the channel closes.
    private FileChannel file;
    // Used by the writer thread only.
    private long end;
    private long deadBytes;

    private final BlockingQueue<Task> writeQueue = new LinkedBlockingQueue<>();
    private final Thread writerThread;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // Guarded by the lock: read under it, changed under the write lock.
    private final Object2LongMap<ByteArrayKey> offsets = new Object2LongOpenHashMap<>();
    // The latest write of each key the writer hasn't appended yet.
    private final Object2ObjectMap<ByteArrayKey, Write<V>> pending = new Object2ObjectOpenHashMap<>();
    private boolean closed;
    private @Nullable Throwable failure;

    // Concurrent: readers fill it under the read lock; saves and deletes change it under the write lock.
    private final @Nullable Map<ByteArrayKey, V> cache;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    private DiskRepository(
            @NotNull Path path,
            @NotNull KeyValueEncoder<K> keyEncoder,
            @NotNull KeyValueEncoder<V> valueEncoder,
            @NotNull RepositoryConfig config) {
        this.path = path;
        this.keyEncoder = keyEncoder;
        this.valueEncoder = valueEncoder;
        this.config = config;

        this.offsets.defaultReturnValue(-1L);
        this.cache = config.useCache() ? new ConcurrentHashMap<>() : null;

        try {
            this.file = openChannel(path);
        } catch (IOException ex) {
            throw new UncheckedIOException("Can't open " + path, ex);
        }
        try {
            this.lockFile();
            // Left by a compaction interrupted before the swap; the data file is still whole.
            Files.deleteIfExists(this.tempPath());
            this.loadOffsets();
        } catch (IOException ex) {
            this.closeQuietly(ex);
            throw new UncheckedIOException("Can't read " + path, ex);
        } catch (RuntimeException ex) {
            this.closeQuietly(ex);
            throw ex;
        }

        this.writerThread = Thread.ofVirtual().start(this::writerLoop);
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    @Override
    public void save(@NotNull K key, @NotNull V value) {
        // Encoded on the caller's thread: the file gets the value as it is at this call,
        // and an encoder error is thrown to the caller and can't stop the writer.
        final byte[] bytes = this.valueEncoder.encode(value);
        this.enqueue(new Write<>(this.keyOf(key), value, bytes));
    }

    @Override
    public void delete(@NotNull K key) {
        this.enqueue(new Write<>(this.keyOf(key), null, null));
    }

    @Override
    public Optional<V> load(@NotNull K key) {
        final ByteArrayKey arrayKey = this.keyOf(key);

        this.lock.readLock().lock();
        try {
            if (this.closed) throw new IllegalStateException("The repository is closed: " + this.path);

            if (this.cache != null) {
                final V cached = this.cache.get(arrayKey);
                if (cached != null) return Optional.of(cached);
            }

            final Write<V> queued = this.pending.get(arrayKey);
            if (queued != null) return Optional.ofNullable(queued.value());

            final long offset = this.offsets.getLong(arrayKey);
            if (offset == -1L) return Optional.empty();

            // [keyLen(4)][keyBytes][flag(1)][valueLen(4)][valueBytes]
            final long valueLenPos = offset + 4 + arrayKey.data.length + 1;
            final int valueLen = readInt(this.file, valueLenPos);

            final ByteBuffer valueBuf = ByteBuffer.allocate(valueLen);
            readFully(this.file, valueBuf, valueLenPos + 4);

            final V value = this.valueEncoder.decode(valueBuf.array());
            // No save or delete runs while the read lock is held, so this is still the latest value.
            if (this.cache != null) this.cache.put(arrayKey, value);
            return Optional.of(value);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public void unloadFromCache(@NotNull K key) {
        if (this.cache == null) return;
        this.cache.remove(this.keyOf(key));
    }

    @Override
    public void close() {
        this.lock.writeLock().lock();
        try {
            if (this.closed) return;
            this.closed = true;
            this.writeQueue.add(Stop.INSTANCE);
        } finally {
            this.lock.writeLock().unlock();
        }

        // Waits even if interrupted: closing the file under the writer would lose the queued writes.
        boolean interrupted = false;
        while (true) {
            try {
                this.writerThread.join();
                break;
            } catch (InterruptedException ex) {
                interrupted = true;
            }
        }
        if (interrupted) Thread.currentThread().interrupt();

        try {
            this.file.close();
        } catch (IOException ex) {
            throw new UncheckedIOException("Can't close " + this.path, ex);
        }

        final Throwable failure = this.failure;
        if (failure != null) {
            throw new IllegalStateException("The writer of " + this.path
                    + " had stopped; the writes queued since then weren't saved", failure);
        }
    }

    // -------------------------------------------------------------------------
    // Writer loop
    // -------------------------------------------------------------------------

    private ByteArrayKey keyOf(K key) {
        return new ByteArrayKey(this.keyEncoder.encode(key));
    }

    private void enqueue(@NotNull Write<V> write) {
        this.lock.writeLock().lock();
        try {
            if (this.closed) throw new IllegalStateException("The repository is closed: " + this.path);
            if (this.failure != null) {
                throw new IllegalStateException("The writer of " + this.path
                        + " stopped, so writes are no longer saved", this.failure);
            }

            this.pending.put(write.key(), write);
            if (this.cache != null) {
                if (write.value() != null) this.cache.put(write.key(), write.value());
                else this.cache.remove(write.key());
            }
            // Queued under the lock too, so the writer gets the writes of a key in the order
            // they became pending.
            this.writeQueue.add(write);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    private void writerLoop() {
        try {
            while (true) {
                Task task = this.writeQueue.take();
                int written = 0;
                while (task instanceof Write<?> write) {
                    this.doWrite(write);
                    if (++written == this.config.batchSize()) break;
                    task = this.writeQueue.poll();
                }
                if (task == Stop.INSTANCE) return;
                this.checkAndCompact();
            }
        } catch (Throwable th) {
            this.stopWriting(th);
        }
    }

    // Later writes are refused: they would only pile up in the queue, unsaved.
    private void stopWriting(@NotNull Throwable cause) {
        this.lock.writeLock().lock();
        try {
            this.failure = cause;
            this.writeQueue.clear();
        } finally {
            this.lock.writeLock().unlock();
        }
        LOGGER.log(System.Logger.Level.ERROR, "The writer of " + this.path + " stopped; later writes are refused", cause);
    }

    // -------------------------------------------------------------------------
    // Write logic
    // -------------------------------------------------------------------------

    private void doWrite(@NotNull Write<?> write) throws IOException {
        final byte[] keyBytes = write.key().data;
        final byte[] valueBytes = write.bytes();
        final boolean isTombstone = (valueBytes == null);

        final ByteBuffer record;
        if (isTombstone) {
            // [keyLen(4)][keyBytes][flag=1(1)]
            record = ByteBuffer.allocate(4 + keyBytes.length + 1)
                    .putInt(keyBytes.length)
                    .put(keyBytes)
                    .put(FLAG_DELETED);
        } else {
            // [keyLen(4)][keyBytes][flag=0(1)][valueLen(4)][valueBytes]
            record = ByteBuffer.allocate(4 + keyBytes.length + 1 + 4 + valueBytes.length)
                    .putInt(keyBytes.length)
                    .put(keyBytes)
                    .put(FLAG_ALIVE)
                    .putInt(valueBytes.length)
                    .put(valueBytes);
        }
        record.flip();

        final long recordOffset = this.end;
        writeBytes(this.file, record, recordOffset);
        this.end += record.limit();

        final long oldOffset;
        this.lock.writeLock().lock();
        try {
            oldOffset = isTombstone
                    ? this.offsets.removeLong(write.key())
                    : this.offsets.put(write.key(), recordOffset);
            // A newer write of the key stays pending until it is appended too.
            if (this.pending.get(write.key()) == write) this.pending.remove(write.key());
        } finally {
            this.lock.writeLock().unlock();
        }

        if (oldOffset != -1L) this.deadBytes += this.sizeOfLiveRecord(keyBytes.length, oldOffset);
        if (isTombstone) this.deadBytes += record.limit();
    }

    // [keyLen(4)][keyBytes][flag(1)][valueLen(4)][valueBytes]
    private long sizeOfLiveRecord(int keyLen, long offset) throws IOException {
        return 4L + keyLen + 1 + 4 + readInt(this.file, offset + 4 + keyLen + 1);
    }

    // -------------------------------------------------------------------------
    // Compaction
    // -------------------------------------------------------------------------

    private void checkAndCompact() throws IOException {
        if (this.deadBytes == 0) return;
        final boolean overRatio = this.deadBytes >= MIN_RATIO_DEAD_BYTES
                && (double) this.deadBytes / this.end >= this.config.compactRatio();
        final boolean overBytes = this.deadBytes >= this.config.compactBytes();
        if (overRatio || overBytes) this.compact();
    }

    private void compact() throws IOException {
        final Path tempPath = this.tempPath();

        final Object2LongMap<ByteArrayKey> newOffsets = new Object2LongOpenHashMap<>(this.offsets.size());
        newOffsets.defaultReturnValue(-1L);
        long newEnd = 0;

        // Truncated: a temp file left by an interrupted compaction may be longer than this one.
        try (FileChannel temp = FileChannel.open(tempPath,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            // The writer is the only thread that changes the index, so it reads it here without the lock.
            for (final Object2LongMap.Entry<ByteArrayKey> entry : this.offsets.object2LongEntrySet()) {
                final long oldOffset = entry.getLongValue();
                final long size = this.sizeOfLiveRecord(entry.getKey().data.length, oldOffset);
                transferFully(this.file, oldOffset, size, temp);
                newOffsets.put(entry.getKey(), newEnd);
                newEnd += size;
            }
            // On the disk before it replaces the data file, or a power loss could leave an empty one.
            temp.force(true);
        }

        this.lock.writeLock().lock();
        try {
            this.file.close();
            try {
                Files.move(tempPath, this.path,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ex) {
                // The old file is still in place, so reads go on with it.
                this.file = openChannel(this.path);
                this.lockFile();
                throw ex;
            }
            this.file = openChannel(this.path);
            this.offsets.clear();
            this.offsets.putAll(newOffsets);
            this.end = newEnd;
            this.deadBytes = 0;

            this.lockFile();
            this.syncDirectory();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    private Path tempPath() {
        return this.path.resolveSibling(this.path.getFileName() + ".tmp");
    }

    // Makes the rename itself survive a power loss.
    private void syncDirectory() {
        final Path directory = this.path.toAbsolutePath().getParent();
        if (directory == null) return;
        try (FileChannel channel = FileChannel.open(directory, StandardOpenOption.READ)) {
            channel.force(true);
        } catch (IOException ex) {
            // Windows can't open a directory as a channel; the rename is as durable as its file system makes it.
        }
    }

    // -------------------------------------------------------------------------
    // Startup
    // -------------------------------------------------------------------------

    private void loadOffsets() throws IOException {
        long pos = 0;

        while (pos < this.file.size()) {
            final int keyLen = readInt(this.file, pos);

            final ByteBuffer keyBuf = ByteBuffer.allocate(keyLen);
            readFully(this.file, keyBuf, pos + 4);

            final ByteBuffer flagBuf = ByteBuffer.allocate(1);
            readFully(this.file, flagBuf, pos + 4 + keyLen);
            final byte flag = flagBuf.get();

            final ByteArrayKey arrayKey = new ByteArrayKey(keyBuf.array());

            if (flag == FLAG_DELETED) {
                this.offsets.removeLong(arrayKey);
                pos += 4 + keyLen + 1;
            } else {
                final int valueLen = readInt(this.file, pos + 4 + keyLen + 1);
                this.offsets.put(arrayKey, pos);
                pos += 4 + keyLen + 1 + 4 + valueLen;
            }
        }

        this.end = pos;
    }

    // -------------------------------------------------------------------------
    // File lock
    // -------------------------------------------------------------------------

    // Held until the channel is closed. Within one JVM tryLock throws
    // OverlappingFileLockException, an IllegalStateException, instead of returning null.
    private void lockFile() throws IOException {
        if (this.file.tryLock(0, 1, false) == null) {
            throw new IllegalStateException("File is already in use: " + this.path);
        }
    }

    private void closeQuietly(@NotNull Throwable cause) {
        try {
            this.file.close();
        } catch (IOException ex) {
            cause.addSuppressed(ex);
        }
    }

    // -------------------------------------------------------------------------
    // IO helpers
    // -------------------------------------------------------------------------

    private static FileChannel openChannel(Path path) throws IOException {
        return FileChannel.open(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE
        );
    }

    private static int readInt(FileChannel ch, long pos) throws IOException {
        final ByteBuffer buf = INT_BUF.get();
        readFully(ch, buf, pos);
        return buf.getInt();
    }

    private static void readFully(FileChannel ch, ByteBuffer buf, long pos) throws IOException {
        buf.clear();
        long p = pos;
        while (buf.hasRemaining()) {
            final int n = ch.read(buf, p);
            if (n < 0) throw new IOException("Unexpected end of file at position " + p);
            p += n;
        }
        buf.flip();
    }

    private static void writeBytes(FileChannel ch, ByteBuffer buf, long pos) throws IOException {
        long p = pos;
        while (buf.hasRemaining()) {
            p += ch.write(buf, p);
        }
    }

    // Appends the range at the destination's position; the kernel copies it without a buffer here.
    private static void transferFully(FileChannel src, long pos, long count, FileChannel dst) throws IOException {
        long done = 0;
        while (done < count) {
            final long n = src.transferTo(pos + done, count - done, dst);
            if (n <= 0) throw new IOException("Unexpected end of file during compaction at " + (pos + done));
            done += n;
        }
    }
}

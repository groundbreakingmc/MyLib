package com.github.groundbreakingmc.mylib.database.kv;

import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
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
 */
final class DiskRepository<K, V> implements KVRepository<K, V> {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final byte FLAG_ALIVE = 0;
    private static final byte FLAG_DELETED = 1;

    private static final int CHUNK_SIZE = 8 * 1024; // 8 KB — used only in copyChunked

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

    private record WriteTask<V>(ByteArrayKey arrayKey, byte[] keyBytes, V value) {}

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

    private static final WriteTask<?> POISON = new WriteTask<>(null, null, null);

    private static final ThreadLocal<ByteBuffer> INT_BUF =
            ThreadLocal.withInitial(() -> ByteBuffer.allocate(4));

    private static final ThreadLocal<ByteBuffer> FLAG_BUF =
            ThreadLocal.withInitial(() -> ByteBuffer.allocate(1));

    // used only in copyChunked during compaction — never grows beyond CHUNK_SIZE
    private static final ThreadLocal<ByteBuffer> CHUNK_BUF =
            ThreadLocal.withInitial(() -> ByteBuffer.allocate(CHUNK_SIZE));

    private final Path path;
    private final KeyValueEncoder<K> keyEncoder;
    private final KeyValueEncoder<V> valueEncoder;
    private final RepositoryConfig config;

    private FileChannel file;
    private FileLock fileLock;
    private long end;
    private long deadBytes;

    private final BlockingQueue<WriteTask<V>> writeQueue = new LinkedBlockingQueue<>();
    private final Thread writerThread;

    private final ReadWriteLock compactLock = new ReentrantReadWriteLock();

    private final Object2LongMap<ByteArrayKey> offsets = new Object2LongOpenHashMap<>();
    private final Object2ObjectMap<ByteArrayKey, V> cache;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    private DiskRepository(
            @NotNull Path path,
            @NotNull KeyValueEncoder<K> keyEncoder,
            @NotNull KeyValueEncoder<V> valueEncoder,
            @NotNull RepositoryConfig config) {
        try {
            this.path = path;
            this.keyEncoder = keyEncoder;
            this.valueEncoder = valueEncoder;
            this.config = config;

            this.file = openChannel(path);
            this.acquireFileLock();

            this.offsets.defaultReturnValue(-1L);
            this.cache = config.useCache()
                    ? Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>())
                    : null;

            this.loadOffsets();

            this.writerThread = Thread.ofVirtual().start(this::writerLoop);
        } catch (IOException ex) {
            this.releaseFileLock();
            throw new RuntimeException(ex);
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    @Override
    public void save(@NotNull K key, @NotNull V value) {
        final byte[] keyBytes = this.keyEncoder.encode(key);
        final ByteArrayKey arrayKey = new ByteArrayKey(keyBytes);
        if (this.cache != null) this.cache.put(arrayKey, value);
        this.putTask(new WriteTask<>(arrayKey, keyBytes, value));
    }

    @Override
    public void delete(@NotNull K key) {
        final byte[] keyBytes = this.keyEncoder.encode(key);
        final ByteArrayKey arrayKey = new ByteArrayKey(keyBytes);
        if (this.cache != null) this.cache.remove(arrayKey);
        this.putTask(new WriteTask<>(arrayKey, keyBytes, null));
    }

    @Override
    public Optional<V> load(@NotNull K key) {
        final byte[] keyBytes = this.keyEncoder.encode(key);
        final ByteArrayKey arrayKey = new ByteArrayKey(keyBytes);

        if (this.cache != null) {
            final V cached = this.cache.get(arrayKey);
            if (cached != null) return Optional.of(cached);
        }

        this.compactLock.readLock().lock();
        try {
            final long offset = this.offsets.getLong(arrayKey);
            if (offset == -1L) return Optional.empty();

            // [keyLen(4)][keyBytes][flag(1)][valueLen(4)][valueBytes]
            final long valueLenPos = offset + 4 + keyBytes.length + 1;
            final int valueLen = readInt(this.file, valueLenPos);

            final ByteBuffer valueBuf = ByteBuffer.allocate(valueLen);
            readFully(this.file, valueBuf, valueLenPos + 4);

            final V value = this.valueEncoder.decode(valueBuf.array());
            if (this.cache != null) this.cache.put(arrayKey, value);
            return Optional.of(value);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            this.compactLock.readLock().unlock();
        }
    }

    @Override
    public void unloadFromCache(@NotNull K key) {
        if (this.cache == null) return;
        this.cache.remove(new ByteArrayKey(this.keyEncoder.encode(key)));
    }

    @Override
    public void close() {
        try {
            //noinspection unchecked
            this.writeQueue.put((WriteTask<V>) POISON);
            this.writerThread.join();
            this.file.close();
        } catch (InterruptedException | IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            this.releaseFileLock();
        }
    }

    // -------------------------------------------------------------------------
    // Writer loop
    // -------------------------------------------------------------------------

    private void putTask(@NotNull WriteTask<V> task) {
        try {
            this.writeQueue.put(task);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        }
    }

    private void writerLoop() {
        outer:
        while (true) {
            try {
                final WriteTask<V> first = this.writeQueue.take();
                if (first == POISON) return;
                this.doWrite(first);

                int taken = 1;
                WriteTask<V> next;
                while (taken < this.config.batchSize() && (next = this.writeQueue.poll()) != null) {
                    if (next == POISON) break outer;
                    this.doWrite(next);
                    taken++;
                }
                this.checkAndCompact();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Write logic
    // -------------------------------------------------------------------------

    private void doWrite(@NotNull WriteTask<V> task) {
        final byte[] keyBytes = task.keyBytes();
        final V value = task.value();
        final boolean isTombstone = (value == null);

        if (isTombstone) {
            // [keyLen(4)][keyBytes][flag=1(1)]
            final ByteBuffer header = ByteBuffer.allocate(4 + keyBytes.length + 1);
            header.putInt(keyBytes.length);
            header.put(keyBytes);
            header.put(FLAG_DELETED);
            header.flip();

            final long recordOffset = this.end;
            writeBytes(this.file, header, recordOffset);
            this.end += header.capacity();

            final long oldOffset = this.offsets.removeLong(task.arrayKey());
            if (oldOffset != -1L) this.deadBytes += sizeOfLiveRecord(keyBytes.length, oldOffset);
            this.deadBytes += header.capacity();
        } else {
            final byte[] valueBytes = this.valueEncoder.encode(value);

            // [keyLen(4)][keyBytes][flag=0(1)][valueLen(4)]
            final ByteBuffer header = ByteBuffer.allocate(4 + keyBytes.length + 1 + 4);
            header.putInt(keyBytes.length);
            header.put(keyBytes);
            header.put(FLAG_ALIVE);
            header.putInt(valueBytes.length);
            header.flip();

            final long recordOffset = this.end;
            writeBytes(this.file, header, recordOffset);
            writeBytes(this.file, ByteBuffer.wrap(valueBytes), recordOffset + header.capacity());
            this.end += header.capacity() + valueBytes.length;

            final long oldOffset = this.offsets.getLong(task.arrayKey());
            if (oldOffset != -1L) this.deadBytes += sizeOfLiveRecord(keyBytes.length, oldOffset);
            this.offsets.put(task.arrayKey(), recordOffset);
        }
    }

    private long sizeOfLiveRecord(int keyLen, long offset) {
        try {
            return 4L + keyLen + 1 + 4 + readInt(this.file, offset + 4 + keyLen + 1);
        } catch (IOException ex) {
            return 0L;
        }
    }

    // -------------------------------------------------------------------------
    // Compaction
    // -------------------------------------------------------------------------

    private void checkAndCompact() {
        if (this.end == 0) return;
        final boolean overRatio = (double) this.deadBytes / this.end >= this.config.compactRatio();
        final boolean overBytes = this.deadBytes >= this.config.compactBytes();
        if (overRatio || overBytes) this.compact();
    }

    private void compact() {
        final Path tempPath = this.path.resolveSibling(this.path.getFileName() + ".tmp");

        final Object2LongMap<ByteArrayKey> newOffsets = new Object2LongOpenHashMap<>();
        newOffsets.defaultReturnValue(-1L);
        long newEnd = 0;

        try (FileChannel temp = openChannel(tempPath)) {
            for (final Object2LongMap.Entry<ByteArrayKey> entry : this.offsets.object2LongEntrySet()) {
                final ByteArrayKey arrayKey = entry.getKey();
                final byte[] keyBytes = arrayKey.data;
                final long oldOffset = entry.getLongValue();
                final int keyLen = keyBytes.length;

                final int valueLen = readInt(this.file, oldOffset + 4 + keyLen + 1);

                // [keyLen(4)][keyBytes][flag(1)][valueLen(4)]
                final ByteBuffer header = ByteBuffer.allocate(4 + keyLen + 1 + 4);
                header.putInt(keyLen);
                header.put(keyBytes);
                header.put(FLAG_ALIVE);
                header.putInt(valueLen);
                header.flip();

                final long recordStart = newEnd;
                writeBytes(temp, header, newEnd);
                newEnd += header.capacity();

                // value is copied in chunks — no allocation proportional to value size
                copyChunked(this.file, oldOffset + 4 + keyLen + 1 + 4, temp, newEnd, valueLen);
                newEnd += valueLen;

                newOffsets.put(arrayKey, recordStart);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Compaction failed while writing temp file", ex);
        }

        this.compactLock.writeLock().lock();
        try {
            this.file.close();
            Files.move(tempPath, this.path,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
            this.file = openChannel(this.path);
            this.acquireFileLock();

            this.offsets.clear();
            this.offsets.putAll(newOffsets);
            this.end = newEnd;
            this.deadBytes = 0;
        } catch (IOException ex) {
            throw new RuntimeException("Compaction failed while swapping files", ex);
        } finally {
            this.compactLock.writeLock().unlock();
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

            final ByteBuffer flagBuf = FLAG_BUF.get();
            readSmall(this.file, flagBuf, pos + 4 + keyLen);
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

    private void acquireFileLock() throws IOException {
        this.fileLock = this.file.tryLock(0, 1, false);
        if (this.fileLock == null) {
            throw new IllegalStateException("File is already in use: " + this.path);
        }
    }

    private void releaseFileLock() {
        if (this.fileLock != null && this.fileLock.isValid()) {
            try {
                this.fileLock.release();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
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
        buf.clear();
        long p = pos;
        while (buf.hasRemaining()) {
            final int n = ch.read(buf, p);
            if (n < 0) throw new IOException("Unexpected end of file at position " + p);
            p += n;
        }
        buf.flip();
        return buf.getInt();
    }

    private static void readSmall(FileChannel ch, ByteBuffer buf, long pos) throws IOException {
        buf.clear();
        long p = pos;
        while (buf.hasRemaining()) {
            final int n = ch.read(buf, p);
            if (n < 0) throw new IOException("Unexpected end of file at position " + p);
            p += n;
        }
        buf.flip();
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

    private static void writeBytes(FileChannel ch, ByteBuffer buf, long pos) {
        try {
            long p = pos;
            while (buf.hasRemaining()) {
                p += ch.write(buf, p);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void copyChunked(
            FileChannel src, long srcPos,
            FileChannel dst, long dstPos,
            int length) throws IOException {
        final ByteBuffer chunk = CHUNK_BUF.get();
        int done = 0;
        while (done < length) {
            final int toRead = Math.min(CHUNK_SIZE, length - done);
            chunk.clear().limit(toRead);
            long rp = srcPos + done;
            while (chunk.hasRemaining()) {
                final int n = src.read(chunk, rp);
                if (n < 0) throw new IOException("Unexpected end of file during compaction at " + rp);
                rp += n;
            }
            chunk.flip();
            long wp = dstPos + done;
            while (chunk.hasRemaining()) {
                wp += dst.write(chunk, wp);
            }
            done += toRead;
        }
    }
}

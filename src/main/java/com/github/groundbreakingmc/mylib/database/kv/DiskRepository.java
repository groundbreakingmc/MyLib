package com.github.groundbreakingmc.mylib.database.kv;

import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
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
import java.util.zip.CRC32C;

/**
 * Disk-backed key-value store using append-only writes (Bitcask model).
 * <p>
 * The file starts with a header, {@code [magic "MLKV"(4)][version(4)]}, followed by records:
 * <pre>
 * [crc(4)][flag(1)][keyLen(4)][valueLen(4)][keyBytes][valueBytes]
 * </pre>
 * {@code crc} is the CRC-32C of the rest of the record; a tombstone has flag 1 and no value.
 * Opening the file reads it up to the first record that isn't whole or doesn't match its
 * checksum, such as one cut off by a crash, and moves the rest aside.
 * <p>
 * Callers encode records and queue them; one writer thread appends them, updates the index and
 * compacts the file. The index, the writes not appended yet and the cache change only under the
 * write lock, so a reader sees either a write still in the queue or the index entry it made.
 */
final class DiskRepository<K, V> implements KVRepository<K, V> {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final int MAGIC = 0x4D4C4B56; // "MLKV"
    private static final int VERSION = 1;
    // [magic(4)][version(4)]
    private static final int FILE_HEADER_SIZE = 8;
    // [crc(4)][flag(1)][keyLen(4)][valueLen(4)]
    private static final int RECORD_HEADER_SIZE = 13;

    private static final byte FLAG_ALIVE = 0;
    private static final byte FLAG_DELETED = 1;
    private static final byte[] NO_VALUE = new byte[0];

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
     * A save, or a delete if {@code value} is {@code null}, with its record encoded on the
     * caller's thread. The value stays for reads until the writer appends the record.
     */
    private record Write<V>(ByteArrayKey key, @Nullable V value, ByteBuffer record) implements Task {

        boolean isDelete() {
            return this.value == null;
        }
    }

    private enum Stop implements Task {
        INSTANCE
    }

    /**
     * A record read while the file opens.
     */
    private record Scanned(ByteArrayKey key, boolean alive, long size) {
    }

    /**
     * Where a value of a first-format file lies.
     */
    private record ValueSpan(long offset, int length) {
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

    private final Path path;
    private final KeyValueEncoder<K> keyEncoder;
    private final KeyValueEncoder<V> valueEncoder;
    private final RepositoryConfig config;

    // Replaced by compaction under the write lock; the file lock lasts until the channel closes.
    private FileChannel file;
    // Written by the writer thread; readers check the length of a record against it.
    private volatile long end;
    // Used by the writer thread only.
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
            this.loadFile();
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
        final ByteArrayKey arrayKey = this.keyOf(key);
        // Encoded on the caller's thread: the file gets the value as it is at this call,
        // and an encoder error is thrown to the caller and can't stop the writer.
        final ByteBuffer record = encodeRecord(FLAG_ALIVE, arrayKey.data, this.valueEncoder.encode(value));
        this.enqueue(new Write<>(arrayKey, value, record));
    }

    @Override
    public void delete(@NotNull K key) {
        final ByteArrayKey arrayKey = this.keyOf(key);
        this.enqueue(new Write<>(arrayKey, null, encodeRecord(FLAG_DELETED, arrayKey.data, NO_VALUE)));
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

            final V value = this.valueEncoder.decode(this.readValue(offset, arrayKey));
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
                if (write.isDelete()) this.cache.remove(write.key());
                else this.cache.put(write.key(), write.value());
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
                    this.append(write);
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
    // Records
    // -------------------------------------------------------------------------

    // [crc(4)][flag(1)][keyLen(4)][valueLen(4)][keyBytes][valueBytes]
    private static ByteBuffer encodeRecord(byte flag, byte[] key, byte[] value) {
        final ByteBuffer record = ByteBuffer.allocate(RECORD_HEADER_SIZE + key.length + value.length);
        record.position(4);
        record.put(flag).putInt(key.length).putInt(value.length).put(key).put(value);

        final CRC32C crc = new CRC32C();
        crc.update(record.array(), 4, record.capacity() - 4);
        record.putInt(0, (int) crc.getValue());
        return record.flip();
    }

    private void append(@NotNull Write<?> write) throws IOException {
        final ByteBuffer record = write.record();
        final int size = record.remaining();
        final long offset = this.end;
        writeBytes(this.file, record, offset);
        this.end = offset + size;

        final long oldOffset;
        this.lock.writeLock().lock();
        try {
            oldOffset = write.isDelete()
                    ? this.offsets.removeLong(write.key())
                    : this.offsets.put(write.key(), offset);
            // A newer write of the key stays pending until it is appended too.
            if (this.pending.get(write.key()) == write) this.pending.remove(write.key());
        } finally {
            this.lock.writeLock().unlock();
        }

        if (oldOffset != -1L) this.deadBytes += this.recordSize(oldOffset, write.key().data.length);
        if (write.isDelete()) this.deadBytes += size;
    }

    // Reads the value of the key's indexed record, checking that the record is whole and is the key's.
    private byte[] readValue(long offset, @NotNull ByteArrayKey key) throws IOException {
        final int keyLength = key.data.length;
        final ByteBuffer head = ByteBuffer.allocate(RECORD_HEADER_SIZE + keyLength);
        readFully(this.file, head, offset);
        final int crc = head.getInt();
        final byte flag = head.get();
        final int recordKeyLength = head.getInt();
        final int valueLength = head.getInt();

        // A damaged length mustn't allocate a huge array: the writer appends a record before
        // indexing it, so a whole record ends within end.
        if (flag != FLAG_ALIVE || recordKeyLength != keyLength
                || valueLength < 0 || valueLength > this.end - offset - head.capacity()
                || !Arrays.equals(head.array(), RECORD_HEADER_SIZE, head.capacity(), key.data, 0, keyLength)) {
            throw this.damaged(offset);
        }

        final byte[] value = new byte[valueLength];
        readFully(this.file, ByteBuffer.wrap(value), offset + head.capacity());

        final CRC32C actual = new CRC32C();
        actual.update(head.array(), 4, head.capacity() - 4);
        actual.update(value);
        if ((int) actual.getValue() != crc) throw this.damaged(offset);
        return value;
    }

    private long recordSize(long offset, int keyLength) throws IOException {
        // valueLen follows crc(4), flag(1) and keyLen(4).
        return RECORD_HEADER_SIZE + (long) keyLength + readInt(this.file, offset + 9);
    }

    private IOException damaged(long offset) {
        return new IOException("The record at offset " + offset + " of " + this.path + " is damaged");
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
        long newEnd = FILE_HEADER_SIZE;

        try (FileChannel temp = openTemp(tempPath)) {
            writeAll(temp, fileHeader());
            // The writer is the only thread that changes the index, so it reads it here without the lock.
            for (final Object2LongMap.Entry<ByteArrayKey> entry : this.offsets.object2LongEntrySet()) {
                final long oldOffset = entry.getLongValue();
                final long size = this.recordSize(oldOffset, entry.getKey().data.length);
                // Copied with its checksum, so a damaged record stays detectable.
                transferFully(this.file, oldOffset, size, temp);
                newOffsets.put(entry.getKey(), newEnd);
                newEnd += size;
            }
            // On the disk before it replaces the data file, or a power loss could leave an empty one.
            temp.force(true);
        }

        this.replaceFile(tempPath, newOffsets, newEnd);
    }

    // Puts a rewritten file in place of the data file; reads wait for the swap under the write lock.
    private void replaceFile(Path tempPath, Object2LongMap<ByteArrayKey> newOffsets, long newEnd) throws IOException {
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

    private void loadFile() throws IOException {
        final long size = this.file.size();
        if (size == 0) {
            writeBytes(this.file, fileHeader(), 0);
            this.end = FILE_HEADER_SIZE;
            return;
        }
        if (size < FILE_HEADER_SIZE || readInt(this.file, 0) != MAGIC) {
            this.convertFirstFormat(size);
            return;
        }
        final int version = readInt(this.file, 4);
        if (version != VERSION) {
            throw new IllegalStateException(this.path + " has format version " + version
                    + ", but this version of MyLib reads version " + VERSION);
        }
        this.loadRecords(size);
    }

    // Builds the index; whatever follows the last whole record is moved aside.
    private void loadRecords(long size) throws IOException {
        // Not closed: that would close the channel as well.
        final DataInputStream in = new DataInputStream(new BufferedInputStream(
                Channels.newInputStream(this.file.position(FILE_HEADER_SIZE)), 64 * 1024));
        final byte[] header = new byte[RECORD_HEADER_SIZE];
        final byte[] chunk = new byte[8 * 1024];
        final CRC32C crc = new CRC32C();

        long pos = FILE_HEADER_SIZE;
        while (pos < size) {
            final Scanned record = scanRecord(in, size - pos, header, chunk, crc);
            if (record == null) break;

            final long oldOffset = record.alive()
                    ? this.offsets.put(record.key(), pos)
                    : this.offsets.removeLong(record.key());
            if (oldOffset != -1L) this.deadBytes += this.recordSize(oldOffset, record.key().data.length);
            if (!record.alive()) this.deadBytes += record.size();
            pos += record.size();
        }

        if (pos < size) this.cutTail(pos, size);
        this.end = pos;
    }

    // Reads the next record, or returns null if the bytes left don't hold a whole record matching its checksum.
    private static @Nullable Scanned scanRecord(DataInputStream in, long left, byte[] header, byte[] chunk, CRC32C crc)
            throws IOException {
        if (left < RECORD_HEADER_SIZE) return null;
        in.readFully(header);
        final ByteBuffer fields = ByteBuffer.wrap(header);
        final int storedCrc = fields.getInt();
        final byte flag = fields.get();
        final int keyLength = fields.getInt();
        final int valueLength = fields.getInt();
        final long size = RECORD_HEADER_SIZE + (long) keyLength + valueLength;
        if ((flag != FLAG_ALIVE && flag != FLAG_DELETED) || keyLength < 0 || valueLength < 0
                || (flag == FLAG_DELETED && valueLength != 0) || size > left) {
            return null;
        }

        crc.reset();
        crc.update(header, 4, RECORD_HEADER_SIZE - 4);
        final byte[] key = new byte[keyLength];
        in.readFully(key);
        crc.update(key);
        // Only checked here: the value is read again when it is loaded.
        for (int remaining = valueLength; remaining > 0; ) {
            final int n = Math.min(chunk.length, remaining);
            in.readFully(chunk, 0, n);
            crc.update(chunk, 0, n);
            remaining -= n;
        }
        if ((int) crc.getValue() != storedCrc) return null;

        return new Scanned(new ByteArrayKey(key), flag == FLAG_ALIVE, size);
    }

    // Copies what can't be read into a file next to this one for inspection and cuts it off,
    // so appends follow whole records. Usually it is a write a crash cut off.
    private void cutTail(long from, long size) throws IOException {
        final Path aside = this.path.resolveSibling(this.path.getFileName() + ".corrupt-" + System.currentTimeMillis());
        try (FileChannel out = FileChannel.open(aside, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            transferFully(this.file, from, size - from, out);
            out.force(true);
        }
        this.file.truncate(from);
        this.file.force(true);
        LOGGER.log(System.Logger.Level.WARNING, this.path + ": " + (size - from) + " bytes from offset " + from
                + " aren't whole records, probably a write cut off by a crash; moved them to " + aside.getFileName());
    }

    // The first format had no file header and no checksums: [keyLen(4)][keyBytes][flag(1)]
    // [valueLen(4)][valueBytes], a tombstone without valueLen and value. Its live records are
    // written again in the current format.
    private void convertFirstFormat(long size) throws IOException {
        final Object2ObjectMap<ByteArrayKey, ValueSpan> live = new Object2ObjectOpenHashMap<>();
        long pos = 0;
        while (true) {
            final long left = size - pos;
            if (left < 5) break;
            final int keyLength = readInt(this.file, pos);
            if (keyLength < 0 || keyLength > left - 5) break;

            final ByteBuffer keyAndFlag = ByteBuffer.allocate(keyLength + 1);
            readFully(this.file, keyAndFlag, pos + 4);
            final ByteArrayKey key = new ByteArrayKey(Arrays.copyOf(keyAndFlag.array(), keyLength));
            final byte flag = keyAndFlag.get(keyLength);
            if (flag == FLAG_DELETED) {
                live.remove(key);
                pos += 5L + keyLength;
                continue;
            }
            if (flag != FLAG_ALIVE || left - 5 - keyLength < 4) break;

            final int valueLength = readInt(this.file, pos + 5 + keyLength);
            if (valueLength < 0 || valueLength > left - 9 - keyLength) break;
            live.put(key, new ValueSpan(pos + 9 + keyLength, valueLength));
            pos += 9L + keyLength + valueLength;
        }
        if (pos < size) this.cutTail(pos, size);

        final Path tempPath = this.tempPath();
        final Object2LongMap<ByteArrayKey> newOffsets = new Object2LongOpenHashMap<>(live.size());
        newOffsets.defaultReturnValue(-1L);
        long newEnd = FILE_HEADER_SIZE;

        try (FileChannel temp = openTemp(tempPath)) {
            writeAll(temp, fileHeader());
            for (final Map.Entry<ByteArrayKey, ValueSpan> entry : live.entrySet()) {
                final ValueSpan span = entry.getValue();
                final ByteBuffer value = ByteBuffer.allocate(span.length());
                readFully(this.file, value, span.offset());
                final ByteBuffer record = encodeRecord(FLAG_ALIVE, entry.getKey().data, value.array());
                newOffsets.put(entry.getKey(), newEnd);
                newEnd += record.remaining();
                writeAll(temp, record);
            }
            // On the disk before it replaces the only copy of the data.
            temp.force(true);
        }

        this.replaceFile(tempPath, newOffsets, newEnd);
        LOGGER.log(System.Logger.Level.INFO, this.path + ": converted to the format with checksums");
    }

    private static ByteBuffer fileHeader() {
        return ByteBuffer.allocate(FILE_HEADER_SIZE).putInt(MAGIC).putInt(VERSION).flip();
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

    // Truncated: a temp file of an earlier, failed compaction may be longer than the new one.
    private static FileChannel openTemp(Path path) throws IOException {
        return FileChannel.open(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    private static int readInt(FileChannel ch, long pos) throws IOException {
        final ByteBuffer buf = ByteBuffer.allocate(4);
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

    // Writes at the channel's position, for files written from the start.
    private static void writeAll(FileChannel ch, ByteBuffer buf) throws IOException {
        while (buf.hasRemaining()) {
            ch.write(buf);
        }
    }

    // Appends the range at the destination's position; the kernel copies it without a buffer here.
    private static void transferFully(FileChannel src, long pos, long count, FileChannel dst) throws IOException {
        long done = 0;
        while (done < count) {
            final long n = src.transferTo(pos + done, count - done, dst);
            if (n <= 0) throw new IOException("Unexpected end of file at position " + (pos + done));
            done += n;
        }
    }
}

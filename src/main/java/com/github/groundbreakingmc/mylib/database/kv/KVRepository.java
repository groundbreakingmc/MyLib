package com.github.groundbreakingmc.mylib.database.kv;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

/**
 * A disk-backed key-value store.
 *
 * <p>Obtain an instance via {@link #builder()}:
 * <pre>{@code
 * Repository<UUID, Player> players = Repository.<UUID, Player>builder()
 *     .path(Path.of("players.db"))
 *     .keyEncoder(uuid -> encodeUuid(uuid), bytes -> decodeUuid(bytes))
 *     .valueEncoder(PlayerEncoder.INSTANCE)
 *     .build();
 *
 * players.save(player.uuid(), player);
 * players.load(uuid).ifPresent(System.out::println);
 * players.close();
 * }</pre>
 *
 * <p>Writes are asynchronous — {@link #save} and {@link #delete} return immediately
 * and a background writer thread appends them to the file in order. {@link #load} sees
 * every save and delete made before it, whether the writer has appended it yet or not.
 * Every method is safe to call from any thread.
 *
 * <p>If the writer can't write, such as when the disk is full, it stops and logs the error:
 * later saves, deletes and {@link #close()} throw {@link IllegalStateException} with the cause.
 *
 * <p>Each record carries a checksum. If the file doesn't end with a whole record, such as after
 * a crash during a write, it is read up to its last whole record: the rest is moved to a
 * {@code .corrupt-<time>} file next to it and a warning is logged. Files written by earlier
 * versions of this class are converted when opened.
 *
 * <p>Only one instance may open the same file at a time. Attempting to open an
 * already-locked file throws {@link IllegalStateException}.
 *
 * @param <K> key type
 * @param <V> value type
 */
public interface KVRepository<K, V> {

    /**
     * Creates a new {@link Builder} for configuring and constructing a {@link KVRepository}.
     *
     * @param <K> key type
     * @param <V> value type
     * @return a fresh builder
     */
    static <K, V> Builder<K, V> builder() {
        return new DiskRepository.Builder<>();
    }

    /**
     * Saves the given key-value pair.
     *
     * <p>If a value for the key already exists it will be overwritten.
     * The value is encoded during this call, so later changes of the object don't reach
     * the file, and {@link #load} returns it at once; the background writer thread
     * appends it to the file later.
     *
     * @param key   the key, not null
     * @param value the value, not null
     * @throws IllegalStateException if the repository is closed or its writer has stopped
     */
    void save(@NotNull K key, @NotNull V value);

    /**
     * Deletes the value associated with the given key.
     *
     * <p>{@link #load} returns empty at once; the background writer thread appends
     * a tombstone record to the file, which the next compaction cleans up.
     *
     * @param key the key to delete, not null
     * @throws IllegalStateException if the repository is closed or its writer has stopped
     */
    void delete(@NotNull K key);

    /**
     * Loads the value associated with the given key.
     *
     * <p>If a cache is enabled and the value is present there, no disk I/O occurs.
     * Otherwise the value is read from the disk by seeking directly to the stored offset.
     *
     * @param key the key to look up, not null
     * @return an {@link Optional} containing the value, or empty if not found
     * @throws IllegalStateException       if the repository is closed
     * @throws java.io.UncheckedIOException if the record can't be read or doesn't match its checksum
     */
    Optional<V> load(@NotNull K key);

    /**
     * Removes the value for the given key from the in-memory cache without
     * deleting it from disk. Useful for evicting large or stale entries.
     *
     * <p>Has no effect if caching is disabled.
     *
     * @param key the key to evict, not null
     */
    void unloadFromCache(@NotNull K key);

    /**
     * Flushes all pending writes and closes the underlying file.
     *
     * <p>Blocks until the background writer thread finishes processing all
     * queued tasks, even if the calling thread is interrupted. After this call
     * the other methods throw {@link IllegalStateException}; closing again does nothing.
     *
     * @throws IllegalStateException if the writer had stopped, so writes queued since then weren't saved
     */
    void close();

    /**
     * Builder for constructing a {@link KVRepository}.
     *
     * @param <K> key type
     * @param <V> value type
     */
    interface Builder<K, V> {

        /**
         * Sets the path to the data file. The file is created if it does not exist.
         *
         * @param path path to the data file, not null
         * @return this builder
         */
        Builder<K, V> path(@NotNull Path path);

        /**
         * Sets the encoder used to serialize and deserialize keys.
         *
         * @param encoder key encoder, not null
         * @return this builder
         */
        Builder<K, V> keyEncoder(@NotNull KeyValueEncoder<K> encoder);

        /**
         * Sets the key encoder using separate encode and decode functions.
         *
         * @param encode function that converts a key to bytes, not null
         * @param decode function that converts bytes back to a key, not null
         * @return this builder
         */
        default Builder<K, V> keyEncoder(
                @NotNull Function<K, byte[]> encode,
                @NotNull Function<byte[], K> decode) {
            return keyEncoder(new KeyValueEncoder<>() {
                public byte[] encode(K key) {
                    return encode.apply(key);
                }

                public K decode(byte[] data) {
                    return decode.apply(data);
                }
            });
        }

        /**
         * Sets the encoder used to serialize and deserialize values.
         *
         * @param encoder value encoder, not null
         * @return this builder
         */
        Builder<K, V> valueEncoder(@NotNull KeyValueEncoder<V> encoder);

        /**
         * Sets the value encoder using separate encode and decode functions.
         *
         * @param encode function that converts a value to bytes, not null
         * @param decode function that converts bytes back to a value, not null
         * @return this builder
         */
        default Builder<K, V> valueEncoder(
                @NotNull Function<V, byte[]> encode,
                @NotNull Function<byte[], V> decode) {
            return valueEncoder(new KeyValueEncoder<>() {
                public byte[] encode(V val) {
                    return encode.apply(val);
                }

                public V decode(byte[] data) {
                    return decode.apply(data);
                }
            });
        }

        /**
         * Sets the repository configuration. Defaults to {@link RepositoryConfig#defaults()}
         * if not specified.
         *
         * @param config configuration, not null
         * @return this builder
         */
        Builder<K, V> config(@NotNull RepositoryConfig config);

        /**
         * Builds and returns the repository.
         *
         * @return a ready-to-use {@link KVRepository}
         * @throws NullPointerException  if path, keyEncoder, or valueEncoder was not set
         * @throws IllegalStateException if the target file is already locked by another instance,
         *                               or has a format version newer than this library reads
         * @throws RuntimeException      if the file cannot be opened or read
         */
        KVRepository<K, V> build();
    }
}

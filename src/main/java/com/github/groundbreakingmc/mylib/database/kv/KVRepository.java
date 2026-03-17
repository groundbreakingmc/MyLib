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
 * and are flushed to disk by a background writer thread. Reads always reflect the
 * latest saved state visible to the in-memory index.
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
     * The write is queued and flushed asynchronously by the background writer thread.
     * If a cache is enabled, the value is immediately visible via {@link #load}.
     *
     * @param key   the key, not null
     * @param value the value, not null
     */
    void save(@NotNull K key, @NotNull V value);

    /**
     * Deletes the value associated with the given key.
     *
     * <p>A tombstone record is appended to the file and the key is removed
     * from the in-memory index and cache immediately. The on-disk tombstone
     * is cleaned up during the next compaction.
     *
     * @param key the key to delete, not null
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
     * queued tasks. After this call the repository must not be used.
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
         * @throws IllegalStateException if the target file is already locked by another instance
         * @throws RuntimeException      if the file cannot be opened or read
         */
        KVRepository<K, V> build();
    }
}

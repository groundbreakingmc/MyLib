package com.github.groundbreakingmc.mylib.database.kv;

/**
 * Configuration for a {@link KVRepository}.
 *
 * <p>Use {@link #defaults()} or {@link #withCache()} for typical setups,
 * or construct directly to fine-tune every parameter:
 * <pre>{@code
 * RepositoryConfig config = new RepositoryConfig(
 *     true,               // useCache
 *     128,                // batchSize
 *     0.30,               // compactRatio
 *     100L * 1024 * 1024  // compactBytes — 100 MB
 * );
 * }</pre>
 *
 * @param useCache     whether to keep recently accessed values in memory.
 *                     Eliminates disk reads for hot keys at the cost of heap usage.
 * @param batchSize    maximum number of write tasks processed in a single writer loop
 *                     iteration. Higher values improve throughput under heavy write load
 *                     at the cost of slightly increased latency per individual write.
 *                     Must be {@code >= 1}.
 * @param compactRatio fraction of dead bytes relative to total file size that triggers
 *                     compaction. For example, {@code 0.20} means compaction runs when
 *                     at least 20% of the file is occupied by stale or deleted records.
 *                     Must be in the range {@code (0, 1)}.
 * @param compactBytes absolute number of dead bytes that triggers compaction, regardless
 *                     of the ratio. Useful for large files where a small ratio still
 *                     represents a lot of wasted space. Must be {@code >= 0}.
 */
public record RepositoryConfig(
        boolean useCache,
        int batchSize,
        double compactRatio,
        long compactBytes
) {

    static final RepositoryConfig DEFAULT = new RepositoryConfig(false, 64, 0.20, 50L * 1024 * 1024);
    static final RepositoryConfig WITH_CACHE = new RepositoryConfig(true, 64, 0.20, 50L * 1024 * 1024);

    public RepositoryConfig {
        if (batchSize < 1)
            throw new IllegalArgumentException("batchSize must be >= 1");
        if (compactRatio <= 0 || compactRatio >= 1)
            throw new IllegalArgumentException("compactRatio must be in (0, 1)");
        if (compactBytes < 0)
            throw new IllegalArgumentException("compactBytes must be >= 0");
    }

    /**
     * Returns the default configuration: no cache, batch size 64,
     * compact at 20% dead ratio or 50 MB dead bytes.
     *
     * @return default config
     */
    public static RepositoryConfig defaults() {
        return DEFAULT;
    }

    /**
     * Returns the default configuration with in-memory cache enabled.
     *
     * @return default config with cache
     */
    public static RepositoryConfig withCache() {
        return WITH_CACHE;
    }
}

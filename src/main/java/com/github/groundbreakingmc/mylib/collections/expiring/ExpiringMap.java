package com.github.groundbreakingmc.mylib.collections.expiring;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@SuppressWarnings("unused") // I don't wanna see suggestions from IDEA
public final class ExpiringMap<K, V> {

    /**
     * Our own fork join pool for ExpiringMap cache operations.
     * <p>
     * By default, Caffeine uses ForkJoinPool.commonPool().
     * Using a dedicated pool ensures that other tasks using the common pool
     * cannot block cache operations.
     */
    private static final ForkJoinPool LOADER_POOL = new ForkJoinPool();

    private final Cache<K, V> cache;

    public ExpiringMap(long duration, @NotNull("TimeUnit can not be null, but it is!") TimeUnit timeUnit) {
        this(duration, timeUnit, LOADER_POOL);
    }

    public ExpiringMap(long duration,
                       @NotNull("TimeUnit can not be null, but it is!") TimeUnit timeUnit,
                       @NotNull("Executor can not be null, but it is!") Executor executor) {
        this.cache = Caffeine.newBuilder()
                .executor(executor)
                .expireAfterWrite(duration, timeUnit)
                .build();
    }

    public V put(final K key, final V value) {
        final V old = this.get(key);
        this.cache.put(key, value);
        return old;
    }

    public V get(final K key) {
        return this.cache.getIfPresent(key);
    }

    public V getOrDefault(final K key, final V defaultValue) {
        final V value;
        return (value = this.cache.getIfPresent(key)) == null ? defaultValue : value;
    }

    public V getOrDefault(final K key, final Function<K, V> defaultValue) {
        final V value;
        return (value = this.cache.getIfPresent(key)) == null ? defaultValue.apply(key) : value;
    }

    public V computeIfAbsent(final K key,
                             final Function<? super K, ? extends V> mappingFunction) {
        return this.cache.get(key, mappingFunction);
    }

    public boolean containsKey(final K key) {
        return this.cache.getIfPresent(key) != null;
    }

    public void remove(final K key) {
        this.cache.invalidate(key);
    }

    public long size() {
        return this.cache.estimatedSize();
    }

    public void clear() {
        this.cache.invalidateAll();
    }

    public ConcurrentMap<K, V> asMap() {
        return this.cache.asMap();
    }
}

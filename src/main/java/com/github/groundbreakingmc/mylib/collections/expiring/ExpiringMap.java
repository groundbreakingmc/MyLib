package com.github.groundbreakingmc.mylib.collections.expiring;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public final class ExpiringMap<K, V> {

    private static final ForkJoinPool LOADER_POOL = new ForkJoinPool();

    private final Cache<K, V> cache;

    public ExpiringMap(final long duration, final TimeUnit unit) {
        this.cache = Caffeine.newBuilder()
                .executor(LOADER_POOL)
                .expireAfterWrite(duration, unit)
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
}

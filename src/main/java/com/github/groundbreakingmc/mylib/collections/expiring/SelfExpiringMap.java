package com.github.groundbreakingmc.mylib.collections.expiring;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.index.qual.NonNegative;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@SuppressWarnings("unused")
public class SelfExpiringMap<K, V> {

    private static final ForkJoinPool LOADER_POOL = new ForkJoinPool();

    private final Cache<K, ExpiringValue<V>> cache;
    private final TimeUnit timeUnit;

    public SelfExpiringMap(final TimeUnit timeUnit) {
        this.cache = Caffeine.newBuilder()
                .executor(LOADER_POOL)
                .expireAfter(new Expiry<K, ExpiringValue<V>>() {
                    @Override
                    public long expireAfterCreate(K k, ExpiringValue<V> expiringValue, long l) {
                        return expiringValue.duration;
                    }

                    @Override
                    public long expireAfterUpdate(K k, ExpiringValue<V> expiringValue, long currentTime, @NonNegative long currentDuration) {
                        return expiringValue.duration;
                    }

                    @Override
                    public long expireAfterRead(K k, ExpiringValue<V> expiringValue, long currentTime, @NonNegative long currentDuration) {
                        return currentDuration;
                    }
                })
                .build();

        this.timeUnit = timeUnit;
    }

    public V put(final K key, final V value, final long expiryDuration) {
        final V old = this.get(key);
        final long counted = this.timeUnit.toNanos(expiryDuration);
        this.cache.put(key, new ExpiringValue<>(value, counted));
        return old;
    }

    public V get(final K key) {
        return this.getOrDefault(key, null);
    }

    public V getOrDefault(final K key, final V defaultValue) {
        final ExpiringValue<V> value = this.cache.getIfPresent(key);
        return value != null ? value.value : defaultValue;
    }

    public V computeIfAbsent(final K key,
                             final Function<? super K, ? extends ExpiringValue<V>> mappingFunction) {
        final ExpiringValue<V> value = this.cache.get(key, mappingFunction);
        return value != null ? value.value : null;
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

    @RequiredArgsConstructor
    @Getter
    private static final class ExpiringValue<V> {

        private final V value;
        private final long duration;
    }
}

package com.github.groundbreakingmc.mylib.collections.expiring;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.index.qual.NonNegative;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@SuppressWarnings("unused")
public class SelfExpiringMap<K, V> {

    private static final ForkJoinPool LOADER_POOL = new ForkJoinPool();

    private final Cache<K, ExpiringValue<V>> cache;
    private final TimeUnit timeUnit;

    public SelfExpiringMap(@NotNull("TimeUnit can not be null, but it is!") TimeUnit timeUnit) {
        this(timeUnit, LOADER_POOL);
    }


    public SelfExpiringMap(@NotNull("TimeUnit can not be null, but it is!") TimeUnit timeUnit,
                           @NotNull("Executor can not be null, but it is!") Executor executor) {
        this.cache = Caffeine.newBuilder()
                .executor(executor)
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
                             final Function<? super K, ? extends V> mappingFunction,
                             final long expiryDuration) {
        final ExpiringValue<V> value = this.cache.get(key, (__) -> new ExpiringValue<>(mappingFunction.apply(key), expiryDuration));
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

    private record ExpiringValue<V>(V value, long duration) {
    }
}

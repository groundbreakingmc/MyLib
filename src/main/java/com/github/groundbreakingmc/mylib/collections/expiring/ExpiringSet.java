package com.github.groundbreakingmc.mylib.collections.expiring;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public final class ExpiringSet<E> {

    private final ExpiringMap<E, Long> cache;
    private final long lifetime;

    public ExpiringSet(long duration, @NotNull("TimeUnit can not be null, but it is!") TimeUnit unit) {
        this.cache = new ExpiringMap<>(duration, unit);
        this.lifetime = unit.toMillis(duration);
    }

    public ExpiringSet(long duration,
                       @NotNull("TimeUnit can not be null, but it is!") TimeUnit timeUnit,
                       @NotNull("Executor can not be null, but it is!") Executor executor) {
        this.cache = new ExpiringMap<>(duration, timeUnit, executor);
        this.lifetime = timeUnit.toMillis(duration);
    }

    public boolean add(final E item) {
        return this.cache.put(item, System.currentTimeMillis() + this.lifetime) == null;
    }

    public boolean contains(final E element) {
        final Long timeout = this.cache.get(element);
        return timeout != null && timeout > System.currentTimeMillis();
    }

    public void remove(final E element) {
        this.cache.remove(element);
    }

    public void clear() {
        this.cache.clear();
    }
}

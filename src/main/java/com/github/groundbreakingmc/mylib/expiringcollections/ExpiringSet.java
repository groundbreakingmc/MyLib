package com.github.groundbreakingmc.mylib.expiringcollections;

import java.util.concurrent.TimeUnit;

public final class ExpiringSet<E> {

    private final ExpiringMap<E, Long> cache;
    private final long lifetime;

    public ExpiringSet(final long duration, final TimeUnit unit) {
        this.cache = new ExpiringMap<>(duration, unit);
        this.lifetime = unit.toMillis(duration);
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

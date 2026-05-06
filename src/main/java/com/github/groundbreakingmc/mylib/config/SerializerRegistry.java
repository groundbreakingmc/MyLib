package com.github.groundbreakingmc.mylib.config;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps types to their {@link ConfigSerializer} implementations.
 * <p>
 * Register custom types via {@link #register} before calling
 * {@link ConfigFactory.Builder#load()}.
 * <p>
 * {@link BukkitConfig} calls {@link #registerIfAbsent} internally for Bukkit types —
 * your own registrations always take precedence.
 */
public final class SerializerRegistry {

    private final Map<Class<?>, ConfigSerializer<?>> serializers = new HashMap<>();

    /**
     * Registers (or replaces) the serializer for {@code type}.
     */
    public <T> SerializerRegistry register(Class<T> type, ConfigSerializer<T> serializer) {
        this.serializers.put(type, serializer);
        return this;
    }

    /**
     * Registers the serializer for {@code type} only if none is registered yet.
     * Used internally by {@link BukkitConfig} so user registrations always win.
     */
    public <T> SerializerRegistry registerIfAbsent(Class<T> type, ConfigSerializer<T> serializer) {
        this.serializers.putIfAbsent(type, serializer);
        return this;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> ConfigSerializer<T> get(Class<T> type) {
        return (ConfigSerializer<T>) this.serializers.get(type);
    }
}

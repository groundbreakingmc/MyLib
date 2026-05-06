package com.github.groundbreakingmc.mylib.config;

import com.github.groundbreakingmc.mylib.config.source.ConfigSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Minimal {@link ConfigSource} backed by a flat {@link Map}.
 * Used in unit tests — no file I/O, no Bukkit dependency.
 *
 * <p>Every typed accessor delegates to {@link #raw}: callers put whatever type
 * they need under a key and the accessor returns it if it matches, {@code null} otherwise.
 */
final class MapConfigSource implements ConfigSource {

    private final Map<String, Object> data;

    MapConfigSource(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    @Nullable
    public Object raw(String path) {
        return data.get(path);
    }

    @Override
    @Nullable
    public String str(String path) {
        final Object v = data.get(path);
        return v instanceof String s ? s : null;
    }

    @Override
    @Nullable
    public Boolean bool(String path) {
        final Object v = data.get(path);
        return v instanceof Boolean b ? b : null;
    }

    @Override
    @Nullable
    public Integer integer(String path) {
        final Object v = data.get(path);
        return v instanceof Integer i ? i : null;
    }

    @Override
    @Nullable
    public Long lng(String path) {
        final Object v = data.get(path);
        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        return null;
    }

    @Override
    @Nullable
    public Double decimal(String path) {
        final Object v = data.get(path);
        if (v instanceof Double d) return d;
        if (v instanceof Number n) return n.doubleValue();
        return null;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public List<?> list(String path) {
        final Object v = data.get(path);
        return v instanceof List<?> l ? l : null;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public Map<String, ?> map(String path) {
        final Object v = data.get(path);
        return v instanceof Map<?, ?> m ? (Map<String, ?>) m : null;
    }

    @Override
    @Nullable
    public ConfigSource section(String path) {
        final Object v = data.get(path);
        if (!(v instanceof Map<?, ?> m)) return null;
        final Map<String, Object> section = new java.util.HashMap<>();
        m.forEach((k, val) -> section.put(k.toString(), val));
        return new MapConfigSource(section);
    }

    @Override
    public Set<String> keys(boolean deep) {
        return data.keySet();
    }
}

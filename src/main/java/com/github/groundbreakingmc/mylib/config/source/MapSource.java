package com.github.groundbreakingmc.mylib.config.source;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link ConfigSource} backed by a plain {@link Map}{@code <String, ?>}.
 * Supports dot-separated paths via recursive map traversal.
 *
 * <p>Used internally by {@link com.github.groundbreakingmc.mylib.config.Config}
 * to wrap individual map entries from a list of sections.
 */
public final class MapSource implements ConfigSource {

    private final Map<String, ?> map;

    public MapSource(Map<String, ?> map) {
        this.map = map;
    }

    private @Nullable Object get(String path) {
        Object current = this.map;
        int start = 0;
        int dot;
        while ((dot = path.indexOf('.', start)) != -1) {
            if (!(current instanceof Map<?, ?> m)) return null;
            current = m.get(path.substring(start, dot));
            start = dot + 1;
        }
        if (!(current instanceof Map<?, ?> m)) return null;
        return m.get(path.substring(start));
    }

    @Override
    public @Nullable String str(String path) {
        final Object val = this.get(path);
        return val != null ? val.toString() : null;
    }

    @Override
    public @Nullable Boolean bool(String path) {
        final Object val = this.get(path);
        return val instanceof Boolean bool ? bool : null;
    }

    @Override
    public @Nullable Integer integer(String path) {
        final Object val = this.get(path);
        return val instanceof Number numb ? numb.intValue() : null;
    }

    @Override
    public @Nullable Long lng(String path) {
        final Object val = this.get(path);
        return val instanceof Number numb ? numb.longValue() : null;
    }

    @Override
    public @Nullable Double decimal(String path) {
        final Object val = this.get(path);
        return val instanceof Number numb ? numb.doubleValue() : null;
    }

    @Override
    public @Nullable List<?> list(String path) {
        final Object val = this.get(path);
        return val instanceof List<?> list ? list : null;
    }

    @Override
    public @Nullable Map<String, ?> map(String path) {
        final Object val = this.get(path);
        if (!(val instanceof Map<?, ?> m)) return null;
        @SuppressWarnings("unchecked") final Map<String, ?> typed = (Map<String, ?>) m;
        return typed;
    }

    @Override
    public @Nullable ConfigSource section(String path) {
        final Object val = this.get(path);
        if (!(val instanceof Map<?, ?> m)) return null;
        @SuppressWarnings("unchecked") final Map<String, ?> typed = (Map<String, ?>) m;
        return new MapSource(typed);
    }

    @Override
    public @Nullable Object raw(String path) {
        return this.get(path);
    }

    @Override
    public Set<String> keys(boolean deep) {
        if (!deep) return new LinkedHashSet<>(this.map.keySet());
        final Set<String> result = new LinkedHashSet<>();
        collectKeys(this.map, "", result);
        return result;
    }

    private static void collectKeys(Map<String, ?> map, String prefix, Set<String> out) {
        for (final Map.Entry<String, ?> entry : map.entrySet()) {
            final String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            out.add(key);
            if (entry.getValue() instanceof Map<?, ?> child) {
                @SuppressWarnings("unchecked") final Map<String, ?> typed = (Map<String, ?>) child;
                collectKeys(typed, key, out);
            }
        }
    }
}

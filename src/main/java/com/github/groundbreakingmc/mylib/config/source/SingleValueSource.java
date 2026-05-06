package com.github.groundbreakingmc.mylib.config.source;

import com.github.groundbreakingmc.mylib.config.Config;
import com.github.groundbreakingmc.mylib.config.ConfigSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Wraps a single raw value under the key {@code "value"}.
 * Used internally by {@link Config}
 * to feed list/map elements into a {@link ConfigSerializer}.
 */
public final class SingleValueSource implements ConfigSource {

    private final Object value;

    public SingleValueSource(Object value) {
        this.value = value;
    }

    @Override
    public @Nullable String str(String path) {
        return "value".equals(path) && this.value != null ? this.value.toString() : null;
    }

    @Override
    public @Nullable Boolean bool(String path) {
        return "value".equals(path) && this.value instanceof Boolean bool ? bool : null;
    }

    @Override
    public @Nullable Integer integer(String path) {
        return "value".equals(path) && this.value instanceof Number numb ? numb.intValue() : null;
    }

    @Override
    public @Nullable Long lng(String path) {
        return "value".equals(path) && this.value instanceof Number numb ? numb.longValue() : null;
    }

    @Override
    public @Nullable Double decimal(String path) {
        return "value".equals(path) && this.value instanceof Number numb ? numb.doubleValue() : null;
    }

    @Override
    public @Nullable List<?> list(String path) {
        return "value".equals(path) && this.value instanceof List<?> list ? list : null;
    }

    @Override
    public @Nullable Map<String, ?> map(String path) {
        if (!"value".equals(path) || !(this.value instanceof Map<?, ?> map)) return null;
        @SuppressWarnings("unchecked") final Map<String, ?> typed = (Map<String, ?>) map;
        return typed;
    }

    @Override
    public @Nullable ConfigSource section(String path) {
        return null;
    }

    @Override
    public @Nullable Object raw(String path) {
        return "value".equals(path) ? this.value : null;
    }

    @Override
    public Set<String> keys(boolean deep) {
        return Collections.singleton("value");
    }
}

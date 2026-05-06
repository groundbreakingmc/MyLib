package com.github.groundbreakingmc.mylib.config.source;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Backend-agnostic raw read interface.
 * <p>
 * All methods return {@code null} when the path is absent — no defaults,
 * no throwing. That is the responsibility of {@link com.github.groundbreakingmc.mylib.config.Config}.
 * <p>
 * Implementations: {@link BukkitYamlSource}, {@link ConfigurateSource}, {@link LightbendSource}.
 */
public interface ConfigSource {

    @Nullable String str(String path);

    @Nullable Boolean bool(String path);

    @Nullable Integer integer(String path);

    @Nullable Long lng(String path);

    @Nullable Double decimal(String path);

    /**
     * Raw list — elements may be String, Number, Boolean, or nested Map/List.
     */
    @Nullable List<?> list(String path);

    /**
     * Raw map — values may be String, Number, Boolean, or nested Map/List.
     */
    @Nullable Map<?, ?> map(String path);

    /**
     * Returns a child source, or {@code null} if the section doesn't exist.
     */
    @Nullable ConfigSource section(String path);

    /**
     * Returns the raw value at the given path as an Object, or {@code null} if absent.
     */
    @Nullable Object raw(String path);

    Set<String> keys(boolean deep);
}

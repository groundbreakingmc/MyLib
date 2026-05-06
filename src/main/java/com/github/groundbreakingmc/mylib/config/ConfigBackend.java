package com.github.groundbreakingmc.mylib.config;

import com.github.groundbreakingmc.mylib.config.source.ConfigSource;

import java.io.File;

/**
 * Loads a {@link ConfigSource} from a file.
 * <p>
 * The source is the backend concern — <em>how</em> to read data.
 * Wrapping it in a {@link Config} (platform concern — <em>what API</em> to expose)
 * is done by {@link ConfigFactory}.
 */
@FunctionalInterface
public interface ConfigBackend {

    ConfigSource load(File file);
}

package com.github.groundbreakingmc.mylib.config;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Brings an outdated config file up to the version of its default, keeping what the user set.
 * {@link ConfigFactory.Builder#updater(ConfigUpdater)} uses it instead of replacing the file.
 */
@FunctionalInterface
public interface ConfigUpdater {

    /**
     * Rewrites a file so it has everything its default has, the default's version included.
     *
     * @param file     the user's file, which exists
     * @param defaults the default file, such as the plugin's resource
     * @throws IOException if a file cannot be read or written
     */
    void update(@NotNull File file, @NotNull InputStream defaults) throws IOException;
}

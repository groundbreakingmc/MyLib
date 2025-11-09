package com.github.groundbreakingmc.mylib.colorizer;

import org.jetbrains.annotations.Nullable;

/**
 * Base interface for text colorization.
 * <p>
 * Defines a generic contract for transforming text strings with color codes
 * into their processed representation. Implementations may target different
 * output formats such as strings or components.
 *
 * @param <R> the type of the colorized result
 * @author groundbreakingmc
 * @since 1.0.0
 */
public interface Colorizer<R> {

    /**
     * Colorizes the given message by processing color codes.
     * <p>
     * The specific behavior depends on the implementation. Common use cases include:
     * <ul>
     *   <li>Converting hex color codes (e.g., &amp;#ff5555) to Minecraft format</li>
     *   <li>Translating alternate color char (&amp;) to section sign (§)</li>
     *   <li>Processing MiniMessage tags</li>
     * </ul>
     *
     * @param message the message to colorize, may be null or empty
     * @return the colorized result, or null/empty if input was null/empty
     */
    R colorize(@Nullable String message);
}

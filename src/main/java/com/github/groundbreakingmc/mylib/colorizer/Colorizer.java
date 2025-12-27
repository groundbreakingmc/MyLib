package com.github.groundbreakingmc.mylib.colorizer;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

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

    /**
     * Removes all color codes and formatting from the colorized result,
     * returning plain text.
     * <p>
     * This method reverses the effects of {@link #colorize(String)} by stripping
     * all color codes, formatting codes, and other decorations from the result,
     * leaving only the raw text content.
     * <p>
     * Common use cases include:
     * <ul>
     *   <li>Extracting plain text for logging or storage</li>
     *   <li>Comparing text content without formatting</li>
     *   <li>Calculating the actual length of displayed text</li>
     *   <li>Converting formatted content back to raw input</li>
     * </ul>
     *
     * @param colorized the colorized result to strip. Can be null or empty
     * @return the plain text without any color codes or formatting,
     * or null/empty if input was null/empty
     */
    @UnknownNullability
    String decolorize(@Nullable R colorized);
}

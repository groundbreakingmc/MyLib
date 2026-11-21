package com.github.groundbreakingmc.mylib.colorizer;

import com.github.groundbreakingmc.mylib.colorizer.string.FastHexStringColorizer;
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

    /**
     * Returns the raw representation of the colorized result, reflecting
     * how the text looked before colorization was applied.
     * <p>
     * This preserves all original symbols such as {@code &}-based codes,
     * MiniMessage tags, or other custom formatting sequences.
     * <p>
     * Example for {@link FastHexStringColorizer}:
     * <pre>
     * toRaw(colorize("&amp;#ff5555Hello &amp;aWorld")) == "&amp;#ff5555Hello &amp;aWorld"
     * </pre>
     *
     * @param colorized the colorized result; may be null
     * @return the raw, pre-colorized representation, or null if input was null
     */
    String toRaw(@Nullable R colorized);

    /**
     * Returns the given message as plain text, with all color codes,
     * formatting codes, and other non-visible elements removed.
     * <p>
     * Useful for logging, text comparison, or calculating visible lengths
     * without any formatting.
     * <p>
     * Example for {@link FastHexStringColorizer}:
     * <pre>
     * stripColors("&amp;#ff5555Hello &amp;aWorld") == "Hello World"
     * </pre>
     *
     * @param message the message to strip; may be null
     * @return plain text with all formatting removed, or null if input was null
     */
    String stripColors(@Nullable R message);

    /**
     * Returns the number of visible characters in the given message.
     * <p>
     * Visible characters are defined as characters that are not part of any
     * color codes, formatting sequences, or other non-text elements supported
     * by the implementation.
     * <p>
     * The calculation is performed on the <b>raw input</b> (e.g. containing
     * {@code &}-based codes or MiniMessage tags), not on a pre-colorized result.
     * <p>
     * Typically equivalent to {@code stripColors(message).length()},
     * but may be optimized to avoid intermediate allocations.
     * <p>
     * Example for {@link FastHexStringColorizer}:
     * <pre>
     * visualLength("&amp;#ff5555Hello &amp;aWorld") == 11
     * </pre>
     *
     * @param message the raw message; may be null or empty
     * @return the number of visible (non-formatting) characters,
     * or {@code 0} if the input is null or empty
     */
    int visualLength(@Nullable String message);
}

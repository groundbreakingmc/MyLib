package com.github.groundbreakingmc.mylib.colorizer;

import com.github.groundbreakingmc.mylib.colorizer.string.AdvancedStringColorizer;
import com.github.groundbreakingmc.mylib.colorizer.string.BasicStringColorizer;
import com.github.groundbreakingmc.mylib.colorizer.string.FastHexStringColorizer;
import com.github.groundbreakingmc.mylib.colorizer.string.MiniMessageStringColorizer;

/**
 * Specialized colorizer interface for string-based colorization.
 * <p>
 * This interface extends the base {@link Colorizer} interface with a fixed
 * return type of {@link String}, making it suitable for implementations that
 * process text strings and return colored string results.
 * <p>
 * Common implementations include:
 * <ul>
 *   <li>{@link BasicStringColorizer} – legacy color codes (e.g. {@code &a}, {@code &l})</li>
 *   <li>{@link FastHexStringColorizer} – legacy + hex format (e.g. {@code &#rrggbb})</li>
 *   <li>{@link AdvancedStringColorizer} – legacy + short hex format (e.g. {@code &##rgb})</li>
 *   <li>{@link MiniMessageStringColorizer} – MiniMessage tags (e.g. {@code <red>}, {@code <bold>})</li>
 * </ul>
 *
 * @author groundbreakingmc
 * @see Colorizer
 * @since 1.0.0
 */
public interface StringColorizer extends Colorizer<String> {
}

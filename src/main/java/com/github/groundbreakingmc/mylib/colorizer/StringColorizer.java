package com.github.groundbreakingmc.mylib.colorizer;

import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.colorizer.string.AdvancedStringColorizer;
import com.github.groundbreakingmc.mylib.colorizer.string.BasicStringColorizer;
import com.github.groundbreakingmc.mylib.colorizer.string.HexStringColorizer;
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
 *   <li>{@link HexStringColorizer} - Hex code conversion</li>
 *   <li>{@link AdvancedStringColorizer} - Advanced hex formats</li>
 *   <li>{@link BasicStringColorizer} - Basic color code translation</li>
 *   <li>{@link MiniMessageStringColorizer} - MiniMessage to legacy conversion</li>
 * </ul>
 *
 * @author groundbreakingmc
 * @see Colorizer
 * @since 1.0.0
 */
public interface StringColorizer extends Colorizer<String> {
}

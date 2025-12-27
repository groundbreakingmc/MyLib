package com.github.groundbreakingmc.mylib.colorizer.string;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import static com.github.groundbreakingmc.mylib.colorizer.ColorCodesTranslator.*;

/**
 * High-performance hex colorizer using manual parsing instead of regex.
 * <p>
 * This colorizer converts hex color codes in the format {@code &#rrggbb} to
 * Minecraft's internal hex format without using regular expressions, providing
 * better performance for strings with multiple color codes.
 * <p>
 * Example transformation:
 * <pre>
 * Input:  "&amp;#ff5555Hello &amp;#00ff00World"
 * Output: "§x§f§f§5§5§5§5Hello §x§0§0§f§f§0§0World"
 * </pre>
 * <p>
 * <b>Performance:</b> Faster than regex-based approach
 * for strings with multiple hex codes due to:
 * <ul>
 *   <li>Single-pass parsing without backtracking</li>
 *   <li>No Matcher object allocation</li>
 *   <li>Direct character manipulation</li>
 *   <li>Optimized hex validation</li>
 * </ul>
 *
 * @author groundbreakingmc
 * @see HexStringColorizer
 * @since 2.0.0
 */
public class FastHexStringColorizer implements StringColorizer {

    private static final char HEX_MARKER = '#';

    /**
     * Colorizes the message by converting hex codes and alternate color codes.
     * <p>
     * Processing approach:
     * <ol>
     *   <li>Iterates through the string looking for {@code &} characters</li>
     *   <li>If followed by {@code &#rrggbb}, validates and converts to {@code §x§r§r§g§g§b§b}</li>
     *   <li>If followed by a color character, converts {@code &} to {@code §} in the array</li>
     *   <li>Builds the result by appending segments and converted hex codes</li>
     * </ol>
     * <p>
     * This implementation avoids regex overhead by using direct character manipulation
     * and validates hex codes manually for better performance.
     *
     * @param message the message to colorize, may be null or empty
     * @return the colorized message, or the original if null/empty
     */
    @Override
    public String colorize(@Nullable String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final char[] chars = message.toCharArray();
        final int length = chars.length;

        final StringBuilder builder = new StringBuilder(length + 32);
        char[] hex = null;

        int start = 0, end;
        loop:
        for (int i = 0; i < length - 1; ) {
            final char ch = chars[i];
            if (ch == ALT_COLOR_CHAR) {
                char nextChar = chars[++i];
                if (nextChar == HEX_MARKER) {
                    if (i + 6 >= length) break;
                    if (hex == null) { // lazy initialization
                        hex = new char[14];
                        hex[0] = MC_COLOR_CHAR;
                        hex[1] = 'x';
                    }
                    end = i - 1;
                    for (int j = 0, hexI = 1; j < 6; j++) {
                        final char hexChar = chars[++i];
                        if (!isHexCharacter(hexChar)) {
                            continue loop;
                        }
                        hex[++hexI] = MC_COLOR_CHAR;
                        hex[++hexI] = hexChar;
                    }
                    builder.append(chars, start, end - start).append(hex);
                    start = i + 1;
                } else if ((nextChar == 'x' || nextChar == 'X') && i + 13 < chars.length) {
                    i = processHexColorCode(chars, i);
                } else if (isColorCharacter(nextChar)) {
                    chars[i - 1] = MC_COLOR_CHAR;
                    chars[i] = (char) (nextChar | 0x20); // quick version of 'to lower case' for character
                }
            }
            ++i;
        }

        builder.append(chars, start, length - start);
        return builder.toString();
    }

    /**
     * Decolorizes the message by converting Minecraft color format back to &amp;#rrggbb format.
     * <p>
     * Delegates to {@link FastHexStringDecolorizer} for efficient conversion of:
     * <ul>
     *   <li>Hex colors: §x§r§r§g§g§b§b → &amp;#rrggbb</li>
     *   <li>Standard codes: §a, §l, etc. → &amp;a, &amp;l, etc.</li>
     * </ul>
     *
     * @param colorized the colorized message to decolorize, may be null or empty
     * @return the message with &amp; and &amp;# color codes, or the original if null/empty
     */
    @Override
    public @UnknownNullability String decolorize(@Nullable String colorized) {
        return FastHexStringDecolorizer.decolorize(colorized);
    }
}

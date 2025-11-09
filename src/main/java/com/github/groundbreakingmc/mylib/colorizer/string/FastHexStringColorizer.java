package com.github.groundbreakingmc.mylib.colorizer.string;

import com.github.groundbreakingmc.mylib.colorizer.ColorCodesTranslator;
import org.jetbrains.annotations.Nullable;

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
            if (ch == ColorCodesTranslator.ALT_COLOR_CHAR) {
                final char nextChar = chars[++i];
                if (nextChar == HEX_MARKER) {
                    if (i + 6 >= length) break;
                    if (hex == null) { // lazy initialization
                        hex = new char[14];
                        hex[0] = ColorCodesTranslator.MC_COLOR_CHAR;
                        hex[1] = 'x';
                    }
                    end = i - 1;
                    for (int j = 0, hexI = 1; j < 6; j++) {
                        final char hexChar = chars[++i];
                        if (!isHexCharacter(hexChar)) {
                            continue loop;
                        }
                        hex[++hexI] = ColorCodesTranslator.MC_COLOR_CHAR;
                        hex[++hexI] = hexChar;
                    }
                    builder.append(chars, start, end - start).append(hex);
                    start = i + 1;
                } else {
                    if (ColorCodesTranslator.isColorCharacter(nextChar)) {
                        chars[i - 1] = ColorCodesTranslator.MC_COLOR_CHAR;
                        chars[i] = (char) (nextChar | 0x20); // quick version of 'to lower case' for character
                    }
                }
            }
            ++i;
        }

        builder.append(chars, start, length - start);
        return builder.toString();
    }

    /**
     * Checks if a character is a valid hexadecimal digit.
     * <p>
     * Valid characters are: 0-9, a-f, A-F (case-insensitive).
     * Uses a switch expression for optimal performance.
     *
     * @param ch the character to check
     * @return true if the character is a valid hex digit, false otherwise
     */
    public static boolean isHexCharacter(final char ch) {
        return switch (ch) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    'a', 'A', 'b', 'B', 'c', 'C', 'd', 'D', 'e', 'E', 'f', 'F' -> true;
            default -> false;
        };
    }
}

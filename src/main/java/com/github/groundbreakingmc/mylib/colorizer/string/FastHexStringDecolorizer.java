package com.github.groundbreakingmc.mylib.colorizer.string;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import static com.github.groundbreakingmc.mylib.colorizer.ColorCodesTranslator.ALT_COLOR_CHAR;
import static com.github.groundbreakingmc.mylib.colorizer.ColorCodesTranslator.MC_COLOR_CHAR;

/**
 * High-performance decolorizer that converts Minecraft's internal color format back to readable format.
 * <p>
 * This decolorizer performs the reverse operation of colorizers, converting:
 * <ul>
 *   <li>Minecraft hex format (§x§r§r§g§g§b§b) back to readable format (&amp;#rrggbb)</li>
 *   <li>Section sign color codes (§a) back to ampersand format (&amp;a)</li>
 * </ul>
 * <p>
 * Example transformation:
 * <pre>
 * Input:  "§x§f§f§5§5§5§5Hello §aWorld"
 * Output: "&amp;#ff5555Hello &amp;aWorld"
 * </pre>
 * <p>
 * <b>Performance:</b> Uses single-pass parsing with manual character manipulation
 * for optimal performance. Validates hex sequences before conversion.
 *
 * @author groundbreakingmc
 * @see FastHexStringColorizer
 * @since 1.0.0
 */
public final class FastHexStringDecolorizer {

    private static final char HEX_MARKER = '#';

    /**
     * Decolorizes the message by converting Minecraft color format to readable format.
     * <p>
     * Processing approach:
     * <ol>
     *   <li>Iterates through the string looking for {@code §} characters</li>
     *   <li>If followed by {@code §x§r§r§g§g§b§b}, validates and converts to {@code &#rrggbb}</li>
     *   <li>If followed by a color character, converts {@code §} to {@code &}</li>
     *   <li>Invalid sequences are preserved in the output</li>
     * </ol>
     * <p>
     * This implementation validates the complete hex sequence before converting it,
     * ensuring only valid Minecraft hex colors are transformed.
     *
     * @param message the message to decolorize, may be null or empty
     * @return the decolorized message with readable color codes, or the original if null/empty
     */
    public static @UnknownNullability String decolorize(@Nullable String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final char[] chars = message.toCharArray();
        final int length = chars.length;
        final StringBuilder builder = new StringBuilder(length);

        char[] hex = null;
        int start = 0;

        for (int i = 0; i < length - 1; i++) {
            if (chars[i] == MC_COLOR_CHAR) {
                char nextChar = chars[i + 1];

                if (nextChar == 'x' && i + 13 < length) {
                    if (hex == null) {
                        hex = new char[8];
                        hex[0] = ALT_COLOR_CHAR;
                        hex[1] = HEX_MARKER;
                    }

                    int pos = i + 2;
                    int hexIndex = 2;
                    boolean valid = true;

                    for (int j = 0; j < 6; j++) {
                        if (pos + 1 >= length || chars[pos] != MC_COLOR_CHAR) {
                            valid = false;
                            break;
                        }
                        char hexChar = chars[pos + 1];
                        if (!isHexCharacter(hexChar)) {
                            if (isColorCharacter(hexChar)) {
                                chars[pos] = ALT_COLOR_CHAR;
                            }
                            valid = false;
                            break;
                        }
                        hex[hexIndex++] = hexChar;
                        pos += 2;
                    }

                    if (valid) {
                        builder.append(chars, start, i - start);
                        builder.append(hex, 0, 8);
                        i = pos - 1;
                        start = pos;
                    }
                } else if (isColorCharacter(nextChar)) {
                    chars[i] = ALT_COLOR_CHAR;
                }
            }
        }

        builder.append(chars, start, length - start);
        return builder.toString();
    }

    /**
     * Checks if the character is a valid Minecraft color or format code.
     * <p>
     * Valid characters include:
     * <ul>
     *   <li>Color codes: 0-9, a-f (hex digits for standard colors)</li>
     *   <li>Format codes: k (obfuscated), l (bold), m (strikethrough), n (underline), o (italic), r (reset)</li>
     * </ul>
     *
     * @param ch the character to check
     * @return true if the character is a valid color or format code, false otherwise
     */
    public static boolean isColorCharacter(char ch) {
        return switch (ch) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    'a', 'b', 'c', 'd', 'e', 'f',
                    'l', 'm', 'n', 'o', 'r', 'k' -> true;
            default -> false;
        };
    }

    /**
     * Checks if the character is a valid hexadecimal digit.
     * <p>
     * Accepts digits 0-9 and letters a-f (case-insensitive is handled by caller).
     * Used for validating hex color codes.
     *
     * @param ch the character to check
     * @return true if the character is a valid hex digit (0-9, a-f), false otherwise
     */
    public static boolean isHexCharacter(final char ch) {
        return switch (ch) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    'a', 'b', 'c', 'd', 'e', 'f' -> true;
            default -> false;
        };
    }
}
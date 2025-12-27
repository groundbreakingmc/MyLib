package com.github.groundbreakingmc.mylib.colorizer;

import lombok.experimental.UtilityClass;

/**
 * Utility class for translating alternate color codes to Minecraft format.
 * <p>
 * This class provides methods to convert ampersand-based color codes (&amp;) to
 * Minecraft's section sign (§) format, which is used internally by the game.
 * <p>
 * Supported color and format codes include:
 * <ul>
 *   <li>0-9, a-f, A-F - Standard Minecraft colors</li>
 *   <li>k-o, K-O - Format codes (obfuscated, bold, strikethrough, underline, italic)</li>
 *   <li>r, R - Reset formatting</li>
 *   <li>x, X - Hex color prefix</li>
 * </ul>
 *
 * @author groundbreakingmc
 * @since 1.0.0
 */
@UtilityClass
public final class ColorCodesTranslator {

    /**
     * The alternate color character used in user-facing strings.
     */
    public static final char ALT_COLOR_CHAR = '&';

    /**
     * The Minecraft color character (section sign) used internally by the game.
     */
    public static final char MC_COLOR_CHAR = '§';

    public static final char HEX_MARKER = 'x';

    /**
     * Translates alternate color codes to Minecraft format.
     * <p>
     * Converts all occurrences of '&amp;' followed by a valid color character
     * to the Minecraft section sign format '§'. Letters are normalized to lowercase.
     * <p>
     * Example: {@code "&aHello &cWorld"} → {@code "§aHello §cWorld"}
     *
     * @param textToTranslate the text containing alternate color codes
     * @return the text with translated color codes
     * @throws NullPointerException if textToTranslate is null
     */
    public static String translateAlternateColorCodes(String textToTranslate) {
        final char[] charArray = textToTranslate.toCharArray();
        for (int i = 0; i < charArray.length - 1; i++) {
            if (charArray[i] == ALT_COLOR_CHAR) {
                char nextChar = charArray[i + 1];
                if ((nextChar == HEX_MARKER || nextChar == 'X') && i + 13 < charArray.length) {
                    i = processHexColorCode(charArray, i + 1);
                } else if (isColorCharacter(nextChar)) {
                    charArray[i] = MC_COLOR_CHAR;
                    charArray[++i] = (char) (nextChar | 0x20);
                }
            }
        }

        return new String(charArray);
    }

    /**
     * Checks if a character is a valid Minecraft color or format code.
     * <p>
     * Valid characters include:
     * <ul>
     *   <li>0-9: Standard colors (black, dark blue, etc.)</li>
     *   <li>a-f, A-F: Standard colors (green to red)</li>
     *   <li>l, L: Bold</li>
     *   <li>m, M: Strikethrough</li>
     *   <li>n, N: Underline</li>
     *   <li>o, O: Italic</li>
     *   <li>r, R: Reset</li>
     *   <li>k, K: Obfuscated</li>
     * </ul>
     * Note: 'x' is intentionally excluded as it requires special handling for hex colors.
     *
     * @param ch the character to check
     * @return true if the character is a valid color code, false otherwise
     */
    public static boolean isColorCharacter(char ch) {
        return switch (ch) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    'a', 'A', 'b', 'B', 'c', 'C', 'd', 'D', 'e', 'E', 'f', 'F',
                    'l', 'L', 'm', 'M', 'n', 'N', 'o', 'O', 'r', 'R', 'k', 'K' -> true;
            default -> false;
        };
    }

    /**
     * Checks if a character is a valid hexadecimal digit.
     * <p>
     * Valid characters are: 0-9, a-f, A-F (case-insensitive).
     * Use a switch expression for optimal performance.
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


    /**
     * Processes hex color code sequence starting at the given position.
     * <p>
     * Validates and converts &amp;x&amp;R&amp;R&amp;G&amp;G&amp;B&amp;B pattern to §x§r§r§g§g§b§b.
     * Modifies the char array in-place and returns the new position after processing.
     *
     * @param chars    the character array to process
     * @param startPos position of 'x' or 'X' character (after initial '&amp;')
     * @return new position after a hex sequence, or original position if invalid
     */
    public static int processHexColorCode(char[] chars, int startPos) {
        int i = startPos + 1; // skip 'x'/'X'
        int changed = 0;

        for (; i < chars.length - 1; i++) {
            if (chars[i] == ALT_COLOR_CHAR) {
                char nextChar = chars[i + 1];
                if (isHexCharacter(nextChar)) {
                    chars[i] = MC_COLOR_CHAR;
                    chars[++i] = (char) (nextChar | 0x20);
                    changed++;
                    continue;
                } else if (isColorCharacter(nextChar)) {
                    chars[i] = MC_COLOR_CHAR;
                    chars[++i] = (char) (nextChar | 0x20);
                    continue;
                }
            }
            break;
        }

        if (changed == 6) {
            // Convert initial &x to §x
            chars[startPos - 1] = MC_COLOR_CHAR;
            chars[startPos] = HEX_MARKER;
        }

        return i;
    }
}

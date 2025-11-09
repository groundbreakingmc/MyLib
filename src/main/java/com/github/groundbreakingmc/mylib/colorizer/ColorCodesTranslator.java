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
        int i = 0;
        while (i < charArray.length - 1) {
            if (charArray[i] == ALT_COLOR_CHAR) {
                final char nextChar = charArray[i + 1];
                if (isColorCharacter(nextChar)) {
                    charArray[i] = MC_COLOR_CHAR;
                    charArray[++i] = (char) (nextChar | 0x20);
                }
            }
            i++;
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
     *   <li>k-o, K-O: Format codes</li>
     *   <li>r, R: Reset</li>
     *   <li>x, X: Hex color prefix</li>
     * </ul>
     *
     * @param ch the character to check
     * @return true if the character is a valid color code, false otherwise
     */
    public static boolean isColorCharacter(final char ch) {
        return switch (ch) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    'a', 'A', 'b', 'B', 'c', 'C', 'd', 'D', 'e', 'E', 'f', 'F',
                    'r', 'R', 'k', 'K', 'm', 'M', 'n', 'N', 'o', 'O', 'x', 'X' -> true;
            default -> false;
        };
    }
}

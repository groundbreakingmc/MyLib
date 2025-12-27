package com.github.groundbreakingmc.mylib.colorizer.string;

import com.github.groundbreakingmc.mylib.colorizer.ColorCodesTranslator;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import static com.github.groundbreakingmc.mylib.colorizer.ColorCodesTranslator.*;

/**
 * Basic colorizer that only translates alternate color codes (&amp;) to section signs (§).
 * Does NOT support hex colors.
 * <p>
 * This is the simplest colorizer implementation that performs a single operation:
 * converting ampersand-based color codes ({@code &}) to Minecraft's section sign
 * format ({@code §}).
 * <p>
 * Example transformation:
 * <pre>
 * Input:  "&amp;aHello &amp;cWorld &amp;r&amp;lBold"
 * Output: "§aHello §cWorld §r§lBold"
 * </pre>
 * <p>
 * This colorizer does NOT process:
 * <ul>
 *   <li>Hex color codes (&amp;#rrggbb)</li>
 *   <li>MiniMessage tags</li>
 *   <li>Any other advanced formatting</li>
 * </ul>
 * <p>
 * Use this colorizer when you only need basic Minecraft color code translation
 * without hex support.
 *
 * @author groundbreakingmc
 * @see ColorCodesTranslator#translateAlternateColorCodes(String)
 * @since 1.0.0
 */
public final class BasicStringColorizer implements StringColorizer {

    /**
     * Colorizes the message by translating alternate color codes to Minecraft format.
     * <p>
     * Converts all {@code &} followed by a valid color character to {@code §}.
     * All other content passes through unchanged.
     *
     * @param message the message to colorize, may be null or empty
     * @return the message with translated color codes, or the original if null/empty
     */
    @Override
    public String colorize(@Nullable String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        return change(ALT_COLOR_CHAR, MC_COLOR_CHAR, message);
    }

    /**
     * Decolorizes the message by converting section signs back to ampersands.
     * <p>
     * This is the reverse operation of {@link #colorize(String)}, converting
     * all valid {@code §} color codes back to {@code &} format.
     * <p>
     * Example transformation:
     * <pre>
     * Input:  "§aHello §cWorld §r§lBold"
     * Output: "&amp;aHello &amp;cWorld &amp;r&amp;lBold"
     * </pre>
     *
     * @param colorized the message with § color codes, may be null or empty
     * @return the message with &amp; color codes, or the original if null/empty
     */
    @Override
    public @UnknownNullability String decolorize(@Nullable String colorized) {
        if (colorized == null || colorized.isEmpty()) {
            return colorized;
        }

        return change(MC_COLOR_CHAR, ALT_COLOR_CHAR, colorized);
    }

    private static String change(char from, char to, String message) {
        final char[] charArray = message.toCharArray();
        for (int i = 0; i < charArray.length - 1; i++) {
            if (charArray[i] == from) {
                char nextChar = charArray[i + 1];
                if (isColorCharacter(nextChar)) {
                    charArray[i] = to;
                    charArray[++i] = (char) (nextChar | 0x20);
                }
            }
        }

        return new String(charArray);
    }
}

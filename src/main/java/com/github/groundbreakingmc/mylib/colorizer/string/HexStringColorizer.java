package com.github.groundbreakingmc.mylib.colorizer.string;

import com.github.groundbreakingmc.mylib.colorizer.ColorCodesTranslator;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Colorizer with hex color support (&amp;#rrggbb format).
 * Converts hex codes to Minecraft's §x format.
 * <p>
 * This colorizer processes hex color codes in the format {@code &#rrggbb} and
 * converts them to Minecraft's internal hex format using the {@code §x} prefix
 * followed by individual character codes for each hex digit.
 * <p>
 * Example transformation:
 * <pre>
 * Input:  "&amp;#ff5555Hello"
 * Output: "§x§f§f§5§5§5§5Hello"
 * </pre>
 * <p>
 * After hex conversion, this colorizer also translates standard alternate color
 * codes (e.g., {@code &a}) to Minecraft format (e.g., {@code §a}).
 * <p>
 * <b>Performance:</b> Uses optimized buffer management with manual string building
 * instead of regex {@code appendReplacement()} for better performance.
 *
 * @author groundbreakingmc
 * @see ColorCodesTranslator#translateAlternateColorCodes(String)
 * @since 1.0.0
 */
public final class HexStringColorizer implements StringColorizer {

    /**
     * Pattern to match hex color codes in the format &#rrggbb.
     * Accepts both uppercase and lowercase hex digits.
     */
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F\\d]{6})");

    /**
     * Colorizes the message by converting hex codes and alternate color codes.
     * <p>
     * Processing steps:
     * <ol>
     *   <li>Finds all {@code &#rrggbb} patterns in the input</li>
     *   <li>Converts each to {@code §x§r§r§g§g§b§b} format</li>
     *   <li>Translates remaining {@code &} color codes to {@code §}</li>
     * </ol>
     *
     * @param message the message to colorize, may be null or empty
     * @return the colorized message, or the original if null/empty
     */
    @Override
    public String colorize(@Nullable String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final Matcher matcher = HEX_PATTERN.matcher(message);
        final StringBuilder builder = new StringBuilder(message.length() + 32);
        final char[] codes = this.createStartCodes();

        int lastEnd = 0;
        while (matcher.find()) {
            builder.append(message, lastEnd, matcher.start());

            final String group = matcher.group(1);
            codes[3] = group.charAt(0);
            codes[5] = group.charAt(1);
            codes[7] = group.charAt(2);
            codes[9] = group.charAt(3);
            codes[11] = group.charAt(4);
            codes[13] = group.charAt(5);

            lastEnd = matcher.end();
            builder.append(codes);
        }

        builder.append(message, lastEnd, message.length());
        return ColorCodesTranslator.translateAlternateColorCodes(builder.toString());
    }

    /**
     * Decolorizes the message by converting Minecraft color format back to &amp;#rrggbb format.
     * <p>
     * Delegates to {@link FastHexStringDecolorizer} for conversion of:
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

    /**
     * Creates the template character array for hex color codes.
     * <p>
     * Returns an array in the format: {@code [§, x, §, 0, §, 1, §, 2, §, 3, §, 4, §, 5]}
     * where the hex digit positions (indices 3, 5, 7, 9, 11, 13) are updated
     * for each color code match.
     *
     * @return a reusable character array template
     */
    private char[] createStartCodes() {
        return new char[]{
                ColorCodesTranslator.MC_COLOR_CHAR, 'x',
                ColorCodesTranslator.MC_COLOR_CHAR, '0',
                ColorCodesTranslator.MC_COLOR_CHAR, '1',
                ColorCodesTranslator.MC_COLOR_CHAR, '2',
                ColorCodesTranslator.MC_COLOR_CHAR, '3',
                ColorCodesTranslator.MC_COLOR_CHAR, '4',
                ColorCodesTranslator.MC_COLOR_CHAR, '5',
        };
    }
}

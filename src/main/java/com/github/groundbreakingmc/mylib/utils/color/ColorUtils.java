package com.github.groundbreakingmc.mylib.utils.color;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * Utility methods for color conversion and manipulation.
 * Provides fast RGB-to-hex conversion and color mixing algorithms.
 */
@UtilityClass
public class ColorUtils {

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    /**
     * Converts RGB components to a packed hex integer.
     *
     * @param r red component (0-255)
     * @param g green component (0-255)
     * @param b blue component (0-255)
     * @return packed hex value
     */
    public static int rgbToHex(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }

    /**
     * Converts RGB components to a 6-character hex string (no # prefix).
     *
     * @param r red component (0-255)
     * @param g green component (0-255)
     * @param b blue component (0-255)
     * @return hex string like "FF00AA"
     */
    @NotNull
    public static String rgbToHexString(int r, int g, int b) {
        return new String(new char[]{
                HEX[r >> 4], HEX[r & 0xF],
                HEX[g >> 4], HEX[g & 0xF],
                HEX[b >> 4], HEX[b & 0xF]
        });
    }

    /**
     * Mixes multiple colors using luminance-preserving algorithm.
     *
     * @param first  first color to mix
     * @param second second color to mix
     * @param colors additional colors to mix
     * @return new mixed Color
     */
    @NotNull
    public Color mix(@NotNull Color first, @NotNull Color second, @NotNull Color... colors) {
        final Color[] all = new Color[colors.length + 2];
        all[0] = first;
        all[1] = second;
        System.arraycopy(colors, 0, all, 2, colors.length);
        return mixColors(all);
    }

    /**
     * Internal method for mixing an array of colors.
     * Uses luminance-preserving algorithm to maintain brightness.
     *
     * @param colors array of colors to mix
     * @return new mixed Color
     */
    static Color mixColors(@NotNull Color[] colors) {
        int totalRed = 0, totalGreen = 0, totalBlue = 0, totalMax = 0;

        for (final Color color : colors) {
            totalRed += color.red;
            totalGreen += color.green;
            totalBlue += color.blue;
            totalMax += Math.max(Math.max(color.red, color.green), color.blue);
        }

        final float avgRed = (float) totalRed / colors.length;
        final float avgGreen = (float) totalGreen / colors.length;
        final float avgBlue = (float) totalBlue / colors.length;
        final float avgMax = (float) totalMax / colors.length;

        final float maxOfAvgs = Math.max(Math.max(avgRed, avgGreen), avgBlue);
        final float gain = maxOfAvgs > 0 ? avgMax / maxOfAvgs : 1f;

        return Color.fromRGB((int) (avgRed * gain), (int) (avgGreen * gain), (int) (avgBlue * gain));
    }
}

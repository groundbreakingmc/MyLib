package com.github.groundbreakingmc.mylib.utils.color;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable RGB color representation with hex conversion utilities.
 * Provides fast color manipulation and mixing operations.
 */
@Getter
public final class Color {

    static final int BIT_MASK = 0xff;

    final int hex;
    final int red;
    final int green;
    final int blue;

    Color(int hex, int red, int green, int blue) {
        this.hex = hex;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * Creates Color from hex string (with or without # prefix).
     *
     * @param hex hex string like "FF00AA" or "#FF00AA"
     * @return Color instance
     */
    @NotNull
    public static Color fromHex(@NotNull String hex) {
        String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;
        return fromHex(Integer.parseInt(cleaned, 16));
    }

    /**
     * Creates Color from packed hex integer.
     *
     * @param hex packed hex value like 0xFF00AA
     * @return Color instance
     */
    @NotNull
    public static Color fromHex(int hex) {
        return new Color(hex, (hex >> 16) & BIT_MASK, (hex >> 8) & BIT_MASK, hex & BIT_MASK);
    }

    /**
     * Creates Color from RGB components.
     *
     * @param red   red component (0-255)
     * @param green green component (0-255)
     * @param blue  blue component (0-255)
     * @return Color instance
     */
    @NotNull
    public static Color fromRGB(int red, int green, int blue) {
        return new Color(ColorUtils.rgbToHex(red, green, blue), red, green, blue);
    }

    /**
     * Mixes this color with another color.
     *
     * @param other color to mix with
     * @return new mixed Color
     */
    @NotNull
    public Color mix(@NotNull Color other) {
        return ColorUtils.mixColors(new Color[]{this, other});
    }

    /**
     * Interpolates between this color and target by given factor.
     *
     * @param target target color
     * @param factor interpolation factor (0.0 = this color, 1.0 = target)
     * @return interpolated Color
     */
    @NotNull
    public Color lerp(@NotNull Color target, float factor) {
        float clampedFactor = Math.max(0f, Math.min(1f, factor));
        int r = (int) (this.red + (target.red - this.red) * clampedFactor);
        int g = (int) (this.green + (target.green - this.green) * clampedFactor);
        int b = (int) (this.blue + (target.blue - this.blue) * clampedFactor);
        return fromRGB(r, g, b);
    }

    /**
     * Creates a darker version of this color.
     *
     * @param factor darkening factor (0.0-1.0, where 0.5 = 50% darker)
     * @return darker Color
     */
    @NotNull
    public Color darken(float factor) {
        float multiplier = 1f - Math.max(0f, Math.min(1f, factor));
        return fromRGB((int) (this.red * multiplier), (int) (this.green * multiplier), (int) (this.blue * multiplier));
    }

    /**
     * Creates a lighter version of this color.
     *
     * @param factor lightening factor (0.0-1.0, where 0.5 = 50% lighter)
     * @return lighter Color
     */
    @NotNull
    public Color lighten(float factor) {
        float clampedFactor = Math.max(0f, Math.min(1f, factor));
        int r = (int) (this.red + (255 - this.red) * clampedFactor);
        int g = (int) (this.green + (255 - this.green) * clampedFactor);
        int b = (int) (this.blue + (255 - this.blue) * clampedFactor);
        return fromRGB(r, g, b);
    }

    /**
     * Returns hex string without # prefix.
     *
     * @return hex string like "FF00AA"
     */
    @NotNull
    public String toHexString() {
        return ColorUtils.rgbToHexString(this.red, this.green, this.blue);
    }

    /**
     * Returns hex string with # prefix.
     *
     * @return hex string like "#FF00AA"
     */
    @NotNull
    public String toHexStringWithPrefix() {
        return "#" + this.toHexString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Color color)) return false;
        return this.hex == color.hex;
    }

    @Override
    public int hashCode() {
        return this.hex;
    }

    @Override
    public String toString() {
        return "Color{#" + toHexString() + " rgb(" + this.red + "," + this.green + "," + this.blue + ")}";
    }
}

package com.github.groundbreakingmc.mylib.colorizer;

import com.github.groundbreakingmc.mylib.colorizer.component.ComponentColorizer;
import com.github.groundbreakingmc.mylib.colorizer.component.LegacyComponentColorizer;
import com.github.groundbreakingmc.mylib.colorizer.component.MiniMessageComponentColorizer;
import com.github.groundbreakingmc.mylib.colorizer.string.*;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * Factory class for creating colorizer instances.
 * <p>
 * Provides convenient factory methods to instantiate different types of colorizers
 * based on string mode identifiers. Supports both string-based and component-based
 * colorization strategies.
 *
 * @author groundbreakingmc
 * @since 1.0.0
 */
@UtilityClass
public class ColorizerFactory {

    /**
     * Creates a component colorizer based on the specified mode.
     * <p>
     * Supported modes (case-insensitive):
     * <ul>
     *   <li><b>MINI_MESSAGES/MINIMESSAGES</b> - MiniMessage format colorizer</li>
     *   <li><b>Any other</b> - Vanilla component colorizer with the specified string colorizer mode</li>
     * </ul>
     *
     * @param mode the colorizer mode identifier
     * @return a component colorizer instance corresponding to the mode
     * @throws NullPointerException if mode is null
     * @see #createStringColorizer(String)
     */
    @NotNull
    public static ComponentColorizer createComponentColorizer(@NotNull String mode) {
        return switch (mode.toUpperCase()) {
            case "MINI_MESSAGE", "MINIMESSAGE" -> new MiniMessageComponentColorizer();
            default -> new LegacyComponentColorizer(createStringColorizer(mode));
        };
    }

    /**
     * Creates a string colorizer based on the specified mode.
     * <p>
     * Supported modes (case-insensitive):
     * <ul>
     *   <li><b>LEGACY</b> - Converts &amp;#rrggbb hex codes to Minecraft format</li>
     *   <li><b>LEGACY_ADVANCED</b> - Advanced legacy format with &amp;#rgb and &amp;## support</li>
     *   <li><b>MINI_MESSAGES/MINIMESSAGES</b> - MiniMessage to legacy string converter</li>
     *   <li><b>Any other</b> - Vanilla colorizer (only &amp; to § translation)</li>
     * </ul>
     *
     * @param mode the colorizer mode identifier
     * @return a string colorizer instance corresponding to the mode
     * @throws NullPointerException if mode is null
     */
    @NotNull
    public static StringColorizer createStringColorizer(@NotNull String mode) {
        return switch (mode.toUpperCase()) {
            case "HEX" -> new FastHexStringColorizer();
            case "PATTER_HEX" -> new HexStringColorizer();
            case "ADVANCED", "EXTENDED" -> new AdvancedStringColorizer();
            case "MINI_MESSAGE", "MINIMESSAGE" -> new MiniMessageStringColorizer();
            default -> new BasicStringColorizer(); // Safe default
        };
    }

    /**
     * Creates a string colorizer based on the specified mode.
     *
     * @param mode the colorizer mode identifier
     * @return a string colorizer instance corresponding to the mode
     * @deprecated Use {@link #createStringColorizer(String)} instead
     */
    @Deprecated
    public static StringColorizer createColorizer(final String mode) {
        return createStringColorizer(mode);
    }
}

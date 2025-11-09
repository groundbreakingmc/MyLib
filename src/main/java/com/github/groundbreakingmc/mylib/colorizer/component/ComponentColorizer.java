package com.github.groundbreakingmc.mylib.colorizer.component;

import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import com.github.groundbreakingmc.mylib.colorizer.string.StringColorizer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Specialized colorizer interface for Adventure Component-based colorization.
 * <p>
 * This interface extends the base {@link Colorizer} interface with a fixed
 * return type of {@link Component}, making it suitable for implementations that
 * produce Adventure Text components as output.
 * <p>
 * Component colorizers also provide access to their underlying string colorizer,
 * allowing for flexible text processing in both component and string forms.
 * <p>
 * Common implementations include:
 * <ul>
 *   <li>{@link LegacyComponentColorizer} - Legacy format to components</li>
 *   <li>{@link MiniMessageComponentColorizer} - MiniMessage to components</li>
 * </ul>
 *
 * @author groundbreakingmc
 * @see Colorizer
 * @see Component
 * @since 1.0.0
 */
public interface ComponentColorizer extends Colorizer<Component> {

    /**
     * Returns the underlying string colorizer used by this component colorizer.
     * <p>
     * This allows users to access string-based colorization functionality
     * when component output is not needed, providing flexibility in usage.
     *
     * @return the string colorizer instance
     */
    @NotNull
    StringColorizer getStringColorizer();
}

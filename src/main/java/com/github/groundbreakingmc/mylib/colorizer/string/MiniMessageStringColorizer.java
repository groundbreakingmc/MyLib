package com.github.groundbreakingmc.mylib.colorizer.string;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Converts MiniMessage format to legacy Minecraft format.
 * <p>
 * This colorizer processes MiniMessage tags (e.g., {@code <red>}, {@code <#ff5555>})
 * and converts them to Minecraft's legacy section sign format.
 * <p>
 * Example transformation:
 * <pre>
 * Input:  "&lt;red&gt;Hello &lt;#00ff00&gt;World&lt;/red&gt;"
 * Output: "§cHello §x§0§0§f§f§0§0World"
 * </pre>
 * <p>
 * Uses Adventure's MiniMessage parser for input and LegacyComponentSerializer for output.
 *
 * @author groundbreakingmc
 * @see <a href="https://docs.papermc.io/adventure/minimessage/">MiniMessage Documentation</a>
 * @since 1.0.0
 */
public final class MiniMessageStringColorizer implements StringColorizer {

    /**
     * Colorizes the message by converting MiniMessage format to legacy format.
     * <p>
     * Processing steps:
     * <ol>
     *   <li>Parses the input as MiniMessage using Adventure's parser</li>
     *   <li>Serializes the resulting component to legacy format with section signs</li>
     * </ol>
     *
     * @param message the message in MiniMessage format, may be null or empty
     * @return the message in legacy Minecraft format, or the original if null/empty
     */
    @Override
    public String colorize(@Nullable String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final Component component = MiniMessage.miniMessage().deserialize(message);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    /**
     * Decolorizes the message by converting legacy Minecraft format to MiniMessage format.
     * <p>
     * This is the reverse operation of {@link #colorize(String)}, converting
     * legacy section sign format back to MiniMessage tags.
     * <p>
     * Example transformation:
     * <pre>
     * Input:  "§cHello §x§0§0§f§f§0§0World"
     * Output: "<red>Hello <#00ff00>World"
     * </pre>
     * <p>
     * Processing steps:
     * <ol>
     *   <li>Deserializes the legacy format using {@link LegacyComponentSerializer}</li>
     *   <li>Serializes the component back to MiniMessage format</li>
     * </ol>
     *
     * @param colorized the message in legacy Minecraft format, may be null or empty
     * @return the message in MiniMessage format, or the original if null/empty
     */
    @Override
    public @UnknownNullability String decolorize(@Nullable String colorized) {
        if (colorized == null) {
            return null;
        }
        if (colorized.isEmpty()) {
            return colorized;
        }

        final Component component = LegacyComponentSerializer.legacySection().deserialize(colorized);
        return MiniMessage.miniMessage().serialize(component);
    }
}

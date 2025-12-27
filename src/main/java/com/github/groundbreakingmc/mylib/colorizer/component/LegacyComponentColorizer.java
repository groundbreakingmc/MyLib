package com.github.groundbreakingmc.mylib.colorizer.component;

import com.github.groundbreakingmc.mylib.colorizer.string.FastHexStringColorizer;
import com.github.groundbreakingmc.mylib.colorizer.string.StringColorizer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Component colorizer that processes legacy Minecraft color codes.
 * <p>
 * This colorizer uses a string colorizer to first process the text (converting
 * color codes, hex values, etc.), then deserializes the result into an Adventure
 * Text component using the legacy section sign format.
 * <p>
 * The processing pipeline:
 * <ol>
 *   <li>String colorizer processes input (e.g., {@code &#ff5555} → {@code §x§f§f§5§5§5§5})</li>
 *   <li>Legacy deserializer converts to Component</li>
 * </ol>
 * <p>
 * Example:
 * <pre>
 * Input:  "&amp;#ff5555Hello &amp;aWorld"
 * Step 1: "§x§f§f§5§5§5§5Hello §aWorld" (after string colorizer)
 * Step 2: Component with hex red "Hello " + green "World"
 * </pre>
 * <p>
 * Supports any {@link StringColorizer} implementation, defaulting to
 * {@link FastHexStringColorizer} which handles both standard and hex formats.
 *
 * @author groundbreakingmc
 * @see LegacyComponentSerializer
 * @since 1.0.0
 */
public final class LegacyComponentColorizer implements ComponentColorizer {

    private final StringColorizer colorizer;

    /**
     * Creates a new vanilla component colorizer with default settings.
     * <p>
     * Uses {@link FastHexStringColorizer} as the string colorizer, which supports:
     * <ul>
     *   <li>Standard color codes: {@code &a}, {@code &l}, etc.</li>
     *   <li>Full hex codes: {@code &#rrggbb}</li>
     *   <li>Short hex codes: {@code &##rgb}</li>
     * </ul>
     */
    public LegacyComponentColorizer() {
        this(new FastHexStringColorizer());
    }

    /**
     * Creates a new vanilla component colorizer with a custom string colorizer.
     * <p>
     * Allows using any string colorizer implementation for the initial processing step.
     *
     * @param colorizer the string colorizer to use for processing text
     * @throws NullPointerException if colorizer is null
     */
    public LegacyComponentColorizer(@NotNull StringColorizer colorizer) {
        this.colorizer = colorizer;
    }

    /**
     * Colorizes the message by processing color codes and creating a Component.
     * <p>
     * Processing steps:
     * <ol>
     *   <li>Passes the message through the string colorizer</li>
     *   <li>Deserializes the result using {@link LegacyComponentSerializer}</li>
     * </ol>
     * <p>
     * Special cases:
     * <ul>
     *   <li>If message is null, returns a component containing the text "null"</li>
     *   <li>If message is empty, returns an empty component</li>
     * </ul>
     *
     * @param message the message to colorize, may be null or empty
     * @return the colorized component
     */
    @Override
    public Component colorize(@Nullable String message) {
        if (message == null) {
            return null;
        }
        if (message.isEmpty()) {
            return Component.empty();
        }

        return LegacyComponentSerializer.legacySection().deserialize(
                this.colorizer.colorize(message)
        );
    }

    /**
     * Decolorizes the component by converting it back to ampersand color code format.
     * <p>
     * This is the reverse operation of {@link #colorize(String)}, converting
     * Adventure components back to legacy ampersand format.
     * <p>
     * Example transformation:
     * <pre>
     * Input:  Component with red "Hello " + green "World"
     * Output: "&amp;cHello &amp;aWorld"
     * </pre>
     * <p>
     * Uses {@link LegacyComponentSerializer#legacyAmpersand()} to serialize
     * the component to string format.
     * <p>
     * Special cases:
     * <ul>
     *   <li>If component is null, returns null</li>
     *   <li>If component is empty, returns empty string</li>
     * </ul>
     *
     * @param colorized the component to decolorize, may be null or empty
     * @return the legacy ampersand format string, or null/empty if input was null/empty
     */
    @Override
    public @UnknownNullability String decolorize(@Nullable Component colorized) {
        if (colorized == null) {
            return null;
        }
        if (colorized.equals(Component.empty())) {
            return "";
        }

        return LegacyComponentSerializer.legacyAmpersand().serialize(
                colorized
        );
    }

    /**
     * Returns the underlying string colorizer used for text processing.
     *
     * @return the string colorizer instance
     */
    @Override
    public @NotNull StringColorizer getStringColorizer() {
        return this.colorizer;
    }
}

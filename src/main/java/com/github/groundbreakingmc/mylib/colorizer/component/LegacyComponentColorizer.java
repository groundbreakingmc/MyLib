package com.github.groundbreakingmc.mylib.colorizer.component;

import com.github.groundbreakingmc.mylib.colorizer.string.AdvancedStringColorizer;
import com.github.groundbreakingmc.mylib.colorizer.string.StringColorizer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
 * {@link AdvancedStringColorizer} which handles both standard and hex formats.
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
     * Uses {@link AdvancedStringColorizer} as the string colorizer, which supports:
     * <ul>
     *   <li>Standard color codes: {@code &a}, {@code &l}, etc.</li>
     *   <li>Full hex codes: {@code &#rrggbb}</li>
     *   <li>Short hex codes: {@code &##rgb}</li>
     * </ul>
     */
    public LegacyComponentColorizer() {
        this(new AdvancedStringColorizer());
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
     * Returns the underlying string colorizer used for text processing.
     *
     * @return the string colorizer instance
     */
    @Override
    public @NotNull StringColorizer getStringColorizer() {
        return this.colorizer;
    }
}

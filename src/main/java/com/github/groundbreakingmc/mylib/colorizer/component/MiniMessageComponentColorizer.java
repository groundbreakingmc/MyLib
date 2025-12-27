package com.github.groundbreakingmc.mylib.colorizer.component;

import com.github.groundbreakingmc.mylib.colorizer.string.FastHexStringColorizer;
import com.github.groundbreakingmc.mylib.colorizer.string.StringColorizer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Component colorizer that processes MiniMessage format.
 * <p>
 * This colorizer parses MiniMessage tags and creates Adventure Text components
 * directly, preserving the full functionality of MiniMessage including:
 * <ul>
 *   <li>Color tags: {@code <red>}, {@code <#ff5555>}</li>
 *   <li>Decorations: {@code <bold>}, {@code <italic>}, {@code <underlined>}</li>
 *   <li>Click events: {@code <click:run_command:/help>}</li>
 *   <li>Hover events: {@code <hover:show_text:'Tooltip'>}</li>
 *   <li>And more...</li>
 * </ul>
 * <p>
 * Example:
 * <pre>
 * Input:  "&lt;red&gt;Hello &lt;bold&gt;&lt;#00ff00&gt;World&lt;/bold&gt;"
 * Output: Component with red "Hello " + bold green "World"
 * </pre>
 * <p>
 * The underlying string colorizer uses {@link FastHexStringColorizer} for
 * cases where string output is needed.
 *
 * @author groundbreakingmc
 * @see <a href="https://docs.papermc.io/adventure/minimessage/">MiniMessage Documentation</a>
 * @since 1.0.0
 */
public final class MiniMessageComponentColorizer implements ComponentColorizer {

    private final StringColorizer colorizer;

    /**
     * Creates a new MiniMessage component colorizer.
     * <p>
     * Initializes with {@link FastHexStringColorizer} as the underlying string colorizer.
     */
    public MiniMessageComponentColorizer() {
        this.colorizer = new FastHexStringColorizer();
    }

    /**
     * Colorizes the message by parsing MiniMessage format into a Component.
     * <p>
     * Uses Adventure's MiniMessage parser to deserialize the input string
     * into a fully-featured text component.
     *
     * @param message the message in MiniMessage format, may be null or empty
     * @return the parsed component, or null if input is null, or empty component if input is empty
     */
    @Override
    public Component colorize(@Nullable String message) {
        if (message == null) {
            return null;
        }
        if (message.isEmpty()) {
            return Component.empty();
        }

        return MiniMessage.miniMessage().deserialize(message);
    }

    /**
     * Decolorizes the component by converting it to MiniMessage format.
     * <p>
     * This is the reverse operation of {@link #colorize(String)}, converting
     * Adventure components back to MiniMessage tag format.
     * <p>
     * Example transformation:
     * <pre>
     * Input:  Component with red "Hello " + bold green "World"
     * Output:  "&lt;red&gt;Hello &lt;bold&gt;&lt;#00ff00&gt;World&lt;/bold&gt;"
     * </pre>
     * <p>
     * Uses {@link MiniMessage} serializer to convert the component
     * back to its string representation.
     * <p>
     * Special cases:
     * <ul>
     *   <li>If component is null, returns null</li>
     *   <li>If component is empty, returns empty string</li>
     * </ul>
     *
     * @param colorized the component to decolorize, may be null or empty
     * @return the MiniMessage format string, or null/empty if input was null/empty
     */
    @Override
    public @UnknownNullability String decolorize(@Nullable Component colorized) {
        if (colorized == null) {
            return null;
        }
        if (colorized.equals(Component.empty())) {
            return "";
        }

        return MiniMessage.miniMessage().serialize(colorized);
    }

    /**
     * Returns the underlying string colorizer.
     *
     * @return the {@link FastHexStringColorizer} instance
     */
    @Override
    public @NotNull StringColorizer getStringColorizer() {
        return this.colorizer;
    }
}

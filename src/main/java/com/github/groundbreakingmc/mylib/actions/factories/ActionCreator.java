package com.github.groundbreakingmc.mylib.actions.factories;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.actions.context.ActionContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Represents a lazily constructed action with a specific prefix.
 * Used to register and create actions dynamically from string inputs.
 *
 * @param <C> the type of {@link ActionContext} used by actions created by this factory
 */
public final class ActionCreator<C extends ActionContext> {

    /**
     * The prefix that identifies this action (e.g., "[MESSAGE]").
     */
    private final String prefix;

    /**
     * A factory function that builds an {@link Action} from a trimmed input string.
     */
    private final Function<String, Action<C>> factory;

    /**
     * Constructs a new {@link ActionCreator}.
     *
     * @param prefix  the prefix used to identify this raw action
     * @param factory the function that creates an action from a string
     */
    public ActionCreator(@NotNull String prefix,
                         @NotNull Function<String, Action<C>> factory) {
        this.prefix = prefix;
        this.factory = factory;
    }

    /**
     * Creates an {@link Action} from a full string command, including the prefix.
     *
     * @param action the full input string, including prefix
     * @return the created {@link Action}
     */
    public Action<C> create(@NotNull String action) {
        return this.factory.apply(action.substring(this.prefix.length()).trim());
    }

    /**
     * Creates an {@link Action} from a raw value, without any prefix.
     *
     * @param rawValue the input string without a prefix
     * @return the created {@link Action}
     */
    public Action<C> createFromRaw(@NotNull String rawValue) {
        return this.factory.apply(rawValue);
    }

    /**
     * Returns the prefix associated with this {@link ActionCreator}.
     *
     * @return the action prefix
     */
    public String prefix() {
        return this.prefix;
    }
}

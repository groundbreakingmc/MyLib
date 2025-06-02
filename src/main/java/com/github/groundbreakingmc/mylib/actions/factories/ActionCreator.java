package com.github.groundbreakingmc.mylib.actions.factories;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.actions.context.ActionContext;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Represents a lazily constructed action with a specific prefix.
 * Used to register and create actions dynamically from string inputs.
 */
public final class ActionCreator {

    /**
     * The prefix that identifies this action (e.g., "[MESSAGE]").
     */
    @Getter
    private final String prefix;

    /**
     * A factory function that builds an Action from the trimmed input string.
     */
    private final Function<String, Action<? extends ActionContext>> factory;

    /**
     * @param prefix  the prefix used to identify this raw action
     * @param factory the function that creates an action from a string
     */
    public ActionCreator(@NotNull String prefix,
                         @NotNull Function<String, Action<? extends ActionContext>> factory) {
        this.prefix = prefix;
        this.factory = factory;
    }

    /**
     * Creates an Action from a full string command.
     *
     * @param action the full input string, including prefix
     * @return the created Action
     */
    public Action<? extends ActionContext> create(@NotNull String action) {
        return this.factory.apply(action.substring(this.prefix.length()).trim());
    }
}

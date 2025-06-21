package com.github.groundbreakingmc.mylib.menu.conditions.factories;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.menu.actions.contexts.MenuContext;
import com.github.groundbreakingmc.mylib.menu.conditions.MenuCondition;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Represents a named, lazily-constructed condition factory.
 * <p>
 * This class allows registering reusable condition constructors by name, optionally supporting
 * deny actions that will be executed if the condition fails during evaluation.
 *
 * @param <C> the type of {@link MenuContext} this condition operates on
 */
public final class ConditionCreator<C extends MenuContext> {

    /**
     * The unique name identifying this condition (e.g., "PERMISSION").
     * Used for registration and lookup.
     */
    @Getter
    private final String name;

    /**
     * The factory function responsible for creating a {@link MenuCondition} instance
     * from input parameters and an optional list of deny actions.
     */
    private final BiFunction<String, List<Action<C>>, MenuCondition<C>> factory;

    /**
     * Constructs a simple condition creator that ignores deny actions.
     *
     * @param name    the name identifying this condition
     * @param factory a function that creates a condition from the parameter string only
     */
    public ConditionCreator(@NotNull String name,
                            @NotNull Function<String, MenuCondition<C>> factory) {
        this.name = name;
        this.factory = (params, denyActions) -> factory.apply(params);
    }

    /**
     * Constructs a condition creator that supports deny actions.
     *
     * @param name    the name identifying this condition
     * @param factory a function that creates a condition from the parameter string
     *                and a list of actions to run when the condition fails
     */
    public ConditionCreator(@NotNull String name,
                            @NotNull BiFunction<String, List<Action<C>>, MenuCondition<C>> factory) {
        this.name = name;
        this.factory = factory;
    }

    /**
     * Creates a {@link MenuCondition} using the provided parameter string.
     * Deny actions will default to an empty list.
     *
     * @param params the parameter string used to configure the condition
     * @return the constructed {@link MenuCondition} instance
     */
    public MenuCondition<C> create(@NotNull String params) {
        return this.create(params, List.of());
    }

    /**
     * Creates a {@link MenuCondition} using the provided parameter string and deny actions.
     *
     * @param params      the parameter string used to configure the condition
     * @param denyActions actions to be executed when the condition fails
     * @return the constructed {@link MenuCondition} instance
     */
    public MenuCondition<C> create(@NotNull String params, @NotNull List<Action<C>> denyActions) {
        return this.factory.apply(params, denyActions);
    }
}

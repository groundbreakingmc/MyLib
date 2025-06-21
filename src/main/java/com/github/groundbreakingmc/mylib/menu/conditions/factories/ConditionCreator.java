package com.github.groundbreakingmc.mylib.menu.conditions.factories;

import com.github.groundbreakingmc.mylib.menu.actions.contexts.MenuContext;
import com.github.groundbreakingmc.mylib.menu.conditions.MenuCondition;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Represents a lazily constructed condition with a specific name.
 * Used to register and create conditions dynamically from string inputs.
 */
public final class ConditionCreator<C extends MenuContext> {

    /**
     * The name that identifies this condition (e.g., "PERMISSION").
     */
    @Getter
    private final String name;

    /**
     * A factory function that builds a Condition from the input params.
     */
    private final Function<String, MenuCondition<C>> factory;

    /**
     * @param name    the name used to identify this raw condition
     * @param factory the function that creates a condition from the params
     */
    public ConditionCreator(@NotNull String name,
                            @NotNull Function<String, MenuCondition<C>> factory) {
        this.name = name;
        this.factory = factory;
    }

    /**
     * Creates a Condition from the params.
     *
     * @param params the full input string
     * @return the created Condition
     */
    public MenuCondition<C> create(@NotNull String params) {
        return this.factory.apply(params);
    }
}

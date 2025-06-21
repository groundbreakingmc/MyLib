package com.github.groundbreakingmc.mylib.menu.conditions;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.menu.actions.contexts.MenuContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a condition that can be evaluated in the context of a menu interaction.
 *
 * @param <T> the type of {@link MenuContext} this condition applies to
 */
public interface MenuCondition<T extends MenuContext> {

    /**
     * Tests whether this condition is satisfied given the provided context.
     *
     * @param context the menu context to test against
     * @return {@code true} if the condition passes, {@code false} otherwise
     */
    boolean test(@NotNull T context);

    /**
     * Returns the list of actions to execute if the condition fails.
     * <p>
     * These actions are typically used to notify the player or cancel interaction.
     *
     * @return a non-null list of deny actions
     */
    @NotNull List<Action<T>> getDenyActions();
}

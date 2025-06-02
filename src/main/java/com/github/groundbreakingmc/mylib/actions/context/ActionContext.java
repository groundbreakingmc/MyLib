package com.github.groundbreakingmc.mylib.actions.context;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a context that holds data required for executing an action.
 */
public interface ActionContext {

    /**
     * @return the player related to the context, or null if not present
     */
    @Nullable Player getPlayer();

    /**
     * @return the player used to parse values, or null if not present
     */
    @Nullable Player getPlaceholderPlayer();

    /**
     * Allows retrieving arbitrary typed data from the context, if supported.
     * Default implementation returns null.
     *
     * @param type the class key
     * @param <T>  the type of the object expected
     * @return the object instance, or null if unsupported
     */
    default @Nullable <T> T get(@NotNull Class<T> type) {
        return null; // optional behavior
    }
}

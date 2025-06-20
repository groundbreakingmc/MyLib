package com.github.groundbreakingmc.mylib.menu.conditions.services;

import com.github.groundbreakingmc.mylib.menu.actions.contexts.MenuContext;
import com.github.groundbreakingmc.mylib.menu.conditions.MenuCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A service for managing {@link MenuCondition} instances by name.
 * <p>
 * This class allows for registration, replacement, and lookup of
 * reusable conditions that can be attached to menu interactions.
 */
@SuppressWarnings("unused")
public class ConditionService {

    private final Map<String, MenuCondition<MenuContext>> conditions;

    /**
     * Constructs a new {@link ConditionService} with the given backing condition map.
     *
     * @param conditions the initial condition map, usually empty
     */
    public ConditionService(@NotNull Map<String, MenuCondition<MenuContext>> conditions) {
        this.conditions = conditions;
    }

    /**
     * Registers a new condition by its name. If a condition with the same name already exists,
     * it will only be overwritten if {@code override} is {@code true}.
     *
     * @param condition the condition to register
     * @param override  whether to overwrite an existing condition with the same name
     * @return {@code true} if the condition was registered or replaced, {@code false} otherwise
     */
    public boolean register(@NotNull MenuCondition<MenuContext> condition,
                            boolean override) {
        if (!override && this.conditions.containsKey(condition.getName())) {
            return false;
        }

        this.conditions.put(condition.getName(), condition);
        return true;
    }

    /**
     * Retrieves a registered condition by its unique name.
     *
     * @param name the name of the condition
     * @return the corresponding condition instance, or {@code null} if not found
     */
    @Nullable
    public MenuCondition<MenuContext> byName(@NotNull String name) {
        return this.conditions.get(name);
    }
}

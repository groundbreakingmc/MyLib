package com.github.groundbreakingmc.mylib.menu.conditions.services;

import com.github.groundbreakingmc.mylib.menu.actions.contexts.MenuContext;
import com.github.groundbreakingmc.mylib.menu.conditions.MenuCondition;
import com.github.groundbreakingmc.mylib.menu.conditions.factories.ConditionCreator;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A service for managing {@link MenuCondition} instances by unique name.
 * <p>
 * This service allows registering, retrieving, and optionally overriding
 * condition factories that produce reusable menu conditions from string parameters.
 * <p>
 * Typical usage involves registering {@link ConditionCreator} instances which,
 * given a string argument, return a {@link MenuCondition}. This enables dynamic and
 * configurable menu behavior.
 *
 * @param <C> the context type this condition service operates on
 */
@SuppressWarnings("unused")
public class ConditionService<C extends MenuContext> {

    private final Map<String, ConditionCreator<C>> conditions;

    /**
     * Constructs a new {@link ConditionService} with the given backing condition map.
     *
     * @param conditions the initial condition factory map (usually empty or synchronized)
     */
    public ConditionService(@NotNull Map<String, ConditionCreator<C>> conditions) {
        this.conditions = conditions;
    }

    /**
     * Registers a new condition factory under its unique name.
     * <p>
     * If a factory with the same name already exists, it will only be replaced
     * if {@code override} is {@code true}.
     *
     * @param conditionFactory the condition factory to register
     * @param override         whether to replace an existing factory with the same name
     * @return {@code true} if the condition was registered or replaced; {@code false} otherwise
     */
    public boolean register(@NotNull ConditionCreator<C> conditionFactory, boolean override) {
        final String name = conditionFactory.getName();

        if (!override && this.conditions.containsKey(name)) {
            return false;
        }

        this.conditions.put(name, conditionFactory);
        return true;
    }

    /**
     * Retrieves and creates a {@link MenuCondition} by name with the given parameter string.
     * <p>
     * This will use the associated {@link ConditionCreator} to construct the condition
     * using the provided raw parameter string.
     *
     * @param name   the name of the registered condition
     * @param params the raw parameters to pass to the factory
     * @return the resulting {@link MenuCondition}, or {@code null} if not found
     */
    @Nullable
    public MenuCondition<C> get(@NotNull String name, @NotNull String params) {
        final ConditionCreator<C> conditionFactory = this.conditions.get(name);
        return conditionFactory != null ? conditionFactory.create(params) : null;
    }
}

package com.github.groundbreakingmc.mylib.actions.services;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.actions.factories.ActionCreator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Service for registering and retrieving actions by string.
 */
@SuppressWarnings("unused")
public class ActionService {

    private final Set<ActionCreator> actions = new ObjectOpenHashSet<>();

    /**
     * Registers a new RawAction.
     *
     * @param actionCreator the raw action to register
     * @param override      whether to override an existing action with the same prefix
     * @return true if the action was registered, false otherwise
     */
    public boolean register(@NotNull ActionCreator actionCreator, boolean override) {
        if (!override) {
            for (final ActionCreator target : this.actions) {
                if (target.getPrefix().equalsIgnoreCase(actionCreator.getPrefix())) {
                    return false;
                }
            }
        }

        this.actions.add(actionCreator);
        return true;
    }

    /**
     * Parses a string and creates a corresponding Action if any prefix matches.
     *
     * @param action the string to parse
     * @return the created Action, or null if no match is found
     */
    @Nullable
    public Action<?> fromString(@NotNull String action) {
        for (final ActionCreator target : this.actions) {
            if (StringUtil.startsWithIgnoreCase(action, target.getPrefix())) {
                return target.create(action);
            }
        }

        return null;
    }

}

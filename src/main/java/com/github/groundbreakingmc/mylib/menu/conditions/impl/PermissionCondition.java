package com.github.groundbreakingmc.mylib.menu.conditions.impl;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.menu.actions.contexts.MenuContext;
import com.github.groundbreakingmc.mylib.menu.conditions.MenuCondition;
import com.google.common.collect.ImmutableList;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class PermissionCondition<C extends MenuContext> implements MenuCondition<C> {

    private static final String NAME = "permission";

    private final String permission;
    private final List<Action<C>> denyActions;

    public PermissionCondition(@NotNull String permission) {
        this.permission = permission;
        this.denyActions = ImmutableList.of();
    }

    public PermissionCondition(@NotNull String permission, @NotNull List<Action<C>> denyActions) {
        this.permission = permission;
        this.denyActions = ImmutableList.copyOf(denyActions);
    }

    @Override
    public boolean test(@NotNull MenuContext context) {
        final Player player = context.getPlayer();
        return player != null && player.hasPermission(this.permission);
    }

    @Override
    public @NotNull List<Action<C>> getDenyActions() {
        return this.denyActions;
    }

    @Override
    public @NotNull String getName() {
        return NAME;
    }
}

package com.github.groundbreakingmc.mylib.menu.actions.impl;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.menu.actions.contexts.MenuContext;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class CloseMenuAction<C extends MenuContext> implements Action<C> {

    @Override
    public void execute(@NotNull C context) {
        final Player player = context.getPlayer();
        if (player != null) {
            player.closeInventory();
        }
    }
}

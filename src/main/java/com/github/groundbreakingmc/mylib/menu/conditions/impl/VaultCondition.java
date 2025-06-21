package com.github.groundbreakingmc.mylib.menu.conditions.impl;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.menu.actions.contexts.MenuContext;
import com.github.groundbreakingmc.mylib.menu.conditions.MenuCondition;
import com.github.groundbreakingmc.mylib.utils.vault.VaultUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class VaultCondition<C extends MenuContext> implements MenuCondition<C> {

    private static final String NAME = "vault";

    private final int amount;
    private final List<Action<C>> denyActions;
    private final Economy economy;

    public VaultCondition(int amount, @NotNull List<Action<C>> denyActions) {
        this.amount = amount;
        this.denyActions = denyActions;
        this.economy = VaultUtils.getEconomyProvider();
    }


    @Override
    public boolean test(@NotNull MenuContext context) {
        final Player player = context.getPlayer();
        return player != null && this.economy.has(player, this.amount);
    }

    @Override
    public @NotNull List<Action<C>> getDenyActions() {
        return this.denyActions;
    }

    public @NotNull String getName() {
        return NAME;
    }
}

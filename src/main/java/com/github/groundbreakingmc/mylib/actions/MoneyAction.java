package com.github.groundbreakingmc.mylib.actions;

import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class MoneyAction extends Action.ActionExecutor {

    private final int amount;

    public MoneyAction(Plugin plugin, Colorizer colorizer, String action) {
        super(plugin, colorizer, action);

        this.amount = Integer.parseInt(super.action);
    }

    @Override
    public void execute(@NotNull Player player) {
        if (this.amount < 0) {
            super.economy.withdrawPlayer(player, -this.amount);
        } else {
            super.economy.depositPlayer(player, amount);
        }
    }
}

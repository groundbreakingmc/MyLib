package com.github.groundbreakingmc.mylib.actions;

import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ConsoleCommandAction extends Action.ActionExecutor {

    public ConsoleCommandAction(Plugin plugin, Colorizer colorizer, String action) {
        super(plugin, colorizer, action);
    }

    @Override
    public void execute(@NotNull Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), super.action.replace("{player}", player.getName()));
    }
}

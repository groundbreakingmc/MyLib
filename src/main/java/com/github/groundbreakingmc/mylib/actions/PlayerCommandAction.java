package com.github.groundbreakingmc.mylib.actions;

import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PlayerCommandAction extends Action.ActionExecutor {

    public PlayerCommandAction(Plugin plugin, Colorizer colorizer, String action) {
        super(plugin, colorizer, action);
    }

    @Override
    public void execute(@NotNull Player player) {
        player.chat(super.action);
    }
}

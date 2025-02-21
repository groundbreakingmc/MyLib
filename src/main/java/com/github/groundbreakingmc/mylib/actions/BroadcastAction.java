package com.github.groundbreakingmc.mylib.actions;

import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class BroadcastAction extends Action.ActionExecutor {

    public BroadcastAction(Plugin plugin, Colorizer colorizer, String action) {
        super(plugin, colorizer, action);
    }

    @Override
    public void execute(@NotNull Player player) {
        final String prefix = super.colorizer.colorize(super.chat.getPlayerPrefix(player));
        final String suffix = super.colorizer.colorize(super.chat.getPlayerPrefix(player));

        final String replaced = super.action
                .replace("{player}", player.getName())
                .replace("{prefix}", prefix)
                .replace("{suffix}", suffix);

        for (final Player target : Bukkit.getOnlinePlayers()) {
            target.sendMessage(replaced);
        }

        Bukkit.getConsoleSender().sendMessage(replaced);
    }
}

package com.github.groundbreakingmc.mylib.actions;

import com.github.groundbreakingmc.mylib.colorizer.Colorizer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class MessageAction extends Action.ActionExecutor {

    public MessageAction(Plugin plugin, Colorizer colorizer, String action) {
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

        player.sendMessage(replaced);
    }
}

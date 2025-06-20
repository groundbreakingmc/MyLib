package com.github.groundbreakingmc.mylib.actions.impl;

import com.github.groundbreakingmc.mylib.actions.Action;
import com.github.groundbreakingmc.mylib.actions.context.ActionContext;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class CommandAction<C extends ActionContext> implements Action<C> {

    private final String command;
    private final boolean console;

    public CommandAction(@NotNull String action, boolean console) {
        this.command = action;
        this.console = console;
    }

    @Override
    public void execute(@NotNull C context) {
        final Player player = context.getPlayer();
        final CommandSender sender = this.console ? Bukkit.getConsoleSender() : player;

        final Player placeholderPlayer = context.getPlaceholderPlayer() != null
                ? context.getPlaceholderPlayer()
                : player;

        final String command = placeholderPlayer != null
                ? this.command.replace("{player}", placeholderPlayer.getName())
                : this.command;

        if (sender != null) {
            Bukkit.dispatchCommand(sender, command);
        }
    }
}

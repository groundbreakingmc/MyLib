package com.github.groundbreakingmc.mylib.command.allowedexecutor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class PlayerExecutorCheck implements ExecutorCheck {

    @Override
    public boolean check(final CommandSender sender) {
        return sender instanceof Player;
    }
}

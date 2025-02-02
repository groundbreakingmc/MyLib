package com.github.groundbreakingmc.mylib.command.allowedexecutor;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public final class ConsoleExecutorCheck implements ExecutorCheck {

    @Override
    public boolean check(final CommandSender sender) {
        return sender instanceof ConsoleCommandSender;
    }
}

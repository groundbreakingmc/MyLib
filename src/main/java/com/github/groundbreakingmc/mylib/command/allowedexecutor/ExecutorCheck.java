package com.github.groundbreakingmc.mylib.command.allowedexecutor;

import org.bukkit.command.CommandSender;

public interface ExecutorCheck {
    default boolean check(CommandSender sender) {
        return true;
    }
}

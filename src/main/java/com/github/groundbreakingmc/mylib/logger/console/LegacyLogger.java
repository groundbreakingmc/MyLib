package com.github.groundbreakingmc.mylib.logger.console;

import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

public final class LegacyLogger implements Logger {

    private final java.util.logging.Logger logger;

    public LegacyLogger(final Plugin plugin) {
        this.logger = plugin.getLogger();
    }

    public void info(final String msg) {
        this.logger.log(Level.INFO, msg);
    }

    public void warn(final String msg) {
        this.logger.log(Level.WARNING, msg);
    }
}

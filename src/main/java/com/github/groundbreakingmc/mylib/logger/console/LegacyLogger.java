package com.github.groundbreakingmc.mylib.logger.console;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class LegacyLogger implements Logger {

    private final java.util.logging.Logger logger;

    public LegacyLogger(@NotNull Plugin plugin) {
        this.logger = plugin.getLogger();
    }

    public void info(String msg) {
        this.logger.info(msg);
    }

    @Override
    public void info(Supplier<String> msg) {
        this.logger.info(msg);
    }

    @Deprecated
    public void warn(String msg) {
        this.warning(msg);
    }

    @Override
    public void warning(String msg) {
        this.logger.warning(msg);
    }

    @Override
    public void warning(Supplier<String> msg) {
        this.logger.warning(msg);
    }
}

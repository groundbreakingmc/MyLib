package com.github.groundbreakingmc.mylib.logger.console;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class ModernLogger implements Logger {

    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final Component NULL_TEXT = LEGACY_COMPONENT_SERIALIZER.deserialize("null");

    private final ComponentLogger logger;

    public ModernLogger(@NotNull Plugin plugin) {
        this.logger = ComponentLogger.logger(plugin.getLogger().getName());
    }

    @SuppressWarnings("unused")
    public ModernLogger(@NotNull String name) {
        this.logger = ComponentLogger.logger(name);
    }

    public void info(final String msg) {
        this.logger.info(msg != null ? LEGACY_COMPONENT_SERIALIZER.deserialize(msg) : NULL_TEXT);
    }

    @Override
    public void info(Supplier<String> msg) {
        this.info(msg.get());
    }

    @Deprecated
    public void warn(final String msg) {
        this.warning(msg);
    }

    @Override
    public void warning(String msg) {
        this.logger.warn(msg != null ? LEGACY_COMPONENT_SERIALIZER.deserialize(msg) : NULL_TEXT);
    }

    @Override
    public void warning(Supplier<String> msg) {
        this.warning(msg.get());
    }
}

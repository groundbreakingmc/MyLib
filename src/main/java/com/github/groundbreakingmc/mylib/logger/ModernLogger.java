package com.github.groundbreakingmc.mylib.logger;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.Plugin;

public final class ModernLogger implements Logger {

    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final Component NULL_TEXT = LEGACY_COMPONENT_SERIALIZER.deserialize("null");

    private final ComponentLogger logger;

    public ModernLogger(final Plugin plugin) {
        this.logger = ComponentLogger.logger(plugin.getLogger().getName());
    }

    public void info(final String msg) {
        this.logger.info(msg != null ? LEGACY_COMPONENT_SERIALIZER.deserialize(msg) : NULL_TEXT);
    }

    public void warn(final String msg) {
        this.logger.warn(msg != null ? LEGACY_COMPONENT_SERIALIZER.deserialize(msg) : NULL_TEXT);
    }
}

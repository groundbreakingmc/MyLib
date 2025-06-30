package com.github.groundbreakingmc.mylib.config.loaders;

import com.github.groundbreakingmc.mylib.logger.console.Logger;
import com.github.groundbreakingmc.mylib.logger.console.LoggerFactory;
import com.typesafe.config.Config;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.function.BiFunction;

public final class Loaders<C> {

    public static final Loaders<ConfigurationNode> YAML = new Loaders<>((plugin, logger) -> new ConfigLoader<>(builder -> new ConfigurateLoader(builder).load(), plugin, logger));
    public static final Loaders<Config> HOCON = new Loaders<>((plugin, logger) -> new ConfigLoader<>(builder -> new ConfLoader(builder).load(), plugin, logger));

    public final BiFunction<Plugin, Logger, ConfigLoader<C>> loader;

    private Loaders(@NotNull BiFunction<Plugin, Logger, ConfigLoader<C>> loader) {
        this.loader = loader;
    }

    public ConfigLoader<C> loader(@NotNull Plugin plugin) {
        return this.loader(plugin, LoggerFactory.createLogger(plugin));
    }

    public ConfigLoader<C> loader(@NotNull Plugin plugin, @NotNull Logger logger) {
        return this.loader.apply(plugin, logger);
    }
}

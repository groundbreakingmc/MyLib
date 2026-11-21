package com.github.groundbreakingmc.mylib.config;

import com.github.groundbreakingmc.mylib.config.source.BukkitYamlSource;
import com.github.groundbreakingmc.mylib.config.source.ConfigurateSource;
import com.github.groundbreakingmc.mylib.config.source.LightbendSource;
import com.typesafe.config.ConfigFactory;
import org.bukkit.configuration.file.YamlConfiguration;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

/**
 * Built-in {@link ConfigBackend} implementations.
 * Each returns a {@link com.github.groundbreakingmc.mylib.config.source.ConfigSource};
 * the wrapping into a {@link Config} or {@link BukkitConfig} is done by {@link ConfigFactory}.
 */
public final class ConfigBackends {

    /**
     * Bukkit's built-in YAML (SnakeYAML). No extra dependencies.
     */
    public static final ConfigBackend BUKKIT_YAML =
            file -> new BukkitYamlSource(YamlConfiguration.loadConfiguration(file));

    /**
     * Configurate YAML. Requires {@code configurate-yaml}.
     */
    public static final ConfigBackend CONFIGURATE_YAML = file -> {
        try {
            return new ConfigurateSource(YamlConfigurationLoader.builder().file(file).build().load());
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to load " + file.getName(), e);
        }
    };

    /**
     * Configurate HOCON. Requires {@code configurate-hocon}.
     */
    public static final ConfigBackend CONFIGURATE_HOCON = file -> {
        try {
            return new ConfigurateSource(HoconConfigurationLoader.builder().file(file).build().load());
        } catch (ConfigurateException e) {
            throw new RuntimeException("Failed to load " + file.getName(), e);
        }
    };

    /**
     * Lightbend/Typesafe Config HOCON. Requires {@code config}.
     */
    public static final ConfigBackend LIGHTBEND_HOCON =
            file -> new LightbendSource(ConfigFactory.parseFile(file).resolve());

    private ConfigBackends() {
    }
}

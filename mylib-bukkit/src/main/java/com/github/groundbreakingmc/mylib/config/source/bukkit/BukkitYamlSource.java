package com.github.groundbreakingmc.mylib.config.source.bukkit;

import com.github.groundbreakingmc.mylib.config.source.ConfigSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link ConfigSource} backed by Bukkit's {@link FileConfiguration} (SnakeYAML).
 */
public final class BukkitYamlSource implements ConfigSource {

    private final ConfigurationSection section;

    public BukkitYamlSource(FileConfiguration config) {
        this.section = config;
    }

    private BukkitYamlSource(ConfigurationSection section) {
        this.section = section;
    }

    /**
     * Loads the file with Bukkit's built-in YAML, which needs no extra libraries.
     * Fits {@code ConfigFactory.Builder#backend} as {@code BukkitYamlSource::load}.
     *
     * <p>Like {@link YamlConfiguration#loadConfiguration(File)}, a missing file loads as empty,
     * and a broken one is logged and loads as empty too.
     *
     * @param file the YAML file
     * @return the source over the loaded file
     */
    public static BukkitYamlSource load(File file) {
        return new BukkitYamlSource(YamlConfiguration.loadConfiguration(file));
    }

    @Override
    public @Nullable String str(String path) {
        final Object val = this.section.get(path);
        return val instanceof String str ? str : null;
    }

    @Override
    public @Nullable Boolean bool(String path) {
        final Object val = this.section.get(path);
        return val instanceof Boolean bool ? bool : null;
    }

    @Override
    public @Nullable Integer integer(String path) {
        final Object val = this.section.get(path);
        return val instanceof Number numb ? numb.intValue() : null;
    }

    @Override
    public @Nullable Long lng(String path) {
        final Object val = this.section.get(path);
        return val instanceof Number numb ? numb.longValue() : null;
    }

    @Override
    public @Nullable Double decimal(String path) {
        final Object val = this.section.get(path);
        return val instanceof Number numb ? numb.doubleValue() : null;
    }

    @Override
    public @Nullable List<?> list(String path) {
        final Object val = this.section.get(path);
        return val instanceof List<?> list ? list : null;
    }

    @Override
    public @Nullable Map<String, ?> map(String path) {
        final ConfigurationSection section = this.section.getConfigurationSection(path);
        return section != null ? section.getValues(false) : null;
    }

    @Override
    public @Nullable ConfigSource section(String path) {
        final ConfigurationSection section = this.section.getConfigurationSection(path);
        return section != null ? new BukkitYamlSource(section) : null;
    }

    @Override
    public @Nullable Object raw(String path) {
        return this.section.get(path);
    }

    @Override
    public Set<String> keys(boolean deep) {
        return this.section.getKeys(deep);
    }
}

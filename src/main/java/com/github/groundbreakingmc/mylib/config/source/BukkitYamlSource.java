package com.github.groundbreakingmc.mylib.config.source;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

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

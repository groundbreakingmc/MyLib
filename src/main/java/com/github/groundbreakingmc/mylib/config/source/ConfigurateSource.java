package com.github.groundbreakingmc.mylib.config.source;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;

/**
 * {@link ConfigSource} backed by Configurate's {@link ConfigurationNode} (YAML or HOCON).
 */
public final class ConfigurateSource implements ConfigSource {

    private final ConfigurationNode node;

    public ConfigurateSource(ConfigurationNode node) {
        this.node = node;
    }

    private ConfigurationNode at(String path) {
        return this.node.node((Object[]) path.split("\\."));
    }

    @Override
    public @Nullable String str(String path) {
        return this.at(path).getString();
    }

    @Override
    public @Nullable Boolean bool(String path) {
        final ConfigurationNode value = this.at(path);
        return value.virtual() ? null : value.getBoolean(false);
    }

    @Override
    public @Nullable Integer integer(String path) {
        final ConfigurationNode value = this.at(path);
        return value.virtual() ? null : value.getInt(0);
    }

    @Override
    public @Nullable Long lng(String path) {
        final ConfigurationNode value = this.at(path);
        return value.virtual() ? null : value.getLong(0L);
    }

    @Override
    public @Nullable Double decimal(String path) {
        final ConfigurationNode value = this.at(path);
        return value.virtual() ? null : value.getDouble(0.0);
    }

    @Override
    public @Nullable List<?> list(String path) {
        final ConfigurationNode section = at(path);
        if (section.virtual() || !section.isList()) return null;
        final List<Object> result = new ArrayList<>();
        for (final ConfigurationNode child : section.childrenList()) result.add(child.raw());
        return result;
    }

    @Override
    public @Nullable Map<String, ?> map(String path) {
        final ConfigurationNode section = this.at(path);
        if (section.virtual() || !section.isMap()) return null;
        final Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : section.childrenMap().entrySet()) {
            result.put(entry.getKey().toString(), entry.getValue().raw());
        }
        return result;
    }

    @Override
    public @Nullable ConfigSource section(String path) {
        final ConfigurationNode section = this.at(path);
        return section.virtual() ? null : new ConfigurateSource(section);
    }

    @Override
    public @Nullable Object raw(String path) {
        return this.at(path).raw();
    }

    @Override
    public Set<String> keys(boolean deep) {
        final Set<String> result = new LinkedHashSet<>();
        this.collectKeys(this.node, "", result, deep);
        return result;
    }

    private void collectKeys(ConfigurationNode section, String prefix, Set<String> out, boolean deep) {
        for (final Map.Entry<Object, ? extends ConfigurationNode> entry : section.childrenMap().entrySet()) {
            final String key = prefix.isEmpty() ? entry.getKey().toString() : prefix + "." + entry.getKey();
            out.add(key);
            if (deep) collectKeys(entry.getValue(), key, out, true);
        }
    }
}

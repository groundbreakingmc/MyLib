package com.github.groundbreakingmc.mylib.config.loaders;

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;

public class ConfigurateLoader extends AbstractConfigLoader<ConfigurationNode> {

    public ConfigurateLoader(ConfigLoader<ConfigurationNode> builder) {
        super(builder);
    }

    @Override
    protected ConfigurationNode loadFromFile(final File file) {
        try {
            return YamlConfigurationLoader.builder().file(file).build().load();
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected double getVersion(final ConfigurationNode config, final String versionPath) {
        if (versionPath == null) return 0;
        final Object[] params = versionPath.split("\\.");
        return config.node(params).getDouble(0);
    }
}

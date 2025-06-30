package com.github.groundbreakingmc.mylib.config.loaders;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

public final class ConfLoader extends AbstractConfigLoader<Config> {

    public ConfLoader(ConfigLoader<Config> builder) {
        super(builder);
    }

    @Override
    protected Config loadFromFile(final File file) {
        return ConfigFactory.parseFile(file).resolve();
    }

    @Override
    protected double getVersion(final Config config, final String versionPath) {
        if (versionPath == null || !config.hasPath(versionPath)) return 0;
        return config.getDouble(versionPath);
    }
}

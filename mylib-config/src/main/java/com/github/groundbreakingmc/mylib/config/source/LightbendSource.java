package com.github.groundbreakingmc.mylib.config.source;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigValue;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link ConfigSource} backed by Lightbend/Typesafe {@link Config} (HOCON).
 */
public final class LightbendSource implements ConfigSource {

    private final Config config;

    public LightbendSource(Config config) {
        this.config = config;
    }

    @Override
    public @Nullable String str(String path) {
        try {
            return this.config.getString(path);
        } catch (ConfigException.Missing e) {
            return null;
        }
    }

    @Override
    public @Nullable Boolean bool(String path) {
        try {
            return this.config.getBoolean(path);
        } catch (ConfigException.Missing e) {
            return null;
        }
    }

    @Override
    public @Nullable Integer integer(String path) {
        try {
            return this.config.getInt(path);
        } catch (ConfigException.Missing e) {
            return null;
        }
    }

    @Override
    public @Nullable Long lng(String path) {
        try {
            return this.config.getLong(path);
        } catch (ConfigException.Missing e) {
            return null;
        }
    }

    @Override
    public @Nullable Double decimal(String path) {
        try {
            return this.config.getDouble(path);
        } catch (ConfigException.Missing e) {
            return null;
        }
    }

    @Override
    public @Nullable List<?> list(String path) {
        try {
            return this.config.getAnyRefList(path);
        } catch (ConfigException.Missing e) {
            return null;
        }
    }

    @Override
    public @Nullable Map<String, ?> map(String path) {
        try {
            return this.config.getConfig(path).root().unwrapped();
        } catch (ConfigException.Missing e) {
            return null;
        }
    }

    @Override
    public @Nullable ConfigSource section(String path) {
        try {
            return new LightbendSource(this.config.getConfig(path));
        } catch (ConfigException.Missing e) {
            return null;
        }
    }

    @Override
    public @Nullable Object raw(String path) {
        try {
            return this.config.getAnyRef(path);
        } catch (ConfigException.Missing e) {
            return null;
        }
    }

    @Override
    public Set<String> keys(boolean deep) {
        if (deep) {
            final Set<String> result = new LinkedHashSet<>();
            for (Map.Entry<String, ConfigValue> e : this.config.entrySet()) result.add(e.getKey());
            return result;
        }
        return this.config.root().keySet();
    }
}

package com.github.groundbreakingmc.mylib.config.loaders;

import com.github.groundbreakingmc.mylib.logger.console.Logger;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@Getter
@Accessors(fluent = true)
public final class ConfigLoader<C> {

    @Getter(AccessLevel.NONE)
    private final Function<ConfigLoader<C>, C> factory;
    private final Plugin plugin;
    private final Logger logger;

    private String fileName = "config";
    private double fileVersion = 1.0d;
    private String versionPath = "config-version";

    ConfigLoader(@NotNull Function<ConfigLoader<C>, C> factory, @NotNull Plugin plugin, @NotNull Logger logger) {
        this.factory = factory;
        this.plugin = plugin;
        this.logger = logger;
    }

    public ConfigLoader<C> fileName(@NotNull String fileName) {
        this.fileName = fileName;
        return this;
    }

    public ConfigLoader<C> fileVersion(double fileVersion) {
        this.fileVersion = fileVersion;
        return this;
    }

    public ConfigLoader<C> versionPath(@NotNull String versionPath) {
        this.versionPath = versionPath;
        return this;
    }

    public C load() {
        return this.factory.apply(this);
    }
}

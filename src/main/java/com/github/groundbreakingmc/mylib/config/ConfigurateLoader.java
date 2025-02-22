package com.github.groundbreakingmc.mylib.config;

import com.github.groundbreakingmc.mylib.logger.console.Logger;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.Objects;

public class ConfigurateLoader {

    private final Plugin plugin;
    private final Logger logger;

    private ConfigurateLoader(final Plugin plugin, final Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    @Nullable
    private ConfigurationNode get(final String fileName, final double fileVersion, final String versionPath) {
        try {
            final File file = new File(this.plugin.getDataFolder(), fileName);
            if (!file.exists()) {
                this.plugin.saveResource(fileName, false);
            }

            final CommentedConfigurationNode config = YamlConfigurationLoader.builder()
                    .file(file)
                    .build()
                    .load();

            return fileVersion != 0
                    ? this.checkVersion(config, fileName, fileVersion, versionPath)
                    : config;
        } catch (final ConfigurateException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private ConfigurationNode checkVersion(final ConfigurationNode config, final String fileName, final double fileVersion, final String versionPath) {
        final Object[] params = versionPath.split("\\.");
        final double configVersion = config.node(params).getDouble(0);

        if (configVersion != fileVersion) {
            this.createBackupAndUpdate(fileName);
            return this.get(fileName, 0, null);
        }

        return config;
    }

    private void createBackupAndUpdate(final String fileName) {
        final File folder = this.plugin.getDataFolder();
        if (!folder.exists() && !folder.mkdirs()) {
            this.logger.warn("An error occurred while creating the backups folder!");
            return;
        }

        final File file = new File(folder, fileName);
        final int backupNumber = folder.listFiles().length;
        final File backupFile = new File(folder, fileName.substring(0, 4) + "_backup_" + backupNumber + ".yml");

        if (file.renameTo(backupFile)) {
            this.plugin.saveResource(fileName, true);
        } else {
            this.logger.warn("Your configuration file \"" + fileName + "\" is outdated, but creating a new one isn't possible.");
        }
    }
    public static Loader loader(final Plugin plugin, final Logger logger) {
        return new Loader(plugin, logger);
    }

    @SuppressWarnings("unused")
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Loader {

        public final Plugin plugin;
        public final Logger logger;

        private String fileName = null;
        private double fileVersion = 0;
        private String fileVersionPath = "config-version";

        public Loader fileName(final String fileName) {
            this.fileName = fileName;
            return this;
        }

        public String fileName() {
            return this.fileName();
        }

        public Loader fileVersion(final double fileVersion) {
            this.fileVersion = fileVersion;
            return this;
        }

        public double fileVersion() {
            return this.fileVersion;
        }

        public Loader fileVersionPath(final String path) {
            this.fileVersionPath = path;
            return this;
        }

        public String fileVersionPath() {
            return this.fileVersionPath;
        }

        public ConfigurationNode load() {
            Objects.requireNonNull(this.fileName, "File name can not be null!");
            if (this.fileVersion != 0) {
                Objects.requireNonNull(this.fileVersionPath, "Path to file version can not be null!");
            }

            return new ConfigurateLoader(this.plugin, this.logger).get(this.fileName, this.fileVersion, this.fileVersionPath);
        }
    }
}

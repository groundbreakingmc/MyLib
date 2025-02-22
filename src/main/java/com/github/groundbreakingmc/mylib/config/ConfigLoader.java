package com.github.groundbreakingmc.mylib.config;

import com.github.groundbreakingmc.mylib.logger.console.Logger;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @deprecated Use {@link ConfigurateLoader ConfigurateLoader} instead
 */
@Deprecated
public class ConfigLoader {

    private final Plugin plugin;
    private final Logger logger;

    private ConfigLoader(final Plugin plugin, final Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    private FileConfiguration get(final String fileName, final double fileVersion, final String fileVersionPath, final boolean setDefaults) {
        final File file = new File(this.plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            this.plugin.saveResource(fileName, false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (fileVersion != 0) {
            config = this.checkVersion(config, fileName, fileVersion, fileVersionPath);
        }
        if (setDefaults) {
            this.setDefaults(config, fileName);
        }

        return config;
    }

    private void setDefaults(final FileConfiguration config, final String fileName) {
        try (final InputStream defConfigStream = this.plugin.getResource(fileName)) {
            if (defConfigStream != null) {
                config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)));
            }
        } catch (final IOException ex) {
            this.logger.warn("Error loading default configuration: " + ex.getMessage());
        }
    }

    private FileConfiguration checkVersion(final FileConfiguration config, final String fileName,
                                           final double fileVersion, final String fileVersionPath) {
        final double configVersion = config.getDouble(fileVersionPath, 0);

        if (configVersion != fileVersion) {
            this.createBackupAndUpdate(fileName);
            return this.get(fileName, 0, null, false);
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

    /**
     * @deprecated Use {@link ConfigurateLoader ConfigurateLoader} instead
     */
    @Deprecated
    public static ConfigLoaderBuilder builder(final Plugin plugin, final Logger logger) {
        return new ConfigLoaderBuilder(plugin, logger);
    }

    @SuppressWarnings("unused")
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ConfigLoaderBuilder {

        public final Plugin plugin;
        public final Logger logger;

        private String fileName = null;
        private String fileVersionPath;
        private double fileVersion = 0;
        private boolean setDefaults = false;

        /**
         * @deprecated Use {@link #fileName(String) fileName(String)} instead
         */
        @Deprecated
        public ConfigLoaderBuilder setFileName(final String fileName) {
            this.fileName = fileName;
            return this;
        }

        public ConfigLoaderBuilder fileName(final String fileName) {
            this.fileName = fileName;
            return this;
        }

        public String fileName() {
            return this.fileName;
        }

        /**
         * @deprecated Use {@link #fileVersion(double) fileVersion(double)} instead
         */
        @Deprecated
        public ConfigLoaderBuilder setFileVersion(final double fileVersion) {
            this.fileVersion = fileVersion;
            return this;
        }

        public ConfigLoaderBuilder fileVersion(final double fileVersion) {
            this.fileVersion = fileVersion;
            return this;
        }

        public double fileVersion() {
            return this.fileVersion;
        }

        /**
         * @deprecated Use {@link #fileVersionPath(String) fileVersionPath(String)} instead
         */
        @Deprecated
        public ConfigLoaderBuilder setFileVersionPath(final String path) {
            this.fileVersionPath = path;
            return this;
        }

        public ConfigLoaderBuilder fileVersionPath(final String path) {
            this.fileVersionPath = path;
            return this;
        }

        public String fileVersionPath() {
            return this.fileVersionPath;
        }

        /**
         * @deprecated Use {@link #setDefaults(boolean) setDefaults(boolean)} instead
         */
        @Deprecated
        public ConfigLoaderBuilder setSetDefaults(final boolean setDefaults) {
            this.setDefaults = setDefaults;
            return this;
        }

        public ConfigLoaderBuilder setDefaults(final boolean setDefaults) {
            this.setDefaults = setDefaults;
            return this;
        }

        public boolean setDefaults() {
            return this.setDefaults;
        }

        public FileConfiguration build() {
            Objects.requireNonNull(this.fileName, "File name can not be null!");
            if (this.fileVersion != 0) {
                Objects.requireNonNull(this.fileVersionPath, "Path to file version can not be null!");
            }

            return new ConfigLoader(this.plugin, this.logger).get(this.fileName, this.fileVersion, this.fileVersionPath, this.setDefaults);
        }
    }
}
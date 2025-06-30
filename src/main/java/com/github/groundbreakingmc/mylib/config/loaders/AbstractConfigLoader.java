package com.github.groundbreakingmc.mylib.config.loaders;

import com.github.groundbreakingmc.mylib.logger.console.Logger;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.Objects;

public abstract class AbstractConfigLoader<T> {

    protected final Plugin plugin;
    protected final Logger logger;
    protected final String fileName;
    protected final double fileVersion;
    protected final String versionPath;

    protected AbstractConfigLoader(ConfigLoader<?> builder) {
        this.plugin = builder.plugin();
        this.logger = builder.logger();
        this.fileName = builder.fileName();
        this.fileVersion = builder.fileVersion();
        this.versionPath = builder.versionPath();
    }

    public T load() {
        return load(this.fileName, this.fileVersion, this.versionPath);
    }

    public T load(final String fileName, final double fileVersion, final String versionPath) {
        final File configFile = new File(this.plugin.getDataFolder(), fileName);
        final boolean fileExists = configFile.exists();
        if (!fileExists) {
            this.plugin.saveResource(fileName, false);
        }

        final T config = this.loadFromFile(configFile);

        return fileExists && fileVersion != 0
                ? this.checkVersion(config, fileName, fileVersion, versionPath)
                : config;
    }

    protected abstract T loadFromFile(File file);

    protected abstract double getVersion(T config, String versionPath);

    private T checkVersion(final T config, final String fileName, final double fileVersion, final String versionPath) {
        final double configVersion = getVersion(config, versionPath);

        if (configVersion != fileVersion) {
            this.createBackupAndUpdate(fileName);
            return this.load(fileName, 0, null);
        }

        return config;
    }

    private void createBackupAndUpdate(final String fileName) {
        final File folder = this.plugin.getDataFolder();
        if (!folder.exists() && !folder.mkdirs()) {
            this.logger.warning("An error occurred while creating the backups folder!");
            return;
        }

        final File file = new File(folder, fileName);
        final int backupNumber = Objects.requireNonNull(folder.listFiles()).length;
        final File backupFile = new File(folder, "backup_" + backupNumber + "_" + fileName);

        if (file.renameTo(backupFile)) {
            this.plugin.saveResource(fileName, true);
        } else {
            this.logger.warning("Your configuration file \"" + fileName + "\" is outdated, but creating a new one isn't possible.");
        }
    }

}

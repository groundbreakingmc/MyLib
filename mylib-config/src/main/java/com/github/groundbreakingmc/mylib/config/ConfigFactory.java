package com.github.groundbreakingmc.mylib.config;

import com.github.groundbreakingmc.mylib.config.source.ConfigSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Entry point for building configs.
 *
 * <p>Assembles a {@link ConfigSource} (backend) into a {@link Config} (wrapper).
 * Use {@link Builder#load()} for a plain wrapper, or {@link Builder#load(BiFunction)} for
 * a platform wrapper such as {@code BukkitConfig} from mylib-bukkit, with Bukkit-typed helpers
 * and auto-registered serializers.
 *
 * <pre>{@code
 * // Plain config
 * Config cfg = ConfigFactory.of(plugin.getDataFolder(), "config.yml", plugin::getResource)
 *     .version(1.3)
 *     .backend(ConfigBackends.CONFIGURATE_YAML)
 *     .serializers(r -> r.register(SoundSettings.class, SoundSettings::fromConfig))
 *     .load();
 *
 * // Bukkit config (mylib-bukkit) — Location/Material/Sound/World/PotionEffectType auto-registered
 * BukkitConfig cfg = ConfigFactory.of(plugin.getDataFolder(), "config.yml", plugin::getResource)
 *     .backend(BukkitYamlSource::load)
 *     .load(BukkitConfig::new);
 *
 * // Already have a File in hand? Skip the folder/name split entirely.
 * File configFile = ...;
 * Config messages = ConfigFactory.of(configFile)
 *     .backend(ConfigBackends.CONFIGURATE_YAML)
 *     .load();
 * }</pre>
 */
public final class ConfigFactory {

    private ConfigFactory() {
    }

    /**
     * Use when the config file is guaranteed to already exist, and you already
     * have a {@link File} pointing at it.
     */
    public static Builder of(@NotNull File configFile) {
        return new Builder(configFile, null);
    }

    /**
     * Use when the config file is guaranteed to already exist.
     *
     * @param dataFolder the plugin's data folder (or any parent directory)
     * @param fileName   the config's file name, e.g. {@code "config.yml"}
     */
    public static Builder of(@NotNull File dataFolder, @NotNull String fileName) {
        return new Builder(new File(dataFolder, fileName), null);
    }

    /**
     * @param configFile path to the config file
     * @param resources  supplier of default resource streams (e.g. {@code plugin::getResource}).
     *                   Used to copy defaults when the config file doesn't exist yet.
     *                   Looked up by the file's own name (see {@link File#getName()}).
     */
    public static Builder of(@NotNull File configFile, @NotNull Function<String, InputStream> resources) {
        return new Builder(configFile, resources);
    }

    /**
     * @param dataFolder the plugin's data folder (or any parent directory)
     * @param fileName   the config's file name, e.g. {@code "config.yml"}
     * @param resources  supplier of default resource streams (e.g. {@code plugin::getResource}).
     *                   Used to copy defaults when the config file doesn't exist yet.
     *                   Looked up by {@code fileName}.
     */
    public static Builder of(@NotNull File dataFolder, @NotNull String fileName,
                             @NotNull Function<String, InputStream> resources) {
        return new Builder(new File(dataFolder, fileName), resources);
    }

    public static final class Builder {

        private final File configFile;
        @Nullable
        private final Function<String, InputStream> resources;
        private final SerializerRegistry registry = new SerializerRegistry();

        @Nullable
        private ConfigBackend backend;
        @Nullable
        private ConfigUpdater updater;
        private double version = 0.0;
        private String versionPath = "config-version";

        private Builder(File configFile, @Nullable Function<String, InputStream> resources) {
            this.configFile = configFile;
            this.resources = resources;
        }

        /**
         * Sets the expected config version. If the value stored at {@link #versionPath(String)}
         * doesn't match, the existing file is backed up and replaced with the default resource.
         *
         * <p>Leave unset (or {@code 0.0}) to disable version checking entirely.
         */
        public Builder version(double version) {
            this.version = version;
            return this;
        }

        /**
         * Updates an outdated file instead of replacing it: when the version doesn't match, the
         * file is backed up as before, then the updater rewrites it from the default resource,
         * keeping what the user set. Needs a {@link #version(double)}.
         */
        public Builder updater(@NotNull ConfigUpdater updater) {
            this.updater = updater;
            return this;
        }

        /**
         * Path within the config where the version number is stored.
         * Defaults to {@code "config-version"}.
         */
        public Builder versionPath(@NotNull String versionPath) {
            this.versionPath = versionPath;
            return this;
        }

        /**
         * Backend used to parse/load the file, e.g. {@link ConfigBackends#CONFIGURATE_YAML}.
         * Required: there is no default, because every backend needs its own library.
         */
        public Builder backend(@NotNull ConfigBackend backend) {
            this.backend = backend;
            return this;
        }

        /**
         * Registers custom (de)serializers on top of whatever the target
         * wrapper (e.g. {@code BukkitConfig}) auto-registers.
         */
        public Builder serializers(@NotNull Consumer<SerializerRegistry> configure) {
            configure.accept(this.registry);
            return this;
        }

        /**
         * Loads and wraps as a plain {@link Config}.
         */
        public Config load() {
            return this.load(Config::new);
        }

        /**
         * Loads and wraps the config with {@code wrapper}, for wrappers with platform types.
         *
         * <p>For example, {@code load(BukkitConfig::new)} from mylib-bukkit returns a config
         * with Bukkit-typed helpers and auto-registered Bukkit serializers.
         *
         * @param wrapper creates the wrapper from the loaded source and the registry
         * @param <C>     the wrapper type
         * @return the loaded config
         */
        public <C extends Config> C load(@NotNull BiFunction<ConfigSource, SerializerRegistry, C> wrapper) {
            return wrapper.apply(this.loadSource(), this.registry);
        }

        // ── internals ──────────────────────────────────────────────────────────

        private ConfigSource loadSource() {
            final ConfigBackend backend = this.backend;
            if (backend == null) {
                throw new IllegalStateException(
                        "Config \"" + this.configFile.getName() + "\" has no backend: call backend(...) before load()"
                );
            }

            if (!this.configFile.exists()) {
                this.copyDefault(this.configFile);
            }

            ConfigSource source = backend.load(this.configFile);

            if (this.version != 0.0) {
                final Double actual = source.decimal(this.versionPath);
                if (actual == null || actual != this.version) {
                    if (this.updater != null) {
                        this.update(this.configFile, this.updater);
                    } else {
                        this.backup(this.configFile);
                        this.copyDefault(this.configFile);
                    }
                    source = backend.load(this.configFile);
                }
            }

            return source;
        }

        private void copyDefault(File dest) {
            if (this.resources == null) {
                throw new IllegalStateException(
                        "Config \"" + dest.getName() + "\" does not exist and no resource supplier was provided."
                );
            }
            final String resourceName = dest.getName();
            try (final InputStream in = this.resources.apply(resourceName)) {
                if (in == null) throw new IllegalArgumentException("Resource not found in jar: " + resourceName);
                final File parent = dest.getParentFile();
                if (parent != null && !parent.exists() && !parent.mkdirs())
                    throw new RuntimeException("Failed to create directory: " + parent);
                Files.copy(in, dest.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy default config \"" + resourceName + "\"", e);
            }
        }

        // The file stays where it is: a copy is the backup, and the updater rewrites the original.
        private void update(File file, ConfigUpdater updater) {
            if (this.resources == null) {
                throw new IllegalStateException(
                        "Config \"" + file.getName() + "\" is outdated and no resource supplier was provided."
                );
            }
            final String resourceName = file.getName();
            try (final InputStream in = this.resources.apply(resourceName)) {
                if (in == null) throw new IllegalArgumentException("Resource not found in jar: " + resourceName);
                final File parent = file.getParentFile();
                final int n = Objects.requireNonNull(parent.listFiles()).length;
                Files.copy(file.toPath(), new File(parent, "backup_" + n + "_" + file.getName()).toPath());
                updater.update(file, in);
            } catch (IOException e) {
                throw new RuntimeException("Failed to update config \"" + resourceName + "\"", e);
            }
        }

        private void backup(File file) {
            final File parent = file.getParentFile();
            final int n = Objects.requireNonNull(parent.listFiles()).length;
            final File dest = new File(parent, "backup_" + n + "_" + file.getName());
            if (!file.renameTo(dest)) {
                throw new RuntimeException(
                        "Config \"" + file.getName() + "\" is outdated but could not be renamed. Check file permissions."
                );
            }
        }
    }
}

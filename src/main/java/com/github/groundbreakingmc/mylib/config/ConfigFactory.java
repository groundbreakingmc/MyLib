package com.github.groundbreakingmc.mylib.config;

import com.github.groundbreakingmc.mylib.config.source.ConfigSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Entry point for building configs.
 *
 * <p>Assembles a {@link ConfigSource} (backend) into a {@link Config} (wrapper).
 * Use {@link Builder#load()} for a plain wrapper, {@link Builder#loadBukkit()} for
 * a {@link BukkitConfig} with Bukkit-typed helpers and auto-registered serializers.
 *
 * <pre>{@code
 * // Plain config
 * Config cfg = ConfigFactory.of(plugin.getDataFolder(), plugin::getResource)
 *     .file("config.yml")
 *     .version(1.3)
 *     .backend(ConfigBackends.CONFIGURATE_YAML)
 *     .serializers(r -> r.register(SoundSettings.class, SoundSettings::fromConfig))
 *     .load();
 *
 * // Bukkit config — Location/Material/Sound/World/PotionEffectType auto-registered
 * BukkitConfig cfg = ConfigFactory.of(plugin.getDataFolder(), plugin::getResource)
 *     .file("config.yml")
 *     .loadBukkit();
 * }</pre>
 */
public final class ConfigFactory {

    private ConfigFactory() {
    }

    /**
     * Use when the config file is guaranteed to already exist.
     */
    public static Builder of(@NotNull File dataFolder) {
        return new Builder(dataFolder, null);
    }

    /**
     * @param resources supplier of default resource streams (e.g. {@code plugin::getResource}).
     *                  Used to copy defaults when the config file doesn't exist yet.
     */
    public static Builder of(@NotNull File dataFolder, @NotNull Function<String, InputStream> resources) {
        return new Builder(dataFolder, resources);
    }

    public static final class Builder {

        private final File dataFolder;
        @Nullable
        private final Function<String, InputStream> resources;
        private final SerializerRegistry registry = new SerializerRegistry();

        private ConfigBackend backend = ConfigBackends.BUKKIT_YAML;
        private String fileName = "config.yml";
        private double version = 0.0;
        private String versionPath = "config-version";

        private Builder(File dataFolder, @Nullable Function<String, InputStream> resources) {
            this.dataFolder = dataFolder;
            this.resources = resources;
        }

        public Builder file(@NotNull String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder version(double version) {
            this.version = version;
            return this;
        }

        public Builder versionPath(@NotNull String versionPath) {
            this.versionPath = versionPath;
            return this;
        }

        public Builder backend(@NotNull ConfigBackend backend) {
            this.backend = backend;
            return this;
        }

        public Builder serializers(@NotNull Consumer<SerializerRegistry> configure) {
            configure.accept(this.registry);
            return this;
        }

        /**
         * Loads and wraps as a plain {@link Config}.
         */
        public Config load() {
            return new Config(loadSource(), registry);
        }

        /**
         * Loads and wraps as a {@link BukkitConfig}.
         * Bukkit-typed serializers (Location, Material, …) are auto-registered.
         */
        public BukkitConfig loadBukkit() {
            return new BukkitConfig(loadSource(), registry);
        }

        // ── internals ──────────────────────────────────────────────────────────

        private ConfigSource loadSource() {
            final File file = new File(dataFolder, fileName);

            if (!file.exists()) {
                copyDefault(file);
            }

            ConfigSource source = backend.load(file);

            if (version != 0.0) {
                final Double actual = source.decimal(versionPath);
                if (actual == null || actual != version) {
                    backup(file);
                    copyDefault(file);
                    source = backend.load(file);
                }
            }

            return source;
        }

        private void copyDefault(File dest) {
            if (resources == null) {
                throw new IllegalStateException(
                        "Config \"" + fileName + "\" does not exist and no resource supplier was provided."
                );
            }
            try (final InputStream in = resources.apply(fileName)) {
                if (in == null) throw new IllegalArgumentException("Resource not found in jar: " + fileName);
                final File parent = dest.getParentFile();
                if (!parent.exists() && !parent.mkdirs())
                    throw new RuntimeException("Failed to create directory: " + parent);
                Files.copy(in, dest.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Failed to copy default config \"" + fileName + "\"", e);
            }
        }

        private void backup(File file) {
            final int n = Objects.requireNonNull(dataFolder.listFiles()).length;
            final File dest = new File(dataFolder, "backup_" + n + "_" + fileName);
            if (!file.renameTo(dest)) {
                throw new RuntimeException(
                        "Config \"" + fileName + "\" is outdated but could not be renamed. Check file permissions."
                );
            }
        }
    }
}

package com.github.groundbreakingmc.mylib.config.bukkit;

import com.github.groundbreakingmc.mylib.config.ConfigUpdater;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * Updates YAML files with Bukkit's YAML: the result has the keys and comments of the default,
 * in its order, with the values the user set. Keys only the user has, such as their own entries,
 * stay after the default's ones. The version comes from the default.
 *
 * <p>Some sections are the user's own list of entries, such as a map of formats or of punishments:
 * a default entry the user removed must not come back. Name them as {@code userEntries}: their
 * missing entries are not added, while the entries the user has still get the default's new keys.
 *
 * <p>Bukkit writes the whole file again, so lists come out in block style and strings may be
 * quoted differently than before.
 *
 * <pre>{@code
 * ConfigFactory.of(plugin.getDataFolder(), "formats.yml", plugin::getResource)
 *     .version(2.0)
 *     .backend(BukkitYamlSource::load)
 *     .updater(new BukkitYamlUpdater("config-version", Set.of("formats")))
 *     .load(BukkitConfig::new);
 * }</pre>
 */
public final class BukkitYamlUpdater implements ConfigUpdater {

    // Keys may contain dots, such as "1.5" or "example.com"; paths here are joined with a character
    // no key has.
    private static final char SEPARATOR = '\0';

    private final String versionPath;
    private final Set<String> userEntries;

    /**
     * Creates an updater.
     *
     * @param versionPath where the version is, such as {@code config-version}, with dots between keys
     * @param userEntries the sections whose entries are the user's, with dots between keys, such as
     *                    {@code formats} or {@code spam.punishments}
     */
    public BukkitYamlUpdater(@NotNull String versionPath, @NotNull Set<String> userEntries) {
        this.versionPath = versionPath.replace('.', SEPARATOR);
        this.userEntries = Set.copyOf(userEntries.stream().map(path -> path.replace('.', SEPARATOR)).toList());
    }

    @Override
    public void update(@NotNull File file, @NotNull InputStream defaults) throws IOException {
        final YamlConfiguration user = yaml();
        final YamlConfiguration base = yaml();
        try (final Reader reader = new InputStreamReader(defaults, StandardCharsets.UTF_8)) {
            user.load(file);
            base.load(reader);
        } catch (InvalidConfigurationException e) {
            throw new IOException("Not valid YAML: " + e.getMessage(), e);
        }

        final YamlConfiguration result = yaml();
        result.options().setHeader(base.options().getHeader());
        result.options().setFooter(base.options().getFooter());
        this.merge(base, user, result, "");
        result.set(this.versionPath, base.get(this.versionPath));
        result.save(file);
    }

    // The default's keys of one section, in its order, then the keys only the user has.
    private void merge(YamlConfiguration base, YamlConfiguration user, YamlConfiguration result, String section) {
        final boolean userOwned = this.userEntries.contains(section);
        for (final String key : keys(base, section)) {
            final String path = join(section, key);
            if (userOwned && !user.contains(path)) {
                continue;
            }
            if (base.isConfigurationSection(path) && (!user.contains(path) || user.isConfigurationSection(path))) {
                result.createSection(path);
                copyComments(base, result, path);
                this.merge(base, user, result, path);
            } else {
                result.set(path, user.contains(path) ? user.get(path) : base.get(path));
                copyComments(base, result, path);
            }
        }
        for (final String key : keys(user, section)) {
            final String path = join(section, key);
            if (!base.contains(path)) {
                result.set(path, user.get(path));
                copyComments(user, result, path);
            }
        }
    }

    private static Set<String> keys(YamlConfiguration config, String section) {
        if (section.isEmpty()) {
            return config.getKeys(false);
        }
        return config.isConfigurationSection(section) ? config.getConfigurationSection(section).getKeys(false) : Set.of();
    }

    // Comments attach to a key that exists, so they are copied after the value.
    private static void copyComments(YamlConfiguration from, YamlConfiguration to, String path) {
        final List<String> comments = from.getComments(path);
        if (!comments.isEmpty()) {
            to.setComments(path, comments);
        }
        final List<String> inline = from.getInlineComments(path);
        if (!inline.isEmpty()) {
            to.setInlineComments(path, inline);
        }
    }

    private static String join(String section, String key) {
        return section.isEmpty() ? key : section + SEPARATOR + key;
    }

    private static YamlConfiguration yaml() {
        final YamlConfiguration yaml = new YamlConfiguration();
        yaml.options().parseComments(true).pathSeparator(SEPARATOR);
        return yaml;
    }
}

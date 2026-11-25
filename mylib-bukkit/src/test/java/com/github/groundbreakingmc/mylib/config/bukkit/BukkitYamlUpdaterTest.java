package com.github.groundbreakingmc.mylib.config.bukkit;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class BukkitYamlUpdaterTest {

    private static final String DEFAULTS = """
            # The cooldown.
            cooldown: 5
            # A new setting.
            added: true
            formats:
              default:
                chat: 'D'
                button: 'X'
              vip:
                chat: 'V'
            domains: [ example.com ]
            config-version: 2.0
            """;

    @TempDir
    Path folder;

    private YamlConfiguration update(String user, Set<String> userEntries) throws IOException {
        final Path file = this.folder.resolve("config.yml");
        Files.writeString(file, user);
        new BukkitYamlUpdater("config-version", userEntries)
                .update(file.toFile(), new ByteArrayInputStream(DEFAULTS.getBytes(StandardCharsets.UTF_8)));
        return YamlConfiguration.loadConfiguration(file.toFile());
    }

    @Test
    void keepsUserValuesAndAddsMissingKeysWithComments() throws IOException {
        final YamlConfiguration updated = this.update("""
                cooldown: 1
                own: 'mine'
                config-version: 1.0
                """, Set.of());

        assertEquals(1, updated.getInt("cooldown"));
        assertTrue(updated.getBoolean("added"));
        assertEquals(List.of("A new setting."), updated.getComments("added"));
        assertEquals("mine", updated.getString("own"));
        assertEquals(2.0, updated.getDouble("config-version"));
        assertEquals(List.of("example.com"), updated.getStringList("domains"));
    }

    @Test
    void removedUserEntriesStayRemovedWhileKeptOnesGetNewKeys() throws IOException {
        final YamlConfiguration updated = this.update("""
                formats:
                  default:
                    chat: 'mine'
                  admin:
                    chat: 'A'
                config-version: 1.0
                """, Set.of("formats"));

        assertEquals("mine", updated.getString("formats.default.chat"));
        assertEquals("X", updated.getString("formats.default.button"));
        assertFalse(updated.contains("formats.vip"));
        assertEquals("A", updated.getString("formats.admin.chat"));
    }
}

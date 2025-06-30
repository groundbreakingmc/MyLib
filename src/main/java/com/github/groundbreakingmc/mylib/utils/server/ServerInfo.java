package com.github.groundbreakingmc.mylib.utils.server;

import com.github.groundbreakingmc.mylib.logger.console.Logger;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

@UtilityClass
@SuppressWarnings("unused")
public final class ServerInfo {

    private int subVersion = -1;
    private int isPaperOrFork = -1;

    public int getSubVersion() {
        try {
            if (subVersion < 0) {
                subVersion = Integer.parseInt(Bukkit.getServer().getMinecraftVersion().split("\\.", 3)[1]);
            }
            return subVersion;
        } catch (final NumberFormatException ex) {
            Bukkit.getLogger().warning("\u001b[32mFailed to extract server version. Plugin may not work correctly!");
            return 0;
        }
    }

    public int getSubVersion(final Logger logger) {
        try {
            if (subVersion < 0) {
                subVersion = Integer.parseInt(Bukkit.getServer().getMinecraftVersion().split("\\.", 3)[1]);
            }
            return subVersion;
        } catch (final NumberFormatException ex) {
            logger.warning("\u001b[32mFailed to extract server version. Plugin may not work correctly!");
            return 0;
        }
    }

    public boolean isPaperOrFork() {
        try {
            if (isPaperOrFork < 0) {
                Class.forName("com.destroystokyo.paper.utils.PaperPluginLogger");
                isPaperOrFork = 1;
            }
            return isPaperOrFork == 1;
        } catch (final ClassNotFoundException ex) {
            isPaperOrFork = 0;
            return false;
        }
    }
}

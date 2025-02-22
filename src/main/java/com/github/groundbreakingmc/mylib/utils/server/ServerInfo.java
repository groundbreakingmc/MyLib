package com.github.groundbreakingmc.mylib.utils.server;

import com.github.groundbreakingmc.mylib.logger.console.Logger;
import lombok.experimental.UtilityClass;
import org.bukkit.plugin.Plugin;

@UtilityClass
public final class ServerInfo {

    public int getSubVersion(final Plugin plugin, final Logger logger) {
        try {
            return Integer.parseInt(plugin.getServer().getMinecraftVersion().split("\\.", 3)[1]);
        } catch (final NumberFormatException ex) {
            logger.warn("\u001b[32mFailed to extract server version. Plugin may not work correctly!");
            return 0;
        }
    }

    public int getSubVersion(final Plugin plugin) {
        try {
            return Integer.parseInt(plugin.getServer().getMinecraftVersion().split("\\.", 3)[1]);
        } catch (final NumberFormatException ex) {
            plugin.getLogger().warning("\u001b[32mFailed to extract server version. Plugin may not work correctly!");
            return 0;
        }
    }

    public boolean isPaperOrFork() {
        try {
            Class.forName("com.destroystokyo.paper.utils.PaperPluginLogger");
            return true;
        } catch (final ClassNotFoundException ex) {
            return false;
        }
    }
}

package com.github.groundbreakingmc.mylib.logger;

import com.github.groundbreakingmc.mylib.utils.server.ServerInfo;
import lombok.experimental.UtilityClass;
import org.bukkit.plugin.Plugin;

@UtilityClass
public final class LoggerFactory {

    public static Logger createLogger(final Plugin plugin) {
        final int minorVersion = ServerInfo.getSubVersion(plugin);
        return createLogger(plugin, minorVersion);
    }

    public static Logger createLogger(final Plugin plugin, final int minorVersion) {
        return minorVersion >= 19 ? new ModernLogger(plugin) : new LegacyLogger(plugin);
    }
}

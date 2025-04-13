package com.github.groundbreakingmc.mylib.logger.console;

import com.github.groundbreakingmc.mylib.utils.server.ServerInfo;
import lombok.experimental.UtilityClass;
import org.bukkit.plugin.Plugin;

@UtilityClass
@SuppressWarnings("unused")
public final class LoggerFactory {

    public static Logger createLogger(final Plugin plugin) {
        final int minorVersion = ServerInfo.getSubVersion();
        return createLogger(plugin, minorVersion);
    }

    public static Logger createLogger(final Plugin plugin, final int minorVersion) {
        return minorVersion >= 19 ? new ModernLogger(plugin) : new LegacyLogger(plugin);
    }
}

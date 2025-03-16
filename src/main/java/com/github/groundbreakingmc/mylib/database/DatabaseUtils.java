package com.github.groundbreakingmc.mylib.database;

import lombok.experimental.UtilityClass;
import org.bukkit.plugin.Plugin;

import java.io.File;

@SuppressWarnings("unused")
@UtilityClass
public final class DatabaseUtils {

    public String getSQLiteDriverUrl(final Plugin plugin) {
        plugin.getDataFolder().mkdir();
        final File dbFile = new File(plugin.getDataFolder() + File.separator + "database.db");
        return "jdbc:sqlite:" + dbFile;
    }

    public String getMariaDBDriverUrl(final String hostName,
                                      final String databaseName) {
        return getMariaDBDriverUrl(hostName, databaseName, null);
    }

    public String getMariaDBDriverUrl(final String hostName,
                                      final String databaseName,
                                      final String connectionParams) {
        return createDriverUrl(
                "jdbc:mariadb://",
                hostName,
                databaseName,
                connectionParams == null ? "" : connectionParams
        );
    }

    public String getMySQLDriverUrl(final String hostName,
                                    final String databaseName) {
        return getMySQLDriverUrl(hostName, databaseName, null);
    }

    public String getMySQLDriverUrl(final String hostName,
                                    final String databaseName,
                                    final String connectionParams) {
        return createDriverUrl(
                "jdbc:mysql://",
                hostName,
                databaseName,
                connectionParams == null ? "" : connectionParams
        );
    }

    private String createDriverUrl(final String prefix,
                                   final String hostName,
                                   final String databaseName,
                                   final String connectionParams) {
        return prefix + hostName + "/" + databaseName + connectionParams;
    }
}

package com.github.groundbreakingmc.mylib.database;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Utility class for creating database connection URLs and configurations
 */
@SuppressWarnings("unused")
@UtilityClass
public final class DatabaseUtils {

    /**
     * Create SQLite JDBC URL from a file path
     */
    public String sqlite(@NotNull String filePath) {
        return "jdbc:sqlite:" + filePath;
    }

    /**
     * Create SQLite JDBC URL from file
     */
    public String sqlite(@NotNull File file) {
        return "jdbc:sqlite:" + file.getAbsolutePath();
    }

    /**
     * Create MySQL JDBC URL
     */
    public String mysql(@NotNull String host, @NotNull String database) {
        return mysql(host, 3306, database, null);
    }

    /**
     * Create MySQL JDBC URL with port
     */
    public String mysql(@NotNull String host, int port, @NotNull String database) {
        return mysql(host, port, database, null);
    }

    /**
     * Create MySQL JDBC URL with connection parameters
     */
    public String mysql(@NotNull String host, @NotNull String database, @Nullable String params) {
        return mysql(host, 3306, database, params);
    }

    /**
     * Create MySQL JDBC URL with all options
     */
    public String mysql(@NotNull String host, int port, @NotNull String database, @Nullable String params) {
        final StringBuilder url = new StringBuilder("jdbc:mysql://")
                .append(host)
                .append(":")
                .append(port)
                .append("/")
                .append(database);

        if (params != null && !params.isEmpty()) {
            if (!params.startsWith("?")) {
                url.append("?");
            }
            url.append(params);
        }

        return url.toString();
    }

    /**
     * Create MariaDB JDBC URL
     */
    public String mariadb(@NotNull String host, @NotNull String database) {
        return mariadb(host, 3306, database, null);
    }

    /**
     * Create MariaDB JDBC URL with port
     */
    public String mariadb(@NotNull String host, int port, @NotNull String database) {
        return mariadb(host, port, database, null);
    }

    /**
     * Create MariaDB JDBC URL with connection parameters
     */
    public String mariadb(@NotNull String host, @NotNull String database, @Nullable String params) {
        return mariadb(host, 3306, database, params);
    }

    /**
     * Create MariaDB JDBC URL with all options
     */
    public String mariadb(@NotNull String host, int port, @NotNull String database, @Nullable String params) {
        final StringBuilder url = new StringBuilder("jdbc:mariadb://")
                .append(host)
                .append(":")
                .append(port)
                .append("/")
                .append(database);

        if (params != null && !params.isEmpty()) {
            if (!params.startsWith("?")) {
                url.append("?");
            }
            url.append(params);
        }

        return url.toString();
    }

    /**
     * Create PostgreSQL JDBC URL
     */
    public String postgresql(@NotNull String host, @NotNull String database) {
        return postgresql(host, 5432, database, null);
    }

    /**
     * Create PostgreSQL JDBC URL with port
     */
    public String postgresql(@NotNull String host, int port, @NotNull String database) {
        return postgresql(host, port, database, null);
    }

    /**
     * Create PostgreSQL JDBC URL with connection parameters
     */
    public String postgresql(@NotNull String host, @NotNull String database, @Nullable String params) {
        return postgresql(host, 5432, database, params);
    }

    /**
     * Create PostgreSQL JDBC URL with all options
     */
    public String postgresql(@NotNull String host, int port, @NotNull String database, @Nullable String params) {
        final StringBuilder url = new StringBuilder("jdbc:postgresql://")
                .append(host)
                .append(":")
                .append(port)
                .append("/")
                .append(database);

        if (params != null && !params.isEmpty()) {
            if (!params.startsWith("?")) {
                url.append("?");
            }
            url.append(params);
        }

        return url.toString();
    }

    /**
     * Create H2 in-memory database JDBC URL
     */
    public String h2Memory(@NotNull String databaseName) {
        return "jdbc:h2:mem:" + databaseName;
    }

    /**
     * Create H2 file-based database JDBC URL
     */
    public String h2File(@NotNull String filePath) {
        return "jdbc:h2:file:" + filePath;
    }

    /**
     * Builder for MySQL connection parameters
     */
    public static class MySQLParams {
        private final StringBuilder params = new StringBuilder();
        private boolean hasParams = false;

        public MySQLParams useSSL(boolean use) {
            this.addParam("useSSL=" + use);
            return this;
        }

        public MySQLParams serverTimezone(@NotNull String timezone) {
            this.addParam("serverTimezone=" + timezone);
            return this;
        }

        public MySQLParams autoReconnect(boolean auto) {
            this.addParam("autoReconnect=" + auto);
            return this;
        }

        public MySQLParams characterEncoding(@NotNull String encoding) {
            this.addParam("characterEncoding=" + encoding);
            return this;
        }

        public MySQLParams allowPublicKeyRetrieval(boolean allow) {
            this.addParam("allowPublicKeyRetrieval=" + allow);
            return this;
        }

        public MySQLParams custom(@NotNull String param, @NotNull String value) {
            this.addParam(param + "=" + value);
            return this;
        }

        private void addParam(String param) {
            if (this.hasParams) {
                this.params.append("&");
            } else {
                this.params.append("?");
                this.hasParams = true;
            }
            this.params.append(param);
        }

        public String build() {
            return this.params.toString();
        }

        @Override
        public String toString() {
            return this.build();
        }
    }

    /**
     * Create MySQL connection parameters builder
     */
    public MySQLParams mysqlParams() {
        return new MySQLParams();
    }
}

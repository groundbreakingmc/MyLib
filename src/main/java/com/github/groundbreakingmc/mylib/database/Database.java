package com.github.groundbreakingmc.mylib.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced database wrapper with support for connection pooling (HikariCP) and simple JDBC.
 */
@SuppressWarnings("unused")
public class Database implements AutoCloseable {

    private final ConnectionProvider connectionProvider;

    private Database(@NotNull ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    /**
     * Creates a new Database builder for configuration
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Quick setup for SQLite without connection pooling
     */
    public static Database sqlite(@NotNull String filePath) {
        return builder()
                .jdbcUrl("jdbc:sqlite:" + filePath)
                .useSimpleConnection()
                .build();
    }

    /**
     * Quick setup for MySQL/MariaDB without connection pooling
     */
    public static Database mysql(@NotNull String host, @NotNull String database,
                                 @NotNull String username, @NotNull String password) {
        return builder()
                .jdbcUrl("jdbc:mysql://" + host + "/" + database)
                .credentials(username, password)
                .useSimpleConnection()
                .build();
    }

    /**
     * Execute a query that modifies data (INSERT, UPDATE, DELETE)
     */
    public int executeUpdate(@NotNull String query, @NotNull Object... params) throws SQLException {
        try (final Connection conn = this.connection()) {
            return this.executeUpdate(conn, query, params);
        }
    }

    /**
     * Execute a query that modifies data using existing connection
     */
    public int executeUpdate(@NotNull Connection connection, @NotNull String query,
                             @NotNull Object... params) throws SQLException {
        connection.setAutoCommit(false);
        try (final PreparedStatement stmt = connection.prepareStatement(query)) {
            this.setParameters(stmt, params);
            int affected = stmt.executeUpdate();
            connection.commit();
            return affected;
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Execute a SELECT query and map results
     */
    public <T> List<T> query(@NotNull String query, @NotNull ResultSetMapper<T> mapper,
                             @NotNull Object... params) throws SQLException {
        try (final Connection conn = this.connection()) {
            return this.query(conn, query, mapper, params);
        }
    }

    /**
     * Execute a SELECT query and map results using existing connection
     */
    public <T> List<T> query(@NotNull Connection connection, @NotNull String query,
                             @NotNull ResultSetMapper<T> mapper, @NotNull Object... params) throws SQLException {
        try (final PreparedStatement stmt = connection.prepareStatement(query)) {
            this.setParameters(stmt, params);
            try (final ResultSet rs = stmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
                return results;
            }
        }
    }

    /**
     * Execute a SELECT query and return first result or null
     */
    public <T> T queryFirst(@NotNull String query, @NotNull ResultSetMapper<T> mapper,
                            @NotNull Object... params) throws SQLException {
        try (final Connection conn = this.connection()) {
            return this.queryFirst(conn, query, mapper, params);
        }
    }

    /**
     * Execute a SELECT query and return first result or null using existing connection
     */
    public <T> T queryFirst(@NotNull Connection connection, @NotNull String query,
                            @NotNull ResultSetMapper<T> mapper, @NotNull Object... params) throws SQLException {
        try (final PreparedStatement stmt = connection.prepareStatement(query)) {
            this.setParameters(stmt, params);
            try (final ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapper.map(rs);
                }
                return null;
            }
        }
    }

    /**
     * Check if a record exists
     */
    public boolean exists(@NotNull String query, @NotNull Object... params) throws SQLException {
        try (final Connection conn = this.connection()) {
            return this.exists(conn, query, params);
        }
    }

    /**
     * Check if a record exists using existing connection
     */
    public boolean exists(@NotNull Connection connection, @NotNull String query,
                          @NotNull Object... params) throws SQLException {
        try (final PreparedStatement stmt = connection.prepareStatement(query)) {
            this.setParameters(stmt, params);
            try (final ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Execute multiple statements in a transaction
     */
    public void transaction(@NotNull TransactionCallback callback) throws SQLException {
        try (final Connection conn = connection()) {
            conn.setAutoCommit(false);
            try {
                callback.execute(conn);
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Create tables if they don't exist
     */
    public void createTables(@NotNull String... queries) throws SQLException {
        try (final Connection conn = connection();
             final Statement stmt = conn.createStatement()) {
            for (final String query : queries) {
                stmt.execute(query);
            }
        }
    }

    /**
     * Get a connection from the pool or create a new one
     */
    public Connection connection() throws SQLException {
        return this.connectionProvider.connection();
    }

    /**
     * Start building a SELECT query
     */
    public SelectBuilder.SelectStep select(@NotNull String... columns) {
        return SelectBuilder.select(this, columns);
    }

    /**
     * Start building an INSERT query
     */
    public InsertBuilder.InsertStep insert(@NotNull String table) {
        return InsertBuilder.insert(this, table);
    }

    /**
     * Start building an UPDATE query
     */
    public UpdateBuilder.UpdateStep update(@NotNull String table) {
        return UpdateBuilder.update(this, table);
    }

    /**
     * Start building a DELETE query
     */
    public DeleteBuilder.DeleteStep delete(@NotNull String table) {
        return DeleteBuilder.delete(this, table);
    }

    @Override
    public void close() {
        this.connectionProvider.close();
    }

    private void setParameters(@NotNull PreparedStatement stmt, @NotNull Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    /**
     * Functional interface for mapping ResultSet rows to objects
     */
    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(@NotNull ResultSet rs) throws SQLException;
    }

    /**
     * Functional interface for transaction callbacks
     */
    @FunctionalInterface
    public interface TransactionCallback {
        void execute(@NotNull Connection connection) throws SQLException;
    }

    /**
     * Builder for Database configuration
     */
    public static class Builder {
        private String jdbcUrl;
        private String username;
        private String password;
        private boolean usePooling = true;
        private int minIdle = 4;
        private int maxPoolSize = 16;
        private long connectionTimeout = 10000;
        private long idleTimeout = 600000;
        private long maxLifetime = 1800000;

        private Builder() {
        }

        public Builder jdbcUrl(@NotNull String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
            return this;
        }

        public Builder credentials(@Nullable String username, @Nullable String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        public Builder poolSize(int min, int max) {
            this.minIdle = min;
            this.maxPoolSize = max;
            return this;
        }

        public Builder connectionTimeout(long millis) {
            this.connectionTimeout = millis;
            return this;
        }

        public Builder idleTimeout(long millis) {
            this.idleTimeout = millis;
            return this;
        }

        public Builder maxLifetime(long millis) {
            this.maxLifetime = millis;
            return this;
        }

        /**
         * Use simple JDBC connections without pooling (no HikariCP dependency)
         */
        public Builder useSimpleConnection() {
            this.usePooling = false;
            return this;
        }

        /**
         * Enable connection pooling (requires HikariCP at runtime)
         */
        public Builder usePooling() {
            this.usePooling = true;
            return this;
        }

        public Database build() {
            if (this.jdbcUrl == null || this.jdbcUrl.isEmpty()) {
                throw new IllegalStateException("JDBC URL must be specified");
            }

            ConnectionProvider provider;
            if (this.usePooling) {
                provider = HikariConnectionProvider.create(
                        this.jdbcUrl, this.username, this.password,
                        this.minIdle, this.maxPoolSize, this.connectionTimeout,
                        this.idleTimeout, this.maxLifetime
                );
            } else {
                provider = new SimpleConnectionProvider(this.jdbcUrl, this.username, this.password);
            }

            return new Database(provider);
        }
    }

    /**
     * Internal interface for connection management
     */
    private interface ConnectionProvider {
        Connection connection() throws SQLException;

        void close();
    }

    /**
     * Simple JDBC connection provider without pooling
     */
    private record SimpleConnectionProvider(
            String jdbcUrl,
            String username,
            String password
    ) implements ConnectionProvider {

        @Override
        public Connection connection() throws SQLException {
            if (this.username != null && this.password != null) {
                return DriverManager.getConnection(this.jdbcUrl, this.username, this.password);
            }
            return DriverManager.getConnection(this.jdbcUrl);
        }

        @Override
        public void close() {
            // Nothing to close for simple connections
        }
    }

    /**
     * HikariCP connection provider
     */
    private static class HikariConnectionProvider implements ConnectionProvider {

        private final HikariDataSource dataSource;

        private HikariConnectionProvider(HikariDataSource dataSource) {
            this.dataSource = dataSource;
        }

        static ConnectionProvider create(String jdbcUrl, String username, String password,
                                         int minIdle, int maxPoolSize, long connectionTimeout,
                                         long idleTimeout, long maxLifetime) {
            try {
                final HikariConfig hikariConfig = new HikariConfig();
                hikariConfig.setJdbcUrl(jdbcUrl);
                if (username != null) {
                    hikariConfig.setUsername(username);
                }
                if (password != null) {
                    hikariConfig.setPassword(password);
                }
                hikariConfig.setMinimumIdle(minIdle);
                hikariConfig.setMaximumPoolSize(maxPoolSize);
                hikariConfig.setConnectionTimeout(connectionTimeout);
                hikariConfig.setIdleTimeout(idleTimeout);
                hikariConfig.setMaxLifetime(maxLifetime);

                return new HikariConnectionProvider(new HikariDataSource(hikariConfig));
            } catch (Throwable th) {
                throw new RuntimeException("Failed to initialize HikariCP. Make sure it's available at runtime.", th);
            }
        }

        @Override
        public Connection connection() throws SQLException {
            return this.dataSource.getConnection();
        }

        @Override
        public void close() {
            try {
                if (!this.dataSource.isClosed()) {
                    this.dataSource.close();
                }
            } catch (Exception ignored) {
            }
        }
    }
}

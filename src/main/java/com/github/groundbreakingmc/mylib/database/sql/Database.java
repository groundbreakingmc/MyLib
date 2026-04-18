package com.github.groundbreakingmc.mylib.database.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced database wrapper with support for connection pooling (HikariCP) and simple JDBC.
 *
 * <p>Create an instance via the fluent {@link Builder}:
 * <pre>{@code
 * // SQLite — no pooling by default
 * Database db = Database.builder()
 *         .sqlite("/data/app.db")
 *         .build();
 *
 * // MySQL with HikariCP
 * Database db = Database.builder()
 *         .mysql("localhost", "mydb")
 *         .credentials("root", "secret")
 *         .poolSize(4, 16)
 *         .build();
 *
 * // PostgreSQL with custom params
 * Database db = Database.builder()
 *         .postgresql("localhost", "mydb", "?sslmode=require")
 *         .credentials("pg", "secret")
 *         .build();
 * }</pre>
 */
@SuppressWarnings("unused")
public class Database implements AutoCloseable {

    private final ConnectionProvider connectionProvider;
    private final DatabaseType type;

    private Database(@NotNull ConnectionProvider connectionProvider, @NotNull DatabaseType type) {
        this.connectionProvider = connectionProvider;
        this.type = type;
    }

    // -------------------------------------------------------------------------
    // Factory helpers (kept for quick one-liners)
    // -------------------------------------------------------------------------

    /**
     * Creates a new Database {@link Builder}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Quick SQLite setup without connection pooling.
     */
    public static Database sqlite(@NotNull String filePath) {
        return builder().sqlite(filePath).build();
    }

    /**
     * Quick SQLite setup from a {@link File} without connection pooling.
     */
    public static Database sqlite(@NotNull File file) {
        return builder().sqlite(file).build();
    }

    /**
     * Quick MySQL setup without connection pooling.
     */
    public static Database mysql(@NotNull String host, @NotNull String database,
                                 @NotNull String username, @NotNull String password) {
        return builder()
                .mysql(host, database)
                .credentials(username, password)
                .useSimpleConnection()
                .build();
    }

    /**
     * Quick PostgreSQL setup without connection pooling.
     */
    public static Database postgresql(@NotNull String host, @NotNull String database,
                                      @NotNull String username, @NotNull String password) {
        return builder()
                .postgresql(host, database)
                .credentials(username, password)
                .useSimpleConnection()
                .build();
    }

    // -------------------------------------------------------------------------
    // Type accessor
    // -------------------------------------------------------------------------

    /**
     * Returns the {@link DatabaseType} this instance was configured for.
     */
    @NotNull
    public DatabaseType type() {
        return this.type;
    }

    // -------------------------------------------------------------------------
    // Core execution methods
    // -------------------------------------------------------------------------

    /**
     * Execute a query that modifies data (INSERT, UPDATE, DELETE).
     */
    public int executeUpdate(@NotNull String query, @NotNull Object... params) throws SQLException {
        try (final Connection conn = this.connection()) {
            return this.executeUpdate(conn, query, params);
        }
    }

    /**
     * Execute a query that modifies data using an existing connection.
     */
    public int executeUpdate(@NotNull Connection connection, @NotNull String query,
                             @NotNull Object... params) throws SQLException {
        connection.setAutoCommit(false);
        try (final PreparedStatement stmt = connection.prepareStatement(query)) {
            this.setParameters(stmt, params);
            final int affected = stmt.executeUpdate();
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
     * Execute a SELECT query and map all results.
     */
    public <T> List<T> query(@NotNull String query, @NotNull ResultSetMapper<T> mapper,
                             @NotNull Object... params) throws SQLException {
        try (final Connection conn = this.connection()) {
            return this.query(conn, query, mapper, params);
        }
    }

    /**
     * Execute a SELECT query and map all results using an existing connection.
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
     * Execute a SELECT query and return the first result, or {@code null}.
     */
    public <T> T queryFirst(@NotNull String query, @NotNull ResultSetMapper<T> mapper,
                            @NotNull Object... params) throws SQLException {
        try (final Connection conn = this.connection()) {
            return this.queryFirst(conn, query, mapper, params);
        }
    }

    /**
     * Execute a SELECT query and return the first result, or {@code null}, using an existing connection.
     */
    public <T> T queryFirst(@NotNull Connection connection, @NotNull String query,
                            @NotNull ResultSetMapper<T> mapper, @NotNull Object... params) throws SQLException {
        try (final PreparedStatement stmt = connection.prepareStatement(query)) {
            this.setParameters(stmt, params);
            try (final ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapper.map(rs) : null;
            }
        }
    }

    /**
     * Returns {@code true} if at least one row matches the query.
     */
    public boolean exists(@NotNull String query, @NotNull Object... params) throws SQLException {
        try (final Connection conn = this.connection()) {
            return this.exists(conn, query, params);
        }
    }

    /**
     * Returns {@code true} if at least one row matches the query, using an existing connection.
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
     * Execute multiple statements inside a single transaction.
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
     * Execute one or more DDL / CREATE TABLE statements.
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
     * Obtain a connection from the pool (or create a new simple connection).
     */
    public Connection connection() throws SQLException {
        return this.connectionProvider.connection();
    }

    // -------------------------------------------------------------------------
    // Query builder entry points
    // -------------------------------------------------------------------------

    /**
     * Start building a SELECT query.
     */
    public SelectBuilder.SelectStep select(@NotNull String... columns) {
        return SelectBuilder.select(this, columns);
    }

    /**
     * Start building an INSERT query.
     */
    public InsertBuilder.InsertStep insert(@NotNull String table) {
        return InsertBuilder.insert(this, table);
    }

    /**
     * Start building an UPDATE query.
     */
    public UpdateBuilder.UpdateStep update(@NotNull String table) {
        return UpdateBuilder.update(this, table);
    }

    /**
     * Start building a DELETE query.
     */
    public DeleteBuilder.DeleteStep delete(@NotNull String table) {
        return DeleteBuilder.delete(this, table);
    }

    @Override
    public void close() {
        this.connectionProvider.close();
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void setParameters(@NotNull PreparedStatement stmt, @NotNull Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    // -------------------------------------------------------------------------
    // Public functional interfaces
    // -------------------------------------------------------------------------

    /**
     * Maps a single {@link ResultSet} row to an object of type {@code T}.
     */
    @FunctionalInterface
    public interface ResultSetMapper<T> {

        T map(@NotNull ResultSet rs) throws SQLException;
    }

    /**
     * Callback used inside {@link #transaction(TransactionCallback)}.
     */
    @FunctionalInterface
    public interface TransactionCallback {

        void execute(@NotNull Connection connection) throws SQLException;
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    /**
     * Fluent builder for {@link Database}.
     *
     * <p>Use one of the {@code sqlite()}, {@code mysql()}, {@code mariadb()},
     * {@code postgresql()}, {@code h2Memory()}, or {@code h2File()} methods to
     * set the JDBC URL and database type in a single call instead of assembling
     * the URL manually via {@link DatabaseUtils}.
     */
    public static class Builder {

        private String jdbcUrl;
        private String username;
        private String password;
        private DatabaseType databaseType = DatabaseType.MYSQL;
        private boolean usePooling = true;
        private int minIdle = 4;
        private int maxPoolSize = 16;
        private long connectionTimeout = 10_000L;
        private long idleTimeout = 600_000L;
        private long maxLifetime = 1_800_000L;

        private Builder() {
        }

        // -- Raw URL (kept for power-users / custom JDBC drivers) --------------

        /**
         * Set the JDBC URL directly. You should also call {@link #type(DatabaseType)}
         * so that query builders can apply the correct SQL dialect.
         */
        public Builder jdbcUrl(@NotNull String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
            return this;
        }

        /**
         * Override the detected {@link DatabaseType}. Only needed when you set
         * the JDBC URL manually via {@link #jdbcUrl(String)}.
         */
        public Builder type(@NotNull DatabaseType type) {
            this.databaseType = type;
            return this;
        }

        // -- Convenience URL builders ------------------------------------------

        /**
         * Configure SQLite from a file path. Disables connection pooling automatically.
         */
        public Builder sqlite(@NotNull String filePath) {
            this.jdbcUrl = "jdbc:sqlite:" + filePath;
            this.databaseType = DatabaseType.SQLITE;
            this.usePooling = false;
            return this;
        }

        /**
         * Configure SQLite from a {@link File}. Disables connection pooling automatically.
         */
        public Builder sqlite(@NotNull File file) {
            return this.sqlite(file.getAbsolutePath());
        }

        /**
         * Configure MySQL with the default port (3306).
         */
        public Builder mysql(@NotNull String host, @NotNull String database) {
            return this.mysql(host, 3306, database, null);
        }

        /**
         * Configure MySQL with a custom port.
         */
        public Builder mysql(@NotNull String host, int port, @NotNull String database) {
            return this.mysql(host, port, database, null);
        }

        /**
         * Configure MySQL with connection parameters (e.g. {@code "?useSSL=false"}).
         */
        public Builder mysql(@NotNull String host, @NotNull String database, @Nullable String params) {
            return this.mysql(host, 3306, database, params);
        }

        /**
         * Configure MySQL with all options.
         */
        public Builder mysql(@NotNull String host, int port,
                             @NotNull String database, @Nullable String params) {
            this.jdbcUrl = buildNetworkUrl("jdbc:mysql", host, port, database, params);
            this.databaseType = DatabaseType.MYSQL;
            return this;
        }

        /**
         * Configure MariaDB with the default port (3306).
         */
        public Builder mariadb(@NotNull String host, @NotNull String database) {
            return this.mariadb(host, 3306, database, null);
        }

        /**
         * Configure MariaDB with a custom port.
         */
        public Builder mariadb(@NotNull String host, int port, @NotNull String database) {
            return this.mariadb(host, port, database, null);
        }

        /**
         * Configure MariaDB with connection parameters.
         */
        public Builder mariadb(@NotNull String host, @NotNull String database, @Nullable String params) {
            return this.mariadb(host, 3306, database, params);
        }

        /**
         * Configure MariaDB with all options.
         */
        public Builder mariadb(@NotNull String host, int port,
                               @NotNull String database, @Nullable String params) {
            this.jdbcUrl = buildNetworkUrl("jdbc:mariadb", host, port, database, params);
            this.databaseType = DatabaseType.MARIADB;
            return this;
        }

        /**
         * Configure PostgreSQL with the default port (5432).
         */
        public Builder postgresql(@NotNull String host, @NotNull String database) {
            return this.postgresql(host, 5432, database, null);
        }

        /**
         * Configure PostgreSQL with a custom port.
         */
        public Builder postgresql(@NotNull String host, int port, @NotNull String database) {
            return this.postgresql(host, port, database, null);
        }

        /**
         * Configure PostgreSQL with connection parameters.
         */
        public Builder postgresql(@NotNull String host, @NotNull String database, @Nullable String params) {
            return this.postgresql(host, 5432, database, params);
        }

        /**
         * Configure PostgreSQL with all options.
         */
        public Builder postgresql(@NotNull String host, int port,
                                  @NotNull String database, @Nullable String params) {
            this.jdbcUrl = buildNetworkUrl("jdbc:postgresql", host, port, database, params);
            this.databaseType = DatabaseType.POSTGRESQL;
            return this;
        }

        /**
         * Configure an H2 in-memory database. Disables connection pooling automatically.
         */
        public Builder h2Memory(@NotNull String databaseName) {
            this.jdbcUrl = "jdbc:h2:mem:" + databaseName;
            this.databaseType = DatabaseType.H2;
            this.usePooling = false;
            return this;
        }

        /**
         * Configure an H2 file-based database.
         */
        public Builder h2File(@NotNull String filePath) {
            this.jdbcUrl = "jdbc:h2:file:" + filePath;
            this.databaseType = DatabaseType.H2;
            return this;
        }

        // -- Credentials -------------------------------------------------------

        public Builder credentials(@Nullable String username, @Nullable String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        // -- Pool settings -----------------------------------------------------

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
         * Disable HikariCP — use a plain {@link java.sql.DriverManager} connection.
         */
        public Builder useSimpleConnection() {
            this.usePooling = false;
            return this;
        }

        /**
         * Enable HikariCP connection pooling (default for non-file-based databases).
         */
        public Builder usePooling() {
            this.usePooling = true;
            return this;
        }

        // -- Build -------------------------------------------------------------

        public Database build() {
            if (this.jdbcUrl == null || this.jdbcUrl.isEmpty()) {
                throw new IllegalStateException(
                        "JDBC URL must be specified — use one of the convenience methods " +
                                "(sqlite, mysql, postgresql, …) or call jdbcUrl(String) directly.");
            }

            final ConnectionProvider provider = this.usePooling
                    ? HikariConnectionProvider.create(
                    this.jdbcUrl, this.username, this.password,
                    this.minIdle, this.maxPoolSize,
                    this.connectionTimeout, this.idleTimeout, this.maxLifetime)
                    : new SimpleConnectionProvider(this.jdbcUrl, this.username, this.password);

            return new Database(provider, this.databaseType);
        }

        // -- Private helpers ---------------------------------------------------

        private static String buildNetworkUrl(@NotNull String scheme,
                                              @NotNull String host, int port,
                                              @NotNull String database, @Nullable String params) {
            final StringBuilder url = new StringBuilder(scheme)
                    .append("://").append(host)
                    .append(":").append(port)
                    .append("/").append(database);

            if (params != null && !params.isEmpty()) {
                if (!params.startsWith("?")) {
                    url.append("?");
                }
                url.append(params);
            }
            return url.toString();
        }
    }

    // -------------------------------------------------------------------------
    // Connection providers (internal)
    // -------------------------------------------------------------------------

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
                final HikariConfig config = new HikariConfig();
                config.setJdbcUrl(jdbcUrl);
                if (username != null) config.setUsername(username);
                if (password != null) config.setPassword(password);
                config.setMinimumIdle(minIdle);
                config.setMaximumPoolSize(maxPoolSize);
                config.setConnectionTimeout(connectionTimeout);
                config.setIdleTimeout(idleTimeout);
                config.setMaxLifetime(maxLifetime);
                return new HikariConnectionProvider(new HikariDataSource(config));
            } catch (Throwable th) {
                throw new RuntimeException(
                        "Failed to initialise HikariCP. Make sure it is on the runtime classpath.", th
                );
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

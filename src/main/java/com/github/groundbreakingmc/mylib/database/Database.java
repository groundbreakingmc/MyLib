package com.github.groundbreakingmc.mylib.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;

@SuppressWarnings("unused")
public class Database {

    private final HikariDataSource dataSource;

    public Database(final @NotNull String jdbcUrl) {
        this(jdbcUrl, null, null);
    }

    public Database(final @NotNull String jdbcUrl,
                    final @Nullable String userName,
                    final @Nullable String password) {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        if (userName != null) {
            hikariConfig.setUsername(userName);
        }
        if (password != null) {
            hikariConfig.setPassword(password);
        }
        hikariConfig.setMinimumIdle(4);
        hikariConfig.setMaximumPoolSize(16);
        hikariConfig.setConnectionTimeout(10000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    public Database(@NotNull HikariConfig hikariConfig) {
        this.dataSource = new HikariDataSource(hikariConfig);
    }

    public void closeConnection() {
        if (this.dataSource != null && !this.dataSource.isClosed()) {
            this.dataSource.close();
        }
    }

    /**
     * Creates tables in the database if they do not already exist
     * If the tables are already present, no changes will be made
     */
    @SuppressWarnings("SqlSourceToSinkFlow")
    public void createTables(final Connection connection, final String... queries) throws SQLException {
        try (final Statement statement = connection.createStatement()) {
            for (final String query : queries) {
                statement.execute(query);
            }
        }
    }

    /**
     * Executes the specified query.
     *
     * @param query      query to execute
     * @param connection opened connection
     * @param params     params to set
     */
    @SuppressWarnings("SqlSourceToSinkFlow")
    public int executeUpdateQuery(@NotNull String query, @NotNull Connection connection, @NotNull Object... params)
            throws SQLException {
        connection.setAutoCommit(false);
        try (final PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            int affectedRows = statement.executeUpdate();
            connection.commit();
            return affectedRows;
        } catch (final SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Executes the specified query and returns result set.
     *
     * @param query      query to execute
     * @param connection opened connection
     * @param params     params to set
     * @return statement
     */
    @SuppressWarnings("SqlSourceToSinkFlow")
    public PreparedStatement getStatement(@NotNull String query, @NotNull Connection connection, @NotNull Object...
            params) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }

        return statement;
    }

    /**
     * Executes the specified query and returns bo.
     *
     * @param query      query to execute
     * @param connection opened connection
     * @param params     search element
     * @return true if the player contains in table
     */
    @SuppressWarnings("SqlSourceToSinkFlow")
    public boolean containsInTable(@NotNull String query, @NotNull Connection connection, @NotNull Object... params) throws
            SQLException {
        try (final PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            try (final ResultSet result = statement.executeQuery()) {
                return result.next() && result.getBoolean(1);
            }
        }
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }
}

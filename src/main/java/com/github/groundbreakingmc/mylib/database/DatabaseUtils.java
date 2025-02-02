package com.github.groundbreakingmc.mylib.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.experimental.UtilityClass;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;

@UtilityClass
public class DatabaseUtils {

    public static HikariDataSource createConnection(final Plugin plugin) {
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getDriverUrl(plugin));
        hikariConfig.setMinimumIdle(4);
        hikariConfig.setMaximumPoolSize(16);
        hikariConfig.setConnectionTimeout(10000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);

        return new HikariDataSource(hikariConfig);
    }

    private static String getDriverUrl(final Plugin plugin) {
        final File dbFile = new File(plugin.getDataFolder() + File.separator + "database.db");
        return "jdbc:sqlite:" + dbFile;
    }

    public static void closeConnection(final HikariDataSource dataSource) {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    /**
     * Creates tables in the database if they do not already exist
     * If the tables are already present, no changes will be made
     */
    public static void createTables(final Connection connection, final String... queries) throws SQLException {
        try (final Statement statement = connection.createStatement()) {
            for (int i = 0; i < queries.length; i++) {
                statement.execute(queries[i]);
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
    public static void executeUpdateQuery(final String query, final Connection connection, final Object... params) throws SQLException {
        connection.setAutoCommit(false);
        try (final PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            statement.executeUpdate();
            connection.commit();
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
    public static PreparedStatement getStatement(final String query, final Connection connection, final Object... params) throws SQLException {
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
    public static boolean containsInTable(final String query, final Connection connection, final Object... params) throws SQLException {
        try (final PreparedStatement statement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            try (final ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }
}
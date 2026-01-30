package com.github.groundbreakingmc.mylib.database;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

/**
 * Prepared SELECT query that can be executed multiple times
 */
public final class SelectQuery implements Query {

    private final Database database;
    private final String sql;
    private final int parameterCount;

    SelectQuery(@NotNull Database database, @NotNull String sql, int parameterCount) {
        this.database = database;
        this.sql = sql;
        this.parameterCount = parameterCount;
    }

    @Override
    public @NotNull String sql() {
        return this.sql;
    }

    @Override
    public int parameterCount() {
        return this.parameterCount;
    }

    /**
     * Execute query and return all results
     */
    public <T> List<T> fetch(@NotNull Database.ResultSetMapper<T> mapper, @NotNull Object... params) throws SQLException {
        Query.validateParameters(this.parameterCount, params);
        return this.database.query(this.sql, mapper, params);
    }

    /**
     * Execute query and return first result or null
     */
    public <T> T fetchFirst(@NotNull Database.ResultSetMapper<T> mapper, @NotNull Object... params) throws SQLException {
        Query.validateParameters(this.parameterCount, params);
        return this.database.queryFirst(this.sql, mapper, params);
    }

    /**
     * Execute a query and check if results exist
     */
    public boolean exists(@NotNull Object... params) throws SQLException {
        Query.validateParameters(this.parameterCount, params);
        return this.database.exists(this.sql, params);
    }

    /**
     * Execute query and count results
     */
    public long count(@NotNull Object... params) throws SQLException {
        Query.validateParameters(this.parameterCount, params);
        // Build count query
        final String countSql = "SELECT COUNT(*) FROM (" + this.sql + ") AS count_query";
        final Long result = this.database.queryFirst(countSql, rs -> rs.getLong(1), params);
        return result != null ? result : 0L;
    }
}

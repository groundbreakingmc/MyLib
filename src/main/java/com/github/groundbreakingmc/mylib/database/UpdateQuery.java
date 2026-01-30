package com.github.groundbreakingmc.mylib.database;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * Prepared UPDATE query that can be executed multiple times
 */
public final class UpdateQuery implements Query {

    private final Database database;
    private final String sql;
    private final int parameterCount;

    UpdateQuery(@NotNull Database database, @NotNull String sql, int parameterCount) {
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
     * Execute update with provided parameters
     *
     * @return number of affected rows
     */
    public int execute(@NotNull Object... params) throws SQLException {
        Query.validateParameters(this.parameterCount, params);
        return this.database.executeUpdate(this.sql, params);
    }
}

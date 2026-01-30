package com.github.groundbreakingmc.mylib.database;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a prepared SQL query that can be executed multiple times
 */
@SuppressWarnings("unused")
public sealed interface Query permits SelectQuery, InsertQuery, UpdateQuery, DeleteQuery {

    /**
     * Get the SQL string for this query
     */
    @NotNull String sql();

    /**
     * Get the number of expected parameters
     */
    int parameterCount();


    static void validateParameters(int expected, Object[] params) {
        if (params.length != expected) {
            throw new IllegalArgumentException(
                    "Expected " + expected + " parameters but got " + params.length
            );
        }
    }
}

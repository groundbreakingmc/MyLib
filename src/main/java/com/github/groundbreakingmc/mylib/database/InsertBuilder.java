package com.github.groundbreakingmc.mylib.database;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Type-safe INSERT query builder using Step Builder Pattern.
 * Ensures at least one value is provided before execution.
 */
@SuppressWarnings("unused")
public class InsertBuilder {

    /**
     * Start building an INSERT query
     */
    public static InsertStep insert(@NotNull Database database, @NotNull String table) {
        return new Builder(database, table);
    }

    /**
     * Step 1: Must specify at least one value
     */
    public interface InsertStep {
        ValueStep value(@NotNull String column, @NotNull Object value);

        ValueStep values(@NotNull Map<String, Object> values);
    }

    /**
     * Step 2: After first value, can add more values, set options, or execute
     */
    public interface ValueStep extends ExecuteStep {
        ValueStep value(@NotNull String column, @NotNull Object value);

        ValueStep values(@NotNull Map<String, Object> values);

        ExecuteStep orIgnore();
    }

    /**
     * Final step: Execute the query or prepare it for reuse
     */
    public interface ExecuteStep {
        /**
         * Execute immediately
         * @return number of affected rows
         */
        int execute() throws SQLException;

        /**
         * Build the SQL string without executing
         */
        String buildQuery();

        /**
         * Get the values array
         */
        Object[] getValues();

        /**
         * Prepare this query for reuse. Returns an InsertQuery that can be executed multiple times.
         */
        InsertQuery prepare();
    }

    /**
     * Internal builder implementation
     */
    private static class Builder implements InsertStep, ValueStep {

        private final Database database;
        private final String table;
        private final Map<String, Object> valueMap = new LinkedHashMap<>();
        private boolean ignore = false;

        private Builder(@NotNull Database database, @NotNull String table) {
            this.database = database;
            this.table = table;
        }

        @Override
        public ValueStep value(@NotNull String column, @NotNull Object value) {
            this.valueMap.put(column, value);
            return this;
        }

        @Override
        public ValueStep values(@NotNull Map<String, Object> values) {
            this.valueMap.putAll(values);
            return this;
        }

        @Override
        public ExecuteStep orIgnore() {
            this.ignore = true;
            return this;
        }

        @Override
        public String buildQuery() {
            final StringBuilder query = new StringBuilder("INSERT ");

            if (this.ignore) {
                query.append("IGNORE ");
            }

            query.append("INTO ").append(this.table).append(" (");

            final List<String> columns = new ArrayList<>(this.valueMap.keySet());
            query.append(String.join(", ", columns));
            query.append(") VALUES (");

            final List<String> placeholders = new ArrayList<>();
            for (int i = 0; i < columns.size(); i++) {
                placeholders.add("?");
            }
            query.append(String.join(", ", placeholders));
            query.append(")");

            return query.toString();
        }

        @Override
        public int execute() throws SQLException {
            return this.database.executeUpdate(buildQuery(), this.valueMap.values().toArray());
        }

        @Override
        public Object[] getValues() {
            return this.valueMap.values().toArray();
        }

        @Override
        public InsertQuery prepare() {
            return new InsertQuery(this.database, buildQuery(), this.valueMap.size());
        }
    }
}

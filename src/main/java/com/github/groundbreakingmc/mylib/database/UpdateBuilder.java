package com.github.groundbreakingmc.mylib.database;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;

/**
 * Type-safe UPDATE query builder using Step Builder Pattern.
 * Ensures at least one SET value is provided before WHERE clause or execution.
 */
@SuppressWarnings("unused")
public class UpdateBuilder {

    /**
     * Start building an UPDATE query
     */
    public static UpdateStep update(@NotNull Database database, @NotNull String table) {
        return new Builder(database, table);
    }

    /**
     * Step 1: Must specify at least one SET value
     */
    public interface UpdateStep {
        SetStep set(@NotNull String column, @NotNull Object value);

        SetStep set(@NotNull Map<String, Object> values);
    }

    /**
     * Step 2: After first SET, can add more values or add WHERE clause
     */
    public interface SetStep extends WhereStep, ExecuteStep {
        SetStep set(@NotNull String column, @NotNull Object value);

        SetStep set(@NotNull Map<String, Object> values);
    }

    /**
     * Step 3: Can add WHERE conditions
     */
    public interface WhereStep extends ExecuteStep {
        ConditionStep where(@NotNull String condition, @NotNull Object... params);
    }

    /**
     * Step 4: After WHERE, can add AND/OR or execute
     */
    public interface ConditionStep extends ExecuteStep {
        ConditionStep and(@NotNull String condition, @NotNull Object... params);

        ConditionStep or(@NotNull String condition, @NotNull Object... params);
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
         * Get all parameters (SET values + WHERE conditions)
         */
        Object[] getParameters();

        /**
         * Prepare this query for reuse. Returns an UpdateQuery that can be executed multiple times.
         */
        UpdateQuery prepare();
    }

    /**
     * Internal builder implementation
     */
    private static class Builder implements UpdateStep, SetStep, ConditionStep {

        private final Database database;
        private final String table;
        private final Map<String, Object> setValues = new LinkedHashMap<>();
        private final List<String> conditions = new ArrayList<>();
        private final List<Object> conditionParams = new ArrayList<>();

        private Builder(@NotNull Database database, @NotNull String table) {
            this.database = database;
            this.table = table;
        }

        @Override
        public SetStep set(@NotNull String column, @NotNull Object value) {
            this.setValues.put(column, value);
            return this;
        }

        @Override
        public SetStep set(@NotNull Map<String, Object> values) {
            this.setValues.putAll(values);
            return this;
        }

        @Override
        public ConditionStep where(@NotNull String condition, @NotNull Object... params) {
            this.conditions.add(condition);
            this.conditionParams.addAll(Arrays.asList(params));
            return this;
        }

        @Override
        public ConditionStep and(@NotNull String condition, @NotNull Object... params) {
            this.conditions.add(condition);
            this.conditionParams.addAll(Arrays.asList(params));
            return this;
        }

        @Override
        public ConditionStep or(@NotNull String condition, @NotNull Object... params) {
            if (!this.conditions.isEmpty()) {
                final String lastCondition = this.conditions.remove(this.conditions.size() - 1);
                this.conditions.add("(" + lastCondition + " OR " + condition + ")");
                this.conditionParams.addAll(Arrays.asList(params));
            }
            return this;
        }

        @Override
        public String buildQuery() {
            final StringBuilder query = new StringBuilder("UPDATE ").append(this.table).append(" SET ");

            final List<String> setParts = new ArrayList<>();
            for (final String column : this.setValues.keySet()) {
                setParts.add(column + " = ?");
            }
            query.append(String.join(", ", setParts));

            if (!this.conditions.isEmpty()) {
                query.append(" WHERE ").append(String.join(" AND ", this.conditions));
            }

            return query.toString();
        }

        @Override
        public int execute() throws SQLException {
            final List<Object> allParams = new ArrayList<>(this.setValues.values());
            allParams.addAll(this.conditionParams);
            return this.database.executeUpdate(buildQuery(), allParams.toArray());
        }

        @Override
        public Object[] getParameters() {
            final List<Object> allParams = new ArrayList<>(this.setValues.values());
            allParams.addAll(this.conditionParams);
            return allParams.toArray();
        }

        @Override
        public UpdateQuery prepare() {
            final int paramCount = this.setValues.size() + this.conditionParams.size();
            return new UpdateQuery(this.database, buildQuery(), paramCount);
        }
    }
}

package com.github.groundbreakingmc.mylib.database;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Type-safe DELETE query builder using Step Builder Pattern.
 * Can execute immediately or add WHERE conditions.
 */
@SuppressWarnings("unused")
public class DeleteBuilder {

    /**
     * Start building a DELETE query
     */
    public static DeleteStep delete(@NotNull Database database, @NotNull String table) {
        return new Builder(database, table);
    }

    /**
     * Step 1: Can add WHERE or execute (delete all)
     */
    public interface DeleteStep extends ExecuteStep {
        ConditionStep where(@NotNull String condition, @NotNull Object... params);
    }

    /**
     * Step 2: After WHERE, can add 'AND/OR' or execute
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
         *
         * @return number of affected rows
         */
        int execute() throws SQLException;

        /**
         * Build the SQL string without executing
         */
        String buildQuery();

        /**
         * Get the parameter array
         */
        Object[] getParameters();

        /**
         * Prepare this query for reuse. Returns a DeleteQuery that can be executed multiple times.
         */
        DeleteQuery prepare();
    }

    /**
     * Internal builder implementation
     */
    private static class Builder implements DeleteStep, ConditionStep {

        private final Database database;
        private final String table;
        private final List<String> conditions = new ArrayList<>();
        private final List<Object> parameters = new ArrayList<>();

        private Builder(@NotNull Database database, @NotNull String table) {
            this.database = database;
            this.table = table;
        }

        @Override
        public ConditionStep where(@NotNull String condition, @NotNull Object... params) {
            this.conditions.add(condition);
            this.parameters.addAll(Arrays.asList(params));
            return this;
        }

        @Override
        public ConditionStep and(@NotNull String condition, @NotNull Object... params) {
            this.conditions.add(condition);
            this.parameters.addAll(Arrays.asList(params));
            return this;
        }

        @Override
        public ConditionStep or(@NotNull String condition, @NotNull Object... params) {
            if (!this.conditions.isEmpty()) {
                final String lastCondition = this.conditions.remove(this.conditions.size() - 1);
                this.conditions.add("(" + lastCondition + " OR " + condition + ")");
                this.parameters.addAll(Arrays.asList(params));
            }
            return this;
        }

        @Override
        public String buildQuery() {
            final StringBuilder query = new StringBuilder("DELETE FROM ").append(this.table);

            if (!this.conditions.isEmpty()) {
                query.append(" WHERE ").append(String.join(" AND ", this.conditions));
            }

            return query.toString();
        }

        @Override
        public int execute() throws SQLException {
            return this.database.executeUpdate(buildQuery(), this.parameters.toArray());
        }

        @Override
        public Object[] getParameters() {
            return this.parameters.toArray();
        }

        @Override
        public DeleteQuery prepare() {
            return new DeleteQuery(this.database, buildQuery(), this.parameters.size());
        }
    }
}

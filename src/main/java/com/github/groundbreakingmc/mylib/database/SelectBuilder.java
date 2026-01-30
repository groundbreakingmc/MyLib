package com.github.groundbreakingmc.mylib.database;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Type-safe SELECT query builder using Step Builder Pattern.
 * Each step only exposes methods that are valid at that point in the query construction.
 */
@SuppressWarnings("unused")
public class SelectBuilder {

    /**
     * Start building a SELECT query
     */
    public static SelectStep select(@NotNull Database database, @NotNull String... columns) {
        return new Builder(database, columns);
    }

    /**
     * Step 1: Must specify FROM clause
     */
    public interface SelectStep {
        FromStep from(@NotNull String table);
    }

    /**
     * Step 2: Can add JOINs, WHERE, or execute
     */
    public interface FromStep extends WhereStep, ExecuteStep {
        FromStep innerJoin(@NotNull String table, @NotNull String condition);

        FromStep leftJoin(@NotNull String table, @NotNull String condition);

        FromStep rightJoin(@NotNull String table, @NotNull String condition);
    }

    /**
     * Step 3: Can add WHERE conditions or skip to GROUP BY/ORDER BY/LIMIT
     */
    public interface WhereStep extends GroupByStep {
        ConditionStep where(@NotNull String condition, @NotNull Object... params);
    }

    /**
     * Step 4: After WHERE, can add AND/OR or continue to GROUP BY/ORDER BY/LIMIT
     */
    public interface ConditionStep extends GroupByStep {
        ConditionStep and(@NotNull String condition, @NotNull Object... params);

        ConditionStep or(@NotNull String condition, @NotNull Object... params);
    }

    /**
     * Step 5: Can add GROUP BY or skip to ORDER BY/LIMIT
     */
    public interface GroupByStep extends OrderByStep {
        HavingStep groupBy(@NotNull String... columns);
    }

    /**
     * Step 6: After GROUP BY, can add HAVING or continue to ORDER BY/LIMIT
     */
    public interface HavingStep extends OrderByStep {
        OrderByStep having(@NotNull String condition);
    }

    /**
     * Step 7: Can add ORDER BY or skip to LIMIT
     */
    public interface OrderByStep extends LimitStep {
        LimitStep orderBy(@NotNull String orderBy);

        LimitStep asc(@NotNull String column);

        LimitStep desc(@NotNull String column);
    }

    /**
     * Step 8: Can add LIMIT and/or OFFSET
     */
    public interface LimitStep extends ExecuteStep {
        OffsetStep limit(int limit);
    }

    /**
     * Step 9: After LIMIT, can add OFFSET
     */
    public interface OffsetStep extends ExecuteStep {
        ExecuteStep offset(int offset);
    }

    /**
     * Final step: Execute the query or prepare it for reuse
     */
    public interface ExecuteStep {
        /**
         * Execute immediately and return all results
         */
        <T> List<T> fetch(@NotNull Database.ResultSetMapper<T> mapper) throws SQLException;

        /**
         * Execute immediately and return first result or null
         */
        <T> T fetchFirst(@NotNull Database.ResultSetMapper<T> mapper) throws SQLException;

        /**
         * Execute immediately and check if results exist
         */
        boolean exists() throws SQLException;

        /**
         * Execute immediately and count results
         */
        long count() throws SQLException;

        /**
         * Build the SQL string without executing
         */
        String buildQuery();

        /**
         * Prepare this query for reuse. Returns a SelectQuery that can be executed multiple times.
         */
        SelectQuery prepare();
    }

    /**
     * Internal builder implementation
     */
    private static class Builder implements SelectStep, FromStep, ConditionStep,
            HavingStep, OffsetStep {

        private final Database database;
        private final String[] columns;
        private String table;
        private final List<String> joins = new ArrayList<>();
        private final List<String> conditions = new ArrayList<>();
        private final List<Object> parameters = new ArrayList<>();
        private String groupBy;
        private String having;
        private String orderBy;
        private Integer limit;
        private Integer offset;

        private Builder(@NotNull Database database, @NotNull String... columns) {
            this.database = database;
            this.columns = columns.length == 0 ? new String[]{"*"} : columns;
        }

        @Override
        public FromStep from(@NotNull String table) {
            this.table = table;
            return this;
        }

        @Override
        public FromStep innerJoin(@NotNull String table, @NotNull String condition) {
            this.joins.add("INNER JOIN " + table + " ON " + condition);
            return this;
        }

        @Override
        public FromStep leftJoin(@NotNull String table, @NotNull String condition) {
            this.joins.add("LEFT JOIN " + table + " ON " + condition);
            return this;
        }

        @Override
        public FromStep rightJoin(@NotNull String table, @NotNull String condition) {
            this.joins.add("RIGHT JOIN " + table + " ON " + condition);
            return this;
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
        public HavingStep groupBy(@NotNull String... columns) {
            this.groupBy = String.join(", ", columns);
            return this;
        }

        @Override
        public OrderByStep having(@NotNull String condition) {
            this.having = condition;
            return this;
        }

        @Override
        public LimitStep orderBy(@NotNull String orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        @Override
        public LimitStep asc(@NotNull String column) {
            this.orderBy = column + " ASC";
            return this;
        }

        @Override
        public LimitStep desc(@NotNull String column) {
            this.orderBy = column + " DESC";
            return this;
        }

        @Override
        public OffsetStep limit(int limit) {
            this.limit = limit;
            return this;
        }

        @Override
        public ExecuteStep offset(int offset) {
            this.offset = offset;
            return this;
        }

        @Override
        public String buildQuery() {
            final StringBuilder query = new StringBuilder("SELECT ");
            query.append(String.join(", ", this.columns));
            query.append(" FROM ").append(this.table);

            for (final String join : this.joins) {
                query.append(" ").append(join);
            }

            if (!this.conditions.isEmpty()) {
                query.append(" WHERE ").append(String.join(" AND ", this.conditions));
            }

            if (this.groupBy != null) {
                query.append(" GROUP BY ").append(this.groupBy);
            }

            if (this.having != null) {
                query.append(" HAVING ").append(this.having);
            }

            if (this.orderBy != null) {
                query.append(" ORDER BY ").append(this.orderBy);
            }

            if (this.limit != null) {
                query.append(" LIMIT ").append(this.limit);
            }

            if (this.offset != null) {
                query.append(" OFFSET ").append(this.offset);
            }

            return query.toString();
        }

        @Override
        public <T> List<T> fetch(@NotNull Database.ResultSetMapper<T> mapper) throws SQLException {
            return this.database.query(buildQuery(), mapper, this.parameters.toArray());
        }

        @Override
        public <T> T fetchFirst(@NotNull Database.ResultSetMapper<T> mapper) throws SQLException {
            return this.database.queryFirst(buildQuery(), mapper, this.parameters.toArray());
        }

        @Override
        public boolean exists() throws SQLException {
            return this.database.exists(buildQuery(), this.parameters.toArray());
        }

        @Override
        public long count() throws SQLException {
            final Builder countBuilder = new Builder(this.database, "COUNT(*)");
            countBuilder.table = this.table;
            countBuilder.conditions.addAll(this.conditions);
            countBuilder.parameters.addAll(this.parameters);
            countBuilder.joins.addAll(this.joins);

            final Long result = countBuilder.fetchFirst(rs -> rs.getLong(1));
            return result != null ? result : 0L;
        }

        @Override
        public SelectQuery prepare() {
            return new SelectQuery(this.database, buildQuery(), this.parameters.size());
        }
    }
}

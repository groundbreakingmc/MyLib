package com.github.groundbreakingmc.mylib.database.sql;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Type-safe SELECT query builder using the Step Builder Pattern.
 *
 * <p>Convenience additions over the basic API:
 * <ul>
 *   <li>{@link WhereStep#whereIn(String, Collection)} / {@link ConditionStep#andIn(String, Collection)}</li>
 *   <li>{@link WhereStep#whereNull(String)} / {@link WhereStep#whereNotNull(String)}</li>
 * </ul>
 */
@SuppressWarnings("unused")
public class SelectBuilder {

    public static SelectStep select(@NotNull Database database, @NotNull String... columns) {
        return new Builder(database, columns);
    }

    // -------------------------------------------------------------------------
    // Step interfaces
    // -------------------------------------------------------------------------

    /**
     * Step 1: Must specify FROM clause.
     */
    public interface SelectStep {

        FromStep from(@NotNull String table);
    }

    /**
     * Step 2: Optional JOINs, then WHERE / ORDER / LIMIT / execute.
     */
    public interface FromStep extends WhereStep, ExecuteStep {

        FromStep innerJoin(@NotNull String table, @NotNull String condition);

        FromStep leftJoin(@NotNull String table, @NotNull String condition);

        FromStep rightJoin(@NotNull String table, @NotNull String condition);
    }

    /**
     * Step 3: Optional WHERE.
     */
    public interface WhereStep extends GroupByStep {

        ConditionStep where(@NotNull String condition, @NotNull Object... params);

        ConditionStep whereIn(@NotNull String column, @NotNull Collection<?> values);

        ConditionStep whereNull(@NotNull String column);

        ConditionStep whereNotNull(@NotNull String column);
    }

    /**
     * Step 4: After WHERE — additional conditions or continue.
     */
    public interface ConditionStep extends GroupByStep {

        ConditionStep and(@NotNull String condition, @NotNull Object... params);

        ConditionStep andIn(@NotNull String column, @NotNull Collection<?> values);

        ConditionStep andNull(@NotNull String column);

        ConditionStep andNotNull(@NotNull String column);

        ConditionStep or(@NotNull String condition, @NotNull Object... params);
    }

    /**
     * Step 5: Optional GROUP BY.
     */
    public interface GroupByStep extends OrderByStep {

        HavingStep groupBy(@NotNull String... columns);
    }

    /**
     * Step 6: Optional HAVING.
     */
    public interface HavingStep extends OrderByStep {

        OrderByStep having(@NotNull String condition);
    }

    /**
     * Step 7: Optional ORDER BY.
     */
    public interface OrderByStep extends LimitStep {

        LimitStep orderBy(@NotNull String expression);

        LimitStep asc(@NotNull String column);

        LimitStep desc(@NotNull String column);
    }

    /**
     * Step 8: Optional LIMIT.
     */
    public interface LimitStep extends ExecuteStep {

        OffsetStep limit(int limit);
    }

    /**
     * Step 9: Optional OFFSET.
     */
    public interface OffsetStep extends ExecuteStep {

        ExecuteStep offset(int offset);
    }

    /**
     * Final step: execute or prepare.
     */
    public interface ExecuteStep {

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

    // -------------------------------------------------------------------------
    // Internal builder
    // -------------------------------------------------------------------------

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

        // -- FROM / JOIN -------------------------------------------------------

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

        // -- WHERE / AND / OR --------------------------------------------------

        @Override
        public ConditionStep where(@NotNull String condition, @NotNull Object... params) {
            this.conditions.add(condition);
            this.parameters.addAll(Arrays.asList(params));
            return this;
        }

        @Override
        public ConditionStep whereIn(@NotNull String column, @NotNull Collection<?> values) {
            return addIn(column, values);
        }

        @Override
        public ConditionStep whereNull(@NotNull String column) {
            this.conditions.add(column + " IS NULL");
            return this;
        }

        @Override
        public ConditionStep whereNotNull(@NotNull String column) {
            this.conditions.add(column + " IS NOT NULL");
            return this;
        }

        @Override
        public ConditionStep and(@NotNull String condition, @NotNull Object... params) {
            this.conditions.add(condition);
            this.parameters.addAll(Arrays.asList(params));
            return this;
        }

        @Override
        public ConditionStep andIn(@NotNull String column, @NotNull Collection<?> values) {
            return addIn(column, values);
        }

        @Override
        public ConditionStep andNull(@NotNull String column) {
            this.conditions.add(column + " IS NULL");
            return this;
        }

        @Override
        public ConditionStep andNotNull(@NotNull String column) {
            this.conditions.add(column + " IS NOT NULL");
            return this;
        }

        @Override
        public ConditionStep or(@NotNull String condition, @NotNull Object... params) {
            if (!this.conditions.isEmpty()) {
                final String last = this.conditions.remove(this.conditions.size() - 1);
                this.conditions.add("(" + last + " OR " + condition + ")");
            } else {
                this.conditions.add(condition);
            }
            this.parameters.addAll(Arrays.asList(params));
            return this;
        }

        private ConditionStep addIn(@NotNull String column, @NotNull Collection<?> values) {
            if (values.isEmpty()) {
                // IN () is invalid SQL — produce a condition that never matches
                this.conditions.add("1=0");
                return this;
            }
            final String placeholders = "?, ".repeat(values.size());
            this.conditions.add(column + " IN ("
                    + placeholders.substring(0, placeholders.length() - 2) + ")");
            this.parameters.addAll(values);
            return this;
        }

        // -- GROUP BY / HAVING -------------------------------------------------

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

        // -- ORDER BY ----------------------------------------------------------

        @Override
        public LimitStep orderBy(@NotNull String expression) {
            this.orderBy = expression;
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

        // -- LIMIT / OFFSET ----------------------------------------------------

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

        // -- Build -------------------------------------------------------------

        @Override
        public String buildQuery() {
            final StringBuilder q = new StringBuilder("SELECT ");
            q.append(String.join(", ", this.columns));
            q.append(" FROM ").append(this.table);

            for (final String join : this.joins) {
                q.append(' ').append(join);
            }
            if (!this.conditions.isEmpty()) {
                q.append(" WHERE ").append(String.join(" AND ", this.conditions));
            }
            if (this.groupBy != null) q.append(" GROUP BY ").append(this.groupBy);
            if (this.having != null) q.append(" HAVING ").append(this.having);
            if (this.orderBy != null) q.append(" ORDER BY ").append(this.orderBy);
            if (this.limit != null) q.append(" LIMIT ").append(this.limit);
            if (this.offset != null) q.append(" OFFSET ").append(this.offset);

            return q.toString();
        }

        // -- Execute -----------------------------------------------------------

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
            final Builder counter = new Builder(this.database, "COUNT(*)");
            counter.table = this.table;
            counter.conditions.addAll(this.conditions);
            counter.parameters.addAll(this.parameters);
            counter.joins.addAll(this.joins);
            final Long result = counter.fetchFirst(rs -> rs.getLong(1));
            return result != null ? result : 0L;
        }

        @Override
        public SelectQuery prepare() {
            return new SelectQuery(this.database, buildQuery(), this.parameters.size());
        }
    }
}

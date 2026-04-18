package com.github.groundbreakingmc.mylib.database.sql;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Type-safe INSERT query builder using the Step Builder Pattern.
 * Generates dialect-correct SQL based on the {@link DatabaseType} of the
 * owning {@link Database} instance:
 *
 * <table>
 *   <caption>SQL syntax for orIgnore() by database dialect</caption>
 *   <tr><th>Dialect</th><th>orIgnore()</th></tr>
 *   <tr><td>SQLite</td>      <td>{@code INSERT OR IGNORE INTO …}</td></tr>
 *   <tr><td>MySQL/MariaDB</td><td>{@code INSERT IGNORE INTO …}</td></tr>
 *   <tr><td>PostgreSQL/H2</td><td>{@code INSERT INTO … ON CONFLICT DO NOTHING}</td></tr>
 * </table>
 */
@SuppressWarnings("unused")
public class InsertBuilder {

    /**
     * Start building an INSERT query for the given table.
     */
    public static InsertStep insert(@NotNull Database database, @NotNull String table) {
        return new Builder(database, table);
    }

    // -------------------------------------------------------------------------
    // Step interfaces
    // -------------------------------------------------------------------------

    /**
     * Step 1: Must specify at least one column / value.
     */
    public interface InsertStep {

        ValueStep value(@NotNull String column, @NotNull Object value);

        ValueStep values(@NotNull Map<String, Object> values);
    }

    /**
     * Step 2: Can add more values, choose a conflict strategy, or execute.
     */
    public interface ValueStep extends ExecuteStep {

        ValueStep value(@NotNull String column, @NotNull Object value);

        ValueStep values(@NotNull Map<String, Object> values);

        /**
         * Skip this row silently if a uniqueness constraint is violated.
         * The exact SQL emitted depends on the database type:
         * <ul>
         *   <li>SQLite        → {@code INSERT OR IGNORE INTO}</li>
         *   <li>MySQL/MariaDB → {@code INSERT IGNORE INTO}</li>
         *   <li>PostgreSQL/H2 → {@code INSERT INTO … ON CONFLICT DO NOTHING}</li>
         * </ul>
         */
        ExecuteStep orIgnore();

        /**
         * Replace the conflicting row entirely (SQLite only: {@code INSERT OR REPLACE}).
         *
         * @throws UnsupportedOperationException for non-SQLite databases
         */
        ExecuteStep orReplace();
    }

    /**
     * Final step: execute immediately or prepare for reuse.
     */
    public interface ExecuteStep {

        /**
         * Execute immediately.
         *
         * @return number of affected rows
         */
        int execute() throws SQLException;

        /**
         * Build the SQL string without executing.
         */
        String buildQuery();

        /**
         * Get the values array in column-declaration order.
         */
        Object[] getValues();

        /**
         * Prepare this query for reuse.
         * Returns an {@link InsertQuery} that can be executed multiple times
         * with different parameter sets.
         */
        InsertQuery prepare();
    }

    // -------------------------------------------------------------------------
    // Internal builder
    // -------------------------------------------------------------------------

    private static class Builder implements InsertStep, ValueStep {

        private final Database database;
        private final String table;
        private final Map<String, Object> valueMap = new LinkedHashMap<>();

        /**
         * How to handle conflicts.
         */
        private ConflictStrategy conflictStrategy = ConflictStrategy.NONE;

        private Builder(@NotNull Database database, @NotNull String table) {
            this.database = database;
            this.table = table;
        }

        // -- Value accumulation ------------------------------------------------

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

        // -- Conflict strategies -----------------------------------------------

        @Override
        public ExecuteStep orIgnore() {
            this.conflictStrategy = ConflictStrategy.IGNORE;
            return this;
        }

        @Override
        public ExecuteStep orReplace() {
            final DatabaseType type = this.database.type();
            if (type != DatabaseType.SQLITE) {
                throw new UnsupportedOperationException(
                        "INSERT OR REPLACE is a SQLite-specific construct. " +
                                "Current database type: " + type.name() + ". " +
                                "Use orIgnore() or a manual upsert instead.");
            }
            this.conflictStrategy = ConflictStrategy.REPLACE;
            return this;
        }

        // -- Query building ----------------------------------------------------

        @Override
        public String buildQuery() {
            final DatabaseType dialect = this.database.type();

            final List<String> columns = new ArrayList<>(this.valueMap.keySet());
            final String columnList = String.join(", ", columns);
            final String placeholders = "?, ".repeat(columns.size());
            final String valClause = "(" + placeholders.substring(0, placeholders.length() - 2) + ")";

            final StringBuilder query = new StringBuilder();

            switch (this.conflictStrategy) {
                case IGNORE -> query.append(dialect.insertIgnorePrefix());
                case REPLACE -> query.append(dialect.insertOrReplacePrefix()); // SQLite-only guard already applied
                default -> query.append(dialect.insertPrefix());
            }

            query.append(this.table)
                    .append(" (").append(columnList).append(")")
                    .append(" VALUES ")
                    .append(valClause);

            if (this.conflictStrategy == ConflictStrategy.IGNORE) {
                query.append(dialect.insertIgnoreSuffix());
            }

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

    // -------------------------------------------------------------------------
    // Conflict strategy enum (internal)
    // -------------------------------------------------------------------------

    private enum ConflictStrategy {
        /**
         * Plain INSERT — fail on conflict.
         */
        NONE,
        /**
         * Skip silently on conflict.
         */
        IGNORE,
        /**
         * Replace on conflict (SQLite only).
         */
        REPLACE
    }
}

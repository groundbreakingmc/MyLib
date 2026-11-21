package com.github.groundbreakingmc.mylib.database.sql;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Type-safe INSERT / UPSERT query builder using the Step Builder Pattern.
 *
 * <p>Dialect matrix:
 * <table>
 *   <caption>SQL syntax per dialect and strategy</caption>
 *   <tr><th>Strategy</th><th>SQLite</th><th>MySQL / MariaDB</th><th>PostgreSQL / H2</th></tr>
 *   <tr><td>{@code orIgnore()}</td>
 *       <td>{@code INSERT OR IGNORE INTO …}</td>
 *       <td>{@code INSERT IGNORE INTO …}</td>
 *       <td>{@code INSERT INTO … ON CONFLICT DO NOTHING}</td></tr>
 *   <tr><td>{@code orReplace()}</td>
 *       <td>{@code INSERT OR REPLACE INTO …}</td>
 *       <td colspan="2">throws {@link UnsupportedOperationException}</td></tr>
 *   <tr><td>{@code upsert(conflictCols, updateCols)}</td>
 *       <td>{@code INSERT OR REPLACE INTO …}</td>
 *       <td>{@code … ON DUPLICATE KEY UPDATE col=VALUES(col)}</td>
 *       <td>{@code … ON CONFLICT (pk) DO UPDATE SET col=EXCLUDED.col}</td></tr>
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
     * Step 1: Must specify at least one column/value.
     */
    public interface InsertStep {

        ValueStep value(@NotNull String column, @NotNull Object value);

        ValueStep values(@NotNull Map<String, Object> values);
    }

    /**
     * Step 2: Can add more values, pick a conflict strategy, or execute.
     */
    public interface ValueStep extends ExecuteStep {

        ValueStep value(@NotNull String column, @NotNull Object value);

        ValueStep values(@NotNull Map<String, Object> values);

        /**
         * Skip silently on uniqueness-constraint violation.
         */
        ExecuteStep orIgnore();

        /**
         * Replace the conflicting row (SQLite only — {@code INSERT OR REPLACE}).
         *
         * @throws UnsupportedOperationException for non-SQLite databases
         */
        ExecuteStep orReplace();

        /**
         * UPSERT: insert the row; if a conflict occurs, update the specified columns.
         *
         * <ul>
         *   <li><b>MySQL / MariaDB</b>: {@code ON DUPLICATE KEY UPDATE col=VALUES(col), …}
         *       — {@code conflictTarget} is ignored.</li>
         *   <li><b>SQLite</b>: {@code INSERT OR REPLACE INTO …}
         *       — both {@code conflictTarget} and {@code updateColumns} are ignored.</li>
         *   <li><b>PostgreSQL / H2</b>: {@code ON CONFLICT (conflictTarget) DO UPDATE SET col=EXCLUDED.col, …}</li>
         * </ul>
         *
         * @param conflictTarget PK / unique-index columns — required for PostgreSQL/H2.
         * @param updateColumns  Columns to overwrite when the row already exists.
         */
        ExecuteStep upsert(@NotNull String[] conflictTarget, @NotNull String... updateColumns);
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
         * Values in column-declaration order.
         */
        Object[] getValues();

        /**
         * Prepare for reuse. Returns an {@link InsertQuery} that accepts
         * different parameter sets on each call.
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

        private ConflictStrategy strategy = ConflictStrategy.NONE;
        /**
         * PostgreSQL / H2 conflict target (PK columns).
         */
        private String[] conflictTarget = new String[0];
        /**
         * Columns to overwrite on conflict (UPSERT).
         */
        private String[] updateColumns = new String[0];

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
            this.strategy = ConflictStrategy.IGNORE;
            return this;
        }

        @Override
        public ExecuteStep orReplace() {
            if (this.database.type() != DatabaseType.SQLITE) {
                throw new UnsupportedOperationException(
                        "INSERT OR REPLACE is SQLite-specific. Current dialect: "
                                + this.database.type() + ". Use upsert() instead.");
            }
            this.strategy = ConflictStrategy.REPLACE;
            return this;
        }

        @Override
        public ExecuteStep upsert(@NotNull String[] conflictTarget, @NotNull String... updateColumns) {
            this.strategy = ConflictStrategy.UPSERT;
            this.conflictTarget = conflictTarget;
            this.updateColumns = updateColumns;
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

            return switch (this.strategy) {
                case NONE -> dialect.insertPrefix()
                        + this.table + " (" + columnList + ") VALUES " + valClause;

                case IGNORE -> dialect.insertIgnorePrefix()
                        + this.table + " (" + columnList + ") VALUES " + valClause
                        + dialect.insertIgnoreSuffix();

                case REPLACE -> dialect.insertOrReplacePrefix()  // SQLite-only guard already checked
                        + this.table + " (" + columnList + ") VALUES " + valClause;

                case UPSERT -> buildUpsertQuery(dialect, columnList, valClause);
            };
        }

        private String buildUpsertQuery(DatabaseType dialect, String columnList, String valClause) {
            return switch (dialect) {
                // SQLite replaces the whole row on conflict — simplest and fully correct
                // for tables without auto-increment PKs in the update set.
                case SQLITE -> "INSERT OR REPLACE INTO " + this.table + " (" + columnList + ") VALUES " + valClause;
                case MYSQL, MARIADB -> {
                    final String updates = Arrays.stream(this.updateColumns)
                            .map(col -> col + "=VALUES(" + col + ")")
                            .collect(Collectors.joining(", "));
                    yield "INSERT INTO " + this.table
                            + " (" + columnList + ") VALUES " + valClause
                            + " ON DUPLICATE KEY UPDATE " + updates;
                }
                case POSTGRESQL, H2 -> {
                    if (this.conflictTarget.length == 0) {
                        throw new IllegalStateException(
                                "conflictTarget must not be empty for " + dialect + " UPSERT.");
                    }
                    final String target = String.join(", ", this.conflictTarget);
                    final String updates = Arrays.stream(this.updateColumns)
                            .map(col -> col + "=EXCLUDED." + col)
                            .collect(Collectors.joining(", "));
                    yield "INSERT INTO " + this.table
                            + " (" + columnList + ") VALUES " + valClause
                            + " ON CONFLICT (" + target + ") DO UPDATE SET " + updates;
                }
            };
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
    // Conflict strategy (internal)
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
        REPLACE,

        /**
         * UPSERT — insert or update on conflict.
         * SQLite: ON CONFLICT(...) DO UPDATE
         * MySQL: ON DUPLICATE KEY UPDATE
         */
        UPSERT
    }
}

package com.github.groundbreakingmc.mylib.database.sql;

/**
 * Supported database types with dialect-specific SQL generation.
 */
@SuppressWarnings("unused")
public enum DatabaseType {

    SQLITE,
    MYSQL,
    MARIADB,
    POSTGRESQL,
    H2;

    // -------------------------------------------------------------------------
    // INSERT dialect
    // -------------------------------------------------------------------------

    /**
     * Returns the INSERT prefix for a normal (non-ignore) insert.
     * All dialects use the same form here.
     */
    public String insertPrefix() {
        return "INSERT INTO ";
    }

    /**
     * Returns the INSERT prefix for an "insert-or-ignore" operation.
     * <ul>
     *   <li>SQLite  → {@code INSERT OR IGNORE INTO}</li>
     *   <li>MySQL / MariaDB → {@code INSERT IGNORE INTO}</li>
     *   <li>PostgreSQL / H2 → {@code INSERT INTO} (suffix carries the intent)</li>
     * </ul>
     */
    public String insertIgnorePrefix() {
        return switch (this) {
            case SQLITE -> "INSERT OR IGNORE INTO ";
            case MYSQL, MARIADB -> "INSERT IGNORE INTO ";
            case POSTGRESQL, H2 -> "INSERT INTO ";
        };
    }

    /**
     * Returns the suffix appended after the VALUES clause for an
     * "insert-or-ignore" operation.
     * <ul>
     *   <li>PostgreSQL / H2 → {@code ON CONFLICT DO NOTHING}</li>
     *   <li>Others           → empty string</li>
     * </ul>
     */
    public String insertIgnoreSuffix() {
        return switch (this) {
            case POSTGRESQL, H2 -> " ON CONFLICT DO NOTHING";
            default -> "";
        };
    }

    // -------------------------------------------------------------------------
    // UPSERT dialect  (INSERT … ON DUPLICATE KEY / ON CONFLICT DO UPDATE)
    // -------------------------------------------------------------------------

    /**
     * Returns whether this dialect supports a native upsert clause
     * ({@code ON DUPLICATE KEY UPDATE} / {@code ON CONFLICT DO UPDATE}).
     */
    public boolean supportsNativeUpsert() {
        return switch (this) {
            case MYSQL, MARIADB, POSTGRESQL, H2 -> true;
            case SQLITE -> true; // INSERT OR REPLACE / INSERT OR UPDATE
        };
    }

    /**
     * Returns the upsert keyword / phrase that follows the VALUES clause.
     *
     * @param targetColumns conflict-target for PostgreSQL / H2 (e.g. {@code "(id)"}),
     *                      ignored for MySQL / MariaDB.
     */
    public String upsertClause(String targetColumns) {
        return switch (this) {
            case MYSQL, MARIADB -> " ON DUPLICATE KEY UPDATE ";
            case POSTGRESQL, H2 -> " ON CONFLICT " + targetColumns + " DO UPDATE SET ";
            case SQLITE -> throw new UnsupportedOperationException(
                    "SQLite uses INSERT OR REPLACE — use insertOrReplacePrefix() instead.");
        };
    }

    /**
     * SQLite-only: prefix for an INSERT-or-REPLACE operation.
     */
    public String insertOrReplacePrefix() {
        if (this != SQLITE) {
            throw new UnsupportedOperationException(
                    "INSERT OR REPLACE is a SQLite-specific construct. Use upsertClause() for " + this.name());
        }
        return "INSERT OR REPLACE INTO ";
    }

    // -------------------------------------------------------------------------
    // LIMIT / OFFSET dialect
    // -------------------------------------------------------------------------

    /**
     * All supported dialects share the same LIMIT / OFFSET syntax,
     * so this is a convenience constant rather than a real dialect split.
     */
    public String limitClause(int limit) {
        return " LIMIT " + limit;
    }

    public String offsetClause(int offset) {
        return " OFFSET " + offset;
    }

    // -------------------------------------------------------------------------
    // Default JDBC URL scheme
    // -------------------------------------------------------------------------

    /**
     * Returns the JDBC URL scheme for this type (without trailing {@code ://}).
     */
    public String jdbcScheme() {
        return switch (this) {
            case SQLITE -> "jdbc:sqlite";
            case MYSQL -> "jdbc:mysql";
            case MARIADB -> "jdbc:mariadb";
            case POSTGRESQL -> "jdbc:postgresql";
            case H2 -> "jdbc:h2";
        };
    }

    // -------------------------------------------------------------------------
    // Default ports
    // -------------------------------------------------------------------------

    /**
     * Returns the conventional default port for this database type,
     * or -1 if the concept does not apply (e.g. SQLite, H2 in-memory).
     */
    public int defaultPort() {
        return switch (this) {
            case MYSQL, MARIADB -> 3306;
            case POSTGRESQL -> 5432;
            case SQLITE, H2 -> -1;
        };
    }
}

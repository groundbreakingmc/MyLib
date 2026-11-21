package com.github.groundbreakingmc.mylib.database.sql;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * Utility helpers for database configuration.
 *
 * <p><b>Note:</b> JDBC URL construction has been moved into {@link Database.Builder}
 * ({@code Database.builder().mysql(…).credentials(…).build()}) and no longer lives here.
 * This class now only exposes {@link MySQLParams} — a fluent builder for MySQL/MariaDB
 * query-string parameters.
 */
@SuppressWarnings("unused")
@UtilityClass
public final class DatabaseUtils {

    /**
     * Start building a MySQL / MariaDB connection-string parameters block.
     *
     * <pre>{@code
     * String params = DatabaseUtils.mysqlParams()
     *         .useSSL(false)
     *         .serverTimezone("UTC")
     *         .autoReconnect(true)
     *         .build();   // → "?useSSL=false&serverTimezone=UTC&autoReconnect=true"
     *
     * Database db = Database.builder()
     *         .mysql("localhost", "mydb", params)
     *         .credentials("root", "secret")
     *         .build();
     * }</pre>
     */
    public MySQLParams mysqlParams() {
        return new MySQLParams();
    }

    // -------------------------------------------------------------------------

    public static final class MySQLParams {

        private final StringBuilder params = new StringBuilder();
        private boolean hasParams = false;

        private MySQLParams() {
        }

        public MySQLParams useSSL(boolean use) {
            return this.addParam("useSSL=" + use);
        }

        public MySQLParams serverTimezone(@NotNull String timezone) {
            return this.addParam("serverTimezone=" + timezone);
        }

        public MySQLParams autoReconnect(boolean auto) {
            return this.addParam("autoReconnect=" + auto);
        }

        public MySQLParams characterEncoding(@NotNull String encoding) {
            return this.addParam("characterEncoding=" + encoding);
        }

        public MySQLParams allowPublicKeyRetrieval(boolean allow) {
            return this.addParam("allowPublicKeyRetrieval=" + allow);
        }

        /**
         * Append any custom {@code key=value} parameter.
         */
        public MySQLParams param(@NotNull String key, @NotNull String value) {
            return this.addParam(key + "=" + value);
        }

        private MySQLParams addParam(@NotNull String param) {
            this.params.append(this.hasParams ? "&" : "?");
            this.params.append(param);
            this.hasParams = true;
            return this;
        }

        public String build() {
            return this.params.toString();
        }

        @Override
        public String toString() {
            return build();
        }
    }
}

package com.rti.ledgercore.db;

import com.rti.ledgercore.config.Env;

import java.util.regex.Pattern;

/**
 * PostgreSQL connection settings, read from {@code .env} or the environment — never from source.
 *
 * <ul>
 *   <li>{@code DB_URL} — JDBC URL, e.g. {@code jdbc:postgresql://localhost:5432/payments}</li>
 *   <li>{@code DB_USERNAME} / {@code DB_PASSWORD} — database credentials</li>
 *   <li>{@code DB_SCHEMA} — Postgres schema LedgerCore's tables live in (default {@code ledgercore}),
 *       so they never collide with other tables in the same database</li>
 *   <li>{@code DB_INIT_MODE} — {@code always} (default) runs {@code schema.sql} + {@code data.sql}
 *       on startup; {@code never} skips them</li>
 * </ul>
 */
public record DatabaseConfig(String url, String username, String password, String schema, boolean initializeOnStartup) {

    private static final Pattern SAFE_SCHEMA_NAME = Pattern.compile("[a-z_][a-z0-9_]{0,62}");

    public DatabaseConfig {
        if (!SAFE_SCHEMA_NAME.matcher(schema).matches()) {
            throw new IllegalArgumentException(
                    "DB_SCHEMA must be a lowercase Postgres identifier (letters, digits, underscore)");
        }
    }

    public static DatabaseConfig fromEnvironment() {
        return new DatabaseConfig(
                Env.require("DB_URL"),
                Env.require("DB_USERNAME"),
                Env.require("DB_PASSWORD"),
                Env.get("DB_SCHEMA").orElse("ledgercore"),
                !Env.get("DB_INIT_MODE").orElse("always").equalsIgnoreCase("never"));
    }

    /** Keeps the password out of logs and stack traces. */
    @Override
    public String toString() {
        return "DatabaseConfig[url=" + url + ", username=" + username + ", schema=" + schema + "]";
    }
}

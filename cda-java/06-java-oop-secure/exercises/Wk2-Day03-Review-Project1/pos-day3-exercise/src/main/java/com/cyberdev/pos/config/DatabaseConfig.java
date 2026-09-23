package com.cyberdev.pos.config;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Reads DB connection settings from environment variables and builds a Spring DataSource
 * from them, exactly mirroring the sibling dndrpg project's DatabaseConfig. DriverManagerDataSource
 * opens a new physical connection on every call -- no pooling -- which keeps this teaching
 * exercise dependency-free. A real deployment would use a pooled DataSource (HikariCP)
 * instead; JdbcTemplate itself doesn't care which kind of DataSource it's handed.
 */
public final class DatabaseConfig {
    private DatabaseConfig() {}

    public static DataSource createDataSource() {
        String url = System.getenv().getOrDefault("POS_DB_URL", "jdbc:postgresql://localhost:5432/quickpay_pos");
        String user = System.getenv().getOrDefault("POS_DB_USER", "quickpay");
        String password = System.getenv().getOrDefault("POS_DB_PASSWORD", "changeme");

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        return dataSource;
    }
}

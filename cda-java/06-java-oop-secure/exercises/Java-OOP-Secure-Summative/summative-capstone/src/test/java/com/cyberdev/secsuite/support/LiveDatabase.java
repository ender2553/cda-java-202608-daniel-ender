package com.cyberdev.secsuite.support;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.junit.jupiter.api.Assumptions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * GIVEN TEST INFRASTRUCTURE -- not a graded TODO.
 *
 * Live-PostgreSQL support for the Jdbc* repository tests (SEC-3, SEC-6, SEC-8, SEC-9, SEC-15),
 * following the QuickPay POS Day2Tests/Day3Tests pattern: connect once in @BeforeAll, and if no
 * database is reachable every DB-backed test is SKIPPED via Assumptions (a missing dev database
 * is an environment problem, not a code problem).
 *
 * Connection settings are the application's own (SECSUITE_DB_URL / SECSUITE_DB_USER /
 * SECSUITE_DB_PASSWORD, see application-jdbc.properties) against a database built from
 * schema/schema.sql, dcl.sql and seed.sql. The tests connect as the least-privilege
 * secsuite_app role, which has
 * NO DELETE privilege -- so instead of deleting test rows afterwards (the POS approach), every
 * test runs inside ONE transaction on ONE connection (autocommit off, shared through Spring's
 * SingleConnectionDataSource) and {@link #rollback()} undoes it after each test. The seeded
 * rows are never modified and nothing a test inserts survives it.
 */
public final class LiveDatabase {

    private final Connection connection;
    private final JdbcTemplate jdbcTemplate;
    private final ConfigurableApplicationContext applicationContext;
    private final boolean available;

    private LiveDatabase(Connection connection, JdbcTemplate jdbcTemplate,
                         ConfigurableApplicationContext applicationContext, boolean available) {
        this.connection = connection;
        this.jdbcTemplate = jdbcTemplate;
        this.applicationContext = applicationContext;
        this.available = available;
    }

    /** Never throws: an unreachable database yields an instance whose isAvailable() is false. */
    public static LiveDatabase connect() {
        Connection connection = null;
        ConfigurableApplicationContext applicationContext = null;
        try {
            applicationContext = new SpringApplicationBuilder(DatabaseTestConfig.class)
                    .profiles("jdbc")
                    .web(WebApplicationType.NONE)
                    .run();
            connection = applicationContext.getBean(DataSource.class).getConnection();
            connection.setAutoCommit(false);
            JdbcTemplate template = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
            template.queryForObject("SELECT 1", Integer.class);
            template.queryForObject("SELECT count(*) FROM asset", Long.class); // schema applied?
            return new LiveDatabase(connection, template, applicationContext, true);
        } catch (Exception | LinkageError e) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                    // nothing more to do
                }
            }
            if (applicationContext != null) {
                applicationContext.close();
            }
            return new LiveDatabase(null, null, null, false);
        }
    }

    public boolean isAvailable() {
        return available;
    }

    /** Call first in every DB-backed test. */
    public void assumeAvailable() {
        Assumptions.assumeTrue(available,
                "No PostgreSQL database with the SecSuite schema reachable at SECSUITE_DB_URL "
                        + "(see REQUIREMENTS.md, 'Database setup') -- skipping JDBC-backed test");
    }

    public JdbcTemplate jdbcTemplate() {
        return jdbcTemplate;
    }

    /** Undo everything the current test wrote. Safe to call when the database is unavailable. */
    public void rollback() {
        if (available) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new IllegalStateException("rollback failed", e);
            }
        }
    }

    public void close() {
        if (available) {
            try {
                connection.rollback();
                connection.close();
                applicationContext.close();
            } catch (SQLException ignored) {
                // best effort at the end of the test class
            }
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class DatabaseTestConfig {
    }
}

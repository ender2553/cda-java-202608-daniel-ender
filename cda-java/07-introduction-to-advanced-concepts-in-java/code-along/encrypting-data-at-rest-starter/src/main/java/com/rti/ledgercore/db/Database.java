package com.rti.ledgercore.db;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LedgerCore's PostgreSQL database. Connection settings come from {@link DatabaseConfig}
 * ({@code .env} or the environment).
 *
 * <p>Every connection's {@code search_path} is set to {@code DB_SCHEMA}, so LedgerCore's tables
 * live in their own Postgres schema. On startup (unless {@code DB_INIT_MODE=never}) it runs
 * {@code schema.sql} then {@code data.sql} from {@code src/main/resources} — the same idea as
 * Spring's {@code spring.sql.init.mode=always}. {@code schema.sql} drops and recreates the tables,
 * so every run starts from the same clean state.
 */
public final class Database {

    private final PGSimpleDataSource dataSource;

    private Database(PGSimpleDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Connects using {@code .env}/environment settings and runs the init scripts. */
    public static Database connect() {
        return connect(DatabaseConfig.fromEnvironment());
    }

    public static Database connect(DatabaseConfig config) {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setURL(config.url());
        ds.setUser(config.username());
        ds.setPassword(config.password());
        ds.setCurrentSchema(config.schema());

        Database database = new Database(ds);
        if (config.initializeOnStartup()) {
            database.initialize(config.schema());
        }
        return database;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    private void initialize(String schema) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            // schema was validated against a strict identifier pattern in DatabaseConfig
            statement.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
            for (String sql : readScript("schema.sql")) {
                statement.execute(sql);
            }
            for (String sql : readScript("data.sql")) {
                statement.execute(sql);
            }
        } catch (SQLException initFailure) {
            throw new IllegalStateException("Failed to initialize LedgerCore schema", initFailure);
        }
    }

    /** Splits a classpath SQL script into statements; {@code --} line comments are dropped. */
    private static List<String> readScript(String resource) {
        try (InputStream in = Database.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                return List.of();
            }
            String withoutComments = new String(in.readAllBytes(), StandardCharsets.UTF_8).lines()
                    .filter(line -> !line.trim().startsWith("--"))
                    .collect(Collectors.joining("\n"));
            return Arrays.stream(withoutComments.split(";"))
                    .map(String::trim)
                    .filter(sql -> !sql.isEmpty())
                    .toList();
        } catch (IOException readFailure) {
            throw new UncheckedIOException("Failed to read " + resource, readFailure);
        }
    }
}

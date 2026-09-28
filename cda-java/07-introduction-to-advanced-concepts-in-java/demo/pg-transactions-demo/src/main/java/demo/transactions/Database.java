package demo.transactions;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Connects to the demo's Postgres container. Settings come from the
 * environment; the fallbacks match docker-compose.yml and are for the local,
 * throwaway container only.
 */
public final class Database {

    private final String port;
    private final String name;
    private final String user;
    private final String password;

    private Database(String port, String name, String user, String password) {
        this.port = port;
        this.name = name;
        this.user = user;
        this.password = password;
    }

    /**
     * Defaults target the Docker container on port 5433. For a locally
     * installed Postgres, set LEDGER_DB_PORT (usually 5432) and, ideally,
     * LEDGER_DB_PASSWORD.
     */
    public static Database fromEnvironment() {
        return new Database(
                env("LEDGER_DB_PORT", "5433"),
                env("LEDGER_DB_NAME", "ledger_demo"),
                env("LEDGER_DB_USER", "demo"),
                env("LEDGER_DB_PASSWORD", "demo"));
    }

    /** A new connection. Like every JDBC connection, it starts with auto-commit ON. */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getUrl(), user, password);
    }

    /** Drops and recreates the three tables and reseeds the two accounts. */
    public void reset() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(loadSchema());
        }
    }

    public String getUrl() {
        return "jdbc:postgresql://localhost:" + port + "/" + name;
    }

    /** psql connection options for this database, run inside the container (Docker) or on the host (local install). */
    public String psqlOptions(boolean insideContainer) {
        String target = "-U " + user + " -d " + name;
        return insideContainer ? target : "-h localhost -p " + port + " " + target;
    }

    private static String loadSchema() {
        try (InputStream in = Database.class.getResourceAsStream("/schema.sql")) {
            if (in == null) {
                throw new IllegalStateException("schema.sql is missing from the classpath");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}

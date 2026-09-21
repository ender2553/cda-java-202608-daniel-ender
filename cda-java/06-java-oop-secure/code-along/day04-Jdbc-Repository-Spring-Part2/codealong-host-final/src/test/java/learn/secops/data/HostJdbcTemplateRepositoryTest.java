package learn.secops.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * INSTRUCTOR REFERENCE. Point SECOPS_DB_PASSWORD and a local
 * "secops_host_codealong_test" database at this before running
 * `mvn test`. Not required for the code-along itself, but a good
 * "does this actually work" check, and a template if you want to
 * hand students a self-check suite after the live build.
 */
class HostJdbcTemplateRepositoryTest {

    private final DataSource dataSource = buildDataSource();
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    private final HostRepository repository = new HostJdbcTemplateRepository(jdbcTemplate);

    @BeforeEach
    void resetKnownGoodState() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("schema.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("data.sql"));
        }
    }

    @Test
    void shouldFindAll() {
        assertEquals(4, repository.findAll().size());
    }

    @Test
    void shouldFindByCriticality() {
        List<Host> critical = repository.findByCriticality(HostCriticality.CRITICAL);
        assertEquals(1, critical.size());
        assertEquals("db-02.internal", critical.get(0).getHostname());
    }

    @Test
    void shouldFindById() {
        Host host = repository.findById(1);
        assertNotNull(host);
        assertEquals("web-01.internal", host.getHostname());
        assertEquals(HostCriticality.MEDIUM, host.getCriticality());
    }

    @Test
    void shouldReturnNullWhenIdMissing() {
        assertNull(repository.findById(9999));
    }

    @Test
    void shouldAdd() {
        Host host = new Host("printer-09.internal", "10.0.5.9", HostCriticality.LOW);

        Host actual = repository.add(host);

        assertNotNull(actual);
        assertTrue(actual.getHostId() > 0);
        assertEquals("printer-09.internal", actual.getHostname());
    }

    @Test
    void shouldDefaultNullCriticalityToHigh() {
        Host host = new Host("mystery-box.internal", "10.0.9.9", null);
        assertEquals(HostCriticality.HIGH, host.getCriticality());
    }

    @Test
    void shouldRejectInvalidIpAddress() {
        assertThrows(IllegalArgumentException.class,
                () -> new Host("bad-host.internal", "999.1.1.1", HostCriticality.LOW));
    }

    @Test
    void shouldUpdateCriticality() {
        assertTrue(repository.updateCriticality(1, HostCriticality.CRITICAL));
        assertEquals(HostCriticality.CRITICAL, repository.findById(1).getCriticality());
    }

    @Test
    void shouldNotUpdateCriticalityOfMissingHost() {
        assertFalse(repository.updateCriticality(9999, HostCriticality.CRITICAL));
    }

    @Test
    void shouldDeleteExisting() {
        assertTrue(repository.deleteById(4));
    }

    @Test
    void shouldNotDeleteMissing() {
        assertFalse(repository.deleteById(9999));
    }

    private static DataSource buildDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setServerNames(new String[]{"localhost"});
        ds.setPortNumbers(new int[]{5432});
        ds.setDatabaseName("secops_host_codealong_test");
        ds.setUser("secops_app");
        ds.setPassword(System.getenv("SECOPS_DB_PASSWORD"));
        return ds;
    }
}

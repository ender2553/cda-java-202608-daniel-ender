package learn.secops.data;

import learn.secops.models.Ticket;
import learn.secops.models.TicketStatus;
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

class TicketJdbcTemplateRepositoryTest {

    private final DataSource dataSource = buildDataSource();
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    private final TicketRepository repository = new TicketJdbcTemplateRepository(jdbcTemplate);

    /**
     * The JDBC lesson ended on an open question: how do we get "known
     * good state" so tests can run again and again? This is the
     * answer -- re-run schema.sql and data.sql before every single
     * test so each test starts from the exact same four seed rows,
     * no matter what earlier tests inserted, updated, or deleted.
     */
    @BeforeEach
    void resetKnownGoodState() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("schema.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("data.sql"));
        }
    }

    @Test
    void shouldFindAll() {
        List<Ticket> tickets = repository.findAll();
        assertEquals(4, tickets.size());
    }

    @Test
    void shouldFindByHostId() {
        List<Ticket> tickets = repository.findByHostId(1);
        assertEquals(2, tickets.size());
    }

    @Test
    void shouldFindById() {
        Ticket ticket = repository.findById(1);
        assertNotNull(ticket);
        assertEquals(TicketStatus.OPEN, ticket.getStatus());
    }

    @Test
    void shouldReturnNullWhenIdMissing() {
        assertNull(repository.findById(9999));
    }

    @Test
    void shouldAdd() {
        Ticket ticket = new Ticket(2, "Suspicious cron job installed");

        Ticket actual = repository.add(ticket);

        assertNotNull(actual);
        assertTrue(actual.getTicketId() > 0);
        assertEquals("Suspicious cron job installed", actual.getDescription());
    }

    @Test
    void shouldUpdateExisting() {
        Ticket ticket = repository.findById(2);
        ticket.resolve();

        assertTrue(repository.update(ticket));
        assertEquals(TicketStatus.RESOLVED, repository.findById(2).getStatus());
    }

    @Test
    void shouldNotUpdateMissing() {
        Ticket ticket = new Ticket(9999, 1, "Ghost ticket", TicketStatus.OPEN,
                java.time.LocalDateTime.now());

        assertFalse(repository.update(ticket));
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
        ds.setDatabaseName("secops_test");
        ds.setUser("secops_app");
        ds.setPassword(System.getenv("SECOPS_DB_PASSWORD"));
        return ds;
    }
}

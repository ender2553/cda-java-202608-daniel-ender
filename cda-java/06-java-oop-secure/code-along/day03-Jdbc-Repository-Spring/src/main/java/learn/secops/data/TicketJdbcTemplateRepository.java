package learn.secops.data;

import learn.secops.models.Ticket;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The preferred implementation. Same TicketRepository contract as
 * TicketJdbcRepository, but every method is a single JdbcTemplate call
 * -- no manual Connection/Statement/ResultSet lifecycle, no
 * try-with-resources, no checked SQLException to catch. Spring wires
 * this class into the application because of the @Repository
 * annotation and the constructor that asks for a JdbcTemplate.
 */
@Repository
public class TicketJdbcTemplateRepository implements TicketRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Ticket> mapper = new TicketMapper();

    public TicketJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Ticket> findAll() {
        final String sql = "select ticket_id, host_id, description, status, opened_at "
                + "from ticket "
                + "order by ticket_id;";
        return jdbcTemplate.query(sql, mapper);
    }

    @Override
    public List<Ticket> findByHostId(int hostId) {
        final String sql = "select ticket_id, host_id, description, status, opened_at "
                + "from ticket "
                + "where host_id = ? "
                + "order by ticket_id;";
        return jdbcTemplate.query(sql, mapper, hostId);
    }

    @Override
    public Ticket findById(int ticketId) {
        final String sql = "select ticket_id, host_id, description, status, opened_at "
                + "from ticket "
                + "where ticket_id = ?;";
        try {
            return jdbcTemplate.queryForObject(sql, mapper, ticketId);
        } catch (EmptyResultDataAccessException ex) {
            // No row for this id is a valid outcome, not an error.
            return null;
        }
    }

    @Override
    public Ticket add(Ticket ticket) {
        // PostgreSQL's `returning` clause lets us treat an insert as a
        // query that hands back the generated id in one round trip --
        // no KeyHolder needed.
        final String sql = "insert into ticket (host_id, description, status, opened_at) "
                + "values (?, ?, ?, ?) "
                + "returning ticket_id;";

        int generatedId = jdbcTemplate.queryForObject(sql, Integer.class,
                ticket.getHostId(), ticket.getDescription(),
                ticket.getStatus().name(), ticket.getOpenedAt());

        return new Ticket(generatedId, ticket.getHostId(), ticket.getDescription(),
                ticket.getStatus(), ticket.getOpenedAt());
    }

    @Override
    public boolean update(Ticket ticket) {
        final String sql = "update ticket set "
                + "description = ?, "
                + "status = ? "
                + "where ticket_id = ?;";
        return jdbcTemplate.update(sql,
                ticket.getDescription(), ticket.getStatus().name(), ticket.getTicketId()) > 0;
    }

    @Override
    public boolean deleteById(int ticketId) {
        final String sql = "delete from ticket where ticket_id = ?;";
        return jdbcTemplate.update(sql, ticketId) > 0;
    }
}

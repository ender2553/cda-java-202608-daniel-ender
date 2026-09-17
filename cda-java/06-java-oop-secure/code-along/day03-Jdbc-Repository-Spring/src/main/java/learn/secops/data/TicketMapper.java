package learn.secops.data;

import learn.secops.models.Ticket;
import learn.secops.models.TicketStatus;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Converts one ResultSet row into one Ticket. JdbcTemplate calls
 * mapRow once per row and assembles the List<Ticket> for us -- this
 * is the only piece of row-mapping logic in the whole class, and it
 * is identical to the mapping code inside TicketJdbcRepository.
 */
public class TicketMapper implements RowMapper<Ticket> {

    @Override
    public Ticket mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Ticket(
                rs.getInt("ticket_id"),
                rs.getInt("host_id"),
                rs.getString("description"),
                TicketStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("opened_at").toLocalDateTime()
        );
    }
}

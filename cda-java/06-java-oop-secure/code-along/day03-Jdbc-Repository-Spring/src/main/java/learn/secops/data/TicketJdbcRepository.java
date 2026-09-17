package learn.secops.data;

import learn.secops.models.Ticket;
import learn.secops.models.TicketStatus;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A TicketRepository implementation using plain JDBC against
 * PostgreSQL. This is deliberately the "long way" -- we build it once
 * so the JdbcTemplate version later in the day reads as a clear
 * improvement, not magic.
 *
 * Every dynamic value goes through a PreparedStatement placeholder.
 * There is no string concatenation into SQL anywhere in this class --
 * see the JDBC lesson's SQL injection example for what we are avoiding.
 */
public class TicketJdbcRepository implements TicketRepository {

    private final DataSource dataSource;

    public TicketJdbcRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Convenience factory for local class use; a real app injects the DataSource instead. */
    public static TicketJdbcRepository connectingTo(String host, int port, String database,
                                                       String user, String password) {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setServerNames(new String[]{host});
        ds.setPortNumbers(new int[]{port});
        ds.setDatabaseName(database);
        ds.setUser(user);
        ds.setPassword(password);
        return new TicketJdbcRepository(ds);
    }

    @Override
    public List<Ticket> findAll() {
        final String sql = "select ticket_id, host_id, description, status, opened_at "
                + "from ticket "
                + "order by ticket_id;";

        List<Ticket> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Ticket> findByHostId(int hostId) {
        // Dynamic value -> PreparedStatement, never string concatenation.
        final String sql = "select ticket_id, host_id, description, status, opened_at "
                + "from ticket "
                + "where host_id = ? "
                + "order by ticket_id;";

        List<Ticket> result = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, hostId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    @Override
    public Ticket findById(int ticketId) {
        final String sql = "select ticket_id, host_id, description, status, opened_at "
                + "from ticket "
                + "where ticket_id = ?;";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, ticketId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public Ticket add(Ticket ticket) {
        // PostgreSQL note: instead of Statement.RETURN_GENERATED_KEYS
        // (which behaves inconsistently across drivers), Postgres lets
        // us ask for the new row's id directly with RETURNING, then
        // read it back as an ordinary query result.
        final String sql = "insert into ticket (host_id, description, status, opened_at) "
                + "values (?, ?, ?, ?) "
                + "returning ticket_id;";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, ticket.getHostId());
            statement.setString(2, ticket.getDescription());
            statement.setString(3, ticket.getStatus().name());
            statement.setObject(4, ticket.getOpenedAt());

            try (ResultSet keys = statement.executeQuery()) {
                if (keys.next()) {
                    int generatedId = keys.getInt("ticket_id");
                    return new Ticket(generatedId, ticket.getHostId(), ticket.getDescription(),
                            ticket.getStatus(), ticket.getOpenedAt());
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(Ticket ticket) {
        final String sql = "update ticket set "
                + "description = ?, "
                + "status = ? "
                + "where ticket_id = ?;";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, ticket.getDescription());
            statement.setString(2, ticket.getStatus().name());
            statement.setInt(3, ticket.getTicketId());

            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteById(int ticketId) {
        final String sql = "delete from ticket where ticket_id = ?;";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, ticketId);
            return statement.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private Ticket mapRow(ResultSet rs) throws SQLException {
        return new Ticket(
                rs.getInt("ticket_id"),
                rs.getInt("host_id"),
                rs.getString("description"),
                TicketStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("opened_at").toLocalDateTime()
        );
    }
}

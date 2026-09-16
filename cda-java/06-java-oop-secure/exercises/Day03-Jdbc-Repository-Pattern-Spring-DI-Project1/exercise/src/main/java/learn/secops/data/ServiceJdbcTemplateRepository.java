package learn.secops.data;

import learn.secops.models.Service;
import learn.secops.models.ServiceStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * TODO: Implement every method below using JdbcTemplate -- the same
 * approach used for TicketJdbcTemplateRepository in the Day 3 lecture.
 *
 * Do not use raw Connection/Statement/ResultSet management here.
 * Do not build any SQL with string concatenation of a parameter value.
 *
 * Run ServiceJdbcTemplateRepositoryTest as you go -- it will not
 * compile-fail on an unfinished method (each stub already compiles),
 * but it WILL fail at runtime until the method is implemented.
 */
@Repository
public class ServiceJdbcTemplateRepository implements ServiceRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Service> mapper = this::mapRow;

    public ServiceJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Service> findAll() {
        String sql = """
                SELECT service_id, host_id, name, port, status
                FROM service
                ORDER BY service_id
                """;

        return jdbcTemplate.query(sql, mapper);
    }

    @Override
    public List<Service> findByHostId(int hostId) {
        String sql = """
                SELECT service_id, host_id, name, port, status
                FROM service
                WHERE host_id = ?
                ORDER BY service_id
                """;

        return jdbcTemplate.query(sql, mapper, hostId);
    }

    @Override
    public Service findById(int serviceId) {
        String sql = """
            SELECT service_id, host_id, name, port, status
            FROM service
            WHERE service_id = ?
            """;

        List<Service> services = jdbcTemplate.query(sql, mapper, serviceId);

        return services.isEmpty() ? null : services.get(0);
    }

    @Override
    public Service add(Service service) {
        String sql = """
                INSERT INTO service (host_id, name, port, status)
                VALUES (?, ?, ?, ?)
                RETURNING service_id
                """;

        int serviceId = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                service.getHostId(),
                service.getName(),
                service.getPort(),
                service.getStatus().name()
        );

        return new Service(
                serviceId,
                service.getHostId(),
                service.getName(),
                service.getPort(),
                service.getStatus()
        );
    }

    @Override
    public boolean update(Service service) {
        String sql = """
                UPDATE service
                SET name = ?, port = ?, status = ?
                WHERE service_id = ?
                """;

        int rowsAffected = jdbcTemplate.update(
                sql,
                service.getName(),
                service.getPort(),
                service.getStatus().name(),
                service.getServiceId()
        );

        return rowsAffected > 0;
    }

    @Override
    public boolean deleteById(int serviceId) {
        String sql = """
                DELETE FROM service
                WHERE service_id = ?
                """;

        int rowsAffected = jdbcTemplate.update(sql, serviceId);

        return rowsAffected > 0;
    }

    private Service mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Service(
                rs.getInt("service_id"),
                rs.getInt("host_id"),
                rs.getString("name"),
                rs.getInt("port"),
                ServiceStatus.valueOf(rs.getString("status"))
        );
    }
}
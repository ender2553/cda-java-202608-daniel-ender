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

    // TODO [REPO7]: Implement mapRow() below to build a Service from
    // one ResultSet row (columns: service_id, host_id, name, port, status).
    private final RowMapper<Service> mapper = this::mapRow;

    public ServiceJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Service> findAll() {
        // TODO [REPO1]: select every service, ordered by service_id.
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<Service> findByHostId(int hostId) {
        // TODO [REPO2]: select services for one host. Use a PreparedStatement
        // placeholder for hostId -- never concatenate it into the SQL string.
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Service findById(int serviceId) {
        // TODO [REPO3]: use jdbcTemplate.queryForObject and handle the
        // case where no row matches (see TicketJdbcTemplateRepository.findById
        // for the exception to catch).
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Service add(Service service) {
        // TODO [REPO4]: insert a new row and use PostgreSQL's `returning`
        // clause to get the generated service_id back in one round trip.
        // Return a NEW Service built with that generated id -- do not try
        // to mutate the Service you were given; serviceId is final.
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean update(Service service) {
        // TODO [REPO5]: update name, port, and status for the given
        // serviceId. Return true only if a row was actually changed.
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean deleteById(int serviceId) {
        // TODO [REPO6]: delete the row. Return true only if a row was
        // actually removed.
        throw new UnsupportedOperationException("TODO");
    }

    private Service mapRow(ResultSet rs, int rowNum) throws SQLException {
        // TODO [REPO7]: build and return a Service from the current row.
        throw new UnsupportedOperationException("TODO");
    }
}

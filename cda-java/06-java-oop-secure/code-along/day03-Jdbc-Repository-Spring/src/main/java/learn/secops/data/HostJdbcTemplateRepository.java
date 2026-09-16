package learn.secops.data;

import learn.secops.models.Host;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public class HostJdbcTemplateRepository implements HostRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Host> mapper = new HostMapper();

    public HostJdbcTemplateRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Host> findAll(){
        final String sql = "SELECT host_id, hostname, ip_address "
                + "FROM host "
                + "ORDER BY host_id;";
        return jdbcTemplate.query(sql, mapper);
    }


    @Override
    public Host findById(int hostId) {
        final String sql = "SELECT host_id, hostname, ip_address "
                + "FROM host "
                +"WHERE host_id = ?;";
        try{
            return jdbcTemplate.queryForObject(sql, mapper);
        }catch (EmptyResultDataAccessException ex){
            return null;
        }

    }

}

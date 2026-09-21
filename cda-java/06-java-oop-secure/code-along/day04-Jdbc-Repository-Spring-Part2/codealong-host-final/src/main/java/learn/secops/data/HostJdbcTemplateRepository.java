package learn.secops.data;

import learn.secops.models.Host;
import learn.secops.models.HostCriticality;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public class HostJdbcTemplateRepository implements HostRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Host> mapper = new HostMapper();

    public HostJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Host> findAll(){
        final String sql = "SELECT host_id, hostname, ip_address, criticality "
                + "FROM host "
                + "ORDER BY host_id;";
        return jdbcTemplate.query(sql, mapper);

    }


    @Override
    public List<Host> findByCriticality(HostCriticality criticality){
        final String sql = "SELECT host_id, hostname, ip_address, criticality "
                + "FROM host "
                + "WHERE criticality = ? "
                + "ORDER BY host_id;";
        return jdbcTemplate.query(sql, mapper, criticality.name());
    }

    @Override
    public Host findById(int hostId){
        final String sql = "SELECT host_id, hostname, ip_address, criticality "
                + "FROM host "
                + "WHERE host_id = ?;";
        try{
            return jdbcTemplate.queryForObject(sql, mapper, hostId);
        }catch (EmptyResultDataAccessException ex) {
            return null; //log error
        }
    }


    @Override
    public Host add(Host host){
        final String sql = "INSERT INTO host (hostname, ip_address, criticality) "
                + "VALUES (?, ?, ?) "
                + "returning host_id;";

        int generatedId = jdbcTemplate.queryForObject(sql, Integer.class, host.getHostname(), host.getIpAddress(), host.getCriticality().name());

        return new Host(generatedId, host.getHostname(), host.getIpAddress(), host.getCriticality());

    }


    @Override
    public boolean updateCriticality(int hostId, HostCriticality newCriticality){
        final String sql = "UPDATE host SET criticality = ? WHERE host_id = ?;";
        return jdbcTemplate.update(sql, newCriticality.name(), hostId)  > 0;
    }




    @Override
    public boolean deleteById(int hostId) {
        final String sql = "DELETE FROM host WHERE host_id = ?;";
        return jdbcTemplate.update(sql, hostId) > 0;
    }
}

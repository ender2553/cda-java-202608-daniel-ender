package learn.secops.data;

import learn.secops.models.Host;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class HostMapper implements RowMapper<Host> {

    @Override
    public Host mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Host(
                rs.getInt("host_id"),
                rs.getString("hostname"),
                rs.getString("ip_address")
        );
    }
}

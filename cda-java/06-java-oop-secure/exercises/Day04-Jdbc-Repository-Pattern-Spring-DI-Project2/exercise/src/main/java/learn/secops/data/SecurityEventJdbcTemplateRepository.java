package learn.secops.data;

import learn.secops.models.EventType;
import learn.secops.models.SecurityEvent;
import learn.secops.models.Severity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class SecurityEventJdbcTemplateRepository
        implements SecurityEventRepository {

    private final JdbcTemplate jdbcTemplate;

    public SecurityEventJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SecurityEvent> rowMapper = (rs, rowNum) ->
            new SecurityEvent(
                    rs.getInt("event_id"),
                    rs.getInt("host_id"),
                    EventType.valueOf(rs.getString("event_type")),
                    Severity.valueOf(rs.getString("severity")),
                    rs.getString("description"),
                    rs.getTimestamp("detected_at").toLocalDateTime(),
                    rs.getBoolean("acknowledged"),
                    rs.getTimestamp("acknowledged_at") == null
                            ? null
                            : rs.getTimestamp("acknowledged_at").toLocalDateTime()
            );

    @Override
    public List<SecurityEvent> findAll() {
        String sql = """
                select event_id, host_id, event_type, severity,
                       description, detected_at, acknowledged, acknowledged_at
                from event
                order by event_id
                """;

        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<SecurityEvent> findByHostId(int hostId) {
        String sql = """
                select event_id, host_id, event_type, severity,
                       description, detected_at, acknowledged, acknowledged_at
                from event
                where host_id = ?
                order by event_id
                """;

        return jdbcTemplate.query(sql, rowMapper, hostId);
    }

    @Override
    public List<SecurityEvent> findUnacknowledged() {
        String sql = """
                select event_id, host_id, event_type, severity,
                       description, detected_at, acknowledged, acknowledged_at
                from event
                where acknowledged = false
                order by event_id
                """;

        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public SecurityEvent findById(int eventId) {
        String sql = """
                select event_id, host_id, event_type, severity,
                       description, detected_at, acknowledged, acknowledged_at
                from event
                where event_id = ?
                """;

        List<SecurityEvent> events =
                jdbcTemplate.query(sql, rowMapper, eventId);

        return events.isEmpty() ? null : events.get(0);
    }

    @Override
    public SecurityEvent add(SecurityEvent event) {
        String sql = """
                insert into event
                    (host_id, event_type, severity, description,
                     detected_at, acknowledged, acknowledged_at)
                values (?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    new String[]{"event_id"}
            );

            ps.setInt(1, event.getHostId());
            ps.setString(2, event.getEventType().name());
            ps.setString(3, event.getSeverity().name());
            ps.setString(4, event.getDescription());
            ps.setTimestamp(
                    5,
                    Timestamp.valueOf(event.getOccurredAt())
            );
            ps.setBoolean(6, event.isAcknowledged());

            if (event.getAcknowledgedAt() == null) {
                ps.setTimestamp(7, null);
            } else {
                ps.setTimestamp(
                        7,
                        Timestamp.valueOf(event.getAcknowledgedAt())
                );
            }

            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();

        if (generatedId == null) {
            throw new IllegalStateException(
                    "Database did not return a generated event ID."
            );
        }

        return new SecurityEvent(
                generatedId.intValue(),
                event.getHostId(),
                event.getEventType(),
                event.getSeverity(),
                event.getDescription(),
                event.getOccurredAt(),
                event.isAcknowledged(),
                event.getAcknowledgedAt()
        );
    }

    @Override
    public boolean acknowledge(int eventId) {
        String sql = """
                update event
                set acknowledged = true,
                    acknowledged_at = CURRENT_TIMESTAMP
                where event_id = ?
                  and acknowledged = false
                """;

        return jdbcTemplate.update(sql, eventId) == 1;
    }

    @Override
    public boolean deleteById(int eventId) {
        String sql = """
                delete from event
                where event_id = ?
                """;

        return jdbcTemplate.update(sql, eventId) == 1;
    }
}


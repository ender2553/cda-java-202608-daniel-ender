package com.cyberdev.dndrpg.repository.jdbc.mapper;

import com.cyberdev.dndrpg.logging.AuditEntry;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * PROVIDED -- maps one row of audit_log to an AuditEntry. actor_id is
 * nullable (a system-level event might have no specific player actor),
 * so it's read as a String first and only converted to a UUID if
 * present.
 */
public final class AuditEntryRowMapper implements RowMapper<AuditEntry> {
    @Override
    public AuditEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
        String actorIdStr = rs.getString("actor_id");
        return new AuditEntry(
                UUID.fromString(rs.getString("id")),
                rs.getString("event_type"),
                actorIdStr == null ? null : UUID.fromString(actorIdStr),
                rs.getString("details"),
                rs.getObject("occurred_at", OffsetDateTime.class).toInstant()
        );
    }
}

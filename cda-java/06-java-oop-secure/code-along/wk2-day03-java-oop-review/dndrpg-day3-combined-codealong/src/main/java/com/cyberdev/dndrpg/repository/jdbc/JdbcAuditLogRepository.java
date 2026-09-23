package com.cyberdev.dndrpg.repository.jdbc;

import com.cyberdev.dndrpg.exception.DataAccessException;
import com.cyberdev.dndrpg.logging.AuditEntry;
import com.cyberdev.dndrpg.repository.AuditLogRepository;
import com.cyberdev.dndrpg.repository.jdbc.mapper.AuditEntryRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * INSTRUCTOR REFERENCE IMPLEMENTATION (DATA-10 solved).
 *
 * Append-only by convention: this class issues INSERT and SELECT only --
 * never UPDATE or DELETE against audit_log, matching the comment on the
 * table itself in schema/schema.sql.
 */
@Repository
@Profile("jdbc")
public final class JdbcAuditLogRepository implements AuditLogRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AuditEntryRowMapper rowMapper = new AuditEntryRowMapper();

    public JdbcAuditLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(AuditEntry entry) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO audit_log (id, event_type, actor_id, details, occurred_at) VALUES (?, ?, ?, ?, ?)",
                    entry.id(), entry.eventType(), entry.actorId(), entry.details(),
                    Timestamp.from(entry.occurredAt()));
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to save audit entry " + entry.eventType(), e);
        }
    }

    @Override
    public List<AuditEntry> findRecent(int limit) {
        try {
            return jdbcTemplate.query(
                    "SELECT id, event_type, actor_id, details, occurred_at FROM audit_log "
                            + "ORDER BY occurred_at DESC LIMIT ?",
                    rowMapper, limit);
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to load audit log", e);
        }
    }
}

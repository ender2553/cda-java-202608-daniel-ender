package com.cyberdev.dndrpg.logging;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * One row of the audit trail. Deliberately holds only IDs and event
 * types -- never raw PII (no email, no password, not even the password
 * hash). If you can't answer "would this be fine for a teammate to grep
 * through the audit log?", it doesn't belong in an AuditEntry.
 *
 * A plain class with private final fields, a validating constructor, and
 * accessor methods, rather than a Java record -- same immutable shape,
 * written out by hand.
 */
public class AuditEntry {
    private final UUID id;
    private final String eventType;
    private final UUID actorId;
    private final String details;
    private final Instant occurredAt;

    public AuditEntry(UUID id, String eventType, UUID actorId, String details, Instant occurredAt) {
        if (id == null) throw new IllegalArgumentException("id must not be null");
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType must not be blank");
        }
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt must not be null");
        this.id = id;
        this.eventType = eventType;
        this.actorId = actorId;
        this.details = details;
        this.occurredAt = occurredAt;
    }

    public UUID id() {
        return id;
    }

    public String eventType() {
        return eventType;
    }

    public UUID actorId() {
        return actorId;
    }

    public String details() {
        return details;
    }

    public Instant occurredAt() {
        return occurredAt;
    }

    // IDENTITY EQUALITY, by id: each AuditEntry is one append-only log
    // row (see audit_log's PRIMARY KEY in schema.sql). Two rows that
    // happen to share an eventType/details string are still two distinct
    // audit events, not duplicates of each other -- exactly the reason
    // AuditLogRepository issues INSERT and never UPDATE/DELETE. Only the
    // generated UUID id distinguishes "this specific recorded event."
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditEntry other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

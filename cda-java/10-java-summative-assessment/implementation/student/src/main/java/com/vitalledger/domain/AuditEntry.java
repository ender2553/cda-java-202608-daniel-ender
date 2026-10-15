package com.vitalledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * An immutable audit trail entry. Audit rows deliberately contain <b>no PHI</b> —
 * only who did what to which record, and when — so the audit log itself is safe to
 * read under the AUDITOR role.
 */
@Entity
@Table(name = "audit_entries")
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String actor;

    @Column(nullable = false, length = 40)
    private String action;

    /** Reference to the affected entity — an id, never PHI. */
    @Column(name = "target_ref", nullable = false, length = 64)
    private String targetRef;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected AuditEntry() {
        // JPA
    }

    public AuditEntry(String actor, String action, String targetRef) {
        this.actor = actor;
        this.action = action;
        this.targetRef = targetRef;
        this.occurredAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getActor() {
        return actor;
    }

    public String getAction() {
        return action;
    }

    public String getTargetRef() {
        return targetRef;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}

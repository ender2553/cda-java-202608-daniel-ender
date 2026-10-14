package com.vitalledger.dto;

import com.vitalledger.domain.AuditEntry;

import java.time.Instant;

/** Response view of an audit entry (contains no PHI). */
public record AuditResponse(
        Long id,
        String actor,
        String action,
        String targetRef,
        Instant occurredAt
) {
    public static AuditResponse from(AuditEntry a) {
        return new AuditResponse(a.getId(), a.getActor(), a.getAction(),
                a.getTargetRef(), a.getOccurredAt());
    }
}

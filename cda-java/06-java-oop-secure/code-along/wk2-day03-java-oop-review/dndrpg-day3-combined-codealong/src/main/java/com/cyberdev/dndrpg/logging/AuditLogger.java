package com.cyberdev.dndrpg.logging;

import com.cyberdev.dndrpg.repository.AuditLogRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * PROVIDED UTILITY -- records security-relevant events. Every audit
 * event goes through here rather than being written ad hoc, so there is
 * exactly one place that decides the shape of an audit record and
 * exactly one place a reviewer needs to check to confirm PII never ends
 * up in it.
 */
@Component
public final class AuditLogger {

    private final AuditLogRepository repository;

    public AuditLogger(AuditLogRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null");
        }
        this.repository = repository;
    }

    public void log(String eventType, UUID actorId, String details) {
        AuditEntry entry = new AuditEntry(UUID.randomUUID(), eventType, actorId, details, Instant.now());
        try {
            repository.save(entry);
        } catch (RuntimeException e) {
            // An audit-logging failure must never crash (or silently
            // swallow) the feature that triggered it -- but it must not
            // vanish either, so it goes to the ordinary application log
            // instead.
            AppLogger.error("Failed to persist audit entry for event " + eventType, e);
        }
    }
}

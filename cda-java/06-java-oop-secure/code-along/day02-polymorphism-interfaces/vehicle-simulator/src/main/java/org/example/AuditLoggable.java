package org.example;

public interface AuditLoggable {

    /**
     * Record an access/operation attempt by the given operator and
     * return the log line that was recorded, so callers (and our demo
     * code) can display it without needing a separate log reader.
     */
    String logAccessAttempt(String operatorId);
}

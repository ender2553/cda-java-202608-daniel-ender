package com.cyberdev.smarthome;

/**
 * Monitorable.java
 * ==================
 * GIVEN \u2014 already complete, no TODOs here.
 *
 * Not every device needs to keep a security log \u2014 only restricted or
 * safety-relevant ones do. Pulling this into its OWN interface (instead
 * of bolting it onto every device) means only the devices that actually
 * need it have to implement it. This mirrors AuditLoggable from the
 * Vehicle Simulator.
 */
public interface Monitorable {

    /**
     * Record an event and return the log line that was recorded.
     * Implementations should REJECT a blank description rather than
     * silently logging an empty event (fail closed).
     */
    String logEvent(String description);
}

package learn.secops.models;

import java.time.LocalDateTime;

public final class SecurityEvent {

    private final int eventId;
    private final int hostId;
    private final EventType eventType;
    private final Severity severity;
    private final String description;
    private final LocalDateTime occurredAt;
    private final boolean acknowledged;
    private final LocalDateTime acknowledgedAt;

    public SecurityEvent(int hostId,
                         EventType eventType,
                         Severity severity,
                         String description) {

        validateHostId(hostId);
        validateEventType(eventType);
        validateSeverity(severity);
        validateDescription(description);

        this.eventId = 0;
        this.hostId = hostId;
        this.eventType = eventType;
        this.severity = severity;
        this.description = description;
        this.occurredAt = LocalDateTime.now();
        this.acknowledged = false;
        this.acknowledgedAt = null;
    }

    public SecurityEvent(int eventId,
                         int hostId,
                         EventType eventType,
                         Severity severity,
                         String description,
                         LocalDateTime occurredAt,
                         boolean acknowledged,
                         LocalDateTime acknowledgedAt) {

        validateEventId(eventId);
        validateHostId(hostId);
        validateEventType(eventType);
        validateSeverity(severity);
        validateDescription(description);

        if (occurredAt == null) {
            throw new IllegalArgumentException("Occurred time is required.");
        }

        if (acknowledged && acknowledgedAt == null) {
            throw new IllegalArgumentException(
                    "An acknowledged event must have an acknowledgment time.");
        }

        if (!acknowledged && acknowledgedAt != null) {
            throw new IllegalArgumentException(
                    "An unacknowledged event cannot have an acknowledgment time.");
        }

        this.eventId = eventId;
        this.hostId = hostId;
        this.eventType = eventType;
        this.severity = severity;
        this.description = description;
        this.occurredAt = occurredAt;
        this.acknowledged = acknowledged;
        this.acknowledgedAt = acknowledgedAt;
    }

    private static void validateEventId(int eventId) {
        if (eventId < 0) {
            throw new IllegalArgumentException("Event ID cannot be negative.");
        }
    }

    private static void validateHostId(int hostId) {
        if (hostId <= 0) {
            throw new IllegalArgumentException("Host ID must be positive.");
        }
    }

    private static void validateEventType(EventType eventType) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type is required.");
        }
    }

    private static void validateSeverity(Severity severity) {
        if (severity == null) {
            throw new IllegalArgumentException("Severity is required.");
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required.");
        }

        if (description.length() > 500) {
            throw new IllegalArgumentException(
                    "Description cannot exceed 500 characters.");
        }
    }

    public SecurityEvent acknowledge() {
        if (acknowledged) {
            throw new IllegalStateException("Event has already been acknowledged.");
        }

        return new SecurityEvent(
                eventId,
                hostId,
                eventType,
                severity,
                description,
                occurredAt,
                true,
                LocalDateTime.now()
        );
    }

    public int getEventId() {
        return eventId;
    }

    public int getHostId() {
        return hostId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }
}


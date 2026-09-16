package learn.secops.models;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A security operations ticket opened against a host.
 *
 * This class keeps the encapsulation habits from Day 1: fields are
 * private, the constructor enforces invariants, and ticketId is final.
 * The database generates the id, so the repository never mutates a
 * Ticket to attach one -- instead, after an insert, it builds a brand
 * new Ticket with the generated id already in place (see
 * TicketJdbcRepository.add). Immutable id, no reflection required.
 */
public class Ticket {

    private final int ticketId;
    private final int hostId;
    private String description;
    private TicketStatus status;
    private final LocalDateTime openedAt;

    /** Creates a brand-new ticket that has not been saved yet. */
    public Ticket(int hostId, String description) {
        this(0, hostId, description, TicketStatus.OPEN, LocalDateTime.now());
    }

    /** Reconstructs a ticket that already exists in the database. */
    public Ticket(int ticketId, int hostId, String description,
                   TicketStatus status, LocalDateTime openedAt) {
        if (hostId <= 0) {
            throw new IllegalArgumentException("hostId must be a positive, existing host id");
        }
        this.ticketId = ticketId;
        this.hostId = hostId;
        setDescription(description);
        this.status = status == null ? TicketStatus.OPEN : status;
        this.openedAt = openedAt == null ? LocalDateTime.now() : openedAt;
    }

    public int getTicketId() {
        return ticketId;
    }

    public int getHostId() {
        return hostId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description is required");
        }
        if (description.length() > 500) {
            throw new IllegalArgumentException("description must be 500 characters or fewer");
        }
        this.description = description.trim();
    }

    public TicketStatus getStatus() {
        return status;
    }

    /**
     * There is deliberately no generic setStatus(TicketStatus). Status
     * changes are named actions, the same way Account exposed freeze()
     * instead of a raw setter -- it keeps every transition explicit and
     * reviewable, and it stops a caller from ever writing an invalid
     * state like re-opening a CLOSED ticket by accident.
     */
    public void start() {
        if (status != TicketStatus.OPEN) {
            throw new IllegalStateException("Only an OPEN ticket can move to IN_PROGRESS");
        }
        status = TicketStatus.IN_PROGRESS;
    }

    public void resolve() {
        if (status != TicketStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only an IN_PROGRESS ticket can be resolved");
        }
        status = TicketStatus.RESOLVED;
    }

    public void close() {
        if (status != TicketStatus.RESOLVED) {
            throw new IllegalStateException("Only a RESOLVED ticket can be closed");
        }
        status = TicketStatus.CLOSED;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "ticketId=" + ticketId +
                ", hostId=" + hostId +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", openedAt=" + openedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return ticketId == ticket.ticketId &&
                hostId == ticket.hostId &&
                Objects.equals(description, ticket.description) &&
                status == ticket.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId, hostId, description, status);
    }
}

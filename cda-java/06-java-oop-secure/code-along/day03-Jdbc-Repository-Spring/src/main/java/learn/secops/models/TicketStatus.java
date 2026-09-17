package learn.secops.models;

/**
 * The lifecycle states of a security operations ticket.
 * A closed set of values, just like the sealed PaymentMethod hierarchy
 * the compiler (and the database CHECK constraint) both
 * agree on the complete list of valid states.
 */
public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED
}

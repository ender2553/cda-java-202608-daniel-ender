package learn.secops.data;

import learn.secops.models.Ticket;

import java.util.List;

/**
 * The Repository pattern: a domain-facing contract for persisting and
 * retrieving Tickets, expressed entirely in terms Ticket already
 * understands -- no Connection, Statement, or SQL leaks out of this
 * interface.
 *
 * The rest of the application (a service class, a controller, a test)
 * depends on THIS interface, never on TicketJdbcRepository or
 * TicketJdbcTemplateRepository directly. That single decision is what
 * makes the persistence technology swappable later without touching a
 * single caller.
 */
public interface TicketRepository {

    List<Ticket> findAll();

    List<Ticket> findByHostId(int hostId);

    Ticket findById(int ticketId);

    Ticket add(Ticket ticket);

    boolean update(Ticket ticket);

    boolean deleteById(int ticketId);
}

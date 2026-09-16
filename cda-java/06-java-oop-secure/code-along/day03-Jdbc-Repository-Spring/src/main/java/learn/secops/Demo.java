package learn.secops;

import learn.secops.config.AppConfig;
import learn.secops.data.TicketJdbcRepository;
import learn.secops.data.TicketRepository;
import learn.secops.models.Ticket;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

/**
 * Two ways to get a working TicketRepository, side by side.
 *
 * Run this against a local PostgreSQL "secops" database created from
 * schema.sql + data.sql before class.
 */
public class Demo {

    public static void main(String[] args) {
        //rawJdbcVersion();
        springManagedVersion();
    }

    /** Part 1 of the lesson: no Spring yet, just JDBC and a DataSource we build by hand. */
    private static void rawJdbcVersion() {
        System.out.println("--- Raw JDBC repository ---");

        TicketRepository repository = TicketJdbcRepository.connectingTo(
                "localhost", 5432, "secops",
                "secops_app", System.getenv("SECOPS_DB_PASSWORD"));

        List<Ticket> openTickets = repository.findByHostId(1);
        //openTickets.forEach(System.out::println);  //alternative

        for (Ticket ticket : openTickets){
            System.out.println(ticket.getHostId());
        }

    }



    /** Part 2 of the lesson: Spring builds and wires the JdbcTemplate repository for us. */
    private static void springManagedVersion() {
        System.out.println("--- Spring-managed JdbcTemplate repository ---");

        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            // Notice: we ask Spring for the TicketRepository INTERFACE,
            // not the concrete TicketJdbcTemplateRepository class.
            TicketRepository repository = context.getBean(TicketRepository.class);

            List<Ticket> allTickets = repository.findAll();
            //allTickets.forEach(System.out::println); //alternative

            for (Ticket ticket : allTickets){
                System.out.println(ticket);
            }
        }
    }
}

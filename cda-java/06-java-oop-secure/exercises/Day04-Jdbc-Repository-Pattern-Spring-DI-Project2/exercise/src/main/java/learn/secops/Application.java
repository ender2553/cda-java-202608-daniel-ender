package learn.secops;

 import learn.secops.config.AppConfig;
 import learn.secops.data.SecurityEventRepository;
 import learn.secops.models.EventType;
 import learn.secops.models.SecurityEvent;
 import learn.secops.models.Severity;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * This is your integration check, separate from the JUnit suite.
 * Once EventType, Severity, SecurityEvent, SecurityEventRepository,
 * and SecurityEventJdbcTemplateRepository all exist and compile,
 * uncomment the body of main() below (delete the leading "// " from
 * each line) and this becomes a real, running program against your
 * database.
 *
 * Passing the given tests and having this program actually run are
 * two different kinds of evidence that your code works -- do both
 * before you submit.
 */
public class Application {

    public static void main(String[] args) {
        System.out.println("Application is wired up but the demo below is still commented out.");
        System.out.println("Finish your model, interface, and repository classes, then uncomment main().");

        // --- Uncomment everything below once your classes compile ---

         try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(AppConfig.class)) {

             SecurityEventRepository repository = context.getBean(SecurityEventRepository.class);

             System.out.println("=== Unacknowledged events ===");
             repository.findUnacknowledged().forEach(System.out::println);

             System.out.println();
             System.out.println("=== Recording a new event ===");
             SecurityEvent newEvent = new SecurityEvent(2, EventType.UNAUTHORIZED_ACCESS,
                     Severity.HIGH, "Privileged account used outside business hours");
             SecurityEvent saved = repository.add(newEvent);
             System.out.println("Saved: " + saved);

             System.out.println();
             System.out.println("=== Acknowledging it ===");
                  boolean acknowledged = repository.acknowledge(saved.getEventId());
             System.out.println("Acknowledge succeeded: " + acknowledged);
             System.out.println("Reloaded: " + repository.findById(saved.getEventId()));

             System.out.println();
             System.out.println("=== Trying to acknowledge it again ===");
             boolean secondAttempt = repository.acknowledge(saved.getEventId());
             System.out.println("Second acknowledge succeeded (should be false): " + secondAttempt);

         System.out.println();
             System.out.println("=== Removing the event ===");
             boolean deleted = repository.deleteById(saved.getEventId());
             System.out.println("Delete succeeded: " + deleted);
         }
    }
}

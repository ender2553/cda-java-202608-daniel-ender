package demo.ledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starts Spring, which builds every {@code @Component}, {@code @Repository}
 * and {@code @Service} in this package, wires them together, and then runs
 * {@link demo.ledger.console.ConsoleMenu}.
 */
@SpringBootApplication
public class LedgerDemoApp {

    public static void main(String[] args) {
        try {
            SpringApplication.run(LedgerDemoApp.class, args);
        } catch (RuntimeException ex) {
            // Console logging is off (see application.properties), so a setup problem such as a
            // missing .env would otherwise exit silently. This is the developer's own console
            // at startup, not a user-facing response, so the root cause is printed.
            Throwable root = ex;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            System.err.println("The demo could not start: " + root.getMessage());
            System.err.println("Full details: logs/ledger-demo.log");
            System.exit(1);
        }
    }
}

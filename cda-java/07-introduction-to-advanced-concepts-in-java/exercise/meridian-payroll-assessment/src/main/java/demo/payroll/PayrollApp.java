package demo.payroll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Starts Spring, which builds and wires every component, then runs the console menu. */
@SpringBootApplication
public class PayrollApp {

    public static void main(String[] args) {
        try {
            SpringApplication.run(PayrollApp.class, args);
        } catch (RuntimeException ex) {
            // Console logging is off, so a setup problem would otherwise exit silently.
            Throwable root = ex;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            System.err.println("The app stopped: " + root);
            System.err.println("Full details: logs/payroll.log");
            System.exit(1);
        }
    }
}

package learn.secops;

// import learn.secops.config.AppConfig;
// import learn.secops.data.HostRepository;
// import learn.secops.models.Host;
// import learn.secops.models.HostCriticality;
// import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * This file compiles and runs on its own -- it just
 * doesn't do anything yet, because the classes it depends on don't
 * exist yet.
 */
public class Application {

    public static void main(String[] args) {
        System.out.println("Application is wired up but the demo below is still commented out.");
        System.out.println("Finish Host, HostRepository, and HostJdbcTemplateRepository, then uncomment main().");

        // --- Uncomment everything below once your classes compile ---

        // AnnotationConfigApplicationContext context =
        //         new AnnotationConfigApplicationContext(AppConfig.class);
        //
        // HostRepository repository = context.getBean(HostRepository.class);
        //
        // System.out.println("=== All hosts ===");
        // repository.findAll().forEach(System.out::println);
        //
        // System.out.println();
        // System.out.println("=== Registering a new host ===");
        // Host newHost = new Host("printer-09.internal", "10.0.5.9", null);
        // Host saved = repository.add(newHost);
        // System.out.println("Saved: " + saved);
        //
        // System.out.println();
        // System.out.println("=== Hosts at HIGH criticality ===");
        // repository.findByCriticality(HostCriticality.HIGH).forEach(System.out::println);
        //
        // System.out.println();
        // System.out.println("=== Promoting the new host to CRITICAL ===");
        // boolean updated = repository.updateCriticality(saved.getHostId(), HostCriticality.CRITICAL);
        // System.out.println("Update succeeded: " + updated);
        // System.out.println("Reloaded: " + repository.findById(saved.getHostId()));
        //
        // System.out.println();
        // System.out.println("=== Removing the new host ===");
        // boolean deleted = repository.deleteById(saved.getHostId());
        // System.out.println("Delete succeeded: " + deleted);
        //
        // context.close();
    }
}

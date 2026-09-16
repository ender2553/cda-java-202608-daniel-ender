package learn.secops;

import learn.secops.config.AppConfig;
import learn.secops.data.ServiceRepository;
import learn.secops.models.Service;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public class ServiceApp {

    private final ServiceRepository repository;
    private final Scanner scanner;
    private final PrintStream out;

    public ServiceApp(ServiceRepository repository, Scanner scanner, PrintStream out) {
        this.repository = repository;
        this.scanner = scanner;
        this.out = out;
    }

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class);
             Scanner scanner = new Scanner(System.in)) {
            ServiceRepository repository = context.getBean(ServiceRepository.class);
            new ServiceApp(repository, scanner, System.out).run();
        }
    }

    public void run() {
        out.println("SecOps Service Console");

        boolean running = true;
        while (running) {
            printMenu();
            switch (readInt("Choose an option: ")) {
                case 1 -> listServices();
                case 2 -> showServiceDetails();
                case 0 -> running = false;
                default -> out.println("Please choose 0, 1, or 2.");
            }
        }

        out.println("Goodbye.");
    }

    private void printMenu() {
        out.println();
        out.println("1. List services");
        out.println("2. View service details");
        out.println("0. Exit");
    }

    private void listServices() {
        List<Service> services = repository.findAll();
        if (services.isEmpty()) {
            out.println("No services found.");
            return;
        }

        out.printf("%-4s %-20s %-8s %-10s%n", "ID", "NAME", "PORT", "STATUS");
        for (Service service : services) {
            out.printf(
                    "%-4d %-20s %-8d %-10s%n",
                    service.getServiceId(),
                    service.getName(),
                    service.getPort(),
                    service.getStatus());
        }
    }

    private void showServiceDetails() {
        listServices();
        int serviceId = readInt("Enter a service ID: ");
        Service service = repository.findById(serviceId);

        if (service == null) {
            out.printf("No service found with ID %d.%n", serviceId);
            return;
        }

        out.println();
        out.println("Service Details");
        out.printf("Service ID: %d%n", service.getServiceId());
        out.printf("Host ID:    %d%n", service.getHostId());
        out.printf("Name:       %s%n", service.getName());
        out.printf("Port:       %d%n", service.getPort());
        out.printf("Status:     %s%n", service.getStatus());
    }

    private int readInt(String prompt) {
        while (true) {
            out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                out.println("Please enter a whole number.");
            }
        }
    }
}

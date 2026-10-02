package demo.clinic.console;

import demo.clinic.domain.Appointment;
import demo.clinic.domain.Patient;
import demo.clinic.log.CheckInCounter;
import demo.clinic.log.VisitLogFiles;
import demo.clinic.log.VisitReport;
import demo.clinic.repository.AppointmentRepository;
import demo.clinic.repository.DataAccessFailure;
import demo.clinic.repository.PatientRepository;
import demo.clinic.service.CheckInService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/** The front-desk menu. It reads input, calls the data layer, and prints the result. */
@Component
public class ConsoleMenu implements CommandLineRunner {

    private final PatientRepository patients;
    private final AppointmentRepository appointments;
    private final CheckInService checkIns;
    private final VisitReport report;
    private final CheckInCounter counter;
    private final Scanner in = new Scanner(System.in);

    public ConsoleMenu(PatientRepository patients, AppointmentRepository appointments, CheckInService checkIns,
                       VisitReport report, CheckInCounter counter) {
        this.patients = patients;
        this.appointments = appointments;
        this.checkIns = checkIns;
        this.report = report;
        this.counter = counter;
    }

    @Override
    public void run(String... args) {
        long maxHeapMb = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        while (true) {
            System.out.printf("""

                    === Riverside Clinic Scheduler (max heap about %d MB) ===
                    1) Search patients by name
                    2) Look up a patient
                    3) Upcoming appointments
                    4) Check in a patient
                    5) Generate the visit log
                    6) Visit report (visits per clinician)
                    7) Export visits between two dates
                    8) Simulate a busy morning (6 front desks)
                    9) Show today's check-in count
                    0) Exit
                    """, maxHeapMb);
            String choice = prompt("Choose");
            if (choice == null || choice.equals("0")) {
                return;
            }
            try {
                switch (choice) {
                    case "1" -> printPatients(patients.searchByName(prompt("Name contains")));
                    case "2" -> patients.findById(prompt("Patient ID")).ifPresentOrElse(
                            p -> System.out.printf("%s  %s  (patient since %s)%n", p.patientId(), p.displayName(), p.patientSince()),
                            () -> System.out.println("No patient with that ID."));
                    case "3" -> appointments.findUpcoming(parseCount(prompt("How many"))).forEach(this::printAppointment);
                    case "4" -> {
                        Appointment a = checkIns.checkIn(prompt("Patient ID"));
                        System.out.print("Checked in: ");
                        printAppointment(a);
                    }
                    case "5" -> generateLog();
                    case "6" -> report.visitsPerClinician().forEach((clinician, visits) ->
                            System.out.printf("%s  %,9d visits%n", clinician, visits));
                    case "7" -> exportVisits();
                    case "8" -> BusyMorning.run(counter);
                    case "9" -> System.out.printf("Check-ins today: %,d%n", counter.count());
                    default -> System.out.println("Please choose 0-9.");
                }
            } catch (IllegalArgumentException | DataAccessFailure ex) {
                System.out.println("Error: " + ex.getMessage());
            } catch (TransactionException ex) {
                System.out.println("Error: " + DataAccessFailure.logged("start transaction",
                        "That action is unavailable right now. Try again later.", ex).getMessage());
            }
        }
    }

    private void generateLog() {
        String text = prompt("How many visits (Enter for " + VisitLogFiles.DEFAULT_VISITS + ")");
        long visits = (text == null || text.isEmpty()) ? VisitLogFiles.DEFAULT_VISITS : parseCount(text);
        if (visits < 1) {
            throw new IllegalArgumentException("Generate at least 1 visit");
        }
        System.out.printf("Writing %,d visits to %s ...%n", visits, VisitLogFiles.LOG);
        try {
            VisitLogFiles.generate(visits);
        } catch (IOException ex) {
            throw DataAccessFailure.logged("generate visit log", "Could not write the visit log.", ex);
        }
        System.out.println("Done.");
    }

    private void exportVisits() {
        VisitReport.ExportResult result = report.exportBetween(
                parseDate(prompt("From date (yyyy-mm-dd)")), parseDate(prompt("To date (yyyy-mm-dd)")));
        System.out.printf("Exported %,d visits to %s%n", result.visits(), result.file());
    }

    private void printPatients(List<Patient> list) {
        if (list.isEmpty()) {
            System.out.println("No patients found.");
        }
        list.forEach(p -> System.out.printf("%s  %s%n", p.patientId(), p.displayName()));
    }

    private void printAppointment(Appointment a) {
        System.out.printf("#%d  %s  %s with %s  (%s, %s)%n", a.id(), a.startsAt().toLocalDateTime(),
                a.patientId(), a.clinicianId(), a.reasonCode(), a.status());
    }

    private String prompt(String label) {
        System.out.print(label + ": ");
        return in.hasNextLine() ? in.nextLine().trim() : null;
    }

    private static int parseCount(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Enter a whole number, such as 20");
        }
    }

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new IllegalArgumentException("Enter a date like 2025-03-15");
        }
    }
}

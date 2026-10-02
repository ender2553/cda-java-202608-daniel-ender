package demo.payroll.console;

import demo.payroll.domain.Employee;
import demo.payroll.domain.ExportResult;
import demo.payroll.domain.PayRunResult;
import demo.payroll.domain.TimesheetSummary;
import demo.payroll.files.ImportStats;
import demo.payroll.files.PayHistoryExporter;
import demo.payroll.files.TimesheetGenerator;
import demo.payroll.files.TimesheetImporter;
import demo.payroll.repository.EmployeeRepository;
import demo.payroll.service.DirectDepositService;
import demo.payroll.service.FailureInjector;
import demo.payroll.service.MigrationService;
import demo.payroll.service.PayrollService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/** The payroll office console: a numeric menu over the services. */
@Component
public class ConsoleMenu implements CommandLineRunner {

    private static final LocalDate NEXT_PAYDAY = LocalDate.of(2026, 10, 9);

    private final DataSource dataSource;
    private final EmployeeRepository employees;
    private final DirectDepositService directDeposit;
    private final PayrollService payroll;
    private final FailureInjector failureInjector;
    private final MigrationService migration;
    private final PayHistoryExporter exporter;
    private final TimesheetGenerator generator;
    private final TimesheetImporter importer;
    private final ImportStats stats;
    private final PaydayRush paydayRush;
    private final Scanner in = new Scanner(System.in);

    public ConsoleMenu(DataSource dataSource, EmployeeRepository employees, DirectDepositService directDeposit,
                       PayrollService payroll, FailureInjector failureInjector, MigrationService migration,
                       PayHistoryExporter exporter, TimesheetGenerator generator, TimesheetImporter importer,
                       ImportStats stats, PaydayRush paydayRush) {
        this.dataSource = dataSource;
        this.employees = employees;
        this.directDeposit = directDeposit;
        this.payroll = payroll;
        this.failureInjector = failureInjector;
        this.migration = migration;
        this.exporter = exporter;
        this.generator = generator;
        this.importer = importer;
        this.stats = stats;
        this.paydayRush = paydayRush;
    }

    @Override
    public void run(String... args) {
        long maxHeapMb = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        while (true) {
            System.out.printf("""

                    === Meridian Payroll - payroll office console (max heap about %d MB) ===
                     1) Search employees
                     2) View employee profile
                     3) Change direct-deposit account
                     4) Run payroll for a department
                     5) Check funding totals
                     6) Toggle failure injection (now %s)
                     7) Export pay history
                     8) Generate timesheet file
                     9) Import timesheet file
                    10) Payday rush (all departments at once)
                    11) Migrate existing employee data
                     0) Exit
                    Timesheet lines processed so far: %d in %d import(s)
                    """, maxHeapMb, failureInjector.isEnabled() ? "ON" : "off", stats.linesProcessed(), stats.importsRun());
            String choice = prompt("Choose");
            if (choice == null || choice.equals("0")) {
                return;
            }
            try {
                handle(choice);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace(System.out);
            }
        }
    }

    private void handle(String choice) throws Exception {
        switch (choice) {
            case "1" -> {
                String prefix = ask("Last name starts with");
                String sort = ask("Sort by column (e.g. last_name)");
                int limit = askInt("How many results", 10);
                List<Employee> found = employees.search(prefix, sort.isEmpty() ? "last_name" : sort);
                found.stream().limit(limit).forEach(System.out::println);
                System.out.println(found.size() + " found.");
            }
            case "2" -> {
                String id = ask("Employee ID");
                List<Employee> found = employees.findById(id);
                if (found.isEmpty()) {
                    System.out.println("No employee " + id);
                } else {
                    System.out.println(found.get(0));
                }
            }
            case "3" -> {
                directDeposit.changeAccount(ask("Employee ID"), ask("Portal PIN"), ask("New bank account number"));
                System.out.println("Direct-deposit account updated.");
            }
            case "4" -> {
                PayRunResult result = payroll.runPayroll(ask("Department code (such as ENG)"), askDate("Pay date", NEXT_PAYDAY));
                System.out.printf("Payroll complete: %s, %d stubs, total %s, pay date %s%n",
                        result.deptCode(), result.stubs(), result.total(), result.payDate());
            }
            case "5" -> checkFunding();
            case "6" -> System.out.println("Failure injection is now " + (failureInjector.toggle() ? "ON" : "off") + ".");
            case "7" -> {
                ExportResult result = exporter.export(askDate("From date", null), askDate("To date", null));
                System.out.println("Exported " + result.rows() + " rows to " + result.path());
            }
            case "8" -> {
                int lines = askInt("Size 1) sample, 100,000 lines  2) full, 3,000,000 lines", 1) == 2
                        ? TimesheetGenerator.FULL_LINES : TimesheetGenerator.SAMPLE_LINES;
                System.out.println("Writing the timesheet file...");
                printSummary("Generated", generator.generate(lines));
            }
            case "9" -> printSummary("Imported", importer.summarize());
            case "10" -> paydayRush.run(askDate("Pay date", NEXT_PAYDAY));
            case "11" -> {
                try {
                    migration.migrate();
                } catch (UnsupportedOperationException e) {
                    System.out.println(e.getMessage());
                }
            }
            default -> System.out.println("Choose a number from the menu.");
        }
    }

    private void checkFunding() throws Exception {
        Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("""
                SELECT opening_balance, balance, (SELECT COALESCE(SUM(net), 0) FROM pay_stubs) AS stub_total
                FROM funding_accounts WHERE account_id = 'FND-0001'""");
        rs.next();
        BigDecimal opening = rs.getBigDecimal("opening_balance");
        BigDecimal current = rs.getBigDecimal("balance");
        BigDecimal stubTotal = rs.getBigDecimal("stub_total");
        BigDecimal diff = opening.subtract(current).subtract(stubTotal);
        System.out.printf("Opening %s, current %s, sum of stub nets %s -> %s%n", opening, current, stubTotal,
                diff.signum() == 0 ? "BALANCED" : "OUT BY " + diff);
    }

    private static void printSummary(String verb, TimesheetSummary s) {
        System.out.printf("%s: %d valid entries, %s total hours, %d overtime entries, %d rejected lines%n",
                verb, s.entries(), s.totalHours(), s.overtimeEntries(), s.rejected());
    }

    private String prompt(String label) {
        System.out.print(label + ": ");
        return in.hasNextLine() ? in.nextLine().trim() : null;
    }

    private String ask(String label) {
        String answer = prompt(label);
        return answer == null ? "" : answer;
    }

    private int askInt(String label, int fallback) {
        String answer = ask(label + " [" + fallback + "]");
        return answer.isEmpty() ? fallback : Integer.parseInt(answer);
    }

    private LocalDate askDate(String label, LocalDate fallback) {
        String answer = ask(label + " (yyyy-MM-dd)" + (fallback == null ? "" : " [" + fallback + "]"));
        if (answer.isEmpty() && fallback != null) {
            return fallback;
        }
        return LocalDate.parse(answer);
    }
}

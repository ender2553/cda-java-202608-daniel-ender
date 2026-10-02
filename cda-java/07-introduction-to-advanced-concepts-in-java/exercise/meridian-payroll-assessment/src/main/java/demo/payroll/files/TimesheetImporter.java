package demo.payroll.files;

import demo.payroll.domain.TimesheetSummary;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class TimesheetImporter {

    private static final long OVERTIME_AFTER = 800;
    private static final long MAX_HUNDREDTHS = 2400;

    private final DataFiles files;
    private final ImportStats stats;

    private long entries;
    private long overtime;
    private long rejected;
    private long totalHours;

    public TimesheetImporter(DataFiles files, ImportStats stats) {
        this.files = files;
        this.stats = stats;
    }

    public TimesheetSummary summarize() {

        Path path = files.requireTimesheets();

        entries = 0;
        overtime = 0;
        rejected = 0;
        totalHours = 0;

        try (BufferedReader reader = Files.newBufferedReader(path)){

            String lines;
            while ((lines = reader.readLine()) != null) {
                if (lines.equals(TimesheetGenerator.HEADER)) {
                    continue;
                }
                long hundredths = parse(lines);
                if (hundredths < 0) {
                    rejected++;
                    continue;
                }
                entries++;
                if (hundredths > OVERTIME_AFTER) {
                    overtime++;
                }
                totalHours += hundredths;
            }

            stats.recordLines(entries + rejected);
            return new TimesheetSummary(entries, totalHours, overtime, rejected);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    /** One line: employee_id,work_date,hours. Returns the hours in hundredths, or -1 if the line is bad. */
    private static long parse(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length != 3 || !parts[0].matches("EMP-\\d{5}")) {
            return -1;
        }
        try {
            LocalDate.parse(parts[1]);
            String text = parts[2];
            int dot = text.length() - 3;
            if (dot < 1 || text.charAt(dot) != '.' || !Character.isDigit(text.charAt(0))
                    || !Character.isDigit(text.charAt(dot + 1))) {
                return -1;
            }
            long hundredths = Long.parseLong(text.substring(0, dot)) * 100 + Long.parseLong(text.substring(dot + 1));
            return hundredths < 1 || hundredths > MAX_HUNDREDTHS ? -1 : hundredths;
        } catch (DateTimeParseException | NumberFormatException e) {
            return -1;
        }
    }
}

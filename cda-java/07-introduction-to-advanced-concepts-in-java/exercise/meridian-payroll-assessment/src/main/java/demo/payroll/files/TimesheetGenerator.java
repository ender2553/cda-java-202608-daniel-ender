package demo.payroll.files;

import demo.payroll.domain.TimesheetSummary;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Random;

/** Writes a sample timesheet file and returns its true totals. */
@Component
public class TimesheetGenerator {

    public static final String HEADER = "employee_id,work_date,hours";
    public static final int SAMPLE_LINES = 100_000;
    public static final int FULL_LINES = 3_000_000;

    private final DataFiles files;

    public TimesheetGenerator(DataFiles files) {
        this.files = files;
    }

    /** Writes {@code lines} data lines (SAMPLE_LINES or FULL_LINES) and returns what a correct import must find. */
    public TimesheetSummary generate(int lines) {
        files.ensureDirectory();
        Random random = new Random(42);
        String[] dates = new String[365];
        for (int i = 0; i < dates.length; i++) {
            dates[i] = LocalDate.of(2026, 1, 1).plusDays(i).toString();
        }

        long entries = 0;
        long hundredths = 0;
        long overtime = 0;
        long rejected = 0;
        try (BufferedWriter out = Files.newBufferedWriter(files.timesheets(), StandardCharsets.UTF_8)) {
            out.write("employee_id,work_date,hours\n");
            for (int i = 0; i < lines; i++) {
                String id = String.format("EMP-%05d", 1 + random.nextInt(600));
                String date = dates[random.nextInt(dates.length)];
                if (random.nextInt(500) == 0) {
                    out.write(random.nextBoolean() ? id + "," + date + ",abc\n" : id + "," + date + "\n");
                    rejected++;
                    continue;
                }
                int h = 25 + random.nextInt(1176);
                out.write(id + "," + date + "," + (h / 100) + "." + String.format("%02d", h % 100) + "\n");
                entries++;
                hundredths += h;
                if (h > 800) {
                    overtime++;
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return new TimesheetSummary(entries, hundredths, overtime, rejected);
    }
}

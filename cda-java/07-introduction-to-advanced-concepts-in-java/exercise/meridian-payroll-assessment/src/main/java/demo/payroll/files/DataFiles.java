package demo.payroll.files;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

/** Where the app's files live: everything under data/, in the working directory. */
@Component
public class DataFiles {

    private static final Path DIR = Path.of("data");

    public Path timesheets() {
        return DIR.resolve("timesheets.csv");
    }

    public Path payHistoryFile(LocalDate from, LocalDate to) {
        return DIR.resolve("pay-history-" + from + "-to-" + to + ".csv");
    }

    public void ensureDirectory() {
        try {
            Files.createDirectories(DIR);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public Path requireTimesheets() {
        Path file = timesheets();
        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException("There is no timesheet file yet. Run menu 8 first.");
        }
        return file;
    }
}

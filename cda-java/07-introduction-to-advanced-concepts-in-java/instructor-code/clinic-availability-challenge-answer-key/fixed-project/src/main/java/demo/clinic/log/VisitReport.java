package demo.clinic.log;

import demo.clinic.domain.Visit;
import demo.clinic.repository.DataAccessFailure;
import demo.clinic.repository.Validate;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Reports and exports from the visit log, a file that grows every day. */
@Component
public class VisitReport {

    public Map<String, Long> visitsPerClinician() {
        Path log = VisitLogFiles.requireLog();
        try (Stream<String> lines = Files.lines(log, StandardCharsets.UTF_8)) {
            return lines.skip(1)
                    .map(Visit::parse)
                    .collect(Collectors.groupingBy(Visit::clinicianId, TreeMap::new, Collectors.counting()));
        } catch (IOException | UncheckedIOException ex) {
            throw DataAccessFailure.logged("visit report", "Could not read the visit log.", ex);
        }
    }

    public ExportResult exportBetween(LocalDate from, LocalDate to) {
        Validate.dateRange(from, to);
        Path log = VisitLogFiles.requireLog();
        Path out = VisitLogFiles.exportFileFor(from, to);
        try (Stream<String> lines = Files.lines(log, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            writer.write(VisitLogFiles.HEADER);
            writer.newLine();
            Iterator<Visit> visits = lines.skip(1)
                    .map(Visit::parse)
                    .filter(v -> inRange(v, from, to))
                    .iterator();
            long written = 0;
            while (visits.hasNext()) {
                writer.write(visits.next().toCsv());
                writer.newLine();
                written++;
            }
            return new ExportResult(out, written);
        } catch (IOException | UncheckedIOException ex) {
            throw DataAccessFailure.logged("export visits", "Could not export the visit log.", ex);
        }
    }

    private static boolean inRange(Visit visit, LocalDate from, LocalDate to) {
        LocalDate day = visit.checkedInAt().toLocalDate();
        return !day.isBefore(from) && !day.isAfter(to);
    }

    public record ExportResult(Path file, long visits) {
    }
}

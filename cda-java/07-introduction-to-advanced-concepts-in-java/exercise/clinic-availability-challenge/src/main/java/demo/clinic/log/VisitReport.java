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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Reports and exports from the visit log, a file that grows every day. */
@Component
public class VisitReport {

    public Map<String, Long> visitsPerClinician() {
        Path log = VisitLogFiles.requireLog();
        try {
            List<String> lines = Files.readAllLines(log, StandardCharsets.UTF_8);
            Map<String, Long> counts = new TreeMap<>();
            for (String line : lines.subList(1, lines.size())) {
                counts.merge(Visit.parse(line).clinicianId(), 1L, Long::sum);
            }
            return counts;
        } catch (IOException | UncheckedIOException ex) {
            throw DataAccessFailure.logged("visit report", "Could not read the visit log.", ex);
        }
    }

    public ExportResult exportBetween(LocalDate from, LocalDate to) {
        Validate.dateRange(from, to);
        Path log = VisitLogFiles.requireLog();
        Path out = VisitLogFiles.exportFileFor(from, to);
        try {
            List<Visit> visits = Files.lines(log, StandardCharsets.UTF_8)
                    .skip(1)
                    .map(Visit::parse)
                    .filter(v -> inRange(v, from, to))
                    .collect(Collectors.toList());
            BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8);
            writer.write(VisitLogFiles.HEADER);
            writer.newLine();
            for (Visit visit : visits) {
                writer.write(visit.toCsv());
                writer.newLine();
            }
            return new ExportResult(out, visits.size());
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

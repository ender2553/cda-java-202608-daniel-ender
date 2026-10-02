package demo.clinic.log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

/** Where the visit log and exports live, a generator for the log, and a line counter. */
public final class VisitLogFiles {

    public static final Path DATA_DIR = Path.of("data");
    public static final Path LOG = DATA_DIR.resolve("visit-log.csv");
    public static final String HEADER = "visit_id,checked_in_at,patient_id,clinician_id,reason_code,minutes";
    public static final long DEFAULT_VISITS = 400_000;

    private static final List<String> REASONS = List.of("CHECKUP", "FLU", "LAB", "FOLLOWUP", "VACCINE");
    private static final LocalDateTime START = LocalDateTime.of(2025, 1, 1, 0, 0);

    private VisitLogFiles() {
    }

    public static Path exportFileFor(LocalDate from, LocalDate to) {
        return DATA_DIR.resolve("visits-" + from + "-to-" + to + ".csv");
    }

    public static Path requireLog() {
        if (!Files.exists(LOG)) {
            throw new IllegalArgumentException("No visit log yet. Choose 5 to generate one.");
        }
        return LOG;
    }

    /** Writes a synthetic log: one visit every 2 minutes from 2025-01-01, across 6 clinicians. */
    public static void generate(long visits) throws IOException {
        Files.createDirectories(DATA_DIR);
        RandomGenerator random = RandomGenerator.getDefault();
        try (BufferedWriter writer = Files.newBufferedWriter(LOG, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (long id = 1; id <= visits; id++) {
                writer.write(id + "," + START.plusMinutes(2 * id)
                        + ",PAT-" + String.format("%08d", 1 + random.nextInt(200_000))
                        + ",CLN-0" + (1 + random.nextInt(6))
                        + "," + REASONS.get(random.nextInt(REASONS.size()))
                        + "," + (10 + 5 * random.nextInt(7)));
                writer.newLine();
            }
        }
    }

    public static long countDataLines(Path file) throws IOException {
        try (Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8)) {
            return Math.max(0, lines.count() - 1);
        }
    }
}

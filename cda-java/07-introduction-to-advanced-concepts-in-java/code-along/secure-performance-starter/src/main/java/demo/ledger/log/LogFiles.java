package demo.ledger.log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

/**
 * Finished file helpers: where the log and the exports live, a generator for a
 * large practice log, and a line counter. Both file methods are finished
 * examples of try-with-resources.
 *
 * <p>Log format (CSV, one header line):
 * {@code id,created_at,from_account,to_account,amount,memo}
 */
public final class LogFiles {

    public static final Path DATA_DIR = Path.of("data");
    public static final Path LOG = DATA_DIR.resolve("transaction-log.csv");
    public static final String HEADER = "id,created_at,from_account,to_account,amount,memo";

    private static final List<String> MEMOS = List.of("Rent", "Groceries", "Invoice", "Refund", "Transfer");
    private static final Instant START = Instant.parse("2020-01-01T00:00:00Z");

    private LogFiles() {
    }

    public static Path exportFileFor(String accountId) {
        return DATA_DIR.resolve("export-" + accountId + ".csv");
    }

    /** The log, or a message a user can act on if it hasn't been generated yet. */
    public static Path requireLog() {
        if (!Files.exists(LOG)) {
            throw new IllegalArgumentException("No transaction log yet. Choose 5 to generate one.");
        }
        return LOG;
    }

    /** Writes a synthetic log of {@code lines} transactions, one per minute, between five accounts. */
    public static void generate(long lines) throws IOException {
        Files.createDirectories(DATA_DIR);
        RandomGenerator random = RandomGenerator.getDefault();
        // try-with-resources: the writer is flushed and closed however this block ends.
        try (BufferedWriter writer = Files.newBufferedWriter(LOG, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            for (long id = 1; id <= lines; id++) {
                int from = random.nextInt(5);
                int to = (from + 1 + random.nextInt(4)) % 5;   // never the same account
                BigDecimal amount = BigDecimal.valueOf(100 + random.nextInt(99_900), 2);   // 1.00 to 999.99
                writer.write(id + "," + START.plus(id, ChronoUnit.MINUTES) + ",ACC-0000000" + (from + 1)
                        + ",ACC-0000000" + (to + 1) + "," + amount + "," + MEMOS.get(random.nextInt(MEMOS.size())));
                writer.newLine();
            }
        }
    }

    /** Counts the data lines (every line but the header) actually on disk. */
    public static long countDataLines(Path file) throws IOException {
        try (Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8)) {
            return Math.max(0, lines.count() - 1);
        }
    }
}

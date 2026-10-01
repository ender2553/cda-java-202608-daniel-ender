package demo.ledger.log;

import demo.ledger.repository.DataAccessFailure;
import demo.ledger.repository.Validate;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reads LedgerCore's transaction log, a file that only ever grows. This code-along
 * builds this class, together with {@code TransactionRepository} and
 * {@code ProcessingStats}.
 *
 * <p>It starts as a working first draft that is fine on a small file and fails
 * on a large one. Work through the {@code TODO: Step N of 7} comments in order:
 * <pre>
 *   Step 1      Reproduce the OutOfMemoryError               (top of this class, no code)
 *   Steps 2-3   Stream the log instead of loading it         (summarize, exportForAccount)
 *   Steps 4-5   Close every resource: files and connections  (exportForAccount, TransactionRepository)
 *   Steps 6-7   Bound the request, fix the shared counter    (TransactionRepository, ProcessingStats)
 * </pre>
 */
@Component
public class TransactionLogExporter {

    // TODO: Step 1 of 7 — Reproduce the failure before you fix anything.
    //   - Run the app with the LedgerDemoApp run configuration. It starts the app with -Xmx128m,
    //     a 128 MB heap. Check that the menu title says "max heap about 128 MB".
    //   - Choose 5 and press Enter. It writes data/transaction-log.csv: 3,000,000 lines, about 200 MB.
    //   - Choose 6. Before you press Enter, predict what will happen. Then read the Run window.
    //   - Nobody attacked this app. What made it fail? Which part of CIA is that?

    private final ProcessingStats stats;

    public TransactionLogExporter(ProcessingStats stats) {
        this.stats = stats;
    }

    /** Counts every transaction in the log and totals the amounts. */
    public LogSummary summarize() {
        Path log = LogFiles.requireLog();
        // TODO: Step 2 of 7 — Stream the log instead of loading it.
        //   - Files.readAllLines reads the whole file into one List before the loop starts.
        //   - Replace the try block with try (Stream<String> lines = Files.lines(log, StandardCharsets.UTF_8)).
        //     Files.lines opens the file, so it must be closed: that is what try (...) does.
        //   - Build the summary lazily: lines.skip(1) (the header), .map(LogSummary::ofLine),
        //     then .reduce(LogSummary.EMPTY, LogSummary::plus). Keep the recordLines call.
        //   - Before you run it, predict: does map run on all 3,000,000 lines first, then reduce?
        //     Or does each line go through map and reduce before the next one is read?
        try {
            List<String> lines = Files.readAllLines(log, StandardCharsets.UTF_8);
            LogSummary summary = LogSummary.EMPTY;
            for (String line : lines.subList(1, lines.size())) {   // skip the header line
                summary = summary.plus(LogSummary.ofLine(line));
            }
            stats.recordLines(summary.transactions());
            return summary;
        } catch (IOException | UncheckedIOException ex) {
            throw DataAccessFailure.logged("summarize transaction log", "Could not read the transaction log.", ex);
        }
    }

    /** Writes every log line that involves one account to data/export-ACC-xxxxxxxx.csv. */
    public ExportResult exportForAccount(String accountId) {
        Validate.accountId(accountId);
        Path log = LogFiles.requireLog();
        Path out = LogFiles.exportFileFor(accountId);
        try {
            // TODO: Step 3 of 7 — Remove the .collect(Collectors.toList()) trap.
            //   - Run it first: menu 7 with ACC-00000001. This already uses Files.lines,
            //     so why does it fail the same way Step 1 did?
            //   - The LAST operation in a pipeline decides whether memory is saved. collect(...)
            //     puts every matching line (about 1,200,000) into one List.
            //   - Keep .skip(1).filter(...), but end the pipeline with .iterator() instead
            //     (Iterator is already imported). After the header is written, loop:
            //     while (matches.hasNext()) { write matches.next(), newLine(), written++ }
            //     Use written (a long that starts at 0) for recordLines and the ExportResult.
            List<String> matches = Files.lines(log, StandardCharsets.UTF_8)
                    .skip(1)
                    .filter(line -> involves(line, accountId))
                    .collect(Collectors.toList());

            // TODO: Step 4 of 7 — Close the reader AND the writer with try-with-resources.
            //   - Run it first (after Step 3): menu 7 with ACC-00000001. Compare the "Exported"
            //     count with the "File check" count on the next line. Where did the data go?
            //   - Nothing here is ever closed. A BufferedWriter holds its last few thousand
            //     characters in memory until close(). The Files.lines stream holds the log
            //     file open, so every export leaks one open file handle. (You may not see
            //     that one on Windows: the garbage collector sometimes closes it for you, later.)
            //   - Move both into the try (...) header, separated by a semicolon:
            //       try (Stream<String> lines = Files.lines(log, StandardCharsets.UTF_8);
            //            BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            //     Then build the pipeline from lines. Keep the existing catch block.
            BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8);
            writer.write(LogFiles.HEADER);
            writer.newLine();
            for (String line : matches) {
                writer.write(line);
                writer.newLine();
            }
            stats.recordLines(matches.size());
            return new ExportResult(out, matches.size());
        } catch (IOException | UncheckedIOException ex) {
            throw DataAccessFailure.logged("export transaction log", "Could not export the transaction log.", ex);
        }
    }

    /** True when the account is the sender (column 3) or the receiver (column 4). */
    private static boolean involves(String line, String accountId) {
        String[] fields = line.split(",", -1);
        return fields[2].equals(accountId) || fields[3].equals(accountId);
    }
}

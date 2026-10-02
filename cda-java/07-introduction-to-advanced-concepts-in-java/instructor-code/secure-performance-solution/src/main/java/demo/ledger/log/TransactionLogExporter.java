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
import java.util.stream.Stream;

/**
 * Reads LedgerCore's transaction log, a file that only ever grows. This code-along
 * builds this class, together with {@code TransactionRepository} and
 * {@code ProcessingStats}.
 *
 * <p><b>This is the completed solution.</b> Every {@code TODO: (Completed) Step N of 7}
 * comment shows the original step instruction next to the code that finishes it.
 *
 * <p>Code-along order (matches the Instructor Guide's segments):
 * <pre>
 *   Step 1      Reproduce the OutOfMemoryError               (top of this class, no code)
 *   Steps 2-3   Stream the log instead of loading it         (summarize, exportForAccount)
 *   Steps 4-5   Close every resource: files and connections  (exportForAccount, TransactionRepository)
 *   Steps 6-7   Bound the request, fix the shared counter    (TransactionRepository, ProcessingStats)
 * </pre>
 */
@Component
public class TransactionLogExporter {

    // TODO: (Completed) Step 1 of 7 — Reproduce the failure before you fix anything.
    //   - The LedgerDemoApp run configuration starts the app with -Xmx128m (a 128 MB heap).
    //     The menu title shows "max heap about 128 MB" when it is in effect.
    //   - Menu 5 writes data/transaction-log.csv: 3,000,000 lines, about 200 MB.
    //   - Menu 6 then crashes the app with java.lang.OutOfMemoryError: Java heap space.
    //     Nobody attacked it. The log simply grew. That is an availability failure.

    private final ProcessingStats stats;

    public TransactionLogExporter(ProcessingStats stats) {
        this.stats = stats;
    }

    /** Counts every transaction in the log and totals the amounts. */
    public LogSummary summarize() {
        Path log = LogFiles.requireLog();
        // TODO: (Completed) Step 2 of 7 — Stream the log instead of loading it.
        //   - Files.readAllLines put all 3,000,000 lines in memory at once.
        //   - Files.lines reads lazily: each line is read, mapped and added, then forgotten,
        //     so memory stays flat however big the file grows.
        //   - Files.lines holds an open file handle, so it goes in try-with-resources.
        try (Stream<String> lines = Files.lines(log, StandardCharsets.UTF_8)) {
            LogSummary summary = lines.skip(1)                  // the header line
                    .map(LogSummary::ofLine)
                    .reduce(LogSummary.EMPTY, LogSummary::plus);
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
        // TODO: (Completed) Step 4 of 7 — Close the reader AND the writer with try-with-resources.
        //   - The export reported more lines than the file check found on disk. The writer was
        //     never closed, so its last buffer was never flushed. The Files.lines stream was
        //     never closed either, so every export leaked an open handle on the log file.
        //   - Both resources are declared in one try (...) header. They close in reverse order,
        //     even when an exception is thrown, and a failure in close() never hides the original.
        try (Stream<String> lines = Files.lines(log, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            writer.write(LogFiles.HEADER);
            writer.newLine();
            // TODO: (Completed) Step 3 of 7 — Remove the .collect(Collectors.toList()) trap.
            //   - The pipeline started with Files.lines, but collect(...) pulled every matching
            //     line into one List: about 1,200,000 lines, and the same OutOfMemoryError.
            //   - The LAST operation decides whether memory is saved. iterator() pulls one
            //     line at a time, and each one is written, then forgotten.
            Iterator<String> matches = lines.skip(1)
                    .filter(line -> involves(line, accountId))
                    .iterator();
            long written = 0;
            while (matches.hasNext()) {
                writer.write(matches.next());
                writer.newLine();
                written++;
            }
            stats.recordLines(written);
            return new ExportResult(out, written);
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

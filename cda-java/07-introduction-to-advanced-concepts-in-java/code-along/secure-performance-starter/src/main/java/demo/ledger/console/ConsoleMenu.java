package demo.ledger.console;

import demo.ledger.log.ExportResult;
import demo.ledger.log.LogFiles;
import demo.ledger.log.LogSummary;
import demo.ledger.log.ProcessingStats;
import demo.ledger.log.TransactionLogExporter;
import demo.ledger.repository.AccountRepository;
import demo.ledger.repository.DataAccessFailure;
import demo.ledger.repository.TransactionRepository;
import demo.ledger.service.TransferService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Scanner;

/**
 * The view: a numeric menu. It only reads input, calls a repository, service
 * or the log exporter, and prints the result. It never touches SQL, a
 * Connection or a file stream itself.
 */
@Component
public class ConsoleMenu implements CommandLineRunner {

    private static final long DEFAULT_LOG_LINES = 3_000_000;

    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final TransferService transfers;
    private final TransactionLogExporter exporter;
    private final ProcessingStats stats;
    private final Scanner in = new Scanner(System.in);

    public ConsoleMenu(AccountRepository accounts, TransactionRepository transactions, TransferService transfers,
                       TransactionLogExporter exporter, ProcessingStats stats) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.transfers = transfers;
        this.exporter = exporter;
        this.stats = stats;
    }

    @Override
    public void run(String... args) {
        long maxHeapMb = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        while (true) {
            System.out.printf("""

                    === LedgerCore Demo (max heap about %d MB) ===
                    1) Show account balances
                    2) Transfer funds
                    3) Show recent transactions
                    4) Total sent by an account
                    5) Generate the transaction log file
                    6) Summarize the transaction log
                    7) Export one account from the log
                    8) Run the concurrency check
                    0) Exit
                    """, maxHeapMb);
            String choice = prompt("Choose");
            if (choice == null || choice.equals("0")) {
                return;
            }
            try {
                switch (choice) {
                    case "1" -> accounts.findAll().forEach(a ->
                            System.out.printf("%s  %12s%n", a.accountId(), a.balance()));
                    case "2" -> {
                        transfers.transfer(prompt("From account"), prompt("To account"),
                                parseAmount(prompt("Amount")), prompt("Memo (optional)"));
                        System.out.println("Transfer complete.");
                    }
                    case "3" -> transactions.findRecent(parseCount(prompt("How many (newest first)"))).forEach(t ->
                            System.out.printf("%8d  %s -> %s  %10s  %-10s %s%n", t.id(), t.fromAccount(),
                                    t.toAccount(), t.amount(), t.memo(), t.createdAt().toLocalDate()));
                    case "4" -> {
                        String accountId = prompt("Account ID");
                        System.out.printf("%s has sent %s in total.%n", accountId, transactions.totalSentBy(accountId));
                    }
                    case "5" -> generateLog();
                    case "6" -> {
                        LogSummary summary = exporter.summarize();
                        System.out.printf("%,d transactions, totaling %,.2f%n", summary.transactions(), summary.total());
                        printStats();
                    }
                    case "7" -> exportAccount();
                    case "8" -> ConcurrencyCheck.run();
                    default -> System.out.println("Please choose 0-8.");
                }
            } catch (IllegalArgumentException | DataAccessFailure ex) {
                // Both carry messages written to be shown to a user; nothing internal.
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private void generateLog() {
        String text = prompt("How many lines (Enter for " + DEFAULT_LOG_LINES + ")");
        long lines = (text == null || text.isEmpty()) ? DEFAULT_LOG_LINES : parseCount(text);
        if (lines < 1) {
            throw new IllegalArgumentException("Generate at least 1 line");
        }
        System.out.printf("Writing %,d lines to %s ...%n", lines, LogFiles.LOG);
        try {
            LogFiles.generate(lines);
        } catch (IOException ex) {
            throw DataAccessFailure.logged("generate transaction log", "Could not write the transaction log.", ex);
        }
        System.out.println("Done.");
    }

    private void exportAccount() {
        ExportResult result = exporter.exportForAccount(prompt("Account ID"));
        System.out.printf("Exported %,d transactions to %s%n", result.transactions(), result.file());
        // Check the file itself, not just what the code reported.
        try {
            long onDisk = LogFiles.countDataLines(result.file());
            System.out.printf("File check: %,d transactions on disk. %s%n", onDisk,
                    onDisk == result.transactions() ? "OK" : "MISMATCH: data was lost.");
        } catch (IOException ex) {
            System.out.println("File check: could not read " + result.file());
        }
        printStats();
    }

    private void printStats() {
        System.out.printf("Log lines processed since startup: %,d%n", stats.linesProcessed());
    }

    private String prompt(String label) {
        System.out.print(label + ": ");
        return in.hasNextLine() ? in.nextLine().trim() : null;
    }

    private static BigDecimal parseAmount(String text) {
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException("Amount must be a number like 25.00");
        }
    }

    private static int parseCount(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Enter a whole number, such as 20");
        }
    }
}

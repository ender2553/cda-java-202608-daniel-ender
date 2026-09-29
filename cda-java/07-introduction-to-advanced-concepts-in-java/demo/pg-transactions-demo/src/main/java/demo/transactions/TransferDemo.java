package demo.transactions;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * Runs the in-class scenarios. Pick one by name (no argument runs naive, then atomic):
 *
 * <pre>
 *   naive       auto-commit transfer, crash after the debit
 *   atomic      the same crash, plus an overdraft, inside a real transaction
 *   visibility  pauses mid-transfer so you can look at the tables from psql
 * </pre>
 */
public final class TransferDemo {

    private static final BigDecimal EXPECTED_TOTAL = new BigDecimal("600.00");
    private static final Runnable NO_CHECKPOINT = () -> { };
    private static final Runnable CRASH = () -> {
        throw new IllegalStateException("Simulated crash between debit and credit");
    };

    private final Database database = Database.fromEnvironment();
    private final TransferService transfers = new TransferService(database);
    private final Scanner console = new Scanner(System.in);

    public static void main(String[] args) throws SQLException {
        TransferDemo demo = new TransferDemo();
        System.out.println("Connected to " + demo.database.getUrl());

        String scenario = args.length == 0 ? "all" : args[0];

        switch (scenario) {
            case "naive" -> demo.naive();
            case "atomic" -> demo.atomic();
            case "visibility" -> demo.visibility();
            case "all" -> {
                demo.naive();
                demo.atomic();
            }
            default -> System.out.println("Unknown scenario '" + scenario + "'. Use: naive | atomic | visibility");
        }
    }

    // ------------------------------------------------------------------ scenarios

    private void naive() throws SQLException {
        banner("NAIVE: auto-commit ON, each statement commits by itself");
        database.reset();
        snapshot("Starting state");

        transfers.naiveTransfer("ACC-1", "ACC-2", new BigDecimal("50.00"), NO_CHECKPOINT);
        snapshot("After a successful $50 transfer (the happy path looks fine)");

        attempt("Transfer $75 ACC-1 -> ACC-2, crashing after the debit",
                () -> transfers.naiveTransfer("ACC-1", "ACC-2", new BigDecimal("75.00"), CRASH));
        snapshot("After the crash: $75 left ACC-1 and never arrived anywhere");
    }

    private void atomic() throws SQLException {
        banner("ATOMIC: one transaction across transfers, accounts, ledger_entries");
        database.reset();
        snapshot("Starting state");

        attempt("Transfer $75 ACC-1 -> ACC-2, crashing after the debit",
                () -> transfers.transfer("ACC-1", "ACC-2", new BigDecimal("75.00"), CRASH));
        snapshot("After the crash: rollback undid the header, the debit, and the debit entry");

        attempt("Transfer $1000 ACC-2 -> ACC-1 (overdraft; Postgres CHECK constraint rejects it)",
                () -> transfers.transfer("ACC-2", "ACC-1", new BigDecimal("1000.00")));
        snapshot("After the overdraft: the transfers row inserted before the failure is gone too");

        transfers.transfer("ACC-1", "ACC-2", new BigDecimal("50.00"));
        snapshot("After a successful $50 transfer: all five writes committed together");
    }

    private void visibility() throws SQLException {
        banner("VISIBILITY: what does another session see mid-transfer?");
        database.reset();
        snapshot("Starting state");

        System.out.println("\n>> NAIVE transfer of $50, paused right after the debit.");
        transfers.naiveTransfer("ACC-1", "ACC-2", new BigDecimal("50.00"), this::pauseForPsql);
        snapshot("Naive transfer finished");

        database.reset();
        System.out.println("\n>> ATOMIC transfer of $50, paused right after the debit.");
        transfers.transfer("ACC-1", "ACC-2", new BigDecimal("50.00"), this::pauseForPsql);
        snapshot("Atomic transfer committed");
    }

    // ------------------------------------------------------------------ helpers

    @FunctionalInterface
    private interface TransferCall {
        void run() throws SQLException;
    }

    private void attempt(String description, TransferCall call) {
        System.out.println("\n>> " + description);
        try {
            call.run();
            System.out.println("   succeeded");
        } catch (TransferFailedException failure) {
            // All the caller ever sees; the database detail went to the internal log (stderr) instead.
            System.out.println("   FAILED, caller sees: " + failure.getMessage());
        } catch (SQLException | RuntimeException failure) {
            System.out.println("   FAILED: " + failure.getMessage().lines().findFirst().orElse(""));
        }
    }

    private void pauseForPsql() {
        String query = " -c \"SELECT * FROM accounts\"";
        System.out.println("""
                   PAUSED between the debit and the credit. In a second terminal run one of:
                     Docker:        docker exec -it ledger-demo-db psql %s
                     Local install: psql %s
                   Then press Enter here to finish the transfer...
                """.formatted(database.psqlOptions(true) + query, database.psqlOptions(false) + query)
                .stripTrailing());
        console.nextLine();
    }

    private void snapshot(String label) throws SQLException {
        try (Connection connection = database.getConnection();
             Statement query = connection.createStatement()) {

            StringBuilder accounts = new StringBuilder();
            BigDecimal total = BigDecimal.ZERO;
            try (ResultSet rows = query.executeQuery(
                    "SELECT account_id, owner, balance FROM accounts ORDER BY account_id")) {
                while (rows.next()) {
                    BigDecimal balance = rows.getBigDecimal("balance");
                    total = total.add(balance);
                    accounts.append(String.format("%s (%s) %8s   ",
                            rows.getString("account_id"), rows.getString("owner"), balance));
                }
            }

            long transferCount = count(query, "SELECT count(*) FROM transfers");
            long entryCount = count(query, "SELECT count(*) FROM ledger_entries");
            // A transfer is unbalanced unless it has exactly two entries that sum to zero.
            long unbalanced = count(query, """
                    SELECT count(*) FROM transfers t
                    WHERE (SELECT count(*) FROM ledger_entries e WHERE e.transfer_id = t.transfer_id) <> 2
                       OR (SELECT coalesce(sum(amount), 0) FROM ledger_entries e WHERE e.transfer_id = t.transfer_id) <> 0
                    """);

            boolean consistent = total.compareTo(EXPECTED_TOTAL) == 0 && unbalanced == 0;
            System.out.println("\n   [" + label + "]");
            System.out.println("   accounts:  " + accounts);
            System.out.printf("   total money: %s (expected %s)   transfers: %d   ledger entries: %d   unbalanced transfers: %d%n",
                    total, EXPECTED_TOTAL, transferCount, entryCount, unbalanced);
            System.out.println(consistent ? "   ledger: CONSISTENT" : "   ledger: *** INCONSISTENT ***");
        }
    }

    private static long count(Statement query, String sql) throws SQLException {
        try (ResultSet rows = query.executeQuery(sql)) {
            rows.next();
            return rows.getLong(1);
        }
    }

    private static void banner(String title) {
        System.out.println("\n==================================================================");
        System.out.println(title);
        System.out.println("==================================================================");
    }
}

package demo.transactions;

import java.lang.System.Logger.Level;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Moves money between two accounts. One transfer writes to THREE tables:
 *
 * <pre>
 *   1. transfers       INSERT the transfer header
 *   2. accounts        UPDATE the sender's balance    (debit)
 *   3. ledger_entries  INSERT the debit line
 *        -- afterDebit checkpoint: where the demo injects a crash or a pause --
 *   4. accounts        UPDATE the receiver's balance  (credit)
 *   5. ledger_entries  INSERT the credit line
 * </pre>
 *
 * {@link #naiveTransfer} and {@link #transfer} run the exact same five
 * writes. The only difference is where the transaction boundary sits.
 */
public final class TransferService {

    private static final String INSERT_TRANSFER_SQL =
            "INSERT INTO transfers (from_account, to_account, amount) VALUES (?, ?, ?) RETURNING transfer_id";
    private static final String ADJUST_BALANCE_SQL =
            "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
    private static final String INSERT_ENTRY_SQL =
            "INSERT INTO ledger_entries (transfer_id, account_id, amount) VALUES (?, ?, ?)";

    private static final System.Logger LOG = System.getLogger(TransferService.class.getName());

    private final Database database;

    public TransferService(Database database) {
        this.database = database;
    }

    /**
     * BEFORE: auto-commit stays ON (the JDBC default), so each of the five
     * writes commits by itself the moment it runs. If anything fails partway
     * through, the writes that already ran stay in the database.
     */
    public long naiveTransfer(String fromId, String toId, BigDecimal amount, Runnable afterDebit)
            throws SQLException {
        try (Connection connection = database.getConnection()) {
            return writeTransfer(connection, fromId, toId, amount, afterDebit);
        }
    }

    /**
     * AFTER: one transaction around all five writes, across all three tables.
     * Either every write commits together, or rollback undoes them all.
     *
     * <p>Rollback lives in {@code finally}, keyed on whether {@code commit()}
     * succeeded, so it runs on every failure path: an {@code SQLException},
     * a {@code RuntimeException}, or an {@code Error}. A {@code catch} block
     * only covers the types it names, and anything it misses would reach
     * {@code setAutoCommit(true)}, which commits the pending writes.
     *
     * <p>The {@code catch} handles reporting, not rollback: by the time it
     * runs, {@code finally} has already rolled back and the connection is
     * closed. It logs the database detail internally and gives the caller a
     * {@link TransferFailedException} with a safe message instead.
     */
    public long transfer(String fromId, String toId, BigDecimal amount, Runnable afterDebit) {
        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);            // BEGIN
            boolean committed = false;
            try {
                long transferId = writeTransfer(connection, fromId, toId, amount, afterDebit);
                connection.commit();                    // all five writes take effect together
                committed = true;
                return transferId;
            } finally {
                if (!committed) {
                    connection.rollback();              // none of them happened; the failure keeps propagating
                }
                connection.setAutoCommit(true);         // must come AFTER rollback: on an open transaction it commits
            }
        } catch (SQLException e) {
            // The detail stays in the internal log; the caller gets a message that's safe to show.
            LOG.log(Level.WARNING, "Transfer {0} -> {1} of {2} rolled back. SQLState {3}: {4}",
                    fromId, toId, amount.toPlainString(), e.getSQLState(), e.getMessage());
            throw new TransferFailedException(
                    "Transfer from " + fromId + " to " + toId + " could not be completed. No money was moved.", e);
        }
    }

    public long transfer(String fromId, String toId, BigDecimal amount) {
        return transfer(fromId, toId, amount, () -> { });
    }

    private long writeTransfer(Connection connection, String fromId, String toId, BigDecimal amount,
                               Runnable afterDebit) throws SQLException {
        long transferId = insertTransfer(connection, fromId, toId, amount);  // 1. transfers
        adjustBalance(connection, fromId, amount.negate());                  // 2. accounts (debit)
        insertEntry(connection, transferId, fromId, amount.negate());        // 3. ledger_entries

        afterDebit.run();                                                    // demo checkpoint

        adjustBalance(connection, toId, amount);                             // 4. accounts (credit)
        insertEntry(connection, transferId, toId, amount);                   // 5. ledger_entries
        return transferId;
    }

    private long insertTransfer(Connection connection, String fromId, String toId, BigDecimal amount)
            throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(INSERT_TRANSFER_SQL)) {
            insert.setString(1, fromId);
            insert.setString(2, toId);
            insert.setBigDecimal(3, amount);
            try (ResultSet generated = insert.executeQuery()) {
                generated.next();
                return generated.getLong("transfer_id");
            }
        }
    }

    private void adjustBalance(Connection connection, String accountId, BigDecimal delta) throws SQLException {
        try (PreparedStatement update = connection.prepareStatement(ADJUST_BALANCE_SQL)) {
            update.setBigDecimal(1, delta);
            update.setString(2, accountId);
            update.executeUpdate();
        }
    }

    private void insertEntry(Connection connection, long transferId, String accountId, BigDecimal amount)
            throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(INSERT_ENTRY_SQL)) {
            insert.setLong(1, transferId);
            insert.setString(2, accountId);
            insert.setBigDecimal(3, amount);
            insert.executeUpdate();
        }
    }
}

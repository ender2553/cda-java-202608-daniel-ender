package com.rti.ledgercore.transactions;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Moves funds between two LedgerCore accounts.
 *
 * <p><b>This is the insecure starting point for the lab.</b> The debit and
 * the credit are two independent, auto-committing writes with nothing tying
 * them together.
 */
public final class TransferService {

    private static final String DEBIT_SQL = "UPDATE accounts SET balance = balance - ? WHERE account_id = ?";
    private static final String CREDIT_SQL = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";

    private final DataSource dataSource;

    public TransferService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // COMPLETE (lab): make this atomic with commit/rollback. Right now, autocommit
    // stays on (the JDBC default), so each write below commits independently
    // the instant it runs. Anything that interrupts execution between the two
    // — a thrown exception, a dropped connection, a process crash — leaves the
    // debit applied with no matching credit.
    public void transfer(String fromAccountId, String toAccountId, BigDecimal amount) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {

            connection.setAutoCommit(false);

            try {
                try (PreparedStatement debit = connection.prepareStatement(DEBIT_SQL)) {
                    debit.setBigDecimal(1, amount);
                    debit.setString(2, fromAccountId);
                    debit.executeUpdate();
                }

                try (PreparedStatement credit = connection.prepareStatement(CREDIT_SQL)) {
                    credit.setBigDecimal(1, amount);
                    credit.setString(2, toAccountId);
                    credit.executeUpdate();
                }

                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw new SQLException("Transfer not complete.");
            }
        }
    }
}
package demo.ledger.repository;

import demo.ledger.domain.Transaction;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * The only path to {@code transactions}.
 *
 * <p><b>This is the completed solution.</b> See {@code TODO: (Completed) Step 5 of 7}
 * and {@code TODO: (Completed) Step 6 of 7}.
 */
@Repository
public class TransactionRepository {

    // TODO: (Completed) Step 6 of 7 — Add an explicit bound on the page size.
    /** The most rows any caller may ask for at once. */
    public static final int MAX_PAGE_SIZE = 100;

    private static final RowMapper<Transaction> TRANSACTION = (rs, rowNum) -> new Transaction(
            rs.getLong("id"),
            rs.getString("from_account"),
            rs.getString("to_account"),
            rs.getBigDecimal("amount"),
            rs.getString("memo"),
            rs.getObject("created_at", OffsetDateTime.class));

    private final JdbcClient jdbc;
    private final DataSource dataSource;

    public TransactionRepository(JdbcClient jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
    }

    public void record(String fromAccountId, String toAccountId, BigDecimal amount, String memo) {
        Validate.accountId(fromAccountId);
        Validate.accountId(toAccountId);
        Validate.amount(amount);
        Validate.maxLength(memo, 256, "Memo");
        try {
            jdbc.sql("""
                            INSERT INTO transactions (from_account, to_account, amount, memo)
                            VALUES (:from, :to, :amount, :memo)""")
                    .param("from", fromAccountId)
                    .param("to", toAccountId)
                    .param("amount", amount)
                    .param("memo", memo)
                    .update();
        } catch (DataAccessException ex) {
            throw DataAccessFailure.logged("record transaction", "Could not record the transaction.", ex);
        }
    }

    /**
     * The total an account has ever sent. Written with plain JDBC before this
     * team adopted JdbcClient, so it manages its own Connection.
     */
    public BigDecimal totalSentBy(String accountId) {
        Validate.accountId(accountId);
        // TODO: (Completed) Step 5 of 7 — Close the Connection, the statement and the result set.
        //   - Five runs of menu 4 used up the pool's five connections, because none was ever
        //     returned. The sixth run, menu 1, and every transfer then waited 3 seconds and failed.
        //   - try-with-resources returns the connection to the pool on every path, including
        //     when the query throws.
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE from_account = ?")) {
            ps.setString(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        } catch (SQLException ex) {
            throw DataAccessFailure.logged("total sent by account", "Could not total that account's transactions.", ex);
        }
    }

    /** The newest transactions first: as many as the caller asks for, up to MAX_PAGE_SIZE. */
    public List<Transaction> findRecent(int limit) {
        // TODO: (Completed) Step 6 of 7 — Bound the request before any SQL runs.
        //   - Menu 3 with 10000000 loaded all 1,000,000 rows and crashed with OutOfMemoryError:
        //     the same failure as the log file, from a different entry point.
        //   - An out-of-range size is REJECTED with a clear message, never quietly truncated
        //     with Math.min. Truncation hides the limit and looks like missing data.
        Validate.between(limit, 1, MAX_PAGE_SIZE, "Show between 1 and " + MAX_PAGE_SIZE + " transactions at a time");
        try {
            return jdbc.sql("""
                            SELECT id, from_account, to_account, amount, memo, created_at
                            FROM transactions
                            ORDER BY id DESC
                            LIMIT :limit""")
                    .param("limit", limit)
                    .query(TRANSACTION)
                    .list();
        } catch (DataAccessException ex) {
            throw DataAccessFailure.logged("list recent transactions", "Could not load transactions.", ex);
        }
    }
}

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

/** The only path to {@code transactions}. */
@Repository
public class TransactionRepository {

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
        // TODO: Step 5 of 7 — Close the Connection, the statement and the result set.
        //   - Run it first: choose 4 with ACC-00000001 five times. Each one works. Choose 4 a
        //     sixth time, then 1 (balances). Search logs/ledger-demo.log for the ref, then for "leak".
        //   - The pool holds 5 connections (see application.properties). This method borrows one
        //     on every call and never gives it back. After 5 calls, every caller in the app waits
        //     3 seconds and fails, including other users' transfers.
        //   - Declare conn and ps inside try (...), and rs in a nested try (...):
        //       try (Connection conn = dataSource.getConnection();
        //            PreparedStatement ps = conn.prepareStatement("SELECT ...")) {
        //           ps.setString(1, accountId);
        //           try (ResultSet rs = ps.executeQuery()) { ... }
        //       }
        //   - Restart the app (leaked connections are only freed when the app stops), then
        //     choose 4 at least six times.
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE from_account = ?");
            ps.setString(1, accountId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getBigDecimal(1);
        } catch (SQLException ex) {
            throw DataAccessFailure.logged("total sent by account", "Could not total that account's transactions.", ex);
        }
    }

    /** The newest transactions first: as many as the caller asks for. */
    public List<Transaction> findRecent(int limit) {
        // TODO: Step 6 of 7 — Bound the request before any SQL runs.
        //   - Run it first: menu 3 with 5 works. Then try 10000000. The table holds 1,000,000 rows.
        //   - The value is bound safely, so this is not injection. The caller simply asked for
        //     more than the app can hold: the same failure as Step 1, from a new entry point.
        //   - At the top of the class, above TRANSACTION, add the bound as a constant:
    //       public static final int MAX_PAGE_SIZE = 100;
    //   - Then, before the try below, add:
        //       Validate.between(limit, 1, MAX_PAGE_SIZE, "Show between 1 and " + MAX_PAGE_SIZE + " transactions at a time");
        //   - Reject; don't truncate. Math.min(limit, MAX_PAGE_SIZE) would quietly return fewer
        //     rows than the caller asked for, and that looks like missing data.
        //   - After the fix, also try 0 and -1.
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

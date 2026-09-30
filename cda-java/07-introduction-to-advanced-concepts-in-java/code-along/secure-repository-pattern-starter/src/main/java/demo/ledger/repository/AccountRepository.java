package demo.ledger.repository;

import demo.ledger.domain.Account;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/** The only path to {@code accounts}. */
@Repository
public class AccountRepository {

    private static final RowMapper<Account> ACCOUNT = (rs, rowNum) -> new Account(
            rs.getString("account_id"),
            rs.getBigDecimal("balance"));

    private final JdbcClient jdbc;

    public AccountRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Account> findAll() {
        try {
            return jdbc.sql("SELECT account_id, balance FROM accounts ORDER BY account_id")
                    .query(ACCOUNT)
                    .list();
        } catch (DataAccessException ex) {
            throw DataAccessFailure.logged("list accounts", "Could not load accounts.", ex);
        }
    }

    public void debit(String accountId, BigDecimal amount) {
        // Without Validate.amount, debiting -50.00 would silently ADD 50.00.
        changeBalance(Validate.accountId(accountId), Validate.amount(amount).negate());
    }

    public void credit(String accountId, BigDecimal amount) {
        changeBalance(Validate.accountId(accountId), Validate.amount(amount));
    }

    private void changeBalance(String accountId, BigDecimal delta) {
        int rows;
        try {
            rows = jdbc.sql("UPDATE accounts SET balance = balance + :delta WHERE account_id = :accountId")
                    .param("delta", delta)
                    .param("accountId", accountId)
                    .update();
        } catch (DataAccessException ex) {
            // e.g. chk_balance_non_negative: the full constraint detail goes to the log, not the screen.
            throw DataAccessFailure.logged("change balance", "The balance could not be updated.", ex);
        }
        if (rows == 0) {
            throw new IllegalArgumentException("No account " + accountId);
        }
    }
}

package demo.ledger.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/** The only path to {@code transactions}. */
@Repository
public class TransactionRepository {

    private final JdbcClient jdbc;

    public TransactionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
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
}

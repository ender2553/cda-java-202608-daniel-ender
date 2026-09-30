package demo.ledger.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class TransactionRepository {

    private final JdbcClient jdbc;

    public TransactionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void record(String fromAccountId, String toAccountId, double amount, String memo) {
        jdbc.sql("INSERT INTO transactions (from_account, to_account, amount, memo) VALUES ('"
                + fromAccountId + "', '" + toAccountId + "', " + amount + ", '" + memo + "')").update();
    }

    public List<Map<String, Object>> findByAccount(String accountId) {
        return jdbc.sql("SELECT * FROM transactions WHERE from_account = '" + accountId
                        + "' OR to_account = '" + accountId + "' ORDER BY created_at")
                .query()
                .listOfRows();
    }
}

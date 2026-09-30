package demo.ledger.repository;

import demo.ledger.domain.Account;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AccountRepository {

    private final JdbcClient jdbc;
    private final Map<String, Account> cache = new HashMap<>();

    public AccountRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Account> findAll() {
        List<Account> list = jdbc.sql("SELECT * FROM accounts ORDER BY account_id").query(Account.class).list();
        list.forEach(a -> cache.put(a.accountId(), a));
        return list;
    }

    public Account findById(String accountId) {
        if (cache.containsKey(accountId)) {
            return cache.get(accountId);
        }
        Account account = jdbc.sql("SELECT * FROM accounts WHERE account_id = '" + accountId + "'")
                .query(Account.class)
                .single();
        cache.put(accountId, account);
        return account;
    }

    public void updateBalance(String accountId, double balance) {
        jdbc.sql("UPDATE accounts SET balance = " + balance + " WHERE account_id = '" + accountId + "'").update();
        cache.put(accountId, new Account(accountId, balance));
    }
}

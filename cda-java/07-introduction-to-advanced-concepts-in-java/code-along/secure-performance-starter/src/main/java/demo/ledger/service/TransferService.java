package demo.ledger.service;

import demo.ledger.repository.AccountRepository;
import demo.ledger.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Lesson 1's atomic transfer, now written the Spring way. {@code @Transactional}
 * does the setAutoCommit(false) / commit / rollback for us: if any step throws,
 * every write in this method is rolled back. The service never touches SQL —
 * it goes through the repositories like every other caller.
 */
@Service
public class TransferService {

    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    public TransferService(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    @Transactional
    public void transfer(String fromAccountId, String toAccountId, BigDecimal amount, String memo) {
        if (Objects.equals(fromAccountId, toAccountId)) {
            throw new IllegalArgumentException("Choose two different accounts");
        }
        accounts.debit(fromAccountId, amount);
        accounts.credit(toAccountId, amount);
        transactions.record(fromAccountId, toAccountId, amount, memo);
    }
}

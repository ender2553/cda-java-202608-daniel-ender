package demo.ledger.service;

import demo.ledger.domain.Account;
import demo.ledger.repository.AccountRepository;
import demo.ledger.repository.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class TransferService {

    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private int transferCount;

    public TransferService(AccountRepository accounts, TransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    public void transfer(String fromAccountId, String toAccountId, double amount, String memo) {
        Account source = accounts.findById(fromAccountId);
        Account target = accounts.findById(toAccountId);
        if (source.balance() >= amount) {
            accounts.updateBalance(fromAccountId, source.balance() - amount);
            accounts.updateBalance(toAccountId, target.balance() + amount);
            transactions.record(fromAccountId, toAccountId, amount, memo);
            transferCount++;
        }
    }

    public int getTransferCount() {
        return transferCount;
    }
}

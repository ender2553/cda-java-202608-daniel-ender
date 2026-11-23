package com.rti.legacy.ledger;

import com.rti.legacy.accounts.Account;
import com.rti.legacy.accounts.AccountRepository;
import com.rti.legacy.accounts.AccountService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Records ledger postings and adjusts the associated account balance.
 */
@Service
public class LedgerService {

    private final AccountService accounts;
    private final AccountRepository repo;
    private final Map<String, List<LedgerEntry>> entries = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    public LedgerService(AccountService accounts, AccountRepository repo) {
        this.accounts = accounts;
        this.repo = repo;
    }

    public LedgerEntry post(String accountId, long delta, String memo) {
        Account account = repo.find(accountId);
        long updated = account.balance + delta;
        account.balance = updated;
        repo.save(account);

        LedgerEntry entry = new LedgerEntry("L-" + seq.getAndIncrement(), accountId, delta, memo);
        entries.computeIfAbsent(accountId, key -> new ArrayList<>()).add(entry);
        return entry;
    }

    public List<LedgerEntry> history(String accountId) {
        return entries.getOrDefault(accountId, new ArrayList<>());
    }
}

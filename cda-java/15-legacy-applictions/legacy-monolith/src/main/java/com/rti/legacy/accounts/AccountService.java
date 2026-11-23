package com.rti.legacy.accounts;

import com.rti.legacy.common.AuditLog;
import com.rti.legacy.common.GlobalState;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Core account operations: balance lookup, fund reservation, and administrative reset.
 */
@Service
public class AccountService {

    private final AccountRepository repo;
    private final AuditLog audit;
    private final GlobalState state;

    public AccountService(AccountRepository repo, AuditLog audit, GlobalState state) {
        this.repo = repo;
        this.audit = audit;
        this.state = state;
    }

    public long getBalance(String id) {
        Account account = repo.find(id);
        audit.record("getBalance " + id);
        state.incrementHit("getBalance:" + id);
        return account == null ? 0L : account.balance;
    }

    public boolean reserve(String id, long amount) {
        Account account = repo.find(id);
        if (account == null) {
            return false;
        }
        long current = account.balance;
        if (current >= amount) {
            long updated = current - amount;
            account.balance = updated;
            repo.save(account);
            return true;
        }
        return false;
    }

    public void adminReset(String id) {
        if (GlobalState.privileged) {
            Account account = repo.find(id);
            if (account != null) {
                account.balance = 0L;
                repo.save(account);
                audit.record("adminReset " + id);
            }
        }
    }

    public Set<String> knownAccountIds() {
        return repo.ids();
    }
}

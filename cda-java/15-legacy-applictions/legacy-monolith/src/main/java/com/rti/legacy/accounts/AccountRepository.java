package com.rti.legacy.accounts;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory account store, seeded at startup.
 */
@Repository
public class AccountRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        save(new Account("A-100", "Ada Lovelace", 500_00L));
        save(new Account("A-200", "Alan Turing", 1_000_00L));
        save(new Account("A-300", "Grace Hopper", 250_00L));
    }

    public Account find(String id) {
        return accounts.get(id);
    }

    public void save(Account account) {
        accounts.put(account.id, account);
    }

    public Set<String> ids() {
        return accounts.keySet();
    }

    public Map<String, Account> all() {
        return accounts;
    }
}

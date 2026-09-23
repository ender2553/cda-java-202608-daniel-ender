package com.cyberdev.pos.day3.inmemory;

import com.cyberdev.pos.day3.CashierAccount;
import com.cyberdev.pos.day3.CashierAccountRepository;
import com.cyberdev.pos.exception.DataAccessException;
import com.cyberdev.pos.exception.ValidationException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GIVEN/PROVIDED IMPLEMENTATION -- not a graded TODO.
 *
 * In-memory stand-in for CashierAccountRepository, same role as
 * InMemoryMerchantRepository/InMemoryTransactionRepository in Day 2. Lets CashierAuthService
 * and Main run with zero database setup. JdbcCashierAccountRepository (day3/jdbc) satisfies
 * the same interface against a real database.
 */
public final class InMemoryCashierAccountRepository implements CashierAccountRepository {

    private final Map<String, CashierAccount> byCashierId = new ConcurrentHashMap<>();

    @Override
    public void save(CashierAccount account) {
        if (account == null) {
            throw new ValidationException("account must not be null");
        }
        try {
            byCashierId.put(account.getCashierId(), account);
        } catch (RuntimeException e) {
            throw new DataAccessException("Failed to save cashier account " + account.getCashierId(), e);
        }
    }

    @Override
    public Optional<CashierAccount> findByCashierId(String cashierId) {
        if (cashierId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byCashierId.get(cashierId));
    }
}

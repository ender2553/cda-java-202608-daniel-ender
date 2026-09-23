package com.cyberdev.pos.day3;

import java.util.Optional;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Mirrors Day 2's MerchantRepository shape exactly: one lookup method, implemented by both
 * an in-memory stand-in and a real JdbcTemplate-backed implementation.
 */
public interface CashierAccountRepository {
    Optional<CashierAccount> findByCashierId(String cashierId);

    void save(CashierAccount account);
}

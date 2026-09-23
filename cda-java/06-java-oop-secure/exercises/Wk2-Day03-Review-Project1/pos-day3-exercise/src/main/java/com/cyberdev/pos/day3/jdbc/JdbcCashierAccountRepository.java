package com.cyberdev.pos.day3.jdbc;

import com.cyberdev.pos.day3.CashierAccount;
import com.cyberdev.pos.day3.CashierAccountRepository;
import com.cyberdev.pos.day3.jdbc.mapper.CashierAccountRowMapper;
import com.cyberdev.pos.exception.DataAccessException;
import com.cyberdev.pos.exception.ValidationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

/**
 * GIVEN/PROVIDED IMPLEMENTATION -- not a graded TODO.
 *
 * CashierAccountRepository backed by a real PostgreSQL "cashier_account" table via Spring's
 * JdbcTemplate (see schema/day3_schema.sql), mirroring JdbcMerchantRepository exactly:
 * constructor-injected JdbcTemplate, "?" parameterized SQL throughout, failures wrapped in
 * this project's own DataAccessException.
 */
public final class JdbcCashierAccountRepository implements CashierAccountRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CashierAccountRowMapper rowMapper = new CashierAccountRowMapper();

    public JdbcCashierAccountRepository(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate == null) {
            throw new ValidationException("jdbcTemplate must not be null");
        }
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(CashierAccount account) {
        if (account == null) {
            throw new ValidationException("account must not be null");
        }
        try {
            jdbcTemplate.update(
                    "INSERT INTO cashier_account (cashier_id, salt, pin_hash) VALUES (?, ?, ?) "
                            + "ON CONFLICT (cashier_id) DO UPDATE SET salt = EXCLUDED.salt, pin_hash = EXCLUDED.pin_hash",
                    account.getCashierId(), account.getSalt(), account.getPinHash());
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to save cashier account " + account.getCashierId(), e);
        }
    }

    @Override
    public Optional<CashierAccount> findByCashierId(String cashierId) {
        if (cashierId == null) {
            return Optional.empty();
        }
        try {
            List<CashierAccount> results = jdbcTemplate.query(
                    "SELECT cashier_id, salt, pin_hash FROM cashier_account WHERE cashier_id = ?",
                    rowMapper, cashierId);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to query cashier account " + cashierId, e);
        }
    }
}

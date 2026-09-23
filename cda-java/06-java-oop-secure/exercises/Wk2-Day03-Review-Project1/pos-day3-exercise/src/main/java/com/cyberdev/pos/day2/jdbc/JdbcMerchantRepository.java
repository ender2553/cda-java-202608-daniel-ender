package com.cyberdev.pos.day2.jdbc;

import com.cyberdev.pos.day2.Merchant;
import com.cyberdev.pos.day2.MerchantRepository;
import com.cyberdev.pos.day2.jdbc.mapper.MerchantRowMapper;
import com.cyberdev.pos.exception.DataAccessException;
import com.cyberdev.pos.exception.ValidationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

/**
 * MerchantRepository backed by a real PostgreSQL "merchant" table via Spring's JdbcTemplate
 * (see schema/schema.sql), replacing the old SimulatedDatabase-backed
 * SimulatedMerchantRepository.
 */
public final class JdbcMerchantRepository implements MerchantRepository {

    private final JdbcTemplate jdbcTemplate;
    private final MerchantRowMapper rowMapper = new MerchantRowMapper();

    public JdbcMerchantRepository(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate == null) {
            throw new ValidationException("jdbcTemplate must not be null");
        }
        this.jdbcTemplate = jdbcTemplate;
    }

    public void register(Merchant merchant) {
        if (merchant == null) {
            throw new ValidationException("merchant must not be null");
        }
        try {
            jdbcTemplate.update(
                    "INSERT INTO merchant (merchant_id, display_name) VALUES (?, ?)",
                    merchant.getMerchantId(), merchant.getDisplayName());
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to register merchant " + merchant.getMerchantId(), e);
        }
    }

    // INSTRUCTOR NOTE [POS2-4]: parameterized lookup, same pattern as
    // JdbcTransactionRepository.findById -- the merchantId travels as a "?" bound
    // parameter, never concatenated into the SQL text.
    @Override
    public Optional<Merchant> findByMerchantId(String merchantId) {
        if (merchantId == null) {
            return Optional.empty();
        }
        try {
            List<Merchant> results = jdbcTemplate.query(
                    "SELECT merchant_id, display_name FROM merchant WHERE merchant_id = ?",
                    rowMapper, merchantId);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to query merchant " + merchantId, e);
        }
    }
}

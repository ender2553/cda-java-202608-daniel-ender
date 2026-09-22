package com.cyberdev.pos.day2.jdbc;

import com.cyberdev.pos.day2.Merchant;
import com.cyberdev.pos.day2.MerchantRepository;
import com.cyberdev.pos.day2.jdbc.mapper.MerchantRowMapper;
import com.cyberdev.pos.exception.DataAccessException;
import com.cyberdev.pos.exception.ValidationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

/**
 * MerchantRepository backed by a real PostgreSQL "merchant" table via Spring's JdbcTemplate
 * (see schema/schema.sql).
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

    // GIVEN: used by the tests to seed a merchant row before exercising POS2-4's TODO below.
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

    // TODO [POS2-4]: Look up a merchant by merchantId using a "?" PARAMETERIZED
    // SELECT ... WHERE merchant_id = ? (see schema/schema.sql). Return Optional.empty() if
    // not found or if merchantId is null. Map results with jdbcTemplate.query(sql,
    // rowMapper, merchantId). Wrap any org.springframework.dao.DataAccessException in this
    // project's own DataAccessException.
    @Override
    public Optional<Merchant> findByMerchantId(String merchantId) {
        throw new UnsupportedOperationException("TODO [POS2-4]: parameterized findByMerchantId lookup via rowMapper, wrap failures in DataAccessException");
    }
}

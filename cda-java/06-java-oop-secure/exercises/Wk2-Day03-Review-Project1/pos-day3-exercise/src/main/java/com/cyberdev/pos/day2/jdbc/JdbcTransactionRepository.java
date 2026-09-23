package com.cyberdev.pos.day2.jdbc;

import com.cyberdev.pos.day2.TransactionRecord;
import com.cyberdev.pos.day2.TransactionRepository;
import com.cyberdev.pos.day2.jdbc.mapper.TransactionRecordRowMapper;
import com.cyberdev.pos.exception.DataAccessException;
import com.cyberdev.pos.exception.ValidationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * TransactionRepository backed by a real PostgreSQL "transaction" table via Spring's
 * JdbcTemplate (see schema/schema.sql), replacing the old SimulatedDatabase-backed
 * SimulatedTransactionRepository. JdbcTemplate is used as a plain library here -- no Spring
 * container, no @Autowired -- constructor-injected exactly like every other collaborator in
 * this series.
 */
public final class JdbcTransactionRepository implements TransactionRepository {

    private final JdbcTemplate jdbcTemplate;
    private final TransactionRecordRowMapper rowMapper = new TransactionRecordRowMapper();

    public JdbcTransactionRepository(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate == null) {
            throw new ValidationException("jdbcTemplate must not be null");
        }
        this.jdbcTemplate = jdbcTemplate;
    }

    // NOTE [POS2-1]: save() must go through JdbcTemplate.update(sql, params...)
    // with a "?" PARAMETERIZED insert -- never a hand-built/concatenated SQL string -- so
    // there is nowhere for untrusted data (e.g. a memo containing a quote) to be interpreted
    // as query syntax. Common mistake: building a debug/logging string via concatenation is
    // fine, but some students try to "validate" the memo by string-matching for quotes/
    // semicolons instead of just using the parameterized API as given -- blacklisting
    // characters is not the lesson and is called out as a near-miss in manual review.
    // Any org.springframework.dao.DataAccessException Spring throws (wrapping the underlying
    // SQLException) is re-wrapped in this project's own com.cyberdev.pos.exception.DataAccessException
    // so callers only ever need to know about POS-specific exception types.
    @Override
    public void save(TransactionRecord record) {
        if (record == null) {
            throw new ValidationException("record must not be null");
        }
        try {
            jdbcTemplate.update(
                    "INSERT INTO transaction (transaction_id, merchant_id, amount, memo, occurred_at) "
                            + "VALUES (?, ?, ?, ?, ?)",
                    record.getTransactionId(), record.getMerchantId(), record.getAmount(),
                    record.getMemo(), Timestamp.from(record.getTimestamp()));
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to save transaction " + record.getTransactionId(), e);
        }
    }

    // NOTE [POS2-2]: findById uses a "?" PARAMETERIZED WHERE clause, not a
    // spliced-in query string. This is the safe pattern POS2-3's fix should mirror.
    @Override
    public Optional<TransactionRecord> findById(String transactionId) {
        if (transactionId == null) {
            return Optional.empty();
        }
        try {
            List<TransactionRecord> results = jdbcTemplate.query(
                    "SELECT transaction_id, merchant_id, amount, memo, occurred_at "
                            + "FROM transaction WHERE transaction_id = ?",
                    rowMapper, transactionId);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to query transaction " + transactionId, e);
        }
    }

    // NOTE [POS2-3]: THIS IS THE FIXED, SAFE VERSION. The vulnerable starter code
    // (shipped to students) instead builds the SQL text itself by string concatenation --
    // "...WHERE memo LIKE '%" + keyword + "%'" -- and runs it through
    // JdbcTemplate.query(String sql, RowMapper<T>) (no bind parameters at all). An
    // attacker-supplied keyword like "' OR '1'='1" then closes the quoted literal early and
    // widens the WHERE clause to match every row -- a classic SQL injection, now against real
    // Postgres SQL syntax instead of a fake engine's stand-in. The fix is simply to route the
    // keyword through JdbcTemplate.query(String sql, RowMapper<T>, Object... args) with a "?"
    // placeholder, so the keyword always travels as a bound parameter and is never
    // interpreted as SQL syntax. Common near-miss: a student "fixes" this by blacklisting
    // characters like quotes or the literal substring "OR 1=1" before concatenating -- that
    // still concatenates, is trivially bypassed by a slightly different payload, and should
    // not receive full credit even though it may pass a narrow test written only against that
    // exact payload string.
    @Override
    public List<TransactionRecord> searchByMemo(String keyword) {
        if (keyword == null) {
            return List.of();
        }
        try {
            return jdbcTemplate.query(
                    "SELECT transaction_id, merchant_id, amount, memo, occurred_at "
                            + "FROM transaction WHERE memo LIKE ?",
                    rowMapper, "%" + keyword + "%");
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to search transactions by memo", e);
        }
    }
}

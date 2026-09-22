package com.cyberdev.pos.day2.jdbc;

import com.cyberdev.pos.day2.TransactionRecord;
import com.cyberdev.pos.day2.TransactionRepository;
import com.cyberdev.pos.day2.jdbc.mapper.TransactionRecordRowMapper;
import com.cyberdev.pos.exception.DataAccessException;
import com.cyberdev.pos.exception.ValidationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

/**
 * TransactionRepository backed by a real PostgreSQL "transaction" table via Spring's
 * JdbcTemplate (see schema/schema.sql). JdbcTemplate is used as a plain library here -- no
 * Spring container, no @Autowired -- constructor-injected exactly like every other
 * collaborator in this series.
 */
public final class JdbcTransactionRepository implements TransactionRepository {

    private final JdbcTemplate jdbcTemplate;
    // GIVEN: the row-to-object mapping is delegated to TransactionRecordRowMapper (POS2-8),
    // which you implement separately. Your TODOs below should call jdbcTemplate.query(sql,
    // rowMapper, ...) rather than mapping columns by hand.
    private final TransactionRecordRowMapper rowMapper = new TransactionRecordRowMapper();

    public JdbcTransactionRepository(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate == null) {
            throw new ValidationException("jdbcTemplate must not be null");
        }
        this.jdbcTemplate = jdbcTemplate;
    }

    // TODO [POS2-1]: Save the record using jdbcTemplate.update(sql, params...) with a "?"
    // PARAMETERIZED insert into the "transaction" table (columns: transaction_id,
    // merchant_id, amount, memo, occurred_at -- see schema/schema.sql), never by building a
    // SQL string via concatenation. occurred_at is a TIMESTAMPTZ column; pass
    // java.sql.Timestamp.from(record.getTimestamp()), since JdbcTemplate does not
    // auto-convert an Instant. Wrap any org.springframework.dao.DataAccessException in this
    // project's own com.cyberdev.pos.exception.DataAccessException rather than letting it
    // leak out directly.
    @Override
    public void save(TransactionRecord record) {
        String sql = "INSERT INTO transaction "
                + "(transaction_id, merchant_id, amount, memo, occurred_at) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            jdbcTemplate.update(
                    sql,
                    record.getTransactionId(),
                    record.getMerchantId(),
                    record.getAmount(),
                    record.getMemo(),
                    java.sql.Timestamp.from(record.getTimestamp())
            );
        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to save transaction", e);
        }
    }

    // TODO [POS2-2]: Look up a record by transactionId with a "?" PARAMETERIZED
    // SELECT ... WHERE transaction_id = ? (see schema/schema.sql for column names). Return
    // Optional.empty() if not found or if transactionId is null. Map results with
    // jdbcTemplate.query(sql, rowMapper, transactionId). Wrap any
    // org.springframework.dao.DataAccessException in this project's DataAccessException.
    @Override
    public Optional<TransactionRecord> findById(String transactionId) {
        if (transactionId == null) {
            return Optional.empty();
        }

        String sql = "SELECT transaction_id, merchant_id, amount, memo, occurred_at "
                + "FROM transaction WHERE transaction_id = ?";

        try {
            List<TransactionRecord> results =
                    jdbcTemplate.query(sql, rowMapper, transactionId);

            if (results.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(results.get(0));

        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to find transaction by ID", e);
        }
    }

    // TODO [POS2-3]: THIS METHOD IS VULNERABLE TO SQL INJECTION AS SHIPPED.
    //
    // It builds the SQL text itself by string concatenation and runs it through
    // jdbcTemplate.query(String sql, RowMapper<T>) with NO bind parameters at all. A keyword
    // like "nonexistent' OR '1'='1" will close the quoted string literal early and match
    // every row instead of just memos actually containing that text. Your job: rewrite this
    // method to use jdbcTemplate.query(String sql, RowMapper<T>, Object... args) with a "?"
    // placeholder in the SQL text and the LIKE pattern passed as a bound parameter, e.g.
    // jdbcTemplate.query("SELECT ... WHERE memo LIKE ?", rowMapper, "%" + keyword + "%") --
    // building the "%...%" wildcard string in Java is fine; concatenating it into the SQL
    // TEXT is not. Do NOT "fix" this by blacklisting characters like quotes before
    // concatenating; that is not a real fix and will not earn credit.
    @Override
    public List<TransactionRecord> searchByMemo(String keyword) {
        if (keyword == null) {
            return List.of();
        }

        try {
            String sql = "SELECT transaction_id, merchant_id, amount, memo, occurred_at "
                    + "FROM transaction WHERE memo LIKE ?";

            return jdbcTemplate.query(
                    sql,
                    rowMapper,
                    "%" + keyword + "%"
            );

        } catch (org.springframework.dao.DataAccessException e) {
            throw new DataAccessException("Failed to search transactions by memo", e);
        }
    }
}

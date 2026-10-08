package demo.sqli.repository;

import demo.sqli.model.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Reads products for the search box.
 *
 * <p><b>THIS IS THE VULNERABILITY.</b> The search term is glued straight into
 * the SQL string with {@code +}. Whatever the user typed becomes part of the
 * command, so the search box can be used to read other tables (UNION) or to run
 * extra statements that change or destroy data (stacked {@code ; UPDATE/DELETE/DROP}).
 *
 * <p>Because this runs through {@code JdbcTemplate.query(String, RowMapper)} —
 * a plain {@code Statement}, with no bind parameters — PostgreSQL happily runs
 * everything in the string, including a second statement after a semicolon.
 *
 * <p>{@link #searchSecure} is the fix, so the demo can flip to it live (the
 * "Secure mode" switch on the page) and re-run the exact same payloads. Never
 * ship code like {@link #search}.
 */
@Repository
public class ProductRepository {

    private static final RowMapper<Product> MAPPER = (rs, rowNum) ->
            new Product(rs.getString(1), rs.getString(2), rs.getString(3));

    private final JdbcTemplate jdbc;

    public ProductRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ---- VULNERABLE path -------------------------------------------------

    /** The exact SQL string {@link #search} runs — concatenated, so it IS what executes. */
    public String buildSearchSql(String term) {
        return "SELECT name, category, CAST(price AS TEXT) AS price "
                + "FROM products WHERE name ILIKE '%" + term + "%'";
    }

    public List<Product> search(String term) {
        // The flaw, in one line: the term is concatenated into the SQL text.
        return jdbc.query(buildSearchSql(term), MAPPER);
    }

    // ---- SECURE path (the fix) ------------------------------------------

    /** The fixed query — a constant, with a {@code ?} placeholder, never the term. */
    private static final String SECURE_SQL =
            "SELECT name, category, CAST(price AS TEXT) AS price FROM products WHERE name ILIKE ?";

    /** What the secure query sends: the fixed template plus the value, bound apart. */
    public String describeSecureSql(String term) {
        return SECURE_SQL + "\n   [bound] ? = '%" + term + "%'";
    }

    public List<Product> searchSecure(String term) {
        // The command is fixed; the value is bound separately. The term can
        // never become SQL, so every injection payload is just text to match.
        return jdbc.query(SECURE_SQL, MAPPER, "%" + term + "%");
    }
}

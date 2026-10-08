package demo.sqli.repository;

import demo.sqli.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Looks up users for login.
 *
 * <p>Two deliberate problems, for the lesson:
 * <ol>
 *   <li>The password is compared in <b>clear text</b> — no hashing.</li>
 *   <li>Username and password are concatenated into the SQL (same flaw as
 *       {@link ProductRepository}), so login is injectable too.</li>
 * </ol>
 *
 * <p>The in-class demo attacks the search box; login is here so that the
 * credentials the search box steals actually mean something (you can log in
 * with them). The Instructor Guide notes login is injectable as well.
 */
@Repository
public class UserRepository {

    private static final RowMapper<User> MAPPER = (rs, rowNum) -> new User(
            rs.getString("username"), rs.getString("password"),
            rs.getString("role"), rs.getString("email"));

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Returns the matching user, or {@code null} — clear-text password compare. */
    public User findByCredentials(String username, String password) {
        String sql = "SELECT username, password, role, email FROM users "
                + "WHERE username = '" + username + "' AND password = '" + password + "'";
        List<User> found = jdbc.query(sql, MAPPER);
        return found.isEmpty() ? null : found.get(0);
    }
}

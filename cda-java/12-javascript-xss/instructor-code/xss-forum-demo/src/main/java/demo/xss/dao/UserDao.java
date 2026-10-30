package demo.xss.dao;

import demo.xss.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Reads and writes the users table. SQL uses ? parameters, so there is no
 * SQL injection here — this demo is about XSS.
 */
@Repository
public class UserDao {

    private static final RowMapper<User> MAPPER = (rs, rowNum) ->
            new User(rs.getInt("user_id"), rs.getString("user_name"));

    private final JdbcTemplate jdbc;

    public UserDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Exact, case-sensitive match ("alice" is not "Alice"). */
    public Optional<User> findByName(String userName) {
        List<User> found = jdbc.query(
                "SELECT user_id, user_name FROM users WHERE user_name = ?",
                MAPPER, userName);
        return found.stream().findFirst();
    }

    /** Inserts a user and returns the saved row (with its new id). */
    public User insert(String userName) {
        return jdbc.queryForObject(
                "INSERT INTO users (user_name) VALUES (?) RETURNING user_id, user_name",
                MAPPER, userName);
    }
}

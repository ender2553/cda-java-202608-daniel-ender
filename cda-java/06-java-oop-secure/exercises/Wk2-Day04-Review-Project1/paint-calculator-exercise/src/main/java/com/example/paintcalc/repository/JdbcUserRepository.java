package com.example.paintcalc.repository;

import com.example.paintcalc.domain.UserAccount;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** JDBC adapter demonstrating coding to the user repository interface. */
@Repository
@Profile("jdbc")
public class JdbcUserRepository implements UserRepository {
    private final JdbcTemplate jdbc;

    public JdbcUserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public UserAccount save(UserAccount user) {
        // TODO 5: implement insert/update with JdbcTemplate and ? placeholders.

        if (user.id() == 0) {
            Long id = jdbc.queryForObject(
                    """
                    INSERT INTO paint_user (username, password_hash)
                    VALUES (?, ?)
                    RETURNING id
                    """,
                    Long.class,
                    user.username(),
                    user.passwordHash()
            );

            return new UserAccount(id, user.username(), user.passwordHash());
        }

        jdbc.update(
                """
                UPDATE paint_user
                SET username = ?, password_hash = ?
                WHERE id = ?
                """,
                user.username(),
                user.passwordHash(),
                user.id()
        );

        return user;
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        // TODO 6: query by username using a parameterized JdbcTemplate query.

        return jdbc.query(
                """
                SELECT id, username, password_hash
                FROM paint_user
                WHERE username = ?
                """,
                (rs, rowNum) -> new UserAccount(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password_hash")
                ),
                username
        ).stream().findFirst();
    }
}


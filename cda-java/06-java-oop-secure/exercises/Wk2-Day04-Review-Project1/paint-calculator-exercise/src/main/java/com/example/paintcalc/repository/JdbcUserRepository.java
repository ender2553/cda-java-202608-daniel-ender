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
        throw new UnsupportedOperationException("TODO: implement JdbcUserRepository.save");
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        // TODO 6: query by username using a parameterized JdbcTemplate query.
        return Optional.empty();
    }
}

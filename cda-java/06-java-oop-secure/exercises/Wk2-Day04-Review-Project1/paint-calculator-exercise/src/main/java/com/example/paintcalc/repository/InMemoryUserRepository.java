package com.example.paintcalc.repository;

import com.example.paintcalc.domain.UserAccount;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@Profile("memory")
public class InMemoryUserRepository implements UserRepository {
    private final Map<String, UserAccount> users = new HashMap<>();
    private final AtomicLong ids = new AtomicLong(1);

    @Override
    public synchronized UserAccount save(UserAccount user) {
        UserAccount copy = new UserAccount(
                user.id() == 0 ? ids.getAndIncrement() : user.id(),
                user.username(),
                user.passwordHash()
        );
        users.put(copy.username(), copy);
        return copy;
    }

    @Override
    public synchronized Optional<UserAccount> findByUsername(String username) {
        return Optional.ofNullable(users.get(username.toLowerCase()));
    }
}

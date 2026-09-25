package com.example.rpg.repository;

import com.example.rpg.domain.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Reference implementation for fast local demos and unit tests.
 */
@Repository
@Profile("memory")
class InMemoryUserRepository implements UserRepository {
    private final Map<String, UserAccount> users = new HashMap<>();
    private final AtomicLong ids = new AtomicLong(1);

    public synchronized UserAccount save(UserAccount u) {
        UserAccount copy = new UserAccount(u.id() == 0 ? ids.getAndIncrement() : u.id(), u.username(), u.passwordHash());
        users.put(copy.username(), copy);
        return copy;
    }

    public synchronized Optional<UserAccount> findByUsername(String n) {
        return Optional.ofNullable(users.get(n));
    }
}

/**
 * Stores snapshots, not live PlayerCharacter objects, preserving the repository boundary.
 */
@Repository
@Profile("memory")
class InMemoryCharacterRepository implements CharacterRepository {
    private final Map<Long, SaveSnapshot> saves = new HashMap<>();

    public synchronized void save(SaveSnapshot s) {
        saves.put(s.userId(), new SaveSnapshot(s.userId(), s.characterName(), s.level(), s.experience(), s.health(), s.maxHealth(), s.gold(), s.location(), s.inventory(), s.weapon(), s.armor()));
    }

    public synchronized Optional<SaveSnapshot> findByUserId(long id) {
        return Optional.ofNullable(saves.get(id));
    }
}

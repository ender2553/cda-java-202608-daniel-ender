package com.cyberdev.dndrpg.repository.inmemory;

import com.cyberdev.dndrpg.model.Player;
import com.cyberdev.dndrpg.repository.PlayerRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PROVIDED IMPLEMENTATION -- an in-memory stand-in for PlayerRepository
 * so the game is playable without any database setup. A JDBC-backed
 * implementation would satisfy the exact same interface (see the
 * stretch-goal notes in USER_STORIES.md) with zero changes anywhere
 * else in the application.
 */
@Repository
@Profile("!jdbc")
public final class InMemoryPlayerRepository implements PlayerRepository {
    private final Map<UUID, Player> byId = new ConcurrentHashMap<>();

    @Override
    public void save(Player player) {
        byId.put(player.getId(), player);
    }

    @Override
    public Optional<Player> findByUsername(String username) {
        return byId.values().stream()
                .filter(p -> p.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    @Override
    public Optional<Player> findById(UUID id) {
        return Optional.ofNullable(byId.get(id));
    }
}

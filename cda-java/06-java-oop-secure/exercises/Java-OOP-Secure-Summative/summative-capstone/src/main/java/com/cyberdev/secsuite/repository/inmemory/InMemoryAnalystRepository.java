package com.cyberdev.secsuite.repository.inmemory;

import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import com.cyberdev.secsuite.exception.DataAccessException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Analyst;
import com.cyberdev.secsuite.repository.AnalystRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * GIVEN/PROVIDED IMPLEMENTATION -- not a graded TODO.
 *
 * In-memory stand-in so the whole application runs with zero database setup. Emulates the
 * PRIMARY KEY / UNIQUE constraints of the real table (a collision throws DataAccessException,
 * just as a real INSERT would), but deliberately does NOT emulate FOREIGN KEY constraints --
 * which is one reason services must check references at the application layer themselves
 * (SEC-4, SEC-10) instead of relying on "the database will catch it".
 */
@Repository
@Profile("inmemory")
public class InMemoryAnalystRepository implements AnalystRepository {

    private final Map<Long, Analyst> byId = new LinkedHashMap<>();

    @Override
    public synchronized Analyst save(Analyst analyst) {
        if (analyst == null) {
            throw new ValidationException("analyst must not be null");
        }
        Analyst persisted = analyst.getId() <= 0 ? analyst.withId(InMemoryIds.nextId(byId)) : analyst;
        if (byId.containsKey(persisted.getId())) {
            throw new DataAccessException("Duplicate analyst id " + persisted.getId(), null);
        }
        if (findByUsername(persisted.getUsername()).isPresent()) {
            throw new DataAccessException("Duplicate analyst username " + persisted.getUsername(), null);
        }
        byId.put(persisted.getId(), persisted);
        return persisted;
    }

    @Override
    public synchronized Optional<Analyst> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return byId.values().stream().filter(a -> a.getUsername().equals(username)).findFirst();
    }

    @Override
    public synchronized Optional<Analyst> findById(Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
    }
}

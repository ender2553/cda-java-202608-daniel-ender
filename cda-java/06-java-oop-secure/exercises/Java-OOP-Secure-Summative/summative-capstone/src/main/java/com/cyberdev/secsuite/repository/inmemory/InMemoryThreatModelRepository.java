package com.cyberdev.secsuite.repository.inmemory;

import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import com.cyberdev.secsuite.exception.DataAccessException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.ThreatModel;
import com.cyberdev.secsuite.repository.ThreatModelRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
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
public class InMemoryThreatModelRepository implements ThreatModelRepository {

    private final Map<Long, ThreatModel> byId = new LinkedHashMap<>();

    @Override
    public synchronized ThreatModel save(ThreatModel threatModel) {
        if (threatModel == null) {
            throw new ValidationException("threatModel must not be null");
        }
        ThreatModel persisted = threatModel.getId() <= 0
                ? threatModel.withId(InMemoryIds.nextId(byId))
                : threatModel;
        if (byId.containsKey(persisted.getId())) {
            throw new DataAccessException("Duplicate threat model id " + persisted.getId(), null);
        }
        byId.put(persisted.getId(), persisted);
        return persisted;
    }

    @Override
    public synchronized Optional<ThreatModel> findById(Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
    }

    @Override
    public synchronized List<ThreatModel> findAll() {
        return byId.values().stream().sorted(Comparator.comparing(ThreatModel::getTitle)).toList();
    }

    @Override
    public synchronized List<ThreatModel> findByAssetId(Long assetId) {
        return byId.values().stream()
                .filter(t -> t.getAssetId().equals(assetId))
                .sorted(Comparator.comparing(ThreatModel::getTitle))
                .toList();
    }
}

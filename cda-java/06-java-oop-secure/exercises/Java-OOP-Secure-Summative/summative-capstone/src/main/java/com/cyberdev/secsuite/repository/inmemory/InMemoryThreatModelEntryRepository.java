package com.cyberdev.secsuite.repository.inmemory;

import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import com.cyberdev.secsuite.exception.DataAccessException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.ThreatModelEntry;
import com.cyberdev.secsuite.repository.ThreatModelEntryRepository;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * GIVEN/PROVIDED IMPLEMENTATION -- not a graded TODO.
 *
 * In-memory stand-in so the whole application runs with zero database setup. Emulates the
 * PRIMARY KEY / UNIQUE constraints of the real table (a collision throws DataAccessException,
 * just as a real INSERT would), but deliberately does NOT emulate FOREIGN KEY constraints --
 * which is one reason services must check references at the application layer themselves
 * (SEC-4, SEC-10) instead of relying on "the database will catch it".
 *
 * Ordering note: entries are sorted by the stride_category NAME (alphabetical), matching the
 * JDBC implementation's "ORDER BY stride_category" on a VARCHAR column -- not by the enum's
 * declaration order. SEC-8's coverage method must not depend on either order.
 */
@Repository
@Profile("inmemory")
public class InMemoryThreatModelEntryRepository implements ThreatModelEntryRepository {

    private final Map<Long, ThreatModelEntry> byId = new LinkedHashMap<>();

    @Override
    public synchronized ThreatModelEntry save(ThreatModelEntry entry) {
        if (entry == null) {
            throw new ValidationException("entry must not be null");
        }
        ThreatModelEntry persisted = entry.getId() <= 0 ? entry.withId(InMemoryIds.nextId(byId)) : entry;
        if (byId.containsKey(persisted.getId())) {
            throw new DataAccessException("Duplicate threat model entry id " + persisted.getId(), null);
        }
        byId.put(persisted.getId(), persisted);
        return persisted;
    }

    @Override
    public synchronized List<ThreatModelEntry> findByThreatModelId(Long threatModelId) {
        return byId.values().stream()
                .filter(e -> e.getThreatModelId().equals(threatModelId))
                .sorted(Comparator.comparing((ThreatModelEntry e) -> e.getStrideCategory().name())
                        .thenComparing(ThreatModelEntry::getDescription))
                .toList();
    }
}

package com.cyberdev.secsuite.repository.inmemory;

import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import com.cyberdev.secsuite.exception.DataAccessException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.RiskRegisterEntry;
import com.cyberdev.secsuite.repository.RiskRegisterRepository;

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
public class InMemoryRiskRegisterRepository implements RiskRegisterRepository {

    private final Map<Long, RiskRegisterEntry> byId = new LinkedHashMap<>();

    @Override
    public synchronized RiskRegisterEntry save(RiskRegisterEntry entry) {
        if (entry == null) {
            throw new ValidationException("entry must not be null");
        }
        RiskRegisterEntry persisted = entry.getId() <= 0 ? entry.withId(InMemoryIds.nextId(byId)) : entry;
        if (byId.containsKey(persisted.getId())) {
            throw new DataAccessException("Duplicate risk register id " + persisted.getId(), null);
        }
        // Emulates the partial unique index uq_risk_scan_finding: one register entry per finding.
        if (persisted.getScanFindingId() != null && findByScanFindingId(persisted.getScanFindingId()).isPresent()) {
            throw new DataAccessException("Unique violation: scan finding " + persisted.getScanFindingId()
                    + " already has a risk register entry", null);
        }
        byId.put(persisted.getId(), persisted);
        return persisted;
    }

    @Override
    public synchronized Optional<RiskRegisterEntry> findById(Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
    }

    @Override
    public synchronized List<RiskRegisterEntry> findAll() {
        return byId.values().stream()
                .sorted(Comparator.comparingInt(RiskRegisterEntry::getRiskScore).reversed()
                        .thenComparing(RiskRegisterEntry::getTitle))
                .toList();
    }

    @Override
    public synchronized Optional<RiskRegisterEntry> findByScanFindingId(Long scanFindingId) {
        if (scanFindingId == null) {
            return Optional.empty();
        }
        return byId.values().stream().filter(e -> scanFindingId.equals(e.getScanFindingId())).findFirst();
    }
}

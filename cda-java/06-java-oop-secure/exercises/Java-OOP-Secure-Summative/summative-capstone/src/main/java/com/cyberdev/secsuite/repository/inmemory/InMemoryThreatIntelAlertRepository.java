package com.cyberdev.secsuite.repository.inmemory;

import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import com.cyberdev.secsuite.exception.DataAccessException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.IndicatorType;
import com.cyberdev.secsuite.model.ThreatIntelAlert;
import com.cyberdev.secsuite.repository.ThreatIntelAlertRepository;

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
 *
 * Emulates the UNIQUE constraint on external_alert_id: saving a second alert with the same
 * feed id throws, exactly as the real INSERT would. SEC-11 must therefore check FIRST rather
 * than rely on catching this.
 */
@Repository
@Profile("inmemory")
public class InMemoryThreatIntelAlertRepository implements ThreatIntelAlertRepository {

    private static final Comparator<ThreatIntelAlert> ORDER = Comparator.comparing(ThreatIntelAlert::getExternalAlertId);

    private final Map<Long, ThreatIntelAlert> byId = new LinkedHashMap<>();

    @Override
    public synchronized ThreatIntelAlert save(ThreatIntelAlert alert) {
        if (alert == null) {
            throw new ValidationException("alert must not be null");
        }
        ThreatIntelAlert persisted = alert.getId() <= 0 ? alert.withId(InMemoryIds.nextId(byId)) : alert;
        if (byId.containsKey(persisted.getId())) {
            throw new DataAccessException("Duplicate alert id " + persisted.getId(), null);
        }
        if (findByExternalAlertId(persisted.getExternalAlertId()).isPresent()) {
            throw new DataAccessException("Unique violation on external_alert_id "
                    + persisted.getExternalAlertId(), null);
        }
        byId.put(persisted.getId(), persisted);
        return persisted;
    }

    @Override
    public synchronized Optional<ThreatIntelAlert> findByExternalAlertId(String externalAlertId) {
        if (externalAlertId == null) {
            return Optional.empty();
        }
        return byId.values().stream().filter(a -> a.getExternalAlertId().equals(externalAlertId)).findFirst();
    }

    @Override
    public synchronized List<ThreatIntelAlert> findAll() {
        return byId.values().stream().sorted(ORDER).toList();
    }

    @Override
    public synchronized List<ThreatIntelAlert> findByIndicatorType(IndicatorType indicatorType) {
        return byId.values().stream().filter(a -> a.getIndicatorType() == indicatorType).sorted(ORDER).toList();
    }

    @Override
    public synchronized long count() {
        return byId.size();
    }
}

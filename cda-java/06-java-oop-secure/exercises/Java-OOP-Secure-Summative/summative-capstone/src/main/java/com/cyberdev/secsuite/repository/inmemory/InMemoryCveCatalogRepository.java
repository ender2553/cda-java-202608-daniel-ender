package com.cyberdev.secsuite.repository.inmemory;

import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import com.cyberdev.secsuite.exception.DataAccessException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.CveCatalogEntry;
import com.cyberdev.secsuite.repository.CveCatalogRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

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
public class InMemoryCveCatalogRepository implements CveCatalogRepository {

    private final Map<String, CveCatalogEntry> byCveId = new TreeMap<>();

    @Override
    public synchronized void save(CveCatalogEntry entry) {
        if (entry == null) {
            throw new ValidationException("entry must not be null");
        }
        if (byCveId.containsKey(entry.cveId())) {
            throw new DataAccessException("Duplicate cve_id " + entry.cveId(), null);
        }
        byCveId.put(entry.cveId(), entry);
    }

    @Override
    public synchronized Optional<CveCatalogEntry> findById(String cveId) {
        return cveId == null ? Optional.empty() : Optional.ofNullable(byCveId.get(cveId));
    }

    @Override
    public synchronized List<CveCatalogEntry> findAll() {
        return List.copyOf(byCveId.values());
    }
}

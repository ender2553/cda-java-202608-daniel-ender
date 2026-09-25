package com.cyberdev.secsuite.repository.inmemory;

import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import com.cyberdev.secsuite.exception.DataAccessException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.FindingStatus;
import com.cyberdev.secsuite.model.ScanFinding;
import com.cyberdev.secsuite.repository.ScanFindingRepository;

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
 * Also emulates the partial unique index uq_scan_finding_open_asset_cve (at most one OPEN
 * finding per asset + CVE) -- the database's last line of defense behind SEC-4's check.
 */
@Repository
@Profile("inmemory")
public class InMemoryScanFindingRepository implements ScanFindingRepository {

    private static final Comparator<ScanFinding> ORDER =
            Comparator.comparing(ScanFinding::getDetectedAt).thenComparing(ScanFinding::getId);

    private final Map<Long, ScanFinding> byId = new LinkedHashMap<>();

    @Override
    public synchronized ScanFinding save(ScanFinding finding) {
        if (finding == null) {
            throw new ValidationException("finding must not be null");
        }
        ScanFinding persisted = finding.getId() <= 0 ? finding.withId(InMemoryIds.nextId(byId)) : finding;
        if (byId.containsKey(persisted.getId())) {
            throw new DataAccessException("Duplicate scan finding id " + persisted.getId(), null);
        }
        if (persisted.isOpen()
                && findOpenByAssetAndCve(persisted.getAssetId(), persisted.getCveId()).isPresent()) {
            throw new DataAccessException("Unique violation: an OPEN finding for " + persisted.getCveId()
                    + " on asset " + persisted.getAssetId() + " already exists", null);
        }
        byId.put(persisted.getId(), persisted);
        return persisted;
    }

    @Override
    public synchronized Optional<ScanFinding> findById(Long id) {
        return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
    }

    @Override
    public synchronized List<ScanFinding> findAll() {
        return byId.values().stream().sorted(ORDER).toList();
    }

    @Override
    public synchronized List<ScanFinding> findByAssetId(Long assetId) {
        return byId.values().stream().filter(f -> f.getAssetId().equals(assetId)).sorted(ORDER).toList();
    }

    @Override
    public synchronized Optional<ScanFinding> findOpenByAssetAndCve(Long assetId, String cveId) {
        return byId.values().stream()
                .filter(f -> f.isOpen() && f.getAssetId().equals(assetId) && f.getCveId().equals(cveId))
                .findFirst();
    }

    @Override
    public synchronized List<ScanFinding> findOpenByCveId(String cveId) {
        return byId.values().stream()
                .filter(f -> f.isOpen() && f.getCveId().equals(cveId))
                .sorted(ORDER)
                .toList();
    }

    @Override
    public synchronized boolean updateStatus(Long findingId, FindingStatus newStatus) {
        if (newStatus == null) {
            throw new ValidationException("newStatus must not be null");
        }
        ScanFinding existing = findingId == null ? null : byId.get(findingId);
        if (existing == null) {
            return false;
        }
        byId.put(findingId, existing.withStatus(newStatus));
        return true;
    }
}

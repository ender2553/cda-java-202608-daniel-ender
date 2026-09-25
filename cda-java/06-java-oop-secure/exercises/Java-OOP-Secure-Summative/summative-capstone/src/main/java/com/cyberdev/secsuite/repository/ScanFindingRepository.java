package com.cyberdev.secsuite.repository;

import com.cyberdev.secsuite.model.FindingStatus;
import com.cyberdev.secsuite.model.ScanFinding;

import java.util.List;
import java.util.Optional;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Persistence for scanner findings (table "scan_finding"). See AnalystRepository for the shared
 * contract; {@link #updateStatus} is the one UPDATE in the application (resolving a finding is
 * a status change, not a delete -- the history of what was found stays in the table).
 */
public interface ScanFindingRepository {

    ScanFinding save(ScanFinding finding);

    Optional<ScanFinding> findById(Long id);

    /** Every finding, ordered by detected_at then id. */
    List<ScanFinding> findAll();

    List<ScanFinding> findByAssetId(Long assetId);

    /** The OPEN finding for this asset + CVE, if any (at most one can exist -- SEC-4). */
    Optional<ScanFinding> findOpenByAssetAndCve(Long assetId, String cveId);

    /** Every OPEN finding for this CVE, across all assets (used by SEC-12 correlation). */
    List<ScanFinding> findOpenByCveId(String cveId);

    /** @return true if a row was updated, false if no finding has that id. */
    boolean updateStatus(Long findingId, FindingStatus newStatus);
}

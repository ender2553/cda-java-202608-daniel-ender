package com.cyberdev.secsuite.service;

import org.springframework.stereotype.Service;

import com.cyberdev.secsuite.exception.DuplicateFindingException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Asset;
import com.cyberdev.secsuite.model.CveCatalogEntry;
import com.cyberdev.secsuite.model.FindingStatus;
import com.cyberdev.secsuite.model.ScanFinding;
import com.cyberdev.secsuite.model.Severity;
import com.cyberdev.secsuite.repository.AssetRepository;
import com.cyberdev.secsuite.repository.CveCatalogRepository;
import com.cyberdev.secsuite.repository.ScanFindingRepository;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The "Nessus-like" vulnerability scanner module: records what a scan observed and answers
 * "what is currently open, and how bad is it?".
 *
 * INSTRUCTOR NOTE on DI discipline (recap of QuickPay POS2-5): every collaborator -- three
 * repositories and a java.time.Clock -- arrives through the constructor, typed as an interface
 * (or, for Clock, an abstract class). There is no no-arg constructor that quietly builds an
 * InMemory* repository. Injecting the Clock too is the same lesson one level further: "now" is
 * a dependency, and a test can hand in Clock.fixed(...) to make detected_at deterministic.
 */
@Service
public class ScannerService {

    private final ScanFindingRepository findingRepository;
    private final AssetRepository assetRepository;
    private final CveCatalogRepository cveCatalogRepository;
    private final Clock clock;

    public ScannerService(ScanFindingRepository findingRepository, AssetRepository assetRepository,
                          CveCatalogRepository cveCatalogRepository, Clock clock) {
        if (findingRepository == null || assetRepository == null || cveCatalogRepository == null || clock == null) {
            throw new ValidationException("ScannerService dependencies must not be null");
        }
        this.findingRepository = findingRepository;
        this.assetRepository = assetRepository;
        this.cveCatalogRepository = cveCatalogRepository;
        this.clock = clock;
    }

    // INSTRUCTOR NOTE [SEC-4]: Concept tested: constructor-injected repositories plus
    // fail-closed checks that all run BEFORE the one write, in this order:
    //   1. inputs present (assetId non-null, cveId non-blank)        -> ValidationException
    //   2. the asset EXISTS (assetRepository.findById)               -> ValidationException
    //   3. the CVE EXISTS in the catalog (cveCatalogRepository)      -> ValidationException
    //   4. no OPEN finding already exists for this asset + CVE
    //      (findingRepository.findOpenByAssetAndCve)                 -> DuplicateFindingException
    //   5. only then build the ScanFinding (id 0L, status OPEN, detectedAt = clock.instant())
    //      and return the persisted finding returned by save().
    // Why it matters: a finding recorded against an asset or CVE that does not exist is a
    // finding about nothing -- it silently corrupts every downstream count, risk and report.
    // The InMemory* repositories do NOT enforce foreign keys, so "the database will reject it"
    // is not an answer; and even with Postgres, an FK violation surfacing as a generic
    // DataAccessException is a far worse error than a clear ValidationException. A RESOLVED
    // finding for the same asset + CVE is NOT a duplicate: the vulnerability has regressed and
    // must be recorded again. Common mistakes: (1) checking for the duplicate AFTER save() --
    // too late, the bad write already happened (the exact Day 2 recordSale lesson); (2)
    // treating ANY existing finding (including RESOLVED) as a duplicate, which hides
    // regressions; (3) throwing a generic IllegalStateException/RuntimeException instead of
    // DuplicateFindingException; (4) catching the DataAccessException from the database's
    // partial unique index and calling that "the duplicate check" -- the index is defense in
    // depth, not the check.
    // SECURITY CALLOUT: this is check-then-act, so two concurrent scans could both pass step 4.
    // That race is exactly why schema.sql ALSO has the partial unique index
    // uq_scan_finding_open_asset_cve: application check for the clear error, database
    // constraint as the last line of defense.
    public ScanFinding recordFinding(Long assetId, String cveId, Integer port, String serviceName) {
        throw new UnsupportedOperationException(
                "TODO [SEC-4]: validate, create with id 0L, then return findingRepository.save(finding)");
    }

    /**
     * GIVEN -- not graded. Marks a finding RESOLVED (an UPDATE, never a DELETE: the history of
     * what was found stays in the table). Unknown id fails closed.
     */
    public void resolveFinding(Long findingId) {
        if (findingId == null) {
            throw new ValidationException("findingId must not be null");
        }
        if (!findingRepository.updateStatus(findingId, FindingStatus.RESOLVED)) {
            throw new ValidationException("Unknown finding id " + findingId);
        }
    }

    /**
     * GIVEN -- not graded, but built on SEC-2. Every OPEN finding grouped by the severity of its
     * CVE, derived with Severity.fromCvssScore via CveCatalogEntry.severity(). The returned map
     * ALWAYS contains all five severities, worst first (CRITICAL, HIGH, MEDIUM, LOW, NONE),
     * each with a possibly-empty list -- the report renders every bucket, never a missing one.
     * Within a bucket, findings keep the repository order (detected_at, then id).
     */
    public Map<Severity, List<ScanFinding>> openFindingsBySeverity() {
        Map<Severity, List<ScanFinding>> grouped = new LinkedHashMap<>();
        Severity[] all = Severity.values();
        for (int i = all.length - 1; i >= 0; i--) {
            grouped.put(all[i], new ArrayList<>());
        }
        for (ScanFinding finding : findingRepository.findAll()) {
            if (!finding.isOpen()) {
                continue;
            }
            CveCatalogEntry cve = cveCatalogRepository.findById(finding.getCveId())
                    .orElseThrow(() -> new ValidationException("Finding " + finding.getId()
                            + " references unknown CVE " + finding.getCveId()));
            grouped.get(cve.severity()).add(finding);
        }
        grouped.replaceAll((severity, list) -> List.copyOf(list));
        return grouped;
    }
}

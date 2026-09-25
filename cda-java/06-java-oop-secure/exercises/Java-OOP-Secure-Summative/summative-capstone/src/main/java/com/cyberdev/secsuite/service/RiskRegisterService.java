package com.cyberdev.secsuite.service;

import org.springframework.stereotype.Service;

import com.cyberdev.secsuite.event.CriticalRisk;
import com.cyberdev.secsuite.event.HighRisk;
import com.cyberdev.secsuite.event.LowRisk;
import com.cyberdev.secsuite.event.MediumRisk;
import com.cyberdev.secsuite.event.RiskAssessment;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Asset;
import com.cyberdev.secsuite.model.Criticality;
import com.cyberdev.secsuite.model.CveCatalogEntry;
import com.cyberdev.secsuite.model.RiskRegisterEntry;
import com.cyberdev.secsuite.model.RiskStatus;
import com.cyberdev.secsuite.model.ScanFinding;
import com.cyberdev.secsuite.model.Severity;
import com.cyberdev.secsuite.repository.AnalystRepository;
import com.cyberdev.secsuite.repository.AssetRepository;
import com.cyberdev.secsuite.repository.CveCatalogRepository;
import com.cyberdev.secsuite.repository.RiskRegisterRepository;
import com.cyberdev.secsuite.repository.ScanFindingRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The risk register module: classifies likelihood x impact into a sealed RiskAssessment band
 * (SEC-5), records register entries, and promotes OPEN scan findings into tracked risks.
 */
@Service
public class RiskRegisterService {

    private final RiskRegisterRepository riskRepository;
    private final AssetRepository assetRepository;
    private final ScanFindingRepository findingRepository;
    private final CveCatalogRepository cveCatalogRepository;
    private final AnalystRepository analystRepository;
    private final Clock clock;

    public RiskRegisterService(RiskRegisterRepository riskRepository, AssetRepository assetRepository,
                               ScanFindingRepository findingRepository, CveCatalogRepository cveCatalogRepository,
                               AnalystRepository analystRepository, Clock clock) {
        if (riskRepository == null || assetRepository == null || findingRepository == null
                || cveCatalogRepository == null || analystRepository == null || clock == null) {
            throw new ValidationException("RiskRegisterService dependencies must not be null");
        }
        this.riskRepository = riskRepository;
        this.assetRepository = assetRepository;
        this.findingRepository = findingRepository;
        this.cveCatalogRepository = cveCatalogRepository;
        this.analystRepository = analystRepository;
        this.clock = clock;
    }

    // INSTRUCTOR NOTE [SEC-5]: Concept tested: mapping a bounded integer domain onto a SEALED
    // result type. likelihood and impact must each be 1-5 (anything else ->
    // ValidationException, fail closed: no clamping 7 down to 5, no treating 0 as 1). The raw
    // score is likelihood * impact (1-25), banded as documented on RiskAssessment:
    // 1-6 LowRisk, 7-12 MediumRisk, 13-19 HighRisk, 20-25 CriticalRisk. Boundary cases the
    // tests use: (2,3)=6 Low, (2,4)=8 Medium, (3,4)=12 Medium, (3,5)=15 High, (4,4)=16 High,
    // (4,5)=20 Critical, (5,5)=25 Critical. Common mistakes: (1) likelihood + impact instead of
    // likelihood * impact; (2) off-by-one at 12/13 or 19/20; (3) returning a String/int/enum
    // instead of the sealed records, which throws away the compiler-checked exhaustiveness the
    // report relies on; (4) THROWING for a high score -- a critical risk is a normal answer,
    // not an error.
    public RiskAssessment assess(int likelihood, int impact) {
        throw new UnsupportedOperationException(
                "TODO [SEC-5]: validate 1-5 ranges and return the sealed band (Low/Medium/High/Critical) for likelihood * impact");
    }

    // INSTRUCTOR NOTE [SEC-5]: Concept tested: EVERY band gets persisted -- a LowRisk is still
    // a tracked risk; the RiskAssessment is used for classification (and here, to derive the
    // due date through an exhaustive switch), never as a filter on what gets stored. Steps:
    // assess(likelihood, impact) first (which validates both ranges); fail closed if the asset
    // does not exist, if scanFindingId is non-null but the finding does not exist OR belongs
    // to a DIFFERENT asset, or if ownerAnalystId is non-null but the analyst does not exist;
    // then build the entry with id 0L, riskScore = assessment.score(), status OPEN, createdAt
    // and a due date of today + remediationWindowDays(assessment), and return the persisted
    // entry returned by save(). Common mistakes:
    // (1) `if (assessment instanceof LowRisk) return null;` -- silently dropping low risks
    // from the register; (2) storing likelihood + impact, or recomputing the score differently
    // from assess() (the RiskRegisterEntry constructor and the database CHECK constraint both
    // reject a score that is not likelihood * impact); (3) accepting a scanFindingId that
    // belongs to another asset, producing a risk whose evidence is about a different host.
    public RiskRegisterEntry createEntry(Long assetId, Long scanFindingId, String title, String description,
                                         int likelihood, int impact, Long ownerAnalystId) {
        throw new UnsupportedOperationException(
                "TODO [SEC-5]: validate, create with id 0L, then return riskRepository.save(entry)");
    }

    /**
     * GIVEN -- remediation SLA per band, as an exhaustive switch over the sealed type with NO
     * default branch (add a fifth band and this stops compiling until someone picks its SLA).
     */
    public static int remediationWindowDays(RiskAssessment assessment) {
        return switch (assessment) {
            case CriticalRisk c -> 7;
            case HighRisk h -> 30;
            case MediumRisk m -> 90;
            case LowRisk l -> 180;
        };
    }

    /**
     * GIVEN -- not graded. Promotes every OPEN scan finding that does not yet have a register
     * entry into one, so the register reflects current scanner output. Idempotent: a finding
     * that already has an entry is skipped, so re-running the pipeline (e.g. against Postgres)
     * does not duplicate risks.
     *
     * Deterministic scoring (documented, so the report is reproducible):
     *   likelihood from the CVE's derived severity:  CRITICAL 5, HIGH 4, MEDIUM 3, LOW 2, NONE 1
     *   impact from the asset's criticality:         CRITICAL 5, HIGH 4, MEDIUM 3, LOW 2
     * (Using CVSS severity as a likelihood proxy is a teaching simplification -- CVSS measures
     * severity, not probability of exploitation; a real program would fold in exploit
     * availability, exposure, and threat intel.)
     */
    public List<RiskRegisterEntry> promoteOpenFindings(Long ownerAnalystId) {
        List<RiskRegisterEntry> created = new ArrayList<>();
        for (ScanFinding finding : findingRepository.findAll()) {
            if (!finding.isOpen() || riskRepository.findByScanFindingId(finding.getId()).isPresent()) {
                continue;
            }
            Asset asset = assetRepository.findById(finding.getAssetId())
                    .orElseThrow(() -> new ValidationException("Finding " + finding.getId() + " references unknown asset"));
            CveCatalogEntry cve = cveCatalogRepository.findById(finding.getCveId())
                    .orElseThrow(() -> new ValidationException("Finding " + finding.getId() + " references unknown CVE"));
            String where = finding.getServiceName() == null ? "" : " (" + finding.getServiceName()
                    + (finding.getPort() == null ? "" : "/" + finding.getPort()) + ")";
            String title = cve.cveId() + " on " + asset.getHostname() + where;
            created.add(createEntry(asset.getId(), finding.getId(), title, cve.description(),
                    likelihoodFor(cve.severity()), impactFor(asset.getCriticality()), ownerAnalystId));
        }
        return created;
    }

    /** GIVEN -- a manually-raised register entry already recorded for this asset + title, if any. */
    public Optional<RiskRegisterEntry> findByAssetAndTitle(Long assetId, String title) {
        return riskRepository.findAll().stream()
                .filter(e -> e.getAssetId().equals(assetId) && e.getTitle().equals(title))
                .findFirst();
    }

    /**
     * GIVEN -- every register entry sorted by riskScore DESCENDING, ties broken by title, so the
     * order is identical whichever repository implementation is wired in.
     */
    public List<RiskRegisterEntry> topRisks() {
        return riskRepository.findAll().stream()
                .sorted(Comparator.comparingInt(RiskRegisterEntry::getRiskScore).reversed()
                        .thenComparing(RiskRegisterEntry::getTitle))
                .toList();
    }

    private static int likelihoodFor(Severity severity) {
        return switch (severity) {
            case CRITICAL -> 5;
            case HIGH -> 4;
            case MEDIUM -> 3;
            case LOW -> 2;
            case NONE -> 1;
        };
    }

    private static int impactFor(Criticality criticality) {
        return switch (criticality) {
            case CRITICAL -> 5;
            case HIGH -> 4;
            case MEDIUM -> 3;
            case LOW -> 2;
        };
    }
}

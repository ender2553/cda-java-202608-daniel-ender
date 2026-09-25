package com.cyberdev.secsuite.service;

import com.cyberdev.secsuite.event.CriticalRisk;
import com.cyberdev.secsuite.event.HighRisk;
import com.cyberdev.secsuite.event.LowRisk;
import com.cyberdev.secsuite.event.MediumRisk;
import com.cyberdev.secsuite.event.RiskAssessment;
import com.cyberdev.secsuite.exception.DuplicateFindingException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.FindingStatus;
import com.cyberdev.secsuite.model.RiskRegisterEntry;
import com.cyberdev.secsuite.model.RiskStatus;
import com.cyberdev.secsuite.model.ScanFinding;
import com.cyberdev.secsuite.repository.ScanFindingRepository;
import com.cyberdev.secsuite.repository.inmemory.InMemorySeedLoader;
import com.cyberdev.secsuite.support.SeededSuite;
import com.cyberdev.secsuite.testkit.GradedTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GIVEN TEST INFRASTRUCTURE -- these tests are provided and graded, not something students
 * edit.
 *
 * SEC-4 (ScannerService.recordFinding) and SEC-5 (the sealed RiskAssessment bands,
 * RiskRegisterService.assess and createEntry). Everything runs against the in-memory seed
 * (SeededSuite) with a fixed clock of 2026-09-24T09:00:00Z -- no database needed. Requires
 * SEC-1 (the seed builds Assets).
 */
public class ScannerAndRiskTests {

    private static final Long FINDING_ON_WEB_PROD = 1L;

    // ---------------------------------------------------------------
    // SEC-4: ScannerService.recordFinding
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "SEC-4", points = 1, description = "a valid scan result is saved OPEN with detectedAt from the injected clock")
    public void recordFinding_savesOpenFinding() {
        SeededSuite suite = SeededSuite.seeded();
        int before = suite.findings.findAll().size();

        ScanFinding finding = suite.scanner().recordFinding(InMemorySeedLoader.BUILD_CI_01, "CVE-2023-91001", 8080,
                "jenkins-http");

        assertNotNull(finding, "recordFinding must return the saved finding");
        assertTrue(finding.isOpen(), "a new finding is OPEN");
        assertEquals(InMemorySeedLoader.BUILD_CI_01, finding.getAssetId());
        assertEquals("CVE-2023-91001", finding.getCveId());
        assertEquals(8080, finding.getPort().intValue());
        assertEquals(SeededSuite.NOW, finding.getDetectedAt(), "detectedAt must come from the injected Clock");
        assertEquals(before + 1, suite.findings.findAll().size(), "exactly one finding must be saved");
        assertTrue(suite.findings.findById(finding.getId()).isPresent(), "the returned finding must be the saved one");
    }

    @Test
    @GradedTest(tag = "SEC-4", points = 1, description = "unknown asset, unknown CVE and missing inputs are refused with ValidationException and nothing is written")
    public void recordFinding_unknownReferencesRefusedBeforeWriting() {
        SeededSuite suite = SeededSuite.seeded();
        ScannerService scanner = suite.scanner();
        int before = suite.findings.findAll().size();

        Long decommissioned = 57005L;
        assertThrows(ValidationException.class,
                () -> scanner.recordFinding(decommissioned, "CVE-2024-91006", 80, "http"),
                "a finding against an asset that does not exist must be refused");
        assertThrows(ValidationException.class,
                () -> scanner.recordFinding(InMemorySeedLoader.DB_PROD_01, "CVE-2099-00001", 5432, "postgresql"),
                "a finding for a CVE that is not in the catalog must be refused");
        assertThrows(ValidationException.class, () -> scanner.recordFinding(null, "CVE-2023-91001", 443, "https"),
                "a null assetId must be refused");
        assertThrows(ValidationException.class,
                () -> scanner.recordFinding(InMemorySeedLoader.DB_PROD_01, "  ", 443, "https"),
                "a blank cveId must be refused");
        assertEquals(before, suite.findings.findAll().size(), "a refused finding must not be written");
    }

    @Test
    @GradedTest(tag = "SEC-4", points = 1, description = "an OPEN duplicate (same asset + CVE) throws DuplicateFindingException and is checked BEFORE save()")
    public void recordFinding_openDuplicateRefusedBeforeSave() {
        SeededSuite suite = SeededSuite.seeded();
        ScanFindingRepository guarded = new SaveGuard(suite.findings);
        ScannerService scanner = new ScannerService(guarded, suite.assets, suite.cves, suite.clock);
        int before = suite.findings.findAll().size();

        assertThrows(DuplicateFindingException.class,
                () -> scanner.recordFinding(InMemorySeedLoader.WEB_PROD_01, "CVE-2023-91001", 443, "https"),
                "web-prod-01 already has an OPEN finding for CVE-2023-91001");
        assertEquals(before, suite.findings.findAll().size(), "the duplicate must not be written");
    }

    @Test
    @GradedTest(tag = "SEC-4", points = 1, description = "a RESOLVED finding for the same asset + CVE is NOT a duplicate (regressions are recorded)")
    public void recordFinding_resolvedFindingIsNotADuplicate() {
        SeededSuite suite = SeededSuite.seeded();
        // hr-portal-01 has a RESOLVED finding for CVE-2023-91003 in the seed.
        ScanFinding regression = assertDoesNotThrow(
                () -> suite.scanner().recordFinding(InMemorySeedLoader.HR_PORTAL_01, "CVE-2023-91003", 8080, "http"),
                "the vulnerability came back after being resolved -- that must be recorded, not refused");
        assertTrue(regression.isOpen());
        long openForCve = suite.findings.findByAssetId(InMemorySeedLoader.HR_PORTAL_01).stream()
                .filter(f -> f.getCveId().equals("CVE-2023-91003") && f.getStatus() == FindingStatus.OPEN)
                .count();
        assertEquals(1L, openForCve, "exactly one OPEN finding for the regressed CVE");
    }

    // ---------------------------------------------------------------
    // SEC-5: sealed RiskAssessment, assess(), createEntry()
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "SEC-5", points = 2, description = "assess() returns the right sealed band and score at every boundary")
    public void assess_bandsAtBoundaries() {
        RiskRegisterService service = SeededSuite.empty().riskService();
        int[][] cases = {{1, 1}, {2, 3}, {2, 4}, {3, 4}, {3, 5}, {4, 4}, {4, 5}, {5, 5}};
        List<Class<? extends RiskAssessment>> bands = List.of(LowRisk.class, LowRisk.class, MediumRisk.class,
                MediumRisk.class, HighRisk.class, HighRisk.class, CriticalRisk.class, CriticalRisk.class);
        for (int i = 0; i < cases.length; i++) {
            int likelihood = cases[i][0];
            int impact = cases[i][1];
            RiskAssessment assessment = service.assess(likelihood, impact);
            assertInstanceOf(bands.get(i), assessment, "likelihood " + likelihood + " x impact " + impact
                    + " = " + (likelihood * impact) + " should be " + bands.get(i).getSimpleName()
                    + " (1-6 Low, 7-12 Medium, 13-19 High, 20-25 Critical)");
            assertEquals(likelihood * impact, assessment.score(), "score must be likelihood * impact");
        }
    }

    @Test
    @GradedTest(tag = "SEC-5", points = 1, description = "likelihood/impact outside 1-5 are rejected, never clamped")
    public void assess_rejectsOutOfRangeInputs() {
        RiskRegisterService service = SeededSuite.empty().riskService();
        int[][] invalid = {{0, 3}, {3, 0}, {6, 1}, {1, 6}, {-1, -1}, {5, 6}};
        for (int[] c : invalid) {
            assertThrows(ValidationException.class, () -> service.assess(c[0], c[1]),
                    "assess(" + c[0] + ", " + c[1] + ") must throw ValidationException");
        }
    }

    @Test
    @GradedTest(tag = "SEC-5", points = 1, description = "each band record rejects a score outside its own range, so a mis-banded instance cannot exist")
    public void bandRecords_rejectMisbandedScores() {
        assertThrows(ValidationException.class, () -> new LowRisk(0), "LowRisk is 1-6");
        assertThrows(ValidationException.class, () -> new LowRisk(7), "LowRisk is 1-6");
        assertThrows(ValidationException.class, () -> new MediumRisk(6), "MediumRisk is 7-12");
        assertThrows(ValidationException.class, () -> new MediumRisk(13), "MediumRisk is 7-12");
        assertThrows(ValidationException.class, () -> new HighRisk(12), "HighRisk is 13-19");
        assertThrows(ValidationException.class, () -> new HighRisk(20), "HighRisk is 13-19");
        assertThrows(ValidationException.class, () -> new CriticalRisk(19), "CriticalRisk is 20-25");
        assertThrows(ValidationException.class, () -> new CriticalRisk(26), "CriticalRisk is 20-25");
        assertEquals(1, new LowRisk(1).score());
        assertEquals(6, new LowRisk(6).score());
        assertEquals(7, new MediumRisk(7).score());
        assertEquals(12, new MediumRisk(12).score());
        assertEquals(13, new HighRisk(13).score());
        assertEquals(19, new HighRisk(19).score());
        assertEquals(20, new CriticalRisk(20).score());
        assertEquals(25, new CriticalRisk(25).score());
    }

    @Test
    @GradedTest(tag = "SEC-5", points = 2, description = "createEntry persists EVERY band (including Low) with score, OPEN status and the band's due date")
    public void createEntry_persistsEveryBandWithDueDate() {
        SeededSuite suite = SeededSuite.seeded();
        RiskRegisterService service = suite.riskService();

        RiskRegisterEntry low = service.createEntry(InMemorySeedLoader.KIOSK_LOBBY_01, null,
                "Low risk still tracked", "d", 1, 1, null);
        RiskRegisterEntry medium = service.createEntry(InMemorySeedLoader.HR_PORTAL_01, null,
                "Medium risk", null, 3, 3, null);
        RiskRegisterEntry high = service.createEntry(InMemorySeedLoader.VPN_GW_01, null,
                "High risk", null, 3, 5, null);
        RiskRegisterEntry critical = service.createEntry(InMemorySeedLoader.WEB_PROD_01, FINDING_ON_WEB_PROD,
                "Critical risk backed by a finding", null, 5, 5, null);

        assertEquals(4, suite.risks.findAll().size(), "every band must be persisted -- a LowRisk is still a risk");
        assertEquals(1, low.getRiskScore());
        assertEquals(9, medium.getRiskScore());
        assertEquals(15, high.getRiskScore());
        assertEquals(25, critical.getRiskScore());
        for (RiskRegisterEntry entry : List.of(low, medium, high, critical)) {
            assertEquals(RiskStatus.OPEN, entry.getStatus(), "a new register entry is OPEN");
            assertEquals(SeededSuite.NOW, entry.getCreatedAt(), "createdAt must come from the injected Clock");
            assertTrue(suite.risks.findById(entry.getId()).isPresent(), "the returned entry must be the saved one");
        }
        LocalDate today = LocalDate.of(2026, 9, 24);
        assertEquals(today.plusDays(180), low.getDueDate(), "Low: 180-day remediation window");
        assertEquals(today.plusDays(90), medium.getDueDate(), "Medium: 90-day remediation window");
        assertEquals(today.plusDays(30), high.getDueDate(), "High: 30-day remediation window");
        assertEquals(today.plusDays(7), critical.getDueDate(), "Critical: 7-day remediation window");
        assertEquals(FINDING_ON_WEB_PROD, critical.getScanFindingId());
    }

    @Test
    @GradedTest(tag = "SEC-5", points = 1, description = "createEntry fails closed on an unknown asset, a finding of ANOTHER asset, an unknown finding or owner")
    public void createEntry_failsClosedOnBadReferences() {
        SeededSuite suite = SeededSuite.seeded();
        RiskRegisterService service = suite.riskService();
        Long unknown = java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);

        assertThrows(ValidationException.class,
                () -> service.createEntry(unknown, null, "t", null, 3, 3, null), "unknown asset");
        assertThrows(ValidationException.class,
                () -> service.createEntry(InMemorySeedLoader.KIOSK_LOBBY_01, FINDING_ON_WEB_PROD, "t", null, 3, 3, null),
                "the finding belongs to web-prod-01, not the kiosk -- the risk's evidence would be about another host");
        assertThrows(ValidationException.class,
                () -> service.createEntry(InMemorySeedLoader.KIOSK_LOBBY_01, unknown, "t", null, 3, 3, null),
                "unknown scan finding");
        assertThrows(ValidationException.class,
                () -> service.createEntry(InMemorySeedLoader.KIOSK_LOBBY_01, null, "t", null, 3, 3, unknown),
                "unknown owner analyst");
        assertThrows(ValidationException.class,
                () -> service.createEntry(InMemorySeedLoader.KIOSK_LOBBY_01, null, "t", null, 0, 3, null),
                "likelihood 0 is out of range");
        assertTrue(suite.risks.findAll().isEmpty(), "nothing may be written when createEntry refuses");
    }

    /**
     * Test double: delegates to the real repository, but fails the test (with an Error, which
     * a catch (RuntimeException) cannot swallow) if save() is ever asked to store a SECOND open
     * finding for the same asset + CVE. Proves the duplicate check happens BEFORE the write
     * rather than by catching the storage layer's constraint violation afterwards.
     */
    private static final class SaveGuard implements ScanFindingRepository {
        private final ScanFindingRepository delegate;

        SaveGuard(ScanFindingRepository delegate) {
            this.delegate = delegate;
        }

        @Override
        public ScanFinding save(ScanFinding finding) {
            if (finding.isOpen() && delegate.findOpenByAssetAndCve(finding.getAssetId(), finding.getCveId()).isPresent()) {
                throw new AssertionError("save() was called for a duplicate OPEN finding -- check for the duplicate "
                        + "BEFORE writing, don't rely on the repository to reject it");
            }
            return delegate.save(finding);
        }

        @Override
        public Optional<ScanFinding> findById(Long id) {
            return delegate.findById(id);
        }

        @Override
        public List<ScanFinding> findAll() {
            return delegate.findAll();
        }

        @Override
        public List<ScanFinding> findByAssetId(Long assetId) {
            return delegate.findByAssetId(assetId);
        }

        @Override
        public Optional<ScanFinding> findOpenByAssetAndCve(Long assetId, String cveId) {
            return delegate.findOpenByAssetAndCve(assetId, cveId);
        }

        @Override
        public List<ScanFinding> findOpenByCveId(String cveId) {
            return delegate.findOpenByCveId(cveId);
        }

        @Override
        public boolean updateStatus(Long findingId, FindingStatus newStatus) {
            return delegate.updateStatus(findingId, newStatus);
        }
    }
}

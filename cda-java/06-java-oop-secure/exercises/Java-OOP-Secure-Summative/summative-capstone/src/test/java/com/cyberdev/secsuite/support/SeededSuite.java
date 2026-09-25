package com.cyberdev.secsuite.support;

import com.cyberdev.secsuite.model.Analyst;
import com.cyberdev.secsuite.repository.inmemory.InMemoryAnalystRepository;
import com.cyberdev.secsuite.repository.inmemory.InMemoryAssetRepository;
import com.cyberdev.secsuite.repository.inmemory.InMemoryComponentRepository;
import com.cyberdev.secsuite.repository.inmemory.InMemoryCveCatalogRepository;
import com.cyberdev.secsuite.repository.inmemory.InMemoryRiskRegisterRepository;
import com.cyberdev.secsuite.repository.inmemory.InMemoryScanFindingRepository;
import com.cyberdev.secsuite.repository.inmemory.InMemorySeedLoader;
import com.cyberdev.secsuite.repository.inmemory.InMemoryThreatIntelAlertRepository;
import com.cyberdev.secsuite.repository.inmemory.InMemoryThreatModelEntryRepository;
import com.cyberdev.secsuite.repository.inmemory.InMemoryThreatModelRepository;
import com.cyberdev.secsuite.service.ReportService;
import com.cyberdev.secsuite.service.RiskRegisterService;
import com.cyberdev.secsuite.service.SbomService;
import com.cyberdev.secsuite.service.ScannerService;
import com.cyberdev.secsuite.service.ThreatIntelAlertService;
import com.cyberdev.secsuite.service.ThreatIntelCsvIngestionService;
import com.cyberdev.secsuite.service.ThreatModelingService;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/**
 * GIVEN TEST INFRASTRUCTURE -- not a graded TODO.
 *
 * One complete, freshly seeded in-memory SecOps Analyst Suite for a single test: every
 * InMemory* repository, loaded by InMemorySeedLoader (the same seed Main uses), plus a FIXED
 * clock (2026-09-24T09:00:00Z) so timestamps and due dates are deterministic. The service
 * factory methods wire services exactly as Main does -- constructor injection only.
 *
 * NOTE FOR STUDENTS: the seed builds Asset objects, so EVERY test that uses a seeded suite
 * needs SEC-1 (the Asset constructor) to be finished first. Until then those tests fail with
 * "TODO [SEC-1]" in their error message -- that is expected, not a test bug.
 */
public final class SeededSuite {

    public static final Instant NOW = Instant.parse("2026-09-24T09:00:00Z");
    public static final Path FEED_FILE = Path.of("data", "threat-intel-feed.csv");

    public final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    public final InMemoryAnalystRepository analysts = new InMemoryAnalystRepository();
    public final InMemoryAssetRepository assets = new InMemoryAssetRepository();
    public final InMemoryCveCatalogRepository cves = new InMemoryCveCatalogRepository();
    public final InMemoryScanFindingRepository findings = new InMemoryScanFindingRepository();
    public final InMemoryRiskRegisterRepository risks = new InMemoryRiskRegisterRepository();
    public final InMemoryComponentRepository components = new InMemoryComponentRepository();
    public final InMemoryThreatModelRepository threatModels = new InMemoryThreatModelRepository();
    public final InMemoryThreatModelEntryRepository threatModelEntries = new InMemoryThreatModelEntryRepository();
    public final InMemoryThreatIntelAlertRepository alerts = new InMemoryThreatIntelAlertRepository();

    private SeededSuite(boolean seed) {
        if (seed) {
            InMemorySeedLoader.load(assets, cves, findings, components, threatModels, threatModelEntries, alerts);
        }
    }

    /** A suite loaded with the standard seed data. */
    public static SeededSuite seeded() {
        return new SeededSuite(true);
    }

    /** A suite with every repository EMPTY (for "no data" report sections). */
    public static SeededSuite empty() {
        return new SeededSuite(false);
    }

    public ScannerService scanner() {
        return new ScannerService(findings, assets, cves, clock);
    }

    public RiskRegisterService riskService() {
        return new RiskRegisterService(risks, assets, findings, cves, analysts, clock);
    }

    public SbomService sbom() {
        return new SbomService(components, cves);
    }

    public ThreatModelingService threatModeling() {
        return new ThreatModelingService(threatModels, threatModelEntries, assets, clock);
    }

    public ThreatIntelCsvIngestionService ingestion() {
        return new ThreatIntelCsvIngestionService(cves, alerts, clock);
    }

    public ThreatIntelAlertService alertService() {
        return new ThreatIntelAlertService(alerts, findings, assets);
    }

    public ReportService reportService() {
        return new ReportService(assets, cves, scanner(), riskService(), sbom(), threatModeling(), alertService(),
                clock);
    }

    /**
     * An Analyst for "Prepared by" / risk ownership, built directly (no AuthService, so report
     * tests do not depend on SEC-13/SEC-14). The hash and ciphertext are placeholders.
     */
    public static Analyst analyst(String username) {
        return new Analyst(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE), username, "pbkdf2_sha256$placeholder", "placeholder-ciphertext", NOW);
    }
}

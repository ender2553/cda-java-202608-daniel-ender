package com.cyberdev.secsuite.service;

import com.cyberdev.secsuite.exception.IngestionException;
import com.cyberdev.secsuite.model.FindingStatus;
import com.cyberdev.secsuite.model.IndicatorType;
import com.cyberdev.secsuite.model.ScanFinding;
import com.cyberdev.secsuite.model.Severity;
import com.cyberdev.secsuite.model.ThreatIntelAlert;
import com.cyberdev.secsuite.repository.ThreatIntelAlertRepository;
import com.cyberdev.secsuite.repository.inmemory.InMemorySeedLoader;
import com.cyberdev.secsuite.support.SeededSuite;
import com.cyberdev.secsuite.testkit.GradedTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GIVEN TEST INFRASTRUCTURE -- these tests are provided and graded, not something students
 * edit.
 *
 * SEC-10 (CSV parse + validate), SEC-11 (persist + deduplicate) and SEC-12 (threat intel
 * correlation). In-memory seed, no database. Requires SEC-1.
 *
 * The three checkpoints are graded INDEPENDENTLY: the SEC-11 tests hand-build the
 * IngestionResult they persist, and the SEC-12 tests save alerts/findings straight into the
 * repositories, so an unfinished SEC-10 parser does not cost SEC-11 or SEC-12 points.
 *
 * The SEC-10 tests that read data/threat-intel-feed.csv expect to run from the module root
 * (Maven's default working directory for tests).
 */
public class ThreatIntelTests {

    private static final String HEADER = ThreatIntelCsvIngestionService.EXPECTED_HEADER;

    // ---------------------------------------------------------------
    // SEC-10: parseFile / validateRow
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "SEC-10", points = 2, description = "the shipped feed parses to 9 valid alerts and 4 skipped rows (lines 11-14), values converted correctly, nothing persisted")
    public void parseFile_shippedFeed() {
        SeededSuite suite = SeededSuite.seeded();
        assertTrue(Files.isRegularFile(SeededSuite.FEED_FILE),
                "run the tests from the module root so that data/threat-intel-feed.csv resolves");
        IngestionResult result = suite.ingestion().parseFile(SeededSuite.FEED_FILE);

        assertEquals(13, result.dataRowsRead(), "13 data rows follow the header");
        assertEquals(9, result.parsed().size(), "9 rows are valid");
        assertEquals(List.of(11, 12, 13, 14), result.skipped().stream().map(SkippedRow::lineNumber).toList(),
                "blank source (11), URL indicator type (12), unparseable date (13) and a CVE missing from the "
                        + "catalog (14) must be skipped, with 1-based PHYSICAL line numbers (header = line 1)");
        assertEquals(List.of("TI-2026-0010", "TI-2026-0011", "TI-2026-0012", "TI-2026-0013"),
                result.skipped().stream().map(SkippedRow::externalAlertId).toList(),
                "each skipped row records the id it claimed");

        ThreatIntelAlert hash = find(result.parsed(), "TI-2026-0004");
        assertEquals(IndicatorType.FILE_HASH, hash.getIndicatorType());
        assertEquals("CVE-2023-91001", hash.getRelatedCveId());
        assertTrue(hash.getDescription().contains("<script>alert(1)</script>"),
                "descriptions are stored as-is (escaping happens on OUTPUT, in the report)");
        assertTrue(hash.getDescription().contains("\"payroll\""), "a doubled \"\" inside quotes is one quote");
        assertTrue(hash.getDescription().contains(" | "), "a quoted field may contain | and commas");
        assertEquals(Instant.parse("2026-09-04T10:00:00Z"), hash.getPublishedAt(),
                "an offset date-time (+02:00) must be converted to the right instant");
        assertEquals(Instant.parse("2026-09-03T00:00:00Z"), find(result.parsed(), "TI-2026-0003").getPublishedAt(),
                "a plain date is midnight UTC");
        assertEquals(Severity.CRITICAL, find(result.parsed(), "TI-2026-0001").getSeverity());
        assertEquals(SeededSuite.NOW, find(result.parsed(), "TI-2026-0001").getIngestedAt(),
                "ingestedAt comes from the injected clock");
        assertEquals(5L, suite.alerts.count(), "parseFile must not write anything -- persisting is SEC-11");
    }

    @Test
    @GradedTest(tag = "SEC-10", points = 2, description = "file-level problems throw IngestionException (never a raw IOException, never an empty result)")
    public void parseFile_fileLevelFailures() throws IOException {
        ThreatIntelCsvIngestionService service = SeededSuite.seeded().ingestion();
        Path dir = Files.createTempDirectory("secsuite-sec10-");

        assertThrows(IngestionException.class, () -> service.parseFile(null), "null path");
        assertThrows(IngestionException.class, () -> service.parseFile(dir.resolve("missing.csv")),
                "a missing feed must be LOUD, not look like 'no alerts today'");
        assertThrows(IngestionException.class, () -> service.parseFile(dir), "a directory is not a feed file");

        Path empty = Files.write(dir.resolve("empty.csv"), new byte[0]);
        assertThrows(IngestionException.class, () -> service.parseFile(empty), "an empty file has no header");

        Path wrongHeader = Files.writeString(dir.resolve("header.csv"),
                "id,source,type,value,cve,severity,description,published\nA-1,S,IP,10.0.0.1,,LOW,d,2026-01-01\n");
        assertThrows(IngestionException.class, () -> service.parseFile(wrongHeader),
                "a header that is not exactly the expected one means the columns cannot be trusted");

        Path reordered = Files.writeString(dir.resolve("reordered.csv"),
                "source,external_alert_id,indicator_type,indicator_value,related_cve_id,severity,description,published_at\n");
        assertThrows(IngestionException.class, () -> service.parseFile(reordered),
                "the same column names in a different order are NOT the expected header");

        Path notUtf8 = Files.write(dir.resolve("latin1.csv"), new byte[]{(byte) 0xC3, (byte) 0x28, '\n'});
        assertThrows(IngestionException.class, () -> service.parseFile(notUtf8), "bytes that are not valid UTF-8");

        Path oversize = Files.write(dir.resolve("huge.csv"),
                new byte[(int) ThreatIntelCsvIngestionService.MAX_FILE_BYTES + 1]);
        assertThrows(IngestionException.class, () -> service.parseFile(oversize),
                "a file larger than MAX_FILE_BYTES must be refused before it is read");
    }

    @Test
    @GradedTest(tag = "SEC-10", points = 2, description = "invalid rows are skipped with their line number and ingestion continues; blank lines are ignored")
    public void parseFile_badRowsAreSkippedNotFatal() throws IOException {
        ThreatIntelCsvIngestionService service = SeededSuite.seeded().ingestion();
        String csv = String.join("\n",
                "Ã¯Â»Â¿" + HEADER,                                                          // 1 (BOM tolerated)
                "GOOD-1,Feed,IP,10.0.0.1,,LOW,ok,2026-01-01",                               // 2 parsed
                "   ",                                                                      // 3 blank: ignored
                "BAD-QUOTE,Feed,IP,10.0.0.1,,LOW,\"unterminated,2026-01-01",                // 4 broken quoting
                "BAD-CTRL,Feed\u0007,IP,10.0.0.1,,LOW,d,2026-01-01",                        // 5 control character
                "BAD-IP,Feed,IP,10.0.0.300,,LOW,d,2026-01-01",                              // 6 octet > 255
                "BAD-SEV,Feed,IP,10.0.0.1,,NONE,d,2026-01-01",                              // 7 NONE not storable
                "BAD-TYPE,Feed,URL,https://x.example/a,,LOW,d,2026-01-01",                  // 8 unknown type
                "BAD-COLS,Feed,IP,10.0.0.1,,LOW,d",                                         // 9 7 fields
                "BAD ID,Feed,IP,10.0.0.1,,LOW,d,2026-01-01",                                // 10 space in id
                "BAD-DESC,Feed,IP,10.0.0.1,,LOW," + "x".repeat(2001) + ",2026-01-01",       // 11 too long
                "BAD-DATE,Feed,IP,10.0.0.1,,LOW,d,yesterday",                               // 12 not ISO-8601
                "BAD-HASH,Feed,FILE_HASH,xyz123,,LOW,d,2026-01-01",                         // 13 not a hash
                "GOOD-2,Feed,DOMAIN,evil.example.com,,HIGH,\"quoted, with comma\",2026-01-02T03:04:05+01:00", // 14
                ",Feed,IP,10.0.0.1,,LOW,d,2026-01-01",                                      // 15 blank id
                "") ;
        Path file = Files.writeString(Files.createTempFile("secsuite-rows-", ".csv"), csv, StandardCharsets.UTF_8);

        IngestionResult result = service.parseFile(file);

        assertEquals(List.of("GOOD-1", "GOOD-2"), result.parsed().stream().map(ThreatIntelAlert::getExternalAlertId).toList(),
                "one bad row must never stop the rows after it from being read");
        assertEquals(List.of(4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15),
                result.skipped().stream().map(SkippedRow::lineNumber).toList(),
                "every invalid row is skipped, reported by its physical line number");
        assertEquals(13, result.dataRowsRead(), "blank lines are not data rows");
        assertEquals("BAD-IP", result.skipped().get(2).externalAlertId(), "a skipped row keeps the id it claimed");
        ThreatIntelAlert good2 = result.parsed().get(1);
        assertEquals("quoted, with comma", good2.getDescription(), "commas inside quotes must not split the field");
        assertEquals(Instant.parse("2026-01-02T02:04:05Z"), good2.getPublishedAt());
    }

    @Test
    @GradedTest(tag = "SEC-10", points = 2, description = "related_cve_id rules: must be well-formed AND in cve_catalog; a CVE indicator requires a matching related_cve_id")
    public void parseFile_cveReferenceRules() throws IOException {
        ThreatIntelCsvIngestionService service = SeededSuite.seeded().ingestion();
        String csv = String.join("\n",
                HEADER,
                "C-1,Feed,CVE,CVE-2023-91002,CVE-2023-91002,CRITICAL,d,2026-09-01T08:00:00Z", // 2 parsed
                "C-2,Feed,CVE,CVE-2025-99999,CVE-2025-99999,CRITICAL,d,2026-09-01",           // 3 not in catalog
                "C-3,Feed,CVE,CVE-2023-91002,,HIGH,d,2026-09-01",                             // 4 CVE without related id
                "C-4,Feed,CVE,CVE-2023-91002,CVE-2023-91004,HIGH,d,2026-09-01",               // 5 mismatch
                "C-5,Feed,IP,203.0.113.9,CVE-2023-91001,HIGH,d,2026-09-01",                   // 6 parsed (context CVE)
                "C-6,Feed,IP,203.0.113.9,CVE-BAD,HIGH,d,2026-09-01",                          // 7 malformed CVE id
                "C-7,Feed,FILE_HASH," + "ab".repeat(32) + ",CVE-2030-11111,HIGH,d,2026-09-01", // 8 not in catalog
                "");
        Path file = Files.writeString(Files.createTempFile("secsuite-cve-", ".csv"), csv, StandardCharsets.UTF_8);

        IngestionResult result = service.parseFile(file);

        assertEquals(List.of("C-1", "C-5"), result.parsed().stream().map(ThreatIntelAlert::getExternalAlertId).toList());
        assertEquals(List.of(3, 4, 5, 7, 8), result.skipped().stream().map(SkippedRow::lineNumber).toList(),
                "a related_cve_id missing from cve_catalog is REJECTED (row skipped), not stored as a dangling "
                        + "reference or silently set to null");
        assertEquals("CVE-2023-91001", result.parsed().get(1).getRelatedCveId(),
                "a non-CVE indicator may carry a (catalogued) related CVE as context");
    }

    // ---------------------------------------------------------------
    // SEC-11: persistAndDeduplicate
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "SEC-11", points = 2, description = "new alerts are inserted; an external_alert_id that is already stored is reported as a duplicate, not thrown")
    public void persist_insertsNewAndReportsDuplicates() {
        SeededSuite suite = SeededSuite.seeded();
        IngestionResult batch = batch(alert("NEW-1"), alert("TI-SEED-001"), alert("NEW-2"));

        PersistenceResult result = assertDoesNotThrow(() -> suite.ingestion().persistAndDeduplicate(batch),
                "a feed re-sending a known alert is normal, not an error");

        assertEquals(List.of("NEW-1", "NEW-2"),
                result.inserted().stream().map(ThreatIntelAlert::getExternalAlertId).toList());
        assertEquals(List.of("TI-SEED-001"), result.duplicateExternalAlertIds());
        assertEquals(7L, suite.alerts.count(), "5 seeded + 2 new");
        assertTrue(suite.alerts.findByExternalAlertId("NEW-2").isPresent());
    }

    @Test
    @GradedTest(tag = "SEC-11", points = 2, description = "persisting the same batch twice is idempotent: nothing inserted, everything reported as duplicate")
    public void persist_replayIsIdempotent() {
        SeededSuite suite = SeededSuite.seeded();
        ThreatIntelCsvIngestionService service = suite.ingestion();
        IngestionResult batch = batch(alert("NEW-1"), alert("NEW-2"), alert("NEW-3"));

        service.persistAndDeduplicate(batch);
        long afterFirst = suite.alerts.count();
        PersistenceResult replay = service.persistAndDeduplicate(batch);

        assertEquals(8L, afterFirst);
        assertEquals(afterFirst, suite.alerts.count(), "the row count must not change on a replay");
        assertTrue(replay.inserted().isEmpty(), "nothing is inserted the second time");
        assertEquals(List.of("NEW-1", "NEW-2", "NEW-3"), replay.duplicateExternalAlertIds());
    }

    @Test
    @GradedTest(tag = "SEC-11", points = 1, description = "the duplicate check happens BEFORE save() and a stored alert is never overwritten")
    public void persist_checksBeforeWritingAndNeverOverwrites() {
        SeededSuite suite = SeededSuite.seeded();
        ThreatIntelAlertRepository guarded = new InsertOnceGuard(suite.alerts);
        ThreatIntelCsvIngestionService service = new ThreatIntelCsvIngestionService(suite.cves, guarded, suite.clock);
        ThreatIntelAlert original = suite.alerts.findByExternalAlertId("TI-SEED-001").orElseThrow();

        service.persistAndDeduplicate(batch(alert("TI-SEED-001")));

        ThreatIntelAlert stored = suite.alerts.findByExternalAlertId("TI-SEED-001").orElseThrow();
        assertEquals(original.getId(), stored.getId(), "the stored alert must be the original row");
        assertEquals("NorthStar ISAC", stored.getSource(), "a feed must not be able to rewrite a stored alert");
        assertEquals(original.getDescription(), stored.getDescription());
    }

    @Test
    @GradedTest(tag = "SEC-11", points = 1, description = "an id that appears twice in the same batch is inserted once")
    public void persist_sameIdTwiceInOneBatch() {
        SeededSuite suite = SeededSuite.seeded();
        PersistenceResult result = suite.ingestion().persistAndDeduplicate(batch(alert("DUP-1"), alert("DUP-1")));
        assertEquals(1, result.inserted().size());
        assertEquals(List.of("DUP-1"), result.duplicateExternalAlertIds());
        assertEquals(6L, suite.alerts.count());
    }

    // ---------------------------------------------------------------
    // SEC-12: correlateWithFindings
    // ---------------------------------------------------------------

    @Test
    @GradedTest(tag = "SEC-12", points = 2, description = "seed only: exactly one hit -- a RESOLVED finding and a FILE_HASH alert's related CVE are not hits")
    public void correlate_seedHasNoFalsePositives() {
        SeededSuite suite = SeededSuite.seeded();
        List<CorrelationHit> hits = suite.alertService().correlateWithFindings();

        assertEquals(List.of("web-prod-01|TI-SEED-001|CVE-2023-91001"), describe(hits),
                "TI-SEED-002 only matches hr-portal-01's RESOLVED finding (patched host = not a hit), and "
                        + "TI-SEED-005 is a FILE_HASH indicator (its related CVE is context, not a hit)");
    }

    @Test
    @GradedTest(tag = "SEC-12", points = 3, description = "after new alerts and a new finding: one hit per matching OPEN finding, sorted by hostname then alert id")
    public void correlate_multipleHitsSortedDeterministically() {
        SeededSuite suite = SeededSuite.seeded();
        // The CVE-type alerts from the shipped feed, saved directly (no dependency on SEC-10/11).
        suite.alerts.save(cveAlert("TI-2026-0001", "CVE-2023-91002"));
        suite.alerts.save(cveAlert("TI-2026-0005", "CVE-2023-91004"));
        suite.alerts.save(cveAlert("TI-2026-0006", "CVE-2024-91007"));
        suite.alerts.save(cveAlert("TI-2026-0008", "CVE-2024-91009")); // no finding for this CVE
        // A second asset with CVE-2023-91001 open (no dependency on SEC-4).
        suite.findings.save(new ScanFinding(0L, InMemorySeedLoader.BUILD_CI_01, "CVE-2023-91001",
                8080, "jenkins-http", SeededSuite.NOW, FindingStatus.OPEN));

        List<CorrelationHit> hits = suite.alertService().correlateWithFindings();

        assertEquals(List.of(
                        "build-ci-01|TI-SEED-001|CVE-2023-91001",
                        "db-prod-01|TI-2026-0005|CVE-2023-91004",
                        "hr-portal-01|TI-2026-0006|CVE-2024-91007",
                        "vpn-gw-01|TI-2026-0001|CVE-2023-91002",
                        "web-prod-01|TI-SEED-001|CVE-2023-91001"),
                describe(hits),
                "one alert can hit SEVERAL assets (TI-SEED-001 hits two); order is hostname, then alert id");
    }

    @Test
    @GradedTest(tag = "SEC-12", points = 1, description = "non-CVE indicators never produce a hit, even when their related CVE has an open finding")
    public void correlate_nonCveIndicatorsNeverHit() {
        SeededSuite suite = SeededSuite.seeded();
        suite.alerts.save(new ThreatIntelAlert(0L, "CTX-IP", "Feed", IndicatorType.IP, "198.51.100.7",
                "CVE-2023-91001", Severity.HIGH, "C2 server seen exploiting LogLite", null, SeededSuite.NOW));
        suite.alerts.save(new ThreatIntelAlert(0L, "CTX-DOMAIN", "Feed", IndicatorType.DOMAIN,
                "bad.example.org", "CVE-2023-91005", Severity.MEDIUM, "phishing kit", null, SeededSuite.NOW));

        List<CorrelationHit> hits = suite.alertService().correlateWithFindings();

        assertEquals(List.of("web-prod-01|TI-SEED-001|CVE-2023-91001"), describe(hits),
                "only CVE-type alerts are evidence that a host is exposed");
        for (CorrelationHit hit : hits) {
            assertEquals(IndicatorType.CVE, hit.alert().getIndicatorType());
        }
    }

    // ---------------------------------------------------------------
    // helpers
    // ---------------------------------------------------------------

    private static ThreatIntelAlert find(List<ThreatIntelAlert> alerts, String externalId) {
        return alerts.stream().filter(a -> a.getExternalAlertId().equals(externalId)).findFirst()
                .orElseThrow(() -> new AssertionError(externalId + " should have been parsed"));
    }

    /** A valid IP-indicator alert (no CVE reference) with the given external id. */
    private static ThreatIntelAlert alert(String externalId) {
        return new ThreatIntelAlert(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE), externalId, "PartnerCERT", IndicatorType.IP, "192.0.2.10",
                null, Severity.HIGH, "re-sent by the feed", Instant.parse("2026-09-09T05:00:00Z"), SeededSuite.NOW);
    }

    private static ThreatIntelAlert cveAlert(String externalId, String cveId) {
        return new ThreatIntelAlert(java.util.concurrent.ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE), externalId, "NorthStar ISAC", IndicatorType.CVE, cveId, cveId,
                Severity.HIGH, "exploited in the wild", Instant.parse("2026-09-01T08:00:00Z"), SeededSuite.NOW);
    }

    private static IngestionResult batch(ThreatIntelAlert... alerts) {
        return new IngestionResult(Path.of("hand-built.csv"), alerts.length, List.of(alerts), List.of());
    }

    private static List<String> describe(List<CorrelationHit> hits) {
        assertNotNull(hits, "correlateWithFindings must return a list, never null");
        return hits.stream()
                .map(h -> h.asset().getHostname() + "|" + h.alert().getExternalAlertId() + "|" + h.finding().getCveId())
                .toList();
    }

    /**
     * Test double: delegates to the real repository but fails the test (with an Error that a
     * catch (RuntimeException) cannot swallow) if save() is called for an external_alert_id
     * that is already stored -- the SEC-11 check must happen BEFORE the write, not by catching
     * the unique-constraint violation afterwards.
     */
    private static final class InsertOnceGuard implements ThreatIntelAlertRepository {
        private final ThreatIntelAlertRepository delegate;

        InsertOnceGuard(ThreatIntelAlertRepository delegate) {
            this.delegate = delegate;
        }

        @Override
        public ThreatIntelAlert save(ThreatIntelAlert alert) {
            if (delegate.findByExternalAlertId(alert.getExternalAlertId()).isPresent()) {
                throw new AssertionError("save() was called for already-stored external_alert_id "
                        + alert.getExternalAlertId() + " -- check BEFORE writing, never upsert or rely on the constraint");
            }
            return delegate.save(alert);
        }

        @Override
        public Optional<ThreatIntelAlert> findByExternalAlertId(String externalAlertId) {
            return delegate.findByExternalAlertId(externalAlertId);
        }

        @Override
        public List<ThreatIntelAlert> findAll() {
            return delegate.findAll();
        }

        @Override
        public List<ThreatIntelAlert> findByIndicatorType(IndicatorType indicatorType) {
            return delegate.findByIndicatorType(indicatorType);
        }

        @Override
        public long count() {
            return delegate.count();
        }
    }
}

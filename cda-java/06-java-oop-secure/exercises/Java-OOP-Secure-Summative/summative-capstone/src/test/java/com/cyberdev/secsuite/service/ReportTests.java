package com.cyberdev.secsuite.service;

import com.cyberdev.secsuite.exception.ReportGenerationException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Analyst;
import com.cyberdev.secsuite.model.IndicatorType;
import com.cyberdev.secsuite.model.Severity;
import com.cyberdev.secsuite.model.ThreatIntelAlert;
import com.cyberdev.secsuite.support.SeededSuite;
import com.cyberdev.secsuite.testkit.GradedTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GIVEN TEST INFRASTRUCTURE -- these tests are provided and graded, not something students
 * edit.
 *
 * SEC-16: ReportService.buildMarkdown / generateMarkdownReport. In-memory seed, no database.
 *
 * The report pulls from every module, so these tests need SEC-1, SEC-2, SEC-7, SEC-8 and
 * SEC-12 to be working (the ingestion summary is hand-built, so SEC-10/SEC-11 are NOT needed,
 * and the register is left empty, so SEC-5's createEntry is not needed either).
 */
public class ReportTests {

    private static final List<String> SECTIONS = List.of(
            "## 1. Asset Inventory",
            "## 2. Open Scan Findings by Severity",
            "## 3. Risk Register -- Top Risks",
            "## 4. SBOM -- Vulnerable Components",
            "## 5. STRIDE Threat Model Coverage",
            "## 6. Threat Intelligence Alerts",
            "## 7. Threat Intelligence Correlation (Active Hits)",
            "## 8. Threat Intel CSV Ingestion Summary");

    @Test
    @GradedTest(tag = "SEC-16", points = 2, description = "buildMarkdown: title, header block, and all eight sections exactly once, in order, with module data")
    public void buildMarkdown_headerAndAllSectionsInOrder() {
        SeededSuite suite = SeededSuite.seeded();
        String md = suite.reportService().buildMarkdown(SeededSuite.analyst("analyst1"), List.of(sampleRun()));

        assertTrue(md.startsWith("# SecOps Analyst Suite -- Security Assessment Report\n"), "report title first");
        assertTrue(md.contains("- **Generated:** 2026-09-24T09:00:00Z\n"), "timestamp from the injected Clock, ISO-8601");
        assertTrue(md.contains("- **Prepared by:** analyst1\n"), "the authenticated analyst's username");
        assertTrue(md.contains("- **Scope:** 6 assets, 10 catalogued CVEs\n"));
        int previous = -1;
        for (String heading : SECTIONS) {
            int at = md.indexOf("\n" + heading + "\n");
            assertTrue(at >= 0, "missing section: " + heading);
            assertEquals(at, md.lastIndexOf("\n" + heading + "\n"), "section appears more than once: " + heading);
            assertTrue(at > previous, "sections out of order at: " + heading);
            previous = at;
        }
        assertTrue(md.contains("| web-prod-01 | 10.0.1.10 | Platform Engineering | CRITICAL |"), "asset inventory rows");
        assertTrue(md.contains("Total open findings: **7**"), "open findings from the scanner module");
        assertTrue(md.contains("Components analysed: **8**, with known vulnerabilities: **5**"), "SBOM module");
        assertTrue(md.contains("Active hits: **1**"), "correlation module");
        assertTrue(md.contains("| 1 | threat-intel-feed.csv | 3 | 2 | 1 | 1 | 1 |"), "one row per ingestion run");
    }

    @Test
    @GradedTest(tag = "SEC-16", points = 1, description = "with NO data every section is still present and says _None._ (never silently missing)")
    public void buildMarkdown_emptyDataStillRendersEverySection() {
        SeededSuite suite = SeededSuite.empty();
        String md = suite.reportService().buildMarkdown(SeededSuite.analyst("analyst1"), List.of());

        for (String heading : SECTIONS) {
            assertTrue(md.contains("\n" + heading + "\n"), "an empty module still gets its section: " + heading);
        }
        assertTrue(md.contains("## 1. Asset Inventory\n\n_None._"), "empty inventory says _None._");
        assertTrue(md.contains("## 7. Threat Intelligence Correlation (Active Hits)\n\n"), "correlation section present");
        assertTrue(md.contains("## 8. Threat Intel CSV Ingestion Summary\n\n_None._"), "no ingestion runs says _None._");
        assertThrows(ValidationException.class, () -> suite.reportService().buildMarkdown(null, List.of()),
                "a report must say who prepared it");
    }

    @Test
    @GradedTest(tag = "SEC-16", points = 2, description = "untrusted text is output-encoded: no raw HTML, no forged headings, header values escaped too")
    public void buildMarkdown_escapesUntrustedText() {
        SeededSuite suite = SeededSuite.seeded();
        suite.alerts.save(new ThreatIntelAlert(0L, "EVIL-1", "Evil_Feed*", IndicatorType.DOMAIN,
                "evil.example.org", null, Severity.HIGH,
                "<script>alert(1)</script> | [click](http://x) \n## 9. Injected heading", null, SeededSuite.NOW));

        String md = suite.reportService().buildMarkdown(SeededSuite.analyst("a_b"), List.of(sampleRun()));

        assertFalse(md.contains("<script>"), "raw HTML from a feed must never reach the report");
        assertTrue(md.contains("&lt;script&gt;alert(1)&lt;/script&gt;"), "HTML metacharacters become entities");
        assertTrue(md.contains("Evil\\_Feed\\*"), "Markdown metacharacters are backslash-escaped");
        assertFalse(md.contains("\n## 9."), "a newline in feed text must not be able to start a new heading");
        assertTrue(md.contains("- **Prepared by:** a\\_b\n"),
                "EVERY data value goes through escapeMd -- including the header's username");
        assertTrue(md.contains("&lt;b&gt;reason&lt;/b&gt;"), "skip reasons are feed-derived and escaped too");
    }

    @Test
    @GradedTest(tag = "SEC-16", points = 1, description = "generateMarkdownReport creates missing parent directories and writes exactly buildMarkdown's text as UTF-8")
    public void generateMarkdownReport_writesFileCreatingDirectories() throws IOException {
        SeededSuite suite = SeededSuite.seeded();
        suite.alerts.save(new ThreatIntelAlert(0L, "UTF8-1", "Feed", IndicatorType.DOMAIN,
                "cafe.example.org", null, Severity.LOW, "naÃƒÂ¯ve cafÃƒÂ© payload Ã¢â‚¬â€œ non-ASCII", null, SeededSuite.NOW));
        ReportService report = suite.reportService();
        Analyst analyst = SeededSuite.analyst("analyst1");
        Path out = Files.createTempDirectory("secsuite-sec16-").resolve("nested").resolve("deeper").resolve("report.md");

        Path written = report.generateMarkdownReport(out, analyst, List.of(sampleRun()));

        assertNotNull(written, "return the path that was written");
        assertTrue(Files.isRegularFile(out), "the report file must exist -- create missing parent directories");
        assertTrue(Files.isSameFile(out, written), "the returned path must be the file that was written");
        assertEquals(report.buildMarkdown(analyst, List.of(sampleRun())), Files.readString(out, StandardCharsets.UTF_8),
                "the file must contain exactly the Markdown document, encoded as UTF-8");
    }

    @Test
    @GradedTest(tag = "SEC-16", points = 1, description = "an I/O failure becomes ReportGenerationException; null path or analyst is rejected")
    public void generateMarkdownReport_failuresWrappedOrRejected() throws IOException {
        SeededSuite suite = SeededSuite.seeded();
        ReportService report = suite.reportService();
        Analyst analyst = SeededSuite.analyst("analyst1");
        Path aFile = Files.writeString(Files.createTempFile("secsuite-not-a-dir-", ".txt"), "x");
        Path impossible = aFile.resolve("report.md"); // parent is a regular file, not a directory

        assertThrows(ReportGenerationException.class, () -> report.generateMarkdownReport(impossible, analyst, List.of()),
                "an IOException must be wrapped in ReportGenerationException -- never escape raw, never be swallowed");
        assertThrows(ValidationException.class, () -> report.generateMarkdownReport(null, analyst, List.of()));
        Path fine = Files.createTempDirectory("secsuite-sec16-").resolve("r.md");
        assertThrows(ValidationException.class, () -> report.generateMarkdownReport(fine, null, List.of()));
        assertFalse(Files.exists(fine), "nothing is written when the request is rejected");
    }

    /**
     * A hand-built ingestion run (no dependency on SEC-10/SEC-11): 3 data rows, 2 parsed, 1
     * skipped (with a hostile reason), 1 inserted, 1 duplicate.
     */
    private static IngestionSummary sampleRun() {
        ThreatIntelAlert inserted = new ThreatIntelAlert(1L,
                "RUN-NEW-1", "Feed", IndicatorType.IP, "192.0.2.10", null, Severity.LOW, "d",
                Instant.parse("2026-09-01T00:00:00Z"), SeededSuite.NOW);
        ThreatIntelAlert duplicate = new ThreatIntelAlert(2L,
                "TI-SEED-001", "Feed", IndicatorType.IP, "192.0.2.11", null, Severity.LOW, "d",
                Instant.parse("2026-09-01T00:00:00Z"), SeededSuite.NOW);
        IngestionResult parsed = new IngestionResult(Path.of("data", "threat-intel-feed.csv"), 3,
                List.of(inserted, duplicate), List.of(new SkippedRow(4, "BAD-1", "<b>reason</b>")));
        return new IngestionSummary(parsed, new PersistenceResult(List.of(inserted), List.of("TI-SEED-001")));
    }
}

package org.example;

import org.example.legacy.LegacyDependencyScanner;
import org.example.model.CvssScore;
import org.example.model.Dependency;
import org.example.model.EpssScore;
import org.example.model.PortScanResult;
import org.example.model.RiskRegisterEntry;
import org.example.model.ThreatIndicator;
import org.example.parse.CvssEpssParser;
import org.example.parse.PortScanResultParser;
import org.example.parse.SbomDependencyParser;
import org.example.parse.ThreatIntelFeedParser;
import org.example.report.ExecutiveReportData;
import org.example.report.ExecutiveReportGenerator;
import org.example.service.RiskRegisterService;
import org.example.util.ParseResult;
import org.example.util.SecurityLogger;
import org.example.util.ValidationException;

import javax.print.attribute.standard.Severity;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Menu-driven simulation of a security analyst's daily workflow: ingest
 * multiple untrusted data feeds, build a prioritized risk register, and
 * generate an executive-ready report.
 *
 * CATCH SCOPE DISCIPLINE: every menu action below must catch SPECIFIC
 * exception types (ValidationException, IOException) -- never a bare
 * `catch (Exception e)`. A bare catch-all would also swallow real bugs
 * (NullPointerException, ArrayIndexOutOfBoundsException) alongside
 * expected data-quality problems, hiding defects instead of surfacing
 * them. Every catch block must also do something (log + inform the
 * analyst via reportFailure(...) below) -- none should be empty.
 *
 * The menu loop, banner, and file-reading/error-reporting helpers below
 * are infrastructure and are already implemented for you. Your work is
 * the eight private "wiring" methods that each menu option calls: read
 * the relevant data file, hand it to the matching parser/service/report
 * class you implemented elsewhere in this project, and report the
 * outcome back to the analyst.
 */
public final class SecurityAnalystConsole {

    private final Path dataDir;
    private final Path reportsDir = Paths.get("reports");

    private final List<ThreatIndicator> threatIndicators = new ArrayList<>();
    private final List<PortScanResult> portScanResults = new ArrayList<>();
    private final Map<String, CvssScore> cvssByCve = new HashMap<>();
    private final Map<String, EpssScore> epssByCve = new HashMap<>();
    private final List<Dependency> dependencies = new ArrayList<>();

    private final ExecutiveReportData reportData = new ExecutiveReportData();
    private List<RiskRegisterEntry> riskRegister = new ArrayList<>();

    public SecurityAnalystConsole(Path dataDir) {
        this.dataDir = dataDir;
    }

    public static void main(String[] args) {
        Path dataDir = args.length > 0 ? Paths.get(args[0]) : Paths.get("./src/main/java/org/example/data");
        new SecurityAnalystConsole(dataDir).run();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        printBanner();
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": loadThreatIntelFeed(); break;
                case "2": loadPortScanResults(); break;
                case "3": loadCvssEpssScores(); break;
                case "4": loadSbomDependencies(); break;
                case "5": loadLegacyDependencyScan(); break;
                case "6": buildAndShowRiskRegister(); break;
                case "7": generateExecutiveReport(); break;
                case "8": runAbuseCaseDemo(); break;
                case "0": running = false; break;
                default: System.out.println("Unrecognized option: " + choice);
            }
        }
        System.out.println("Goodbye.");
    }

    private void printBanner() {
        System.out.println("=======================================================================");
        System.out.println(" SECURE JAVA FUNDAMENTALS -- SECURITY ANALYST/CYBER DEVELOPER CONSOLE");
        System.out.println("=======================================================================");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Load & validate threat intelligence feed");
        System.out.println("2. Load & validate port scan results");
        System.out.println("3. Load & validate CVSS/EPSS scores");
        System.out.println("4. Load & validate SBOM dependencies (JSON)");
        System.out.println("5. Load legacy dependency scan log (pipe-delimited)");
        System.out.println("6. Build & view risk register summary");
        System.out.println("7. Generate executive report (File I/O)");
        System.out.println("8. Run abuse-case demo (malicious/malformed input)");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    /**
     * TODO (menu option 1): read "threat_intel_feed.json" via
     * readDataFile(...), parse it with new ThreatIntelFeedParser().parseFeed(json),
     * clear and repopulate threatIndicators from result.getAccepted(),
     * call recordIngestion("threat_intel_feed.json", result), and print a
     * summary line with accepted/rejected counts. Catch ValidationException
     * and IOException separately, routing both to reportFailure(...) with
     * a context string identifying this method.
     */
    private void loadThreatIntelFeed() {
        try {
            System.out.println("DEBUG: Starting threat intel load...");

            Path path = dataDir.resolve("threat_intel_feed.json");
            System.out.println("DEBUG: Looking for file at: " + path.toAbsolutePath());

            String json = readDataFile("threat_intel_feed.json");

            System.out.println("DEBUG: File was successfully read.");

            ThreatIntelFeedParser parser = new ThreatIntelFeedParser();
            ParseResult<ThreatIndicator> result = parser.parseFeed(json);

            System.out.println("DEBUG: File was successfully parsed.");

            threatIndicators.clear();
            threatIndicators.addAll(result.getAccepted());

            recordIngestion("threat_intel_feed.json", result);

            System.out.println(
                    "Threat Intel Feed: accepted=" +
                            result.getAcceptedCount() +
                            ", rejected=" +
                            result.getRejectedCount()
            );

        } catch (ValidationException e) {
            System.out.println("DEBUG: VALIDATION ERROR");
            e.printStackTrace();

        } catch (IOException e) {
            System.out.println("DEBUG: IO ERROR");
            e.printStackTrace();
        }
    }

    /**
     * TODO (menu option 2): same pattern as loadThreatIntelFeed(), but
     * for "port_scan_results.json" via new PortScanResultParser().parseResults(json),
     * repopulating portScanResults.
     */
    private void loadPortScanResults() {
        try {
            String json = readDataFile("port_scan_results.json");

            PortScanResultParser parser = new PortScanResultParser();
            ParseResult<PortScanResult> result = parser.parseResults(json);

            portScanResults.clear();
            portScanResults.addAll(result.getAccepted());

            recordIngestion("port_scan_results.json", result);

            System.out.println(
                    "Port Scan Results: accepted=" +
                            result.getAcceptedCount() +
                            ", rejected=" +
                            result.getRejectedCount()
            );

        } catch (ValidationException e) {
            reportFailure("loadPortScanResults: validation failure", e);

        } catch (IOException e) {
            reportFailure("loadPortScanResults: I/O failure", e);
        }
    }

    /**
     * TODO (menu option 3): read "cvss_epss_scores.json", parse it with
     * new CvssEpssParser().parseScores(json), clear cvssByCve and
     * epssByCve, then for each CvssEpssParser.ScoredCve in
     * result.getAccepted() put scored.cvss into cvssByCve keyed by
     * scored.cvss.getCveId() and scored.epss into epssByCve keyed by
     * scored.epss.getCveId(). Call recordIngestion(...) and print a
     * summary, same exception handling as the other load* methods.
     */
    private void loadCvssEpssScores() {
        try {
            String json = readDataFile("cvss_epss_scores.json");

            CvssEpssParser parser = new CvssEpssParser();
            ParseResult<CvssEpssParser.ScoredCve> result = parser.parseScores(json);

            cvssByCve.clear();
            epssByCve.clear();

            for (CvssEpssParser.ScoredCve scored : result.getAccepted()) {
                cvssByCve.put(
                        scored.cvss.getCveId(),
                        scored.cvss
                );

                epssByCve.put(
                        scored.epss.getCveId(),
                        scored.epss
                );
            }

            recordIngestion("cvss_epss_scores.json", result);

            System.out.println(
                    "CVSS/EPSS Scores: accepted=" +
                            result.getAcceptedCount() +
                            ", rejected=" +
                            result.getRejectedCount()
            );

        } catch (ValidationException e) {
            reportFailure("loadCvssEpssScores: validation failure", e);

        } catch (IOException e) {
            reportFailure("loadCvssEpssScores: I/O failure", e);
        }
    }
    /**
     * TODO (menu option 4): same pattern as loadThreatIntelFeed(), but
     * for "sbom_dependencies.json" via new SbomDependencyParser().parseDependencies(json),
     * repopulating dependencies.
     */
    private void loadSbomDependencies() {
        try {
            String json = readDataFile("sbom_dependencies.json");

            SbomDependencyParser parser = new SbomDependencyParser();
            ParseResult<Dependency> result = parser.parseDependencies(json);

            dependencies.clear();
            dependencies.addAll(result.getAccepted());

            recordIngestion("sbom_dependencies.json", result);

            System.out.println(
                    "SBOM Dependencies: accepted=" +
                            result.getAcceptedCount() +
                            ", rejected=" +
                            result.getRejectedCount()
            );

        } catch (ValidationException e) {
            reportFailure("loadSbomDependencies: validation failure", e);

        } catch (IOException e) {
            reportFailure("loadSbomDependencies: I/O failure", e);
        }
    }

    /**
     * TODO (menu option 5): resolve dataDir.resolve("legacy_dependency_scan.log"),
     * call new LegacyDependencyScanner().readLegacyScanLog(path), add
     * result.accepted to dependencies (note: ADD, do not clear first --
     * this is meant to layer on top of any dependencies already loaded
     * via option 4), and print a summary with result.accepted.size() and
     * result.rejectedCount. This class's readLegacyScanLog(...) only
     * throws IOException (not ValidationException -- malformed lines are
     * handled internally), so only one catch block is needed here.
     */
    private void loadLegacyDependencyScan() {
        try {
            Path path = dataDir.resolve("legacy_dependency_scan.log");

            LegacyDependencyScanner scanner = new LegacyDependencyScanner();
            LegacyDependencyScanner.ScanReadResult result =
                    scanner.readLegacyScanLog(path);

            dependencies.addAll(result.accepted);

            System.out.println(
                    "Legacy Dependency Scan: accepted=" +
                            result.accepted.size() +
                            ", rejected=" +
                            result.rejectedCount
            );

        } catch (IOException e) {
            reportFailure("loadLegacyDependencyScan: I/O failure", e);
        }
    }

    /**
     * TODO (menu option 6): construct a new RiskRegisterService, call
     * service.buildRegister(dependencies, cvssByCve, epssByCve) and store
     * the result in the riskRegister field, then call
     * service.countBySeverity(riskRegister) and use it to populate
     * reportData (setSeverityCounts, setTopRisks via
     * service.topRisks(riskRegister, 10), setTotalDependencies via
     * dependencies.size(), setVulnerableDependencies via counting
     * dependencies where Dependency::isVulnerable is true). Then print a
     * "Risk Register Summary" section to the console: the severity
     * counts, followed by the top risks list. (This method has no
     * checked exceptions to catch -- everything it calls is pure
     * in-memory computation.)
     */
    private void buildAndShowRiskRegister() {

        RiskRegisterService service = new RiskRegisterService();

        riskRegister = service.buildRegister(
                dependencies,
                cvssByCve,
                epssByCve
        );

        Map<String, Long> severityCounts =
                service.countBySeverity(riskRegister);

        reportData.setSeverityCounts(severityCounts);

        List<RiskRegisterEntry> topRisks =
                service.topRisks(riskRegister, 10);

        reportData.setTopRisks(topRisks);

        reportData.setTotalDependencies(
                dependencies.size()
        );

        long vulnerableCount = dependencies.stream()
                .filter(Dependency::isVulnerable)
                .count();

        reportData.setVulnerableDependencies(
                (int) vulnerableCount
        );

        System.out.println();
        System.out.println("=== Risk Register Summary ===");

        System.out.println("Severity Counts:");
        System.out.println(severityCounts);

        System.out.println();
        System.out.println("Top Risks:");

        for (RiskRegisterEntry entry : topRisks) {
            System.out.println(entry);
        }
    }

    /**
     * TODO (menu option 7): if riskRegister is empty, print a message
     * telling the analyst to run option 6 first and return early.
     * Otherwise call new ExecutiveReportGenerator().writeExecutiveSummary(
     * reportsDir, reportData); if it returns false, print a message
     * telling the analyst the report generation failed (the generator
     * itself already printed a detailed-enough reference-coded message,
     * so this is just a fallback notice).
     */
    private void generateExecutiveReport() {
        if (riskRegister.isEmpty()) {
            System.out.println(
                    "Risk register is empty. Please run option 6 first."
            );
            return;
        }

        ExecutiveReportGenerator generator =
                new ExecutiveReportGenerator();

        boolean success =
                generator.writeExecutiveSummary(
                        reportsDir,
                        reportData
                );

        if (!success) {
            System.out.println(
                    "Executive report generation failed."
            );
        }
    }

    /**
     * TODO (menu option 8, the abuse-case demo): read
     * "malicious_sample_feed.json" via readDataFile(...), parse it with
     * new ThreatIntelFeedParser().parseFeed(json), and print the accepted
     * and rejected counts plus a short explanation that this demonstrates
     * fail-closed parsing. If parseFeed(...) throws ValidationException
     * (a WHOLE-DOCUMENT rejection -- also a correct, expected outcome for
     * a sufficiently corrupted sample file), catch it separately from the
     * per-record case: call SecurityLogger.logDetailed("runAbuseCaseDemo", e)
     * to get a reference code, then print
     * SecurityLogger.userMessageFor("The sample feed was rejected outright
     * (whole-document failure).", ref). Catch IOException separately and
     * route it to reportFailure(...).
     */
    private void runAbuseCaseDemo() {
        try {
            String json = readDataFile("malicious_sample_feed.json");

            ThreatIntelFeedParser parser =
                    new ThreatIntelFeedParser();

            ParseResult<ThreatIndicator> result =
                    parser.parseFeed(json);

            System.out.println();
            System.out.println("=== Abuse Case Demo ===");
            System.out.println(
                    "Malicious Sample Feed: accepted=" +
                            result.getAcceptedCount() +
                            ", rejected=" +
                            result.getRejectedCount()
            );

            System.out.println(
                    "This demonstrates fail-closed parsing: " +
                            "invalid records are rejected rather than trusted."
            );

        } catch (ValidationException e) {
            String ref =
                    SecurityLogger.logDetailed(
                            "runAbuseCaseDemo",
                            e
                    );

            System.out.println(
                    SecurityLogger.userMessageFor(
                            "The sample feed was rejected outright " +
                                    "(whole-document failure).",
                            ref
                    )
            );

        } catch (IOException e) {
            reportFailure(
                    "runAbuseCaseDemo: I/O failure",
                    e
            );
        }
    }
    private void recordIngestion(String feedName, ParseResult<?> result) {
        List<String> sample = result.getRejectionReasons().stream().limit(3).toList();
        reportData.addIngestionStats(new ExecutiveReportData.FeedIngestionStats(
                feedName, result.getAcceptedCount(), result.getRejectedCount(), sample));
    }

    private String readDataFile(String filename) throws IOException {
        Path path = dataDir.resolve(filename);
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    /** SAFE ERROR SURFACE: generic message to the console, full detail to the server log. */
    private void reportFailure(String context, Exception e) {
        String ref = SecurityLogger.logDetailed(context, e);
        System.out.println(SecurityLogger.userMessageFor(
                "The requested operation could not be completed.", ref));
    }
}

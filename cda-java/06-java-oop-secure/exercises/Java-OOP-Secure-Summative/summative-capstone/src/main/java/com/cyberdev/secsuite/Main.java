package com.cyberdev.secsuite;

import com.cyberdev.secsuite.config.EncryptionKeyConfig;
import com.cyberdev.secsuite.config.SpringConfig;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Profiles;
import com.cyberdev.secsuite.exception.AuthenticationException;
import com.cyberdev.secsuite.exception.DuplicateFindingException;
import com.cyberdev.secsuite.exception.SecSuiteException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Analyst;
import com.cyberdev.secsuite.model.Asset;
import com.cyberdev.secsuite.model.RiskRegisterEntry;
import com.cyberdev.secsuite.model.ScanFinding;
import com.cyberdev.secsuite.repository.AnalystRepository;
import com.cyberdev.secsuite.repository.AssetRepository;
import com.cyberdev.secsuite.repository.ComponentRepository;
import com.cyberdev.secsuite.repository.CveCatalogRepository;
import com.cyberdev.secsuite.repository.RiskRegisterRepository;
import com.cyberdev.secsuite.repository.ScanFindingRepository;
import com.cyberdev.secsuite.repository.ThreatIntelAlertRepository;
import com.cyberdev.secsuite.repository.ThreatModelEntryRepository;
import com.cyberdev.secsuite.repository.ThreatModelRepository;
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
import com.cyberdev.secsuite.security.EncryptionService;
import com.cyberdev.secsuite.security.PasswordHasher;
import com.cyberdev.secsuite.service.AuthService;
import com.cyberdev.secsuite.service.CorrelationHit;
import com.cyberdev.secsuite.service.IngestionSummary;
import com.cyberdev.secsuite.service.ReportService;
import com.cyberdev.secsuite.service.RiskRegisterService;
import com.cyberdev.secsuite.service.SbomService;
import com.cyberdev.secsuite.service.ScannerService;
import com.cyberdev.secsuite.service.SkippedRow;
import com.cyberdev.secsuite.service.ThreatIntelAlertService;
import com.cyberdev.secsuite.service.ThreatIntelCsvIngestionService;
import com.cyberdev.secsuite.service.ThreatModelingService;
import com.cyberdev.secsuite.ui.ConsoleUI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * SecOps Analyst Suite -- composition root and end-to-end pipeline (SEC-17).
 *
 * Run from the project root (so the relative data/ and reports/ paths resolve):
 * <pre>
 *   mvn -q compile exec:java
 *   # or: mvn -q package && java -jar target/secsuite-capstone-student.jar
 *   # reproducible report: mvn -q compile exec:java -Dsecsuite.clock=2026-09-24T09:00:00Z
 * </pre>
 * The only interactive step is the login gate (ConsoleUI). Its answers may be piped in, e.g.
 * <pre>
 *   printf 'y\nanalyst1\nCorrect-Horse-42\nanalyst1@example.com\nanalyst1\nCorrect-Horse-42\n' \
 *       | mvn -q compile exec:java
 * </pre>
 *
 * Exit codes: 0 report written; 1 login failed; 2 pipeline failed (message printed, no stack
 * trace -- internal details are not dumped on the console).
 */
public final class Main {

    static final Path FEED_FILE = Path.of("data", "threat-intel-feed.csv");
    static final Path REPORT_FILE = Path.of("reports", "security-assessment-report.md");

    /** A stale asset id from a decommissioned host, used to prove SEC-4 fails closed. */
    private static final Long DECOMMISSIONED_ASSET = 999999L;

    private Main() {
    }

    public static void main(String[] args) {
        System.out.println("""
         ███████╗███████╗ ██████╗ ██████╗ ██████╗ ███████╗
         ██╔════╝██╔════╝██╔════╝██╔═══██╗██╔══██╗██╔════╝
         ███████╗█████╗  ██║     ██║   ██║██████╔╝███████╗
         ╚════██║██╔══╝  ██║     ██║   ██║██╔═══╝ ╚════██║
         ███████║███████╗╚██████╗╚██████╔╝██║     ███████║
         ╚══════╝╚══════╝ ╚═════╝ ╚═════╝ ╚═╝     ╚══════╝

             S E C O P S   A N A L Y S T   S U I T E
        =================================================
               Secure • Analyze • Detect • Respond
        =================================================
        """);
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(SpringConfig.class)
                .web(WebApplicationType.NONE)
                .run(args)) {
            ConsoleUI ui = context.getBean(ConsoleUI.class);
            AnalystRepository analystRepository = context.getBean(AnalystRepository.class);
            AssetRepository assetRepository = context.getBean(AssetRepository.class);
            CveCatalogRepository cveCatalogRepository = context.getBean(CveCatalogRepository.class);
            ScanFindingRepository scanFindingRepository = context.getBean(ScanFindingRepository.class);
            RiskRegisterRepository riskRegisterRepository = context.getBean(RiskRegisterRepository.class);
            ComponentRepository componentRepository = context.getBean(ComponentRepository.class);
            ThreatModelRepository threatModelRepository = context.getBean(ThreatModelRepository.class);
            ThreatModelEntryRepository threatModelEntryRepository = context.getBean(ThreatModelEntryRepository.class);
            ThreatIntelAlertRepository threatIntelAlertRepository = context.getBean(ThreatIntelAlertRepository.class);

            if (context.getEnvironment().acceptsProfiles(Profiles.of("inmemory"))) {
                InMemorySeedLoader.load(assetRepository, cveCatalogRepository, scanFindingRepository, componentRepository,
                        threatModelRepository, threatModelEntryRepository, threatIntelAlertRepository);
            }

            AuthService authService = context.getBean(AuthService.class);
            Analyst analyst = ui.runLoginGate(authService);

            // Interactive application mode: after authentication, keep the analyst in a
            // professional menu loop until Logout is selected. Each capstone workflow can be
            // run independently, which makes the application better for demonstrations and
            // lets students explore one security concept at a time.
            runInteractiveMenu(ui, analyst, REPORT_FILE, assetRepository, threatIntelAlertRepository,
                    context.getBean(ThreatIntelCsvIngestionService.class), context.getBean(ScannerService.class),
                    context.getBean(RiskRegisterService.class), context.getBean(SbomService.class),
                    context.getBean(ThreatModelingService.class), context.getBean(ThreatIntelAlertService.class),
                    context.getBean(ReportService.class));
        } catch (AuthenticationException e) {
            System.out.println("Access denied: " + e.getMessage() + ". Exiting.");
            System.exit(1);
        } catch (SecSuiteException e) {
            System.out.println("Pipeline failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.exit(2);
        }
    }

    /**
     * Interactive console shell used by the real application. The graded run(...) method below
     * intentionally remains deterministic so automated tests can still exercise the complete
     * pipeline without having to script menu choices.
     */
    private static void runInteractiveMenu(ConsoleUI ui, Analyst analyst, Path reportFile,
                                           AssetRepository assetRepository,
                                           ThreatIntelAlertRepository threatIntelAlertRepository,
                                           ThreatIntelCsvIngestionService ingestionService,
                                           ScannerService scannerService,
                                           RiskRegisterService riskRegisterService,
                                           SbomService sbomService,
                                           ThreatModelingService threatModelingService,
                                           ThreatIntelAlertService threatIntelAlertService,
                                           ReportService reportService) {
        java.util.ArrayList<IngestionSummary> ingestionRuns = new java.util.ArrayList<>();
        boolean running = true;

        while (running) {
            ui.showMainMenu(analyst.getUsername());
            int choice = ui.readMenuChoice(0, 7);

            try {
                switch (choice) {
                    case 1 -> {
                        ui.showSection("THREAT INTELLIGENCE", "Ingest and validate the external intelligence feed");
                        long before = threatIntelAlertRepository.count();
                        IngestionSummary summary = ingestionService.ingest(FEED_FILE);
                        ingestionRuns.add(summary);
                        printIngestion("Ingestion", summary);
                        for (SkippedRow row : summary.parseResult().skipped()) {
                            System.out.println("    [SKIPPED] line " + row.lineNumber() + ": " + row.reason());
                        }
                        System.out.println("  Alert rows: " + before + " -> " + threatIntelAlertRepository.count());
                    }
                    case 2 -> {
                        ui.showSection("VULNERABILITY SCAN", "Record findings and demonstrate fail-closed validation");
                        Long buildCi = requireAsset(assetRepository, "build-ci-01");
                        Long webProd = requireAsset(assetRepository, "web-prod-01");
                        Long dbProd = requireAsset(assetRepository, "db-prod-01");
                        scan(scannerService, buildCi, "CVE-2023-91001", 8080, "jenkins-http", "build-ci-01");
                        scan(scannerService, webProd, "CVE-2023-91001", 443, "https", "web-prod-01");
                        scan(scannerService, DECOMMISSIONED_ASSET, "CVE-2024-91006", 80, "http", "decommissioned host");
                        scan(scannerService, dbProd, "CVE-2099-00001", 5432, "postgresql", "db-prod-01");
                    }
                    case 3 -> {
                        ui.showSection("RISK REGISTER", "Promote open findings and register operational risks");
                        List<RiskRegisterEntry> promoted = riskRegisterService.promoteOpenFindings(analyst.getId());
                        System.out.println("  Promoted " + promoted.size() + " open finding(s) into the risk register.");
                        raiseManualRisk(riskRegisterService, requireAsset(assetRepository, "vpn-gw-01"),
                                "No MFA enforced on the VPN admin portal",
                                "Admin logins to the VPN appliance use a password only.", 3, 4, analyst.getId());
                        raiseManualRisk(riskRegisterService, requireAsset(assetRepository, "kiosk-lobby-01"),
                                "Shared local administrator password on lobby kiosks",
                                "All lobby kiosks use the same local admin password.", 3, 2, analyst.getId());
                        System.out.println("  Register now holds " + riskRegisterService.topRisks().size() + " entries.");
                    }
                    case 4 -> {
                        ui.showSection("SECURITY ANALYSIS", "Review SBOM, STRIDE models, and threat-intel correlation");
                        var components = sbomService.findVulnerableComponents();
                        long vulnerable = components.stream().filter(c -> c.isVulnerable()).count();
                        System.out.println("  SBOM components analysed : " + components.size());
                        System.out.println("  Components with CVEs      : " + vulnerable);
                        System.out.println("  STRIDE threat models      : " + threatModelingService.listThreatModels().size());
                        List<CorrelationHit> hits = threatIntelAlertService.correlateWithFindings();
                        System.out.println("  Active threat-intel hits  : " + hits.size());
                        for (CorrelationHit hit : hits) {
                            System.out.println("    [HIT] " + hit.asset().getHostname() + " <- "
                                    + hit.alert().getExternalAlertId() + " (" + hit.finding().getCveId() + ")");
                        }
                    }
                    case 5 -> {
                        ui.showSection("SQL INJECTION CHECK", "Verify asset search treats hostile input as data");
                        List<Asset> prod = assetRepository.searchByHostname("prod");
                        System.out.println("  searchByHostname(\"prod\") -> " + prod.size() + " asset(s)");
                        String payload = "' OR '1'='1";
                        List<Asset> injected = assetRepository.searchByHostname(payload);
                        System.out.println("  hostile payload -> " + injected.size()
                                + " asset(s) (expected 0; parameterized query treats it literally)");
                    }
                    case 6 -> {
                        ui.showSection("SECURITY REPORT", "Generate the current Markdown security assessment");
                        Path written = reportService.generateMarkdownReport(reportFile, analyst, ingestionRuns);
                        long size;
                        try { size = Files.size(written); } catch (java.io.IOException e) { size = -1; }
                        System.out.println("  [OK] Report written: " + written + " (" + size + " bytes)");
                    }
                    case 7 -> {
                        ui.showSection("FULL ASSESSMENT", "Run all six SecOps workflow steps in sequence");
                        runPipeline(analyst, reportFile, assetRepository, threatIntelAlertRepository,
                                ingestionService, scannerService, riskRegisterService, sbomService,
                                threatModelingService, threatIntelAlertService, reportService);
                    }
                    case 0 -> {
                        ui.showLogout(analyst.getUsername());
                        running = false;
                    }
                    default -> throw new IllegalStateException("Unexpected menu choice: " + choice);
                }
            } catch (SecSuiteException e) {
                System.out.println();
                System.out.println("  [!] Operation could not be completed: " + e.getMessage());
                System.out.println("      No internal stack trace was exposed.");
            }

            if (running) {
                ui.pause();
            }
        }
    }

    // INSTRUCTOR NOTE [SEC-17]: Concept tested: the composition root -- the ONE place in the
    // program that knows which concrete classes exist. Everything below it depends only on
    // interfaces handed to it through constructors. run() receives its two outside-world
    // dependencies (the login UI and the report path) as parameters -- main() builds the real
    // ones, the SEC-17 tests hand in scripted ones. Requirements:
    //   (a) build one instance of every InMemory* repository. The real main() uses Spring
    //       profiles for implementation selection; this deterministic grading seam stays
    //       in-memory and requires no application context or database;
    //   (b) build every service by constructor injection, sharing ONE Clock (resolveClock());
    //   (c) load the seed data (InMemory path only -- seed.sql covers the Jdbc path);
    //   (d) run the login gate (ui.runLoginGate(authService)); on failure NOTHING after it may
    //       run (AuthenticationException propagates out of run() before any pipeline step --
    //       the POS3-10 lesson);
    //   (e) hand the authenticated analyst and the wired objects to the GIVEN runPipeline(...),
    //       which runs the scripted, non-interactive pipeline: CSV ingestion (twice, proving
    //       SEC-11 idempotency), simulated scan (SEC-4, including a duplicate and two unknown
    //       references that must be refused), risk promotion (SEC-5), correlation (SEC-12), the
    //       SEC-15 search demo, and finally the Markdown report (SEC-16) at reportFile. Return
    //       the path runPipeline returns.
    // Common mistakes: a service constructing its own repository (DI defeated); creating two
    // repositories of the same type so services do not share state; the login failure printed
    // but the pipeline still running; several Clock instances; ignoring the reportFile
    // parameter and writing to a hard-coded path.
    static Path run(ConsoleUI ui, Path reportFile) {
        if (ui == null || reportFile == null) {
            throw new ValidationException("ui and reportFile must not be null");
        }
        throw new UnsupportedOperationException(
                "TODO [SEC-17]: wire shared in-memory repositories and services with one Clock, seed, authenticate, then return runPipeline(...)" +
                        "TODO: Then swap to Jdbc repositiories"

        );

        /*
            // ---- (b) security primitives + services, all constructor-injected ----------------------
            PasswordHasher passwordHasher = new PasswordHasher();
            EncryptionService encryptionService = new EncryptionService(EncryptionKeyConfig.loadOrGenerateKey());

            AuthService authService = new AuthService(analystRepository, passwordHasher, encryptionService, clock);
            ScannerService scannerService = new ScannerService(scanFindingRepository, assetRepository,
                    cveCatalogRepository, clock);
            RiskRegisterService riskRegisterService = new RiskRegisterService(riskRegisterRepository, assetRepository,
                    scanFindingRepository, cveCatalogRepository, analystRepository, clock);
            SbomService sbomService = new SbomService(componentRepository, cveCatalogRepository);
            ThreatModelingService threatModelingService = new ThreatModelingService(threatModelRepository,
                    threatModelEntryRepository, assetRepository, clock);
            ThreatIntelCsvIngestionService ingestionService = new ThreatIntelCsvIngestionService(cveCatalogRepository,
                    threatIntelAlertRepository, clock);
            ThreatIntelAlertService threatIntelAlertService = new ThreatIntelAlertService(threatIntelAlertRepository,
                    scanFindingRepository, assetRepository);
            ReportService reportService = new ReportService(assetRepository, cveCatalogRepository, scannerService,
                    riskRegisterService, sbomService, threatModelingService, threatIntelAlertService, clock);

            // ---- (c) reference data ---------------------------------------------------------------
            InMemorySeedLoader.load(assetRepository, cveCatalogRepository, scanFindingRepository, componentRepository,
                    threatModelRepository, threatModelEntryRepository, threatIntelAlertRepository);
            System.out.println("Seed data loaded: " + assetRepository.findAll().size() + " assets, "
                    + cveCatalogRepository.findAll().size() + " CVEs, " + scanFindingRepository.findAll().size()
                    + " scan findings, " + threatIntelAlertRepository.count() + " threat intel alerts.");

            // ---- (d) login gate: the ONLY interactive step -----------------------------------------
            Analyst analyst = ui.runLoginGate(authService); // throws AuthenticationException -> nothing below runs
            String email = authService.decryptContactEmail(analyst);
            System.out.println("Contact email decrypts correctly from its AES-GCM ciphertext: " + mask(email));

            // ---- (e) scripted, deterministic pipeline (GIVEN) --------------------------------------
            return runPipeline(analyst, reportFile, assetRepository, threatIntelAlertRepository, ingestionService,
                    scannerService, riskRegisterService, sbomService, threatModelingService, threatIntelAlertService,
                    reportService);
        }


         */
    }

    /**
     * GIVEN -- not graded. The scripted, deterministic pipeline that runs after a successful
     * login (step (e) of SEC-17). Every collaborator is handed in; this method constructs
     * nothing. Returns the path of the written report.
     */
    static Path runPipeline(Analyst analyst, Path reportFile, AssetRepository assetRepository,
                            ThreatIntelAlertRepository threatIntelAlertRepository,
                            ThreatIntelCsvIngestionService ingestionService, ScannerService scannerService,
                            RiskRegisterService riskRegisterService, SbomService sbomService,
                            ThreatModelingService threatModelingService,
                            ThreatIntelAlertService threatIntelAlertService, ReportService reportService) {
        System.out.println();
        System.out.println("--- Step 1: ingest threat intel feed " + FEED_FILE + " (SEC-10/SEC-11) ---");
        long before = threatIntelAlertRepository.count();
        IngestionSummary firstRun = ingestionService.ingest(FEED_FILE);
        printIngestion("Run 1", firstRun);
        for (SkippedRow row : firstRun.parseResult().skipped()) {
            System.out.println("    skipped line " + row.lineNumber() + ": " + row.reason());
        }
        long afterFirst = threatIntelAlertRepository.count();
        IngestionSummary replayRun = ingestionService.ingest(FEED_FILE);
        printIngestion("Run 2 (replay of the same file)", replayRun);
        long afterReplay = threatIntelAlertRepository.count();
        System.out.println("  Alert rows: " + before + " -> " + afterFirst + " after run 1 -> " + afterReplay
                + " after replay (" + (afterFirst == afterReplay ? "idempotent" : "NOT IDEMPOTENT") + ")");

        System.out.println();
        System.out.println("--- Step 2: simulated vulnerability scan (SEC-4) ---");
        Long buildCi = requireAsset(assetRepository, "build-ci-01");
        Long webProd = requireAsset(assetRepository, "web-prod-01");
        Long dbProd = requireAsset(assetRepository, "db-prod-01");
        scan(scannerService, buildCi, "CVE-2023-91001", 8080, "jenkins-http", "build-ci-01");
        scan(scannerService, webProd, "CVE-2023-91001", 443, "https", "web-prod-01");
        scan(scannerService, DECOMMISSIONED_ASSET, "CVE-2024-91006", 80, "http", "decommissioned host");
        scan(scannerService, dbProd, "CVE-2099-00001", 5432, "postgresql", "db-prod-01");

        System.out.println();
        System.out.println("--- Step 3: risk register (SEC-5) ---");
        List<RiskRegisterEntry> promoted = riskRegisterService.promoteOpenFindings(analyst.getId());
        System.out.println("  Promoted " + promoted.size() + " open finding(s) into the risk register.");
        raiseManualRisk(riskRegisterService, requireAsset(assetRepository, "vpn-gw-01"),
                "No MFA enforced on the VPN admin portal",
                "Admin logins to the VPN appliance use a password only.", 3, 4, analyst.getId());
        raiseManualRisk(riskRegisterService, requireAsset(assetRepository, "kiosk-lobby-01"),
                "Shared local administrator password on lobby kiosks",
                "All lobby kiosks use the same local admin password.", 3, 2, analyst.getId());
        System.out.println("  Register now holds " + riskRegisterService.topRisks().size() + " entries.");

        System.out.println();
        System.out.println("--- Step 4: SBOM, STRIDE and threat intel correlation (SEC-7/SEC-8/SEC-12) ---");
        long vulnerableComponents = sbomService.findVulnerableComponents().stream()
                .filter(c -> c.isVulnerable()).count();
        System.out.println("  SBOM: " + vulnerableComponents + " of " + sbomService.findVulnerableComponents().size()
                + " components have known CVEs.");
        System.out.println("  Threat models: " + threatModelingService.listThreatModels().size());
        List<CorrelationHit> hits = threatIntelAlertService.correlateWithFindings();
        System.out.println("  Active threat intel hits: " + hits.size());
        for (CorrelationHit hit : hits) {
            System.out.println("    " + hit.asset().getHostname() + " <- " + hit.alert().getExternalAlertId()
                    + " (" + hit.finding().getCveId() + ")");
        }

        System.out.println();
        System.out.println("--- Step 5: asset search, SQL injection regression check (SEC-15) ---");
        List<Asset> prod = assetRepository.searchByHostname("prod");
        System.out.println("  searchByHostname(\"prod\") -> " + prod.size() + " asset(s)");
        String payload = "' OR '1'='1";
        List<Asset> injected = assetRepository.searchByHostname(payload);
        System.out.println("  searchByHostname(\"" + payload + "\") -> " + injected.size()
                + " asset(s) (expected 0: the payload is treated as literal text)");

        System.out.println();
        System.out.println("--- Step 6: Markdown report (SEC-16) ---");
        Path written = reportService.generateMarkdownReport(reportFile, analyst, List.of(firstRun, replayRun));
        long size;
        try {
            size = Files.size(written);
        } catch (java.io.IOException e) {
            size = -1;
        }
        System.out.println("  Report written: " + written + " (" + size + " bytes)");
        System.out.println("=== Done ===");
        return written;
    }

    /** -Dsecsuite.clock=2026-09-24T09:00:00Z pins "now" for a byte-reproducible report; malformed fails closed. */
    public static Clock resolveClock() {
        String fixed = System.getProperty("secsuite.clock");
        if (fixed == null || fixed.isBlank()) {
            return Clock.systemUTC();
        }
        try {
            return Clock.fixed(Instant.parse(fixed.strip()), ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new ValidationException("secsuite.clock must be an ISO-8601 instant, e.g. 2026-09-24T09:00:00Z", e);
        }
    }

    private static Long requireAsset(AssetRepository assets, String hostname) {
        return assets.findByHostname(hostname)
                .orElseThrow(() -> new ValidationException("Seed asset " + hostname + " is missing"))
                .getId();
    }

    private static void scan(ScannerService scanner, Long assetId, String cveId, int port, String service,
                             String label) {
        try {
            ScanFinding finding = scanner.recordFinding(assetId, cveId, port, service);
            System.out.println("  RECORDED  " + cveId + " on " + label + " port " + finding.getPort());
        } catch (DuplicateFindingException e) {
            System.out.println("  DUPLICATE " + e.getMessage() + " -- not recorded again");
        } catch (ValidationException e) {
            System.out.println("  REJECTED  " + e.getMessage());
        }
    }

    private static void raiseManualRisk(RiskRegisterService risks, Long assetId, String title, String description,
                                        int likelihood, int impact, Long owner) {
        if (risks.findByAssetAndTitle(assetId, title).isPresent()) {
            System.out.println("  Manual risk already registered: " + title);
            return;
        }
        RiskRegisterEntry entry = risks.createEntry(assetId, null, title, description, likelihood, impact, owner);
        System.out.println("  Manual risk registered: " + entry.getTitle() + " (score " + entry.getRiskScore() + ")");
    }

    private static void printIngestion(String label, IngestionSummary summary) {
        System.out.println("  " + label + ": " + summary.parseResult().dataRowsRead() + " data rows, "
                + summary.parseResult().parsed().size() + " parsed, "
                + summary.parseResult().skipped().size() + " skipped, "
                + summary.persistenceResult().inserted().size() + " inserted, "
                + summary.persistenceResult().duplicateExternalAlertIds().size() + " duplicate(s) skipped");
    }

    /** a.analyst@example.com -> a*********@example.com: prove the round trip without printing PII. */
    private static String mask(String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***" + (at >= 0 ? email.substring(at) : "");
        }
        return email.charAt(0) + "*".repeat(at - 1) + email.substring(at);
    }
}

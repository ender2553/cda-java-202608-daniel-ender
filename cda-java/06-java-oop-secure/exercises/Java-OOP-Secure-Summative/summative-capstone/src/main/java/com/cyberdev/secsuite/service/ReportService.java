package com.cyberdev.secsuite.service;

import org.springframework.stereotype.Service;

import com.cyberdev.secsuite.event.CriticalRisk;
import com.cyberdev.secsuite.event.HighRisk;
import com.cyberdev.secsuite.event.LowRisk;
import com.cyberdev.secsuite.event.MediumRisk;
import com.cyberdev.secsuite.event.RiskAssessment;
import com.cyberdev.secsuite.exception.ReportGenerationException;
import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Analyst;
import com.cyberdev.secsuite.model.Asset;
import com.cyberdev.secsuite.model.CveCatalogEntry;
import com.cyberdev.secsuite.model.RiskRegisterEntry;
import com.cyberdev.secsuite.model.ScanFinding;
import com.cyberdev.secsuite.model.Severity;
import com.cyberdev.secsuite.model.StrideCategory;
import com.cyberdev.secsuite.model.ThreatIntelAlert;
import com.cyberdev.secsuite.model.ThreatModel;
import com.cyberdev.secsuite.model.ThreatModelEntry;
import com.cyberdev.secsuite.model.ThreatModelEntryStatus;
import com.cyberdev.secsuite.repository.AssetRepository;
import com.cyberdev.secsuite.repository.CveCatalogRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Markdown report generator that ties every module together (SEC-16).
 *
 * Split in two on purpose: {@link #buildMarkdown} is a pure function of the repositories'
 * current state (easy to test without touching the disk), and
 * {@link #generateMarkdownReport} writes that text to a file.
 *
 * Reproducibility: the report never prints generated database ids or wall-clock detection times of
 * newly recorded rows; the only time-dependent values are the "Generated" timestamp and the
 * risk due dates, both taken from the injected Clock. Run Main with
 * -Dsecsuite.clock=2026-09-24T09:00:00Z and two runs produce byte-identical reports.
 */
@Service
public class ReportService {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ISO_INSTANT;

    private final AssetRepository assetRepository;
    private final CveCatalogRepository cveCatalogRepository;
    private final ScannerService scannerService;
    private final RiskRegisterService riskRegisterService;
    private final SbomService sbomService;
    private final ThreatModelingService threatModelingService;
    private final ThreatIntelAlertService threatIntelAlertService;
    private final Clock clock;

    public ReportService(AssetRepository assetRepository, CveCatalogRepository cveCatalogRepository,
                         ScannerService scannerService, RiskRegisterService riskRegisterService,
                         SbomService sbomService, ThreatModelingService threatModelingService,
                         ThreatIntelAlertService threatIntelAlertService, Clock clock) {
        if (assetRepository == null || cveCatalogRepository == null || scannerService == null
                || riskRegisterService == null || sbomService == null || threatModelingService == null
                || threatIntelAlertService == null || clock == null) {
            throw new ValidationException("ReportService dependencies must not be null");
        }
        this.assetRepository = assetRepository;
        this.cveCatalogRepository = cveCatalogRepository;
        this.scannerService = scannerService;
        this.riskRegisterService = riskRegisterService;
        this.sbomService = sbomService;
        this.threatModelingService = threatModelingService;
        this.threatIntelAlertService = threatIntelAlertService;
        this.clock = clock;
    }

    // INSTRUCTOR NOTE [SEC-16]: Concept tested: composing every module into one artifact and
    // writing it safely. generateMarkdownReport must: (1) reject a null outputPath/preparedBy
    // (ValidationException); (2) build the full document via buildMarkdown; (3) create the
    // parent directory if needed (Files.createDirectories); (4) write UTF-8 text; (5) wrap ANY
    // IOException in ReportGenerationException (never let a raw IOException escape, never
    // swallow it and return as if the report exists). This implementation writes to a
    // temporary sibling file and then moves it into place (atomically where the filesystem
    // supports it), so a crash mid-write can never leave a half-written report that looks
    // complete -- a nice-to-have, not a grading requirement.
    // Common mistakes: forgetting createDirectories (works on the author's machine where
    // reports/ already exists, fails on a clean checkout); catching IOException and printing a
    // stack trace, then carrying on; writing with the platform default charset.
    public Path generateMarkdownReport(Path outputPath, Analyst preparedBy, List<IngestionSummary> ingestionRuns) {
        if (outputPath == null) {
            throw new ValidationException("outputPath must not be null");
        }

        if (preparedBy == null) {
            throw new ValidationException("preparedBy must not be null");
        }

        String markdown = buildMarkdown(preparedBy, ingestionRuns);

        try {
            Path parent = outputPath.toAbsolutePath().getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            Path tempFile = Files.createTempFile(
                    parent,
                    outputPath.getFileName().toString(),
                    ".tmp"
            );

            try {
                Files.writeString(
                        tempFile,
                        markdown,
                        StandardCharsets.UTF_8
                );

                try {
                    Files.move(
                            tempFile,
                            outputPath,
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE
                    );
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(
                            tempFile,
                            outputPath,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            } finally {
                Files.deleteIfExists(tempFile);
            }

            return outputPath;

        } catch (IOException e) {
            throw new ReportGenerationException(
                    "Unable to generate Markdown report at " + outputPath,
                    e
            );
        }
    }

    // INSTRUCTOR NOTE [SEC-16]: Concept tested: every module gets its own "## " section EVEN
    // WHEN IT HAS NO DATA -- an explicit "_None._" line, never a silently missing heading (a
    // reader cannot tell "no correlated hits" from "the correlation step never ran" if the
    // section just isn't there). Sections, in order: Asset Inventory; Open Findings by
    // Severity (all five buckets via ScannerService.openFindingsBySeverity -> SEC-2); Risk
    // Register (sorted by riskScore desc via RiskRegisterService.topRisks, band label from
    // assess() through an exhaustive switch -> SEC-5); SBOM Vulnerable Components (SEC-7,
    // components with zero CVEs listed as such); STRIDE Coverage (SEC-8, all six categories
    // per model, 0 shown, gaps called out); Threat Intelligence Alerts (the viewer: every
    // stored alert); Threat Intel Correlation (SEC-12); CSV Ingestion Summary (SEC-10/SEC-11:
    // per-run parsed/skipped/inserted/duplicate counts plus every skip reason).
    // SECURITY CALLOUT -- OUTPUT ENCODING: descriptions, sources, indicator values and skip
    // reasons originate from third-party feeds and seed data. Every data value goes through
    // escapeMd() before it lands in the document: Markdown/HTML metacharacters are neutralized
    // (so a feed description containing "<script>" or "|" cannot inject HTML into a rendered
    // report or break a table), and control characters/newlines are flattened (so a value
    // cannot forge extra lines or headings). Encode ALL data values uniformly -- deciding
    // field-by-field which ones are "trusted" is how the one you got wrong becomes the hole.
    public String buildMarkdown(Analyst preparedBy, List<IngestionSummary> ingestionRuns) {
        if (preparedBy == null) {
            throw new ValidationException("preparedBy must not be null");
        }

        if (ingestionRuns == null) {
            throw new ValidationException("ingestionRuns must not be null");
        }

        List<Asset> assets = assetRepository.findAll();

        Map<Long, Asset> assetsById = new LinkedHashMap<>();
        for (Asset asset : assets) {
            assetsById.put(asset.getId(), asset);
        }

        StringBuilder md = new StringBuilder();

        md.append("# SecOps Analyst Suite -- Security Assessment Report\n");
        md.append("- **Generated:** ")
                .append(escapeMd(TIMESTAMP.format(clock.instant())))
                .append("\n");
        md.append("- **Prepared by:** ")
                .append(escapeMd(preparedBy.getUsername()))
                .append("\n");
        md.append("- **Scope:** ")
                .append(escapeMd(String.valueOf(assets.size())))
                .append(" assets, ")
                .append(escapeMd(String.valueOf(cveCatalogRepository.findAll().size())))
                .append(" catalogued CVEs\n\n");

        md.append("> This report contains fictional training data for the SecOps Analyst Suite capstone.\n\n");
        appendAssetInventory(md, assets);
        appendOpenFindings(md, assetsById);
        appendRiskRegister(md, assetsById);
        appendSbom(md);
        appendStrideCoverage(md, assetsById);
        appendAlerts(md);
        appendCorrelation(md);
        appendIngestionSummary(md, ingestionRuns);

        return md.toString();
    }

    private void appendAssetInventory(StringBuilder md, List<Asset> assets) {
        md.append("## 1. Asset Inventory\n\n");
        if (assets.isEmpty()) {
            md.append("_None._\n\n");
            return;
        }
        md.append("| Hostname | IP address | Owner team | Criticality |\n");
        md.append("|---|---|---|---|\n");
        for (Asset a : assets) {
            md.append("| ").append(escapeMd(a.getHostname())).append(" | ").append(escapeMd(a.getIpAddress()))
                    .append(" | ").append(escapeMd(a.getOwnerTeam())).append(" | ").append(a.getCriticality()).append(" |\n");
        }
        md.append('\n');
    }

    private void appendOpenFindings(StringBuilder md, Map<Long, Asset> assetsById) {
        md.append("## 2. Open Scan Findings by Severity\n\n");
        Map<Severity, List<ScanFinding>> grouped = scannerService.openFindingsBySeverity();
        int total = grouped.values().stream().mapToInt(List::size).sum();
        md.append("Severity is derived from each CVE's CVSS v3 base score (0.0 NONE, 0.1-3.9 LOW, 4.0-6.9 MEDIUM, "
                + "7.0-8.9 HIGH, 9.0-10.0 CRITICAL). Total open findings: **").append(total).append("**.\n\n");
        md.append("| Severity | Open findings |\n|---|---|\n");
        for (Map.Entry<Severity, List<ScanFinding>> bucket : grouped.entrySet()) {
            md.append("| ").append(bucket.getKey()).append(" | ").append(bucket.getValue().size()).append(" |\n");
        }
        md.append('\n');
        for (Map.Entry<Severity, List<ScanFinding>> bucket : grouped.entrySet()) {
            md.append("### ").append(bucket.getKey()).append(" (").append(bucket.getValue().size()).append(")\n\n");
            if (bucket.getValue().isEmpty()) {
                md.append("_None._\n\n");
                continue;
            }
            md.append("| Host | CVE | CVSS | Port/Service | Description |\n|---|---|---|---|---|\n");
            for (ScanFinding f : bucket.getValue()) {
                CveCatalogEntry cve = cveCatalogRepository.findById(f.getCveId())
                        .orElseThrow(() -> new ValidationException("Unknown CVE " + f.getCveId()));
                Asset asset = assetsById.get(f.getAssetId());
                md.append("| ").append(asset == null ? "(unknown asset)" : escapeMd(asset.getHostname()))
                        .append(" | ").append(escapeMd(cve.cveId()))
                        .append(" | ").append(cve.cvssScore().toPlainString())
                        .append(" | ").append(f.getPort() == null ? "-" : f.getPort())
                        .append('/').append(f.getServiceName() == null ? "-" : escapeMd(f.getServiceName()))
                        .append(" | ").append(escapeMd(cve.description())).append(" |\n");
            }
            md.append('\n');
        }
    }

    private void appendRiskRegister(StringBuilder md, Map<Long, Asset> assetsById) {
        md.append("## 3. Risk Register -- Top Risks\n\n");
        List<RiskRegisterEntry> risks = riskRegisterService.topRisks();
        md.append("Sorted by risk score (likelihood x impact) descending. Bands: 1-6 Low, 7-12 Medium, "
                + "13-19 High, 20-25 Critical. Total entries: **").append(risks.size()).append("**.\n\n");
        if (risks.isEmpty()) {
            md.append("_None._\n\n");
            return;
        }
        md.append("| # | Band | Score | L x I | Asset | Title | Status | Due |\n");
        md.append("|---|---|---|---|---|---|---|---|\n");
        int rank = 1;
        for (RiskRegisterEntry r : risks) {
            RiskAssessment assessment = riskRegisterService.assess(r.getLikelihood(), r.getImpact());
            Asset asset = assetsById.get(r.getAssetId());
            md.append("| ").append(rank++)
                    .append(" | ").append(bandLabel(assessment))
                    .append(" | ").append(r.getRiskScore())
                    .append(" | ").append(r.getLikelihood()).append(" x ").append(r.getImpact())
                    .append(" | ").append(asset == null ? "(unknown asset)" : escapeMd(asset.getHostname()))
                    .append(" | ").append(escapeMd(r.getTitle()))
                    .append(" | ").append(r.getStatus())
                    .append(" | ").append(r.getDueDate() == null ? "-" : r.getDueDate())
                    .append(" |\n");
        }
        md.append('\n');
    }

    /** Exhaustive switch over the sealed RiskAssessment -- no default branch (SEC-5). */
    private static String bandLabel(RiskAssessment assessment) {
        return switch (assessment) {
            case CriticalRisk c -> "**CRITICAL**";
            case HighRisk h -> "HIGH";
            case MediumRisk m -> "MEDIUM";
            case LowRisk l -> "LOW";
        };
    }

    private void appendSbom(StringBuilder md) {
        md.append("## 4. SBOM -- Vulnerable Components\n\n");
        List<ComponentVulnerabilities> components = sbomService.findVulnerableComponents();
        long vulnerable = components.stream().filter(ComponentVulnerabilities::isVulnerable).count();
        md.append("Components analysed: **").append(components.size()).append("**, with known vulnerabilities: **")
                .append(vulnerable).append("**.\n\n");
        if (components.isEmpty()) {
            md.append("_None._\n\n");
            return;
        }
        md.append("| Application | Component | Version | Ecosystem | Known CVEs | Worst severity |\n");
        md.append("|---|---|---|---|---|---|\n");
        for (ComponentVulnerabilities cv : components) {
            String cves;
            if (cv.cves().isEmpty()) {
                cves = "No known vulnerabilities";
            } else {
                StringBuilder list = new StringBuilder();
                for (CveCatalogEntry cve : cv.cves()) {
                    if (list.length() > 0) {
                        list.append(", ");
                    }
                    list.append(escapeMd(cve.cveId())).append(" (").append(cve.cvssScore().toPlainString()).append(')');
                }
                cves = list.toString();
            }
            md.append("| ").append(escapeMd(cv.component().getApplicationName()))
                    .append(" | ").append(escapeMd(cv.component().getComponentName()))
                    .append(" | ").append(escapeMd(cv.component().getComponentVersion()))
                    .append(" | ").append(escapeMd(cv.component().getEcosystem()))
                    .append(" | ").append(cves)
                    .append(" | ").append(cv.highestSeverity().map(Enum::name).orElse("-"))
                    .append(" |\n");
        }
        md.append('\n');
    }

    private void appendStrideCoverage(StringBuilder md, Map<Long, Asset> assetsById) {
        md.append("## 5. STRIDE Threat Model Coverage\n\n");
        List<ThreatModel> models = threatModelingService.listThreatModels();
        if (models.isEmpty()) {
            md.append("_None._\n\n");
            return;
        }
        for (ThreatModel model : models) {
            Asset asset = assetsById.get(model.getAssetId());
            md.append("### ").append(escapeMd(model.getTitle())).append(" (")
                    .append(asset == null ? "unknown asset" : escapeMd(asset.getHostname())).append(")\n\n");
            Map<StrideCategory, List<ThreatModelEntry>> coverage = threatModelingService.strideCoverage(model.getId());
            md.append("| STRIDE category | Violates | Entries | Open (IDENTIFIED) |\n|---|---|---|---|\n");
            StringBuilder gaps = new StringBuilder();
            for (Map.Entry<StrideCategory, List<ThreatModelEntry>> e : coverage.entrySet()) {
                long open = e.getValue().stream()
                        .filter(t -> t.getStatus() == ThreatModelEntryStatus.IDENTIFIED)
                        .count();
                md.append("| ").append(e.getKey().displayName())
                        .append(" | ").append(e.getKey().violatedProperty())
                        .append(" | ").append(e.getValue().size())
                        .append(" | ").append(open).append(" |\n");
                if (e.getValue().isEmpty()) {
                    if (gaps.length() > 0) {
                        gaps.append(", ");
                    }
                    gaps.append(e.getKey().displayName());
                }
            }
            md.append('\n');
            if (gaps.length() > 0) {
                md.append("> **Coverage gap:** no threats recorded for ").append(gaps)
                        .append(". Either these were analysed and none apply (record that explicitly) or they "
                                + "were never considered.\n\n");
            } else {
                md.append("All six STRIDE categories have at least one recorded threat.\n\n");
            }
            for (Map.Entry<StrideCategory, List<ThreatModelEntry>> e : coverage.entrySet()) {
                for (ThreatModelEntry t : e.getValue()) {
                    md.append("- **").append(e.getKey().displayName()).append("** [").append(t.getStatus()).append("] ")
                            .append(escapeMd(t.getDescription()));
                    if (t.getMitigation() != null) {
                        md.append(" -- _Mitigation:_ ").append(escapeMd(t.getMitigation()));
                    }
                    md.append('\n');
                }
            }
            md.append('\n');
        }
    }

    private void appendAlerts(StringBuilder md) {
        md.append("## 6. Threat Intelligence Alerts\n\n");
        List<ThreatIntelAlert> alerts = threatIntelAlertService.listAlerts();
        md.append("Every stored alert (seeded and CSV-ingested). Feed text is untrusted and is output-encoded "
                + "below. Total alerts: **").append(alerts.size()).append("**.\n\n");
        if (alerts.isEmpty()) {
            md.append("_None._\n\n");
            return;
        }
        md.append("| Alert | Source | Type | Indicator | Related CVE | Severity | Published | Description |\n");
        md.append("|---|---|---|---|---|---|---|---|\n");
        for (ThreatIntelAlert a : alerts) {
            md.append("| ").append(escapeMd(a.getExternalAlertId()))
                    .append(" | ").append(escapeMd(a.getSource()))
                    .append(" | ").append(a.getIndicatorType())
                    .append(" | ").append(escapeMd(a.getIndicatorValue()))
                    .append(" | ").append(a.getRelatedCveId() == null ? "-" : escapeMd(a.getRelatedCveId()))
                    .append(" | ").append(a.getSeverity())
                    .append(" | ").append(a.getPublishedAt() == null ? "-" : TIMESTAMP.format(a.getPublishedAt()))
                    .append(" | ").append(a.getDescription() == null ? "-" : escapeMd(a.getDescription()))
                    .append(" |\n");
        }
        md.append('\n');
    }

    private void appendCorrelation(StringBuilder md) {
        md.append("## 7. Threat Intelligence Correlation (Active Hits)\n\n");
        List<CorrelationHit> hits = threatIntelAlertService.correlateWithFindings();
        md.append("CVE-type alerts matched against **OPEN** scan findings only (resolved findings and non-CVE "
                + "indicators are never counted). Active hits: **").append(hits.size()).append("**.\n\n");
        if (hits.isEmpty()) {
            md.append("_None._\n\n");
            return;
        }
        md.append("| Host | CVE | Alert | Source | Alert severity | Alert description |\n");
        md.append("|---|---|---|---|---|---|\n");
        for (CorrelationHit h : hits) {
            md.append("| ").append(escapeMd(h.asset().getHostname()))
                    .append(" | ").append(escapeMd(h.finding().getCveId()))
                    .append(" | ").append(escapeMd(h.alert().getExternalAlertId()))
                    .append(" | ").append(escapeMd(h.alert().getSource()))
                    .append(" | ").append(h.alert().getSeverity())
                    .append(" | ").append(h.alert().getDescription() == null ? "-" : escapeMd(h.alert().getDescription()))
                    .append(" |\n");
        }
        md.append('\n');
    }

    private void appendIngestionSummary(StringBuilder md, List<IngestionSummary> runs) {
        md.append("## 8. Threat Intel CSV Ingestion Summary\n\n");
        if (runs.isEmpty()) {
            md.append("_None._ No CSV ingestion was run.\n\n");
            return;
        }
        md.append("| Run | File | Data rows | Parsed | Skipped | Inserted | Duplicates (not re-inserted) |\n");
        md.append("|---|---|---|---|---|---|---|\n");
        int runNumber = 1;
        for (IngestionSummary run : runs) {
            IngestionResult p = run.parseResult();
            PersistenceResult s = run.persistenceResult();
            Path fileName = p.source().getFileName();
            md.append("| ").append(runNumber++)
                    .append(" | ").append(escapeMd(fileName == null ? p.source().toString() : fileName.toString()))
                    .append(" | ").append(p.dataRowsRead())
                    .append(" | ").append(p.parsed().size())
                    .append(" | ").append(p.skipped().size())
                    .append(" | ").append(s.inserted().size())
                    .append(" | ").append(s.duplicateExternalAlertIds().size())
                    .append(" |\n");
        }
        md.append('\n');

        IngestionSummary first = runs.get(0);
        md.append("### Skipped rows (run 1)\n\n");
        if (first.parseResult().skipped().isEmpty()) {
            md.append("_None._\n\n");
        } else {
            md.append("| Line | Claimed alert id | Reason |\n|---|---|---|\n");
            for (SkippedRow row : first.parseResult().skipped()) {
                md.append("| ").append(row.lineNumber())
                        .append(" | ").append(row.externalAlertId() == null || row.externalAlertId().isBlank()
                                ? "-" : escapeMd(row.externalAlertId()))
                        .append(" | ").append(escapeMd(row.reason())).append(" |\n");
            }
            md.append('\n');
        }
        md.append("### Duplicates skipped (run 1)\n\n");
        List<String> dups = first.persistenceResult().duplicateExternalAlertIds();
        if (dups.isEmpty()) {
            md.append("_None._\n\n");
        } else {
            for (String id : dups) {
                md.append("- ").append(escapeMd(id)).append(" (already stored -- kept the existing alert, did not overwrite)\n");
            }
            md.append('\n');
        }
    }

    /**
     * Output-encodes one data value for Markdown: control characters (including CR/LF) become
     * spaces; HTML-significant characters become entities; Markdown metacharacters that could
     * start emphasis, code, links or table cells are backslash-escaped.
     */
    static String escapeMd(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isISOControl(c)) {
                out.append(' ');
                continue;
            }
            switch (c) {
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '&' -> out.append("&amp;");
                case '\\', '`', '*', '_', '[', ']', '|', '#' -> out.append('\\').append(c);
                default -> out.append(c);
            }
        }
        return out.toString();
    }
}

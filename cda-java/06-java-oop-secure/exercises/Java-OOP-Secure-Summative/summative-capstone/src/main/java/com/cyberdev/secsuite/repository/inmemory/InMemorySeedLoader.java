package com.cyberdev.secsuite.repository.inmemory;

import com.cyberdev.secsuite.exception.ValidationException;
import com.cyberdev.secsuite.model.Asset;
import com.cyberdev.secsuite.model.Component;
import com.cyberdev.secsuite.model.Criticality;
import com.cyberdev.secsuite.model.CveCatalogEntry;
import com.cyberdev.secsuite.model.FindingStatus;
import com.cyberdev.secsuite.model.IndicatorType;
import com.cyberdev.secsuite.model.ScanFinding;
import com.cyberdev.secsuite.model.Severity;
import com.cyberdev.secsuite.model.StrideCategory;
import com.cyberdev.secsuite.model.ThreatIntelAlert;
import com.cyberdev.secsuite.model.ThreatModel;
import com.cyberdev.secsuite.model.ThreatModelEntry;
import com.cyberdev.secsuite.model.ThreatModelEntryStatus;
import com.cyberdev.secsuite.repository.AssetRepository;
import com.cyberdev.secsuite.repository.ComponentRepository;
import com.cyberdev.secsuite.repository.CveCatalogRepository;
import com.cyberdev.secsuite.repository.ScanFindingRepository;
import com.cyberdev.secsuite.repository.ThreatIntelAlertRepository;
import com.cyberdev.secsuite.repository.ThreatModelEntryRepository;
import com.cyberdev.secsuite.repository.ThreatModelRepository;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * GIVEN INFRASTRUCTURE -- not a graded TODO.
 *
 * Loads EXACTLY the content of schema/seed.sql -- same numeric ids, hostnames, CVE ids, scores,
 * timestamps -- into the given repositories. Main calls this ONLY on the InMemory* path; when
 * the Jdbc* repositories are wired in, seed.sql has already put this data in Postgres and this
 * loader must NOT be called (it would collide with the existing primary keys).
 *
 * KEEP IN SYNC WITH schema/seed.sql. The instructor build verified the two against each other
 * row-for-row (see the phase-1 build notes); if you edit one, edit the other.
 *
 * Every row goes through the real model constructors, so the seed data is itself validated by
 * SEC-1 (Asset) and the other fail-closed constructors -- a typo in an IP address here fails
 * loudly at startup instead of producing a subtly wrong report.
 */
public final class InMemorySeedLoader {

    // ---- well-known seed ids (shared with seed.sql) ----
    public static final Long WEB_PROD_01 = 1L;
    public static final Long DB_PROD_01 = 2L;
    public static final Long VPN_GW_01 = 3L;
    public static final Long HR_PORTAL_01 = 4L;
    public static final Long BUILD_CI_01 = 5L;
    public static final Long KIOSK_LOBBY_01 = 6L;

    public static final Long STOREFRONT_THREAT_MODEL = 1L;
    public static final Long VPN_THREAT_MODEL = 2L;

    private InMemorySeedLoader() {
    }

    public static void load(AssetRepository assets,
                            CveCatalogRepository cves,
                            ScanFindingRepository findings,
                            ComponentRepository components,
                            ThreatModelRepository threatModels,
                            ThreatModelEntryRepository threatModelEntries,
                            ThreatIntelAlertRepository alerts) {
        if (assets == null || cves == null || findings == null || components == null
                || threatModels == null || threatModelEntries == null || alerts == null) {
            throw new ValidationException("all repositories must be non-null");
        }
        loadAssets(assets);
        loadCves(cves);
        loadFindings(findings);
        loadComponents(components);
        loadThreatModels(threatModels, threatModelEntries);
        loadAlerts(alerts);
    }

    private static void loadAssets(AssetRepository assets) {
        assets.save(new Asset(WEB_PROD_01, "web-prod-01", "10.0.1.10", "Platform Engineering", Criticality.CRITICAL));
        assets.save(new Asset(DB_PROD_01, "db-prod-01", "10.0.2.20", "Data Platform", Criticality.CRITICAL));
        assets.save(new Asset(VPN_GW_01, "vpn-gw-01", "10.0.0.5", "Network Operations", Criticality.HIGH));
        assets.save(new Asset(HR_PORTAL_01, "hr-portal-01", "10.0.3.15", "Corporate IT", Criticality.MEDIUM));
        assets.save(new Asset(BUILD_CI_01, "build-ci-01", "10.0.4.40", "DevOps", Criticality.HIGH));
        assets.save(new Asset(KIOSK_LOBBY_01, "kiosk-lobby-01", "10.0.9.99", "Facilities", Criticality.LOW));
    }

    private static void loadCves(CveCatalogRepository cves) {
        cve(cves, "CVE-2023-91001", "(Fictional) Remote code execution via crafted log message lookup in the LogLite logging library", "10.0");
        cve(cves, "CVE-2023-91002", "(Fictional) Authentication bypass in the web portal of the GateKeeper VPN appliance", "9.0");
        cve(cves, "CVE-2023-91003", "(Fictional) SQL injection in the fastform Python form-handling package", "8.9");
        cve(cves, "CVE-2023-91004", "(Fictional) Unsafe deserialization of polymorphic types in the jsonkit JSON library", "7.0");
        cve(cves, "CVE-2023-91005", "(Fictional) Stored cross-site scripting in admin-ui-widgets data table component", "6.9");
        cve(cves, "CVE-2024-91006", "(Fictional) Information disclosure through verbose error pages in templating-core", "5.3");
        cve(cves, "CVE-2024-91007", "(Fictional) Open redirect in the login return-URL handling of admin-ui-widgets", "4.0");
        cve(cves, "CVE-2024-91008", "(Fictional) Weak default TLS cipher suites negotiated by the tlswrap package", "3.9");
        cve(cves, "CVE-2024-91009", "(Fictional) Minor timing side channel in date-helper locale lookup", "0.1");
        cve(cves, "CVE-2024-91010", "(Fictional) Disputed informational report: server banner reveals product name", "0.0");
    }

    private static void loadFindings(ScanFindingRepository findings) {
        finding(findings, 1L, WEB_PROD_01, "CVE-2023-91001", 443, "https", "2026-08-10T09:00:00Z", FindingStatus.OPEN);
        finding(findings, 2L, WEB_PROD_01, "CVE-2023-91005", 443, "https", "2026-08-10T09:05:00Z", FindingStatus.OPEN);
        finding(findings, 3L, DB_PROD_01, "CVE-2023-91004", 5432, "postgresql", "2026-08-11T10:00:00Z", FindingStatus.OPEN);
        finding(findings, 4L, VPN_GW_01, "CVE-2023-91002", 443, "vpn-web", "2026-08-11T11:00:00Z", FindingStatus.OPEN);
        finding(findings, 5L, HR_PORTAL_01, "CVE-2023-91003", 8080, "http", "2026-07-01T08:00:00Z", FindingStatus.RESOLVED);
        finding(findings, 6L, HR_PORTAL_01, "CVE-2024-91007", 8080, "http", "2026-08-12T08:30:00Z", FindingStatus.OPEN);
        finding(findings, 7L, BUILD_CI_01, "CVE-2024-91008", 22, "ssh", "2026-08-12T14:00:00Z", FindingStatus.OPEN);
        finding(findings, 8L, KIOSK_LOBBY_01, "CVE-2024-91010", 80, "http", "2026-08-13T07:45:00Z", FindingStatus.OPEN);
    }

    private static void loadComponents(ComponentRepository components) {
        Long loglite = component(components, 1L, "storefront-web", "org.fictional:loglite", "2.14.0", "Maven");
        Long jsonkit = component(components, 2L, "storefront-web", "com.example:jsonkit", "1.9.2", "Maven");
        component(components, 3L, "storefront-web", "org.example:templating-core", "3.1.0", "Maven");
        Long widgets = component(components, 4L, "storefront-web", "admin-ui-widgets", "4.2.1", "npm");
        Long fastform = component(components, 5L, "hr-portal", "fastform", "0.9.3", "PyPI");
        component(components, 6L, "hr-portal", "requests-lite", "2.31.0", "PyPI");
        Long tlswrap = component(components, 7L, "hr-portal", "tlswrap", "1.0.4", "PyPI");
        component(components, 8L, "hr-portal", "date-helper", "1.2.0", "npm");

        components.linkCve(loglite, "CVE-2023-91001");
        components.linkCve(jsonkit, "CVE-2023-91004");
        components.linkCve(widgets, "CVE-2023-91005");
        components.linkCve(widgets, "CVE-2024-91007");
        components.linkCve(fastform, "CVE-2023-91003");
        components.linkCve(tlswrap, "CVE-2024-91008");
    }

    private static void loadThreatModels(ThreatModelRepository models, ThreatModelEntryRepository entries) {
        models.save(new ThreatModel(STOREFRONT_THREAT_MODEL, WEB_PROD_01, "Customer storefront web tier",
                "Public-facing web servers handling login, browsing and checkout.", Instant.parse("2026-08-01T12:00:00Z")));
        models.save(new ThreatModel(VPN_THREAT_MODEL, VPN_GW_01, "Remote access VPN gateway",
                "Internet-facing VPN appliance used by all remote staff.", Instant.parse("2026-08-02T12:00:00Z")));

        // Storefront: NO REPUDIATION, NO ELEVATION_OF_PRIVILEGE (deliberate gap for SEC-8).
        entry(entries, 1L, STOREFRONT_THREAT_MODEL, StrideCategory.SPOOFING,
                "Credential stuffing against the customer login endpoint", "Rate limiting plus MFA for high-value accounts", ThreatModelEntryStatus.MITIGATED);
        entry(entries, 2L, STOREFRONT_THREAT_MODEL, StrideCategory.TAMPERING,
                "Price parameter manipulation in checkout requests", "Server-side price lookup; never trust client-supplied totals", ThreatModelEntryStatus.MITIGATED);
        entry(entries, 3L, STOREFRONT_THREAT_MODEL, StrideCategory.INFORMATION_DISCLOSURE,
                "Verbose stack traces returned on HTTP 500 errors", "Generic error pages; log details server-side only", ThreatModelEntryStatus.IDENTIFIED);
        entry(entries, 4L, STOREFRONT_THREAT_MODEL, StrideCategory.INFORMATION_DISCLOSURE,
                "Session tokens exposed in URL query strings", null, ThreatModelEntryStatus.IDENTIFIED);
        entry(entries, 5L, STOREFRONT_THREAT_MODEL, StrideCategory.DENIAL_OF_SERVICE,
                "Unbounded search queries exhausting database connections", "Query timeouts and pagination limits", ThreatModelEntryStatus.ACCEPTED);

        // VPN: all six categories covered (contrast case).
        entry(entries, 6L, VPN_THREAT_MODEL, StrideCategory.SPOOFING,
                "Stolen VPN credentials reused by an attacker", "Client certificate plus MFA authentication", ThreatModelEntryStatus.MITIGATED);
        entry(entries, 7L, VPN_THREAT_MODEL, StrideCategory.TAMPERING,
                "Gateway configuration altered via the exposed admin interface", "Admin interface reachable from the management VLAN only", ThreatModelEntryStatus.MITIGATED);
        entry(entries, 8L, VPN_THREAT_MODEL, StrideCategory.REPUDIATION,
                "Shared admin account prevents attributing configuration changes", "Named admin accounts with centralized audit logging", ThreatModelEntryStatus.IDENTIFIED);
        entry(entries, 9L, VPN_THREAT_MODEL, StrideCategory.INFORMATION_DISCLOSURE,
                "Split tunneling leaks internal DNS queries", "Force-tunnel DNS for corporate domains", ThreatModelEntryStatus.IDENTIFIED);
        entry(entries, 16L, VPN_THREAT_MODEL, StrideCategory.DENIAL_OF_SERVICE,
                "Pre-authentication handshake flood exhausts the session table", "Upstream rate limiting", ThreatModelEntryStatus.ACCEPTED);
        entry(entries, 17L, VPN_THREAT_MODEL, StrideCategory.ELEVATION_OF_PRIVILEGE,
                "Authentication bypass (CVE-2023-91002) grants an admin session", "Apply vendor patch; restrict the admin portal", ThreatModelEntryStatus.IDENTIFIED);
    }

    private static void loadAlerts(ThreatIntelAlertRepository alerts) {
        alert(alerts, 1L, "TI-SEED-001", "NorthStar ISAC", IndicatorType.CVE,
                "CVE-2023-91001", "CVE-2023-91001", Severity.CRITICAL,
                "Active exploitation of LogLite message-lookup RCE observed against retail sector",
                "2026-08-20T06:00:00Z", "2026-08-20T06:05:00Z");
        alert(alerts, 2L, "TI-SEED-002", "PartnerCERT", IndicatorType.CVE,
                "CVE-2023-91003", "CVE-2023-91003", Severity.HIGH,
                "Proof-of-concept exploit released for fastform SQL injection",
                "2026-08-21T09:00:00Z", "2026-08-21T09:10:00Z");
        alert(alerts, 3L, "TI-SEED-003", "Internal SOC", IndicatorType.IP,
                "203.0.113.45", null, Severity.HIGH,
                "Source of repeated brute-force attempts against the VPN portal",
                "2026-08-22T14:30:00Z", "2026-08-22T14:31:00Z");
        alert(alerts, 4L, "TI-SEED-004", "NorthStar ISAC", IndicatorType.DOMAIN,
                "login-secure-update.example", null, Severity.MEDIUM,
                "Credential phishing domain impersonating the corporate SSO page",
                "2026-08-23T08:00:00Z", "2026-08-23T08:02:00Z");
        alert(alerts, 5L, "TI-SEED-005", "PartnerCERT", IndicatorType.FILE_HASH,
                "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08", "CVE-2023-91004", Severity.HIGH,
                "Malware sample bundling a jsonkit deserialization exploit",
                "2026-08-24T11:00:00Z", "2026-08-24T11:05:00Z");
    }

    // ---- small builders keep the tables above readable ----

    private static void cve(CveCatalogRepository repo, String id, String description, String score) {
        repo.save(new CveCatalogEntry(id, description, new BigDecimal(score)));
    }

    private static void finding(ScanFindingRepository repo, Long id, Long assetId, String cveId, int port,
                                String service, String detectedAt, FindingStatus status) {
        repo.save(new ScanFinding(id, assetId, cveId, port, service, Instant.parse(detectedAt), status));
    }

    private static Long component(ComponentRepository repo, Long id, String app, String name, String version,
                                  String ecosystem) {
        repo.save(new Component(id, app, name, version, ecosystem));
        return id;
    }

    private static void entry(ThreatModelEntryRepository repo, Long id, Long modelId, StrideCategory category,
                              String description, String mitigation, ThreatModelEntryStatus status) {
        repo.save(new ThreatModelEntry(id, modelId, category, description, mitigation, status));
    }

    private static void alert(ThreatIntelAlertRepository repo, Long id, String externalId, String source,
                              IndicatorType type, String value, String relatedCve, Severity severity,
                              String description, String publishedAt, String ingestedAt) {
        repo.save(new ThreatIntelAlert(id, externalId, source, type, value, relatedCve, severity,
                description, Instant.parse(publishedAt), Instant.parse(ingestedAt)));
    }
}

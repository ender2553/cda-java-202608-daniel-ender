# SecOps Analyst Suite -- Security Assessment Report
- **Generated:** 2026-09-25T20:19:06.031285100Z
- **Prepared by:** daniel
- **Scope:** 6 assets, 10 catalogued CVEs

> This report contains fictional training data for the SecOps Analyst Suite capstone.

## 1. Asset Inventory

| Hostname | IP address | Owner team | Criticality |
|---|---|---|---|
| build-ci-01 | 10.0.4.40 | DevOps | HIGH |
| db-prod-01 | 10.0.2.20 | Data Platform | CRITICAL |
| hr-portal-01 | 10.0.3.15 | Corporate IT | MEDIUM |
| kiosk-lobby-01 | 10.0.9.99 | Facilities | LOW |
| vpn-gw-01 | 10.0.0.5 | Network Operations | HIGH |
| web-prod-01 | 10.0.1.10 | Platform Engineering | CRITICAL |

## 2. Open Scan Findings by Severity

Severity is derived from each CVE's CVSS v3 base score (0.0 NONE, 0.1-3.9 LOW, 4.0-6.9 MEDIUM, 7.0-8.9 HIGH, 9.0-10.0 CRITICAL). Total open findings: **8**.

| Severity | Open findings |
|---|---|
| CRITICAL | 3 |
| HIGH | 1 |
| MEDIUM | 2 |
| LOW | 1 |
| NONE | 1 |

### CRITICAL (3)

| Host | CVE | CVSS | Port/Service | Description |
|---|---|---|---|---|
| web-prod-01 | CVE-2023-91001 | 10.0 | 443/https | (Fictional) Remote code execution via crafted log message lookup in the LogLite logging library |
| vpn-gw-01 | CVE-2023-91002 | 9.0 | 443/vpn-web | (Fictional) Authentication bypass in the web portal of the GateKeeper VPN appliance |
| build-ci-01 | CVE-2023-91001 | 10.0 | 8080/jenkins-http | (Fictional) Remote code execution via crafted log message lookup in the LogLite logging library |

### HIGH (1)

| Host | CVE | CVSS | Port/Service | Description |
|---|---|---|---|---|
| db-prod-01 | CVE-2023-91004 | 7.0 | 5432/postgresql | (Fictional) Unsafe deserialization of polymorphic types in the jsonkit JSON library |

### MEDIUM (2)

| Host | CVE | CVSS | Port/Service | Description |
|---|---|---|---|---|
| web-prod-01 | CVE-2023-91005 | 6.9 | 443/https | (Fictional) Stored cross-site scripting in admin-ui-widgets data table component |
| hr-portal-01 | CVE-2024-91007 | 4.0 | 8080/http | (Fictional) Open redirect in the login return-URL handling of admin-ui-widgets |

### LOW (1)

| Host | CVE | CVSS | Port/Service | Description |
|---|---|---|---|---|
| build-ci-01 | CVE-2024-91008 | 3.9 | 22/ssh | (Fictional) Weak default TLS cipher suites negotiated by the tlswrap package |

### NONE (1)

| Host | CVE | CVSS | Port/Service | Description |
|---|---|---|---|---|
| kiosk-lobby-01 | CVE-2024-91010 | 0.0 | 80/http | (Fictional) Disputed informational report: server banner reveals product name |

## 3. Risk Register -- Top Risks

Sorted by risk score (likelihood x impact) descending. Bands: 1-6 Low, 7-12 Medium, 13-19 High, 20-25 Critical. Total entries: **10**.

| # | Band | Score | L x I | Asset | Title | Status | Due |
|---|---|---|---|---|---|---|---|
| 1 | **CRITICAL** | 25 | 5 x 5 | web-prod-01 | CVE-2023-91001 on web-prod-01 (https/443) | OPEN | 2026-10-02 |
| 2 | **CRITICAL** | 20 | 5 x 4 | build-ci-01 | CVE-2023-91001 on build-ci-01 (jenkins-http/8080) | OPEN | 2026-10-02 |
| 3 | **CRITICAL** | 20 | 5 x 4 | vpn-gw-01 | CVE-2023-91002 on vpn-gw-01 (vpn-web/443) | OPEN | 2026-10-02 |
| 4 | **CRITICAL** | 20 | 4 x 5 | db-prod-01 | CVE-2023-91004 on db-prod-01 (postgresql/5432) | OPEN | 2026-10-02 |
| 5 | HIGH | 15 | 3 x 5 | web-prod-01 | CVE-2023-91005 on web-prod-01 (https/443) | OPEN | 2026-10-25 |
| 6 | MEDIUM | 12 | 3 x 4 | vpn-gw-01 | No MFA enforced on the VPN admin portal | OPEN | 2026-12-24 |
| 7 | MEDIUM | 9 | 3 x 3 | hr-portal-01 | CVE-2024-91007 on hr-portal-01 (http/8080) | OPEN | 2026-12-24 |
| 8 | MEDIUM | 8 | 2 x 4 | build-ci-01 | CVE-2024-91008 on build-ci-01 (ssh/22) | OPEN | 2026-12-24 |
| 9 | LOW | 6 | 3 x 2 | kiosk-lobby-01 | Shared local administrator password on lobby kiosks | OPEN | 2027-03-24 |
| 10 | LOW | 2 | 1 x 2 | kiosk-lobby-01 | CVE-2024-91010 on kiosk-lobby-01 (http/80) | OPEN | 2027-03-24 |

## 4. SBOM -- Vulnerable Components

Components analysed: **8**, with known vulnerabilities: **5**.

| Application | Component | Version | Ecosystem | Known CVEs | Worst severity |
|---|---|---|---|---|---|
| hr-portal | date-helper | 1.2.0 | npm | No known vulnerabilities | - |
| hr-portal | fastform | 0.9.3 | PyPI | CVE-2023-91003 (8.9) | HIGH |
| hr-portal | requests-lite | 2.31.0 | PyPI | No known vulnerabilities | - |
| hr-portal | tlswrap | 1.0.4 | PyPI | CVE-2024-91008 (3.9) | LOW |
| storefront-web | admin-ui-widgets | 4.2.1 | npm | CVE-2023-91005 (6.9), CVE-2024-91007 (4.0) | MEDIUM |
| storefront-web | com.example:jsonkit | 1.9.2 | Maven | CVE-2023-91004 (7.0) | HIGH |
| storefront-web | org.example:templating-core | 3.1.0 | Maven | No known vulnerabilities | - |
| storefront-web | org.fictional:loglite | 2.14.0 | Maven | CVE-2023-91001 (10.0) | CRITICAL |

## 5. STRIDE Threat Model Coverage

### Customer storefront web tier (web-prod-01)

| STRIDE category | Violates | Entries | Open (IDENTIFIED) |
|---|---|---|---|
| Spoofing | Authentication | 1 | 0 |
| Tampering | Integrity | 1 | 0 |
| Repudiation | Non-repudiation | 0 | 0 |
| Information Disclosure | Confidentiality | 2 | 2 |
| Denial of Service | Availability | 1 | 0 |
| Elevation of Privilege | Authorization | 0 | 0 |

> **Coverage gap:** no threats recorded for Repudiation, Elevation of Privilege. Either these were analysed and none apply (record that explicitly) or they were never considered.

- **Spoofing** [MITIGATED] Credential stuffing against the customer login endpoint -- _Mitigation:_ Rate limiting plus MFA for high-value accounts
- **Tampering** [MITIGATED] Price parameter manipulation in checkout requests -- _Mitigation:_ Server-side price lookup; never trust client-supplied totals
- **Information Disclosure** [IDENTIFIED] Session tokens exposed in URL query strings
- **Information Disclosure** [IDENTIFIED] Verbose stack traces returned on HTTP 500 errors -- _Mitigation:_ Generic error pages; log details server-side only
- **Denial of Service** [ACCEPTED] Unbounded search queries exhausting database connections -- _Mitigation:_ Query timeouts and pagination limits

### Remote access VPN gateway (vpn-gw-01)

| STRIDE category | Violates | Entries | Open (IDENTIFIED) |
|---|---|---|---|
| Spoofing | Authentication | 1 | 0 |
| Tampering | Integrity | 1 | 0 |
| Repudiation | Non-repudiation | 1 | 1 |
| Information Disclosure | Confidentiality | 1 | 1 |
| Denial of Service | Availability | 1 | 0 |
| Elevation of Privilege | Authorization | 1 | 1 |

All six STRIDE categories have at least one recorded threat.

- **Spoofing** [MITIGATED] Stolen VPN credentials reused by an attacker -- _Mitigation:_ Client certificate plus MFA authentication
- **Tampering** [MITIGATED] Gateway configuration altered via the exposed admin interface -- _Mitigation:_ Admin interface reachable from the management VLAN only
- **Repudiation** [IDENTIFIED] Shared admin account prevents attributing configuration changes -- _Mitigation:_ Named admin accounts with centralized audit logging
- **Information Disclosure** [IDENTIFIED] Split tunneling leaks internal DNS queries -- _Mitigation:_ Force-tunnel DNS for corporate domains
- **Denial of Service** [ACCEPTED] Pre-authentication handshake flood exhausts the session table -- _Mitigation:_ Upstream rate limiting
- **Elevation of Privilege** [IDENTIFIED] Authentication bypass (CVE-2023-91002) grants an admin session -- _Mitigation:_ Apply vendor patch; restrict the admin portal

## 6. Threat Intelligence Alerts

Every stored alert (seeded and CSV-ingested). Feed text is untrusted and is output-encoded below. Total alerts: **13**.

| Alert | Source | Type | Indicator | Related CVE | Severity | Published | Description |
|---|---|---|---|---|---|---|---|
| TI-2026-0001 | NorthStar ISAC | CVE | CVE-2023-91002 | CVE-2023-91002 | CRITICAL | 2026-09-01T08:00:00Z | GateKeeper VPN authentication bypass exploited in the wild, patch immediately |
| TI-2026-0002 | PartnerCERT | IP | 198.51.100.23 | - | HIGH | 2026-09-02T10:15:00Z | Command-and-control server observed beaconing from retail networks |
| TI-2026-0003 | Internal SOC | DOMAIN | update-check.example.net | - | MEDIUM | 2026-09-03T00:00:00Z | Typosquatted software-update domain serving a fake installer |
| TI-2026-0004 | PartnerCERT | FILE_HASH | e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855 | CVE-2023-91001 | HIGH | 2026-09-04T10:00:00Z | Loader dropping a LogLite exploit \| lure page embeds &lt;script&gt;alert(1)&lt;/script&gt; and a "payroll" macro |
| TI-2026-0005 | NorthStar ISAC | CVE | CVE-2023-91004 | CVE-2023-91004 | HIGH | 2026-09-05T09:30:00Z | Public gadget chain published for jsonkit deserialization |
| TI-2026-0006 | PartnerCERT | CVE | CVE-2024-91007 | CVE-2024-91007 | MEDIUM | 2026-09-06T07:00:00Z | Open redirect abused in phishing campaign targeting HR portals |
| TI-2026-0007 | Internal SOC | IP | 192.0.2.77 | - | LOW | 2026-09-07T16:45:00Z | Internet-wide scanner noise (informational) |
| TI-2026-0008 | NorthStar ISAC | CVE | CVE-2024-91009 | CVE-2024-91009 | LOW | 2026-09-08T11:20:00Z | Timing side channel in date-helper discussed at a conference |
| TI-SEED-001 | NorthStar ISAC | CVE | CVE-2023-91001 | CVE-2023-91001 | CRITICAL | 2026-08-20T06:00:00Z | Active exploitation of LogLite message-lookup RCE observed against retail sector |
| TI-SEED-002 | PartnerCERT | CVE | CVE-2023-91003 | CVE-2023-91003 | HIGH | 2026-08-21T09:00:00Z | Proof-of-concept exploit released for fastform SQL injection |
| TI-SEED-003 | Internal SOC | IP | 203.0.113.45 | - | HIGH | 2026-08-22T14:30:00Z | Source of repeated brute-force attempts against the VPN portal |
| TI-SEED-004 | NorthStar ISAC | DOMAIN | login-secure-update.example | - | MEDIUM | 2026-08-23T08:00:00Z | Credential phishing domain impersonating the corporate SSO page |
| TI-SEED-005 | PartnerCERT | FILE_HASH | 9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08 | CVE-2023-91004 | HIGH | 2026-08-24T11:00:00Z | Malware sample bundling a jsonkit deserialization exploit |

## 7. Threat Intelligence Correlation (Active Hits)

CVE-type alerts matched against **OPEN** scan findings only (resolved findings and non-CVE indicators are never counted). Active hits: **5**.

| Host | CVE | Alert | Source | Alert severity | Alert description |
|---|---|---|---|---|---|
| build-ci-01 | CVE-2023-91001 | TI-SEED-001 | NorthStar ISAC | CRITICAL | Active exploitation of LogLite message-lookup RCE observed against retail sector |
| db-prod-01 | CVE-2023-91004 | TI-2026-0005 | NorthStar ISAC | HIGH | Public gadget chain published for jsonkit deserialization |
| hr-portal-01 | CVE-2024-91007 | TI-2026-0006 | PartnerCERT | MEDIUM | Open redirect abused in phishing campaign targeting HR portals |
| vpn-gw-01 | CVE-2023-91002 | TI-2026-0001 | NorthStar ISAC | CRITICAL | GateKeeper VPN authentication bypass exploited in the wild, patch immediately |
| web-prod-01 | CVE-2023-91001 | TI-SEED-001 | NorthStar ISAC | CRITICAL | Active exploitation of LogLite message-lookup RCE observed against retail sector |

## 8. Threat Intel CSV Ingestion Summary

| Run | File | Data rows | Parsed | Skipped | Inserted | Duplicates (not re-inserted) |
|---|---|---|---|---|---|---|
| 1 | threat-intel-feed.csv | 13 | 9 | 4 | 8 | 1 |
| 2 | threat-intel-feed.csv | 13 | 9 | 4 | 0 | 9 |

### Skipped rows (run 1)

| Line | Claimed alert id | Reason |
|---|---|---|
| 11 | TI-2026-0010 | required field 'source' is blank |
| 12 | TI-2026-0011 | indicator\_type 'URL' is not supported |
| 13 | TI-2026-0012 | published\_at 'last Tuesday' is not an ISO-8601 date or timestamp |
| 14 | TI-2026-0013 | related\_cve\_id 'CVE-2025-99999' does not exist in the CVE catalog |

### Duplicates skipped (run 1)

- TI-SEED-001 (already stored -- kept the existing alert, did not overwrite)


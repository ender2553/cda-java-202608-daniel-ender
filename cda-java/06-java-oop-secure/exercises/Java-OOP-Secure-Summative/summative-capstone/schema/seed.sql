-- =============================================================================
-- SecOps Analyst Suite -- seed data, PostgreSQL 13+
-- =============================================================================
-- Run AFTER schema.sql and dcl.sql:   psql -d secsuite -f schema/seed.sql
--
-- KEEP IN SYNC WITH repository/inmemory/InMemorySeedLoader.java -- that class loads EXACTLY
-- this data (same numeric ids, same values) into the InMemory* repositories, so a report generated
-- with zero database setup and one generated against this seeded Postgres tell the same story.
--
-- Deliberately NOT seeded:
--   * analyst       -- analysts register live through the login gate; seeding credentials
--                      (even hashed) into a script would be a bad habit to model.
--   * risk_register_entry -- the pipeline in Main promotes OPEN scan findings into the
--                      register (SEC-5), so risks are produced by the application, not typed in.
--
-- ALL CVE IDENTIFIERS BELOW ARE FICTIONAL (sequence numbers 91001-91010 were chosen so they
-- do not collide with well-known real CVEs). Do not look them up; the descriptions and
-- scores are invented for teaching. Likewise every product/package name is fictional, and
-- IP addresses/domains use documentation-reserved ranges (RFC 5737 / RFC 2606).
--
-- The whole script runs in one transaction and is idempotent (ON CONFLICT DO NOTHING), so
-- re-running it neither fails half-way nor duplicates rows.
-- =============================================================================
BEGIN;

-- ---------------------------------------------------------------- asset (6)
INSERT INTO asset (id, hostname, ip_address, owner_team, criticality) OVERRIDING SYSTEM VALUE VALUES
    (1, 'web-prod-01',    '10.0.1.10', 'Platform Engineering', 'CRITICAL'),
    (2, 'db-prod-01',     '10.0.2.20', 'Data Platform',        'CRITICAL'),
    (3, 'vpn-gw-01',      '10.0.0.5',  'Network Operations',   'HIGH'),
    (4, 'hr-portal-01',   '10.0.3.15', 'Corporate IT',         'MEDIUM'),
    (5, 'build-ci-01',    '10.0.4.40', 'DevOps',               'HIGH'),
    (6, 'kiosk-lobby-01', '10.0.9.99', 'Facilities',           'LOW')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- cve_catalog (10)
-- Scores deliberately sit on every CVSS v3 band boundary so SEC-2 has real edge cases:
--   10.0 CRITICAL | 9.0 CRITICAL (lower edge) | 8.9 HIGH (upper edge) | 7.0 HIGH (lower edge)
--   6.9 MEDIUM (upper edge) | 5.3 MEDIUM | 4.0 MEDIUM (lower edge) | 3.9 LOW (upper edge)
--   0.1 LOW (lower edge) | 0.0 NONE
INSERT INTO cve_catalog (cve_id, description, cvss_score) VALUES
    ('CVE-2023-91001', '(Fictional) Remote code execution via crafted log message lookup in the LogLite logging library', 10.0),
    ('CVE-2023-91002', '(Fictional) Authentication bypass in the web portal of the GateKeeper VPN appliance',              9.0),
    ('CVE-2023-91003', '(Fictional) SQL injection in the fastform Python form-handling package',                          8.9),
    ('CVE-2023-91004', '(Fictional) Unsafe deserialization of polymorphic types in the jsonkit JSON library',            7.0),
    ('CVE-2023-91005', '(Fictional) Stored cross-site scripting in admin-ui-widgets data table component',               6.9),
    ('CVE-2024-91006', '(Fictional) Information disclosure through verbose error pages in templating-core',              5.3),
    ('CVE-2024-91007', '(Fictional) Open redirect in the login return-URL handling of admin-ui-widgets',                 4.0),
    ('CVE-2024-91008', '(Fictional) Weak default TLS cipher suites negotiated by the tlswrap package',                   3.9),
    ('CVE-2024-91009', '(Fictional) Minor timing side channel in date-helper locale lookup',                             0.1),
    ('CVE-2024-91010', '(Fictional) Disputed informational report: server banner reveals product name',                  0.0)
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- scan_finding (8)
-- 7 OPEN + 1 RESOLVED. f...05 (hr-portal-01 / CVE-2023-91003) is RESOLVED on purpose: the
-- seeded alert TI-SEED-002 references that CVE and must NOT show up as an active
-- correlation hit (SEC-12).
INSERT INTO scan_finding (id, asset_id, cve_id, port, service_name, detected_at, status) OVERRIDING SYSTEM VALUE VALUES
    (1, 1, 'CVE-2023-91001',  443, 'https',      '2026-08-10T09:00:00Z', 'OPEN'),
    (2, 1, 'CVE-2023-91005',  443, 'https',      '2026-08-10T09:05:00Z', 'OPEN'),
    (3, 2, 'CVE-2023-91004', 5432, 'postgresql', '2026-08-11T10:00:00Z', 'OPEN'),
    (4, 3, 'CVE-2023-91002',  443, 'vpn-web',    '2026-08-11T11:00:00Z', 'OPEN'),
    (5, 4, 'CVE-2023-91003', 8080, 'http',       '2026-07-01T08:00:00Z', 'RESOLVED'),
    (6, 4, 'CVE-2024-91007', 8080, 'http',       '2026-08-12T08:30:00Z', 'OPEN'),
    (7, 5, 'CVE-2024-91008',   22, 'ssh',        '2026-08-12T14:00:00Z', 'OPEN'),
    (8, 6, 'CVE-2024-91010',   80, 'http',       '2026-08-13T07:45:00Z', 'OPEN')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- component (8)
-- Two applications, three ecosystems. c...03, c...06 and c...08 have NO component_cve links:
-- SEC-7 must list them with an empty CVE list ("no known vulnerabilities"), not drop them.
INSERT INTO component (id, application_name, component_name, component_version, ecosystem) OVERRIDING SYSTEM VALUE VALUES
    (1, 'storefront-web', 'org.fictional:loglite',       '2.14.0', 'Maven'),
    (2, 'storefront-web', 'com.example:jsonkit',         '1.9.2',  'Maven'),
    (3, 'storefront-web', 'org.example:templating-core', '3.1.0',  'Maven'),
    (4, 'storefront-web', 'admin-ui-widgets',            '4.2.1',  'npm'),
    (5, 'hr-portal',      'fastform',                    '0.9.3',  'PyPI'),
    (6, 'hr-portal',      'requests-lite',               '2.31.0', 'PyPI'),
    (7, 'hr-portal',      'tlswrap',                     '1.0.4',  'PyPI'),
    (8, 'hr-portal',      'date-helper',                 '1.2.0',  'npm')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- component_cve (6 links)
-- admin-ui-widgets has TWO CVEs (the many side of many-to-many); CVE-2023-91001 etc. could be
-- linked from several components without its description being copied anywhere.
INSERT INTO component_cve (component_id, cve_id) VALUES
    (1, 'CVE-2023-91001'),
    (2, 'CVE-2023-91004'),
    (4, 'CVE-2023-91005'),
    (4, 'CVE-2024-91007'),
    (5, 'CVE-2023-91003'),
    (7, 'CVE-2024-91008')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- threat_model (2)
INSERT INTO threat_model (id, asset_id, title, description, created_at) OVERRIDING SYSTEM VALUE VALUES
    (1, 1,
     'Customer storefront web tier', 'Public-facing web servers handling login, browsing and checkout.', '2026-08-01T12:00:00Z'),
    (2, 3,
     'Remote access VPN gateway', 'Internet-facing VPN appliance used by all remote staff.', '2026-08-02T12:00:00Z')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- threat_model_entry (11)
-- The storefront model (d...01) deliberately has NO REPUDIATION and NO ELEVATION_OF_PRIVILEGE
-- entries -- SEC-8 must still show those two categories, with 0 entries, as a visible gap.
-- The VPN model (d...02) covers all six categories, as the contrast case.
INSERT INTO threat_model_entry (id, threat_model_id, stride_category, description, mitigation, status) OVERRIDING SYSTEM VALUE VALUES
    (1, 1, 'SPOOFING',
     'Credential stuffing against the customer login endpoint', 'Rate limiting plus MFA for high-value accounts', 'MITIGATED'),
    (2, 1, 'TAMPERING',
     'Price parameter manipulation in checkout requests', 'Server-side price lookup; never trust client-supplied totals', 'MITIGATED'),
    (3, 1, 'INFORMATION_DISCLOSURE',
     'Verbose stack traces returned on HTTP 500 errors', 'Generic error pages; log details server-side only', 'IDENTIFIED'),
    (4, 1, 'INFORMATION_DISCLOSURE',
     'Session tokens exposed in URL query strings', NULL, 'IDENTIFIED'),
    (5, 1, 'DENIAL_OF_SERVICE',
     'Unbounded search queries exhausting database connections', 'Query timeouts and pagination limits', 'ACCEPTED'),
    (6, 2, 'SPOOFING',
     'Stolen VPN credentials reused by an attacker', 'Client certificate plus MFA authentication', 'MITIGATED'),
    (7, 2, 'TAMPERING',
     'Gateway configuration altered via the exposed admin interface', 'Admin interface reachable from the management VLAN only', 'MITIGATED'),
    (8, 2, 'REPUDIATION',
     'Shared admin account prevents attributing configuration changes', 'Named admin accounts with centralized audit logging', 'IDENTIFIED'),
    (9, 2, 'INFORMATION_DISCLOSURE',
     'Split tunneling leaks internal DNS queries', 'Force-tunnel DNS for corporate domains', 'IDENTIFIED'),
    (10, 2, 'DENIAL_OF_SERVICE',
     'Pre-authentication handshake flood exhausts the session table', 'Upstream rate limiting', 'ACCEPTED'),
    (11, 2, 'ELEVATION_OF_PRIVILEGE',
     'Authentication bypass (CVE-2023-91002) grants an admin session', 'Apply vendor patch; restrict the admin portal', 'IDENTIFIED')
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- threat_intel_alert (5)
-- Seeded directly (separate from the CSV feed ingested by SEC-10/SEC-11):
--   TI-SEED-001  CVE-2023-91001  -> matches OPEN finding on web-prod-01      => ACTIVE HIT
--   TI-SEED-002  CVE-2023-91003  -> matches only a RESOLVED finding          => must NOT hit
--   TI-SEED-003  IP                                                          => never correlated
--   TI-SEED-004  DOMAIN                                                      => never correlated
--   TI-SEED-005  FILE_HASH with related_cve_id CVE-2023-91004, which DOES have an OPEN finding
--                on db-prod-01 -- still must NOT hit: only CVE-type indicators are correlated.
-- The CSV feed also re-sends TI-SEED-001 with different text; SEC-11 must skip it as a
-- duplicate (not throw, not overwrite).
INSERT INTO threat_intel_alert (id, external_alert_id, source, indicator_type, indicator_value, related_cve_id,
                                severity, description, published_at, ingested_at) OVERRIDING SYSTEM VALUE VALUES
    (1, 'TI-SEED-001', 'NorthStar ISAC', 'CVE', 'CVE-2023-91001', 'CVE-2023-91001',
     'CRITICAL', 'Active exploitation of LogLite message-lookup RCE observed against retail sector',
     '2026-08-20T06:00:00Z', '2026-08-20T06:05:00Z'),
    (2, 'TI-SEED-002', 'PartnerCERT', 'CVE', 'CVE-2023-91003', 'CVE-2023-91003',
     'HIGH', 'Proof-of-concept exploit released for fastform SQL injection',
     '2026-08-21T09:00:00Z', '2026-08-21T09:10:00Z'),
    (3, 'TI-SEED-003', 'Internal SOC', 'IP', '203.0.113.45', NULL,
     'HIGH', 'Source of repeated brute-force attempts against the VPN portal',
     '2026-08-22T14:30:00Z', '2026-08-22T14:31:00Z'),
    (4, 'TI-SEED-004', 'NorthStar ISAC', 'DOMAIN', 'login-secure-update.example', NULL,
     'MEDIUM', 'Credential phishing domain impersonating the corporate SSO page',
     '2026-08-23T08:00:00Z', '2026-08-23T08:02:00Z'),
    (5, 'TI-SEED-005', 'PartnerCERT', 'FILE_HASH',
     '9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08', 'CVE-2023-91004',
     'HIGH', 'Malware sample bundling a jsonkit deserialization exploit',
     '2026-08-24T11:00:00Z', '2026-08-24T11:05:00Z')
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('asset', 'id'), COALESCE((SELECT max(id) FROM asset), 1), true);
SELECT setval(pg_get_serial_sequence('scan_finding', 'id'), COALESCE((SELECT max(id) FROM scan_finding), 1), true);
SELECT setval(pg_get_serial_sequence('component', 'id'), COALESCE((SELECT max(id) FROM component), 1), true);
SELECT setval(pg_get_serial_sequence('threat_model', 'id'), COALESCE((SELECT max(id) FROM threat_model), 1), true);
SELECT setval(pg_get_serial_sequence('threat_model_entry', 'id'), COALESCE((SELECT max(id) FROM threat_model_entry), 1), true);
SELECT setval(pg_get_serial_sequence('threat_intel_alert', 'id'), COALESCE((SELECT max(id) FROM threat_intel_alert), 1), true);

COMMIT;

/* ============================================================
   CYBER DEVELOPER PROGRAM — SQL TRACK — DAY 3
   SCHEMA SETUP: Vulnerability & Risk Management Data Warehouse
   ------------------------------------------------------------
   Run this entire file FIRST, top to bottom, before opening
   the exercise file. It builds six related tables that model
   a small security program's data:

       asset               -- systems the org owns and tracks
       vulnerability_scan  -- findings from a vulnerability scanner
       sbom_component      -- software bill of materials entries
       risk_register       -- formally tracked, assessed risks
       threat_intel_alert  -- external threat intelligence feed
       system_log          -- operational/security log events

   These tables are NOT all wired together with tidy 1-to-1
   coverage. Some assets have no scan results yet. Some
   vulnerabilities have no matching threat intel. Some risks
   are tracked in the register, some aren't. Some vulnerability
   findings have no remediation deadline set. That's on purpose --
   real security data is incomplete, and this afternoon's work
   is about learning to SEE those gaps in SQL, not just combine
   tables together.
   ============================================================ */

DROP TABLE IF EXISTS system_log;
DROP TABLE IF EXISTS threat_intel_alert;
DROP TABLE IF EXISTS risk_register;
DROP TABLE IF EXISTS sbom_component;
DROP TABLE IF EXISTS vulnerability_scan;
DROP TABLE IF EXISTS asset;


/* ------------------------------------------------------------
   TABLE: asset
   The systems the organization tracks. Everything else in
   this schema hangs off of asset_id.
   ------------------------------------------------------------ */
CREATE TABLE asset (
    asset_id     SERIAL PRIMARY KEY,
    asset_name   VARCHAR(60) NOT NULL,
    asset_type   VARCHAR(30) NOT NULL,   -- 'Server','Workstation','Container','Network Device'
    criticality  VARCHAR(20) NOT NULL,   -- 'Low','Medium','High','Critical'
    owner_team   VARCHAR(40) NOT NULL
);

/* ------------------------------------------------------------
   TABLE: vulnerability_scan
   One row per vulnerability finding from a scanner run against
   an asset. An asset can have zero, one, or many findings.

   remediation_deadline is NULLABLE ON PURPOSE: a NULL here
   does not always mean the same thing. For an 'Accepted Risk'
   finding, no deadline is expected -- that NULL is normal. For
   an 'Open' finding, no deadline usually means nobody has set
   one yet -- that NULL is a process gap. Same column, same
   NULL value, two very different meanings depending on status.
   This is a deliberate, realistic wrinkle for the NULL-handling
   exercises later this afternoon.
   ------------------------------------------------------------ */
CREATE TABLE vulnerability_scan (
    scan_id                SERIAL PRIMARY KEY,
    asset_id               INTEGER REFERENCES asset(asset_id),
    cve_id                 VARCHAR(20) NOT NULL,
    cvss_score             NUMERIC(3,1) NOT NULL,
    scan_date              DATE NOT NULL,
    status                 VARCHAR(20) NOT NULL,   -- 'Open','Remediated','Accepted Risk'
    remediation_deadline   DATE                    -- nullable; see comment above
);

/* ------------------------------------------------------------
   TABLE: sbom_component
   Software Bill of Materials: third-party/open-source
   components known to be running on a given asset.
   ------------------------------------------------------------ */
CREATE TABLE sbom_component (
    component_id        SERIAL PRIMARY KEY,
    asset_id             INTEGER REFERENCES asset(asset_id),
    component_name       VARCHAR(50) NOT NULL,
    component_version    VARCHAR(20) NOT NULL,
    license               VARCHAR(30),
    is_deprecated         BOOLEAN NOT NULL DEFAULT FALSE
);

/* ------------------------------------------------------------
   TABLE: risk_register
   Formally assessed risks that have gone through a risk
   management process (likelihood x impact). NOT every
   vulnerability or SBOM finding automatically becomes a
   tracked risk -- that gap is intentional and important.
   ------------------------------------------------------------ */
CREATE TABLE risk_register (
    risk_id            SERIAL PRIMARY KEY,
    asset_id            INTEGER REFERENCES asset(asset_id),
    risk_title          VARCHAR(80) NOT NULL,
    likelihood          VARCHAR(10) NOT NULL,  -- 'Low','Medium','High'
    impact              VARCHAR(10) NOT NULL,  -- 'Low','Medium','High'
    risk_score          INTEGER NOT NULL,      -- analyst-assigned, 1-25
    mitigation_status   VARCHAR(20) NOT NULL,  -- 'Open','In Progress','Closed'
    risk_owner          VARCHAR(40) NOT NULL
);

/* ------------------------------------------------------------
   TABLE: threat_intel_alert
   An EXTERNAL feed (think CISA KEV, vendor advisories, ISAC
   bulletins). Notice related_cve_id is a plain VARCHAR, not a
   foreign key into vulnerability_scan. Threat intel arrives
   independently of your own scanning -- you match it by VALUE
   (the CVE string), not by a database relationship. This is
   realistic and is also why a JOIN here can surprise you.
   ------------------------------------------------------------ */
CREATE TABLE threat_intel_alert (
    alert_id         SERIAL PRIMARY KEY,
    related_cve_id    VARCHAR(20) NOT NULL,
    alert_source       VARCHAR(30) NOT NULL,  -- 'CISA KEV','Vendor Advisory','ISAC Bulletin'
    severity            VARCHAR(20) NOT NULL,
    published_date      DATE NOT NULL,
    description          VARCHAR(200)
);

/* ------------------------------------------------------------
   TABLE: system_log
   Operational/security log events per asset.
   ------------------------------------------------------------ */
CREATE TABLE system_log (
    log_id       SERIAL PRIMARY KEY,
    asset_id     INTEGER REFERENCES asset(asset_id),
    log_time     TIMESTAMP NOT NULL,
    event_type   VARCHAR(30) NOT NULL,  -- 'Login Failure','Config Change','Service Restart','Anomalous Traffic'
    message      VARCHAR(200)
);


/* ============================================================
   SEED DATA
   ============================================================ */

-- 8 assets. Insert order fixes their IDs 1-8 (SERIAL), referenced
-- by comment below for readability when writing INSERTs further down.
INSERT INTO asset (asset_name, asset_type, criticality, owner_team) VALUES
    ('web-app-prod-01',      'Server',         'Critical', 'Web Team'),        -- asset_id 1
    ('db-cluster-02',        'Server',         'Critical', 'Data Team'),       -- asset_id 2
    ('auth-service-03',      'Server',         'Critical', 'IAM Team'),        -- asset_id 3
    ('build-agent-04',       'Server',         'Medium',   'DevOps Team'),     -- asset_id 4
    ('workstation-hr-01',    'Workstation',    'Low',      'HR'),              -- asset_id 5
    ('container-api-gateway','Container',      'High',     'Platform Team'),   -- asset_id 6
    ('legacy-file-server',   'Server',         'Medium',   'IT Team'),         -- asset_id 7
    ('iot-badge-reader',     'Network Device', 'Low',      'Facilities');      -- asset_id 8

-- Vulnerability scan findings. Note: assets 5 and 8 have NO rows here.
-- remediation_deadline notes (used in the Part 4 date/NULL exercises):
--   scan_id 1 (CVE-2024-1111): deadline already PASSED as of the
--     class reference date 2024-04-10 -- an overdue, Open, Critical
--     finding.
--   scan_id 4 (CVE-2024-4444): deadline falls EXACTLY ON the
--     reference date -- a deliberate boundary case for a
--     predict-then-check exercise (is "due today" overdue or not?).
--   scan_id 5 (CVE-2024-5555, Accepted Risk): NULL deadline is
--     EXPECTED here -- accepted-risk findings aren't on a
--     remediation clock.
--   scan_id 7 (CVE-2024-7777, Open): NULL deadline here is a GAP --
--     it's an open, unmanaged finding that was never given a
--     deadline at all.
INSERT INTO vulnerability_scan (asset_id, cve_id, cvss_score, scan_date, status, remediation_deadline) VALUES
    (1, 'CVE-2024-1111', 9.8, '2024-04-01', 'Open',           '2024-04-08'),
    (1, 'CVE-2024-2222', 7.5, '2024-04-01', 'Remediated',     '2024-04-15'),
    (2, 'CVE-2024-3333', 9.1, '2024-04-02', 'Open',           '2024-04-16'),
    (3, 'CVE-2024-4444', 8.6, '2024-04-03', 'Open',           '2024-04-10'),
    (4, 'CVE-2024-5555', 5.0, '2024-04-04', 'Accepted Risk',  NULL),
    (6, 'CVE-2024-6666', 7.2, '2024-04-05', 'Open',           '2024-04-12'),
    (7, 'CVE-2024-7777', 6.1, '2024-04-06', 'Open',           NULL);

-- SBOM components. Note: assets 5 and 8 have NO rows here either.
INSERT INTO sbom_component (asset_id, component_name, component_version, license, is_deprecated) VALUES
    (1, 'log4j-core',     '2.14.1', 'Apache-2.0', FALSE),
    (1, 'spring-boot',    '2.7.0',  'Apache-2.0', FALSE),
    (2, 'postgresql-jdbc','42.3.1', 'BSD-2',      FALSE),
    (3, 'openssl',        '1.1.1',  'Apache-2.0', TRUE),   -- EOL crypto library
    (4, 'jenkins-core',   '2.375',  'MIT',        FALSE),
    (6, 'node',           '16.14',  'MIT',        TRUE),   -- EOL runtime
    (7, 'struts2',        '2.3.20', 'Apache-2.0', TRUE);   -- EOL, historically high-severity framework

-- Risk register. Note: only assets 1, 2, 3, and 6 have entries.
-- Assets 4 (build-agent-04) and 7 (legacy-file-server) have KNOWN
-- vulnerability and/or SBOM findings above but NO risk register
-- entry. That gap is intentional.
INSERT INTO risk_register (asset_id, risk_title, likelihood, impact, risk_score, mitigation_status, risk_owner) VALUES
    (1, 'Public-facing RCE exposure',          'High',   'High',     20, 'In Progress', 'Web Team Lead'),
    (2, 'Unencrypted database backups',        'Medium', 'High',     15, 'Open',        'Data Team Lead'),
    (3, 'Deprecated crypto library in use',    'High',   'Critical', 24, 'Open',        'IAM Team Lead'),
    (6, 'Unpatched container base image',      'Medium', 'Medium',   10, 'Closed',      'Platform Team Lead');

-- Threat intel alerts. related_cve_id is matched by VALUE against
-- vulnerability_scan.cve_id -- there is no foreign key. Notice
-- CVE-2024-9999 below does not appear anywhere in vulnerability_scan.
INSERT INTO threat_intel_alert (related_cve_id, alert_source, severity, published_date, description) VALUES
    ('CVE-2024-1111', 'CISA KEV',        'Critical', '2024-04-02', 'Actively exploited in the wild'),
    ('CVE-2024-4444', 'Vendor Advisory', 'High',     '2024-04-04', 'Vendor patch released'),
    ('CVE-2024-7777', 'ISAC Bulletin',   'Medium',   '2024-04-07', 'Sector-wide advisory issued'),
    ('CVE-2024-9999', 'CISA KEV',        'Critical', '2024-04-08', 'Actively exploited -- added to KEV catalog');

-- System log events. Note: asset 4 (build-agent-04) has NO log
-- rows despite having a vulnerability finding above.
INSERT INTO system_log (asset_id, log_time, event_type, message) VALUES
    (1, '2024-04-01 08:15:00', 'Login Failure',      'Repeated failed admin login attempts'),
    (1, '2024-04-02 03:20:00', 'Anomalous Traffic',  'Unusual outbound traffic spike'),
    (2, '2024-04-02 11:00:00', 'Config Change',      'Backup schedule modified'),
    (3, '2024-04-03 09:45:00', 'Service Restart',    'Auth service restarted unexpectedly'),
    (5, '2024-04-04 14:00:00', 'Login Failure',      'Single failed login, account locked'),
    (6, '2024-04-05 16:30:00', 'Config Change',      'API gateway rate limit updated'),
    (7, '2024-04-06 02:10:00', 'Anomalous Traffic',  'Off-hours file access detected'),
    (8, '2024-04-07 07:00:00', 'Service Restart',    'Badge reader firmware restarted');


-- Sanity check row counts after loading (you should see 8, 7, 7, 4, 4, 8).
SELECT 'asset' AS table_name, COUNT(*) FROM asset
UNION ALL SELECT 'vulnerability_scan', COUNT(*) FROM vulnerability_scan
UNION ALL SELECT 'sbom_component', COUNT(*) FROM sbom_component
UNION ALL SELECT 'risk_register', COUNT(*) FROM risk_register
UNION ALL SELECT 'threat_intel_alert', COUNT(*) FROM threat_intel_alert
UNION ALL SELECT 'system_log', COUNT(*) FROM system_log;

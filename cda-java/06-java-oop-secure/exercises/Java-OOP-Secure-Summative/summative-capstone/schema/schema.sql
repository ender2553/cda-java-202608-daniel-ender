-- ==================================================================================
-- SecOps Analyst Suite -- schema (DDL), PostgreSQL:  CREATE DATABSE - "secsuite_app"
-- ==================================================================================
-- Run order:   1. schema.sql   (this file, as the database owner / an admin role)
--              2. dcl.sql      (roles + least-privilege grants)
--              3. seed.sql     (reference/demo data)
--
--   psql -d secsuite -f schema/schema.sql
--
-- Normalization (3NF) summary -- read aloud before walking the tables:
--   * Every table has a single-purpose primary key and every non-key column describes
--     "the key, the whole key, and nothing but the key".
--   * CVE metadata (description, CVSS score) lives in exactly ONE place: cve_catalog.
--     scan_finding, component_cve and threat_intel_alert all REFERENCE a cve_id; none of
--     them copies the description or score.
--   * Severity is NOT stored on cve_catalog: it is a pure function of cvss_score
--     (cve_id -> cvss_score -> severity would be a transitive dependency, i.e. a 3NF
--     violation with a built-in update anomaly). The application derives it via
--     Severity.fromCvssScore (SEC-2).
--   * Many-to-many relationships use a link table (component_cve) instead of a repeating
--     group (no "cve_ids" CSV column, no cve_1/cve_2/cve_3 columns).
--   * The one deliberate derived column, risk_register_entry.risk_score, is pinned to its
--     inputs with a CHECK constraint so it can never drift -- see that table's comment.
--
-- Surrogate keys use SQL-standard identity columns. PostgreSQL generates every id; the
-- application never supplies one for runtime inserts. Foreign keys use BIGINT to match.
-- =============================================================================

-- analyst: one row per analyst account. Stores ONLY a PBKDF2 hash of the password
-- (self-describing "pbkdf2_sha256$iterations$salt$hash" text) and the AES-256-GCM encrypted
-- contact email (Base64 of IV || ciphertext) -- never a plaintext password or email.
CREATE TABLE IF NOT EXISTS analyst (
    id                      BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username                VARCHAR(32)  NOT NULL UNIQUE,
    password_hash           VARCHAR(255) NOT NULL,
    encrypted_contact_email VARCHAR(512) NOT NULL,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- asset: the inventory of hosts. Every other operational table (findings, risks, threat
-- models) references an asset by id rather than repeating hostname/IP/owner.
CREATE TABLE IF NOT EXISTS asset (
    id          BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    hostname    VARCHAR(253) NOT NULL UNIQUE,
    ip_address  VARCHAR(45)  NOT NULL,
    owner_team  VARCHAR(100) NOT NULL,
    criticality VARCHAR(10)  NOT NULL CHECK (criticality IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL'))
);

-- cve_catalog: the single source of truth for vulnerability metadata, keyed by the natural
-- key cve_id. No severity column -- severity is derived from cvss_score (see header).
CREATE TABLE IF NOT EXISTS cve_catalog (
    cve_id      VARCHAR(20)  PRIMARY KEY CHECK (cve_id ~ '^CVE-[0-9]{4}-[0-9]{4,7}$'),
    description TEXT         NOT NULL,
    cvss_score  NUMERIC(3,1) NOT NULL CHECK (cvss_score BETWEEN 0.0 AND 10.0)
);

-- scan_finding: one observation of one CVE on one asset. Holds only the facts about the
-- OBSERVATION (port, service, when, status) plus two foreign keys; the CVE's description
-- and score are looked up through cve_id, the host's details through asset_id.
CREATE TABLE IF NOT EXISTS scan_finding (
    id           BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    asset_id     BIGINT       NOT NULL REFERENCES asset (id),
    cve_id       VARCHAR(20)  NOT NULL REFERENCES cve_catalog (cve_id),
    port         INTEGER      CHECK (port BETWEEN 0 AND 65535),
    service_name VARCHAR(100),
    detected_at  TIMESTAMPTZ  NOT NULL,
    status       VARCHAR(10)  NOT NULL CHECK (status IN ('OPEN', 'RESOLVED'))
);
CREATE INDEX IF NOT EXISTS ix_scan_finding_asset ON scan_finding (asset_id);
CREATE INDEX IF NOT EXISTS ix_scan_finding_cve   ON scan_finding (cve_id);
-- Defense in depth behind SEC-4: at most ONE open finding per asset + CVE. The application
-- checks first (fail closed, clear DuplicateFindingException); this partial unique index is
-- the database's last line of defense if some other code path forgets to. RESOLVED history
-- rows are not constrained, so a CVE can be found, resolved, and found again.
CREATE UNIQUE INDEX IF NOT EXISTS uq_scan_finding_open_asset_cve
    ON scan_finding (asset_id, cve_id) WHERE status = 'OPEN';

-- risk_register_entry: one tracked risk against one asset. scan_finding_id is NULLABLE
-- because not every risk comes from a scanner (e.g. "no MFA on the VPN portal").
-- NORMALIZATION NOTE: risk_score equals likelihood * impact, so strictly speaking it is a
-- derived value. It is kept (as the design requires) so the register can be sorted and
-- indexed by score directly, but the CHECK constraint below makes an inconsistent row
-- impossible -- the redundancy cannot turn into an update anomaly. (A PostgreSQL GENERATED
-- ALWAYS AS (likelihood * impact) STORED column would be the alternative; it was not used
-- because the application is required to compute and store the score itself -- SEC-5.)
CREATE TABLE IF NOT EXISTS risk_register_entry (
    id               BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    asset_id         BIGINT       NOT NULL REFERENCES asset (id),
    scan_finding_id  BIGINT       NULL REFERENCES scan_finding (id),
    title            VARCHAR(200) NOT NULL,
    description      TEXT,
    likelihood       INTEGER      NOT NULL CHECK (likelihood BETWEEN 1 AND 5),
    impact           INTEGER      NOT NULL CHECK (impact BETWEEN 1 AND 5),
    risk_score       INTEGER      NOT NULL,
    status           VARCHAR(12)  NOT NULL CHECK (status IN ('OPEN', 'MITIGATED', 'ACCEPTED', 'TRANSFERRED')),
    owner_analyst_id BIGINT       NULL REFERENCES analyst (id),
    due_date         DATE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_risk_score_is_product CHECK (risk_score = likelihood * impact)
);
CREATE INDEX IF NOT EXISTS ix_risk_asset ON risk_register_entry (asset_id);
-- At most one register entry per scan finding (the pipeline promotes each OPEN finding once).
CREATE UNIQUE INDEX IF NOT EXISTS uq_risk_scan_finding
    ON risk_register_entry (scan_finding_id) WHERE scan_finding_id IS NOT NULL;

-- component: one third-party library/package in one application's SBOM. Holds NO CVE
-- information at all -- that relationship is many-to-many and lives in component_cve.
CREATE TABLE IF NOT EXISTS component (
    id                BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    application_name  VARCHAR(100) NOT NULL,
    component_name    VARCHAR(200) NOT NULL,
    component_version VARCHAR(50)  NOT NULL,
    ecosystem         VARCHAR(20)  NOT NULL,
    CONSTRAINT uq_component_app_name_version UNIQUE (application_name, component_name, component_version)
);

-- component_cve: many-to-many between component and cve_catalog -- a component can have
-- zero or many CVEs and a CVE can affect many components; this avoids repeating CVE
-- metadata inside component (no repeating groups, no copied descriptions/scores). The
-- composite primary key makes the same link impossible to record twice.
CREATE TABLE IF NOT EXISTS component_cve (
    component_id BIGINT      NOT NULL REFERENCES component (id),
    cve_id       VARCHAR(20) NOT NULL REFERENCES cve_catalog (cve_id),
    PRIMARY KEY (component_id, cve_id)
);
CREATE INDEX IF NOT EXISTS ix_component_cve_cve ON component_cve (cve_id);

-- threat_model: header row for one STRIDE threat model of one asset. The individual threats
-- are rows in threat_model_entry (one-to-many), not a list column here.
CREATE TABLE IF NOT EXISTS threat_model (
    id          BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    asset_id    BIGINT       NOT NULL REFERENCES asset (id),
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS ix_threat_model_asset ON threat_model (asset_id);

-- threat_model_entry: one identified threat, classified into exactly one STRIDE category.
-- Depends only on its own id; the owning model is a foreign key.
CREATE TABLE IF NOT EXISTS threat_model_entry (
    id              BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    threat_model_id BIGINT      NOT NULL REFERENCES threat_model (id),
    stride_category VARCHAR(30) NOT NULL CHECK (stride_category IN
                        ('SPOOFING', 'TAMPERING', 'REPUDIATION', 'INFORMATION_DISCLOSURE',
                         'DENIAL_OF_SERVICE', 'ELEVATION_OF_PRIVILEGE')),
    description     TEXT        NOT NULL,
    mitigation      TEXT,
    status          VARCHAR(12) NOT NULL CHECK (status IN ('IDENTIFIED', 'MITIGATED', 'ACCEPTED'))
);
CREATE INDEX IF NOT EXISTS ix_threat_model_entry_model ON threat_model_entry (threat_model_id);

-- threat_intel_alert: one alert from an external intelligence feed (seeded, or ingested from
-- CSV by SEC-10/SEC-11). external_alert_id is the FEED's id and is UNIQUE -- the
-- deduplication key that makes re-ingesting the same file idempotent. related_cve_id is an
-- optional reference into cve_catalog (never a copy of CVE data). severity is the feed's
-- own rating, so it IS stored here (unlike cve_catalog) -- and only the four actionable
-- ratings are allowed (no NONE: see Severity.java).
-- Consistency rule for CVE indicators: the indicator IS the CVE, so related_cve_id must be
-- present and equal to indicator_value (ck_cve_indicator_consistent).
CREATE TABLE IF NOT EXISTS threat_intel_alert (
    id                BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    external_alert_id VARCHAR(100) NOT NULL UNIQUE,
    source            VARCHAR(100) NOT NULL,
    indicator_type    VARCHAR(10)  NOT NULL CHECK (indicator_type IN ('IP', 'DOMAIN', 'FILE_HASH', 'CVE')),
    indicator_value   VARCHAR(512) NOT NULL,
    related_cve_id    VARCHAR(20)  NULL REFERENCES cve_catalog (cve_id),
    severity          VARCHAR(10)  NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    description       TEXT,
    published_at      TIMESTAMPTZ,
    ingested_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_cve_indicator_consistent CHECK (
        indicator_type <> 'CVE' OR (related_cve_id IS NOT NULL AND indicator_value = related_cve_id))
);
CREATE INDEX IF NOT EXISTS ix_threat_intel_alert_cve ON threat_intel_alert (related_cve_id);

-- =============================================================================
-- SecOps Analyst Suite
-- Roles and Grants (DCL)
-- PostgreSQL 13+
--
-- Run AFTER schema.sql as postgres, the database owner,
-- or another role with sufficient privileges.
--
-- IMPORTANT:
-- Passwords below are DEVELOPMENT / CLASSROOM passwords only.
-- Production credentials should never be committed to source control.

-- -----------------------------------------------------------------------------------------
-- SECURITY PRINCIPLE -- LEAST PRIVILEGE:
--
--   The application must NOT connect to the database as the role that owns the tables
--   (or, worse, as the postgres superuser). If it did, then any bug that lets an attacker
--   run SQL through the app -- a SQL injection like the one in SEC-15 -- would inherit the
--   owner's power: DROP TABLE, TRUNCATE, ALTER, DELETE everything, read every other schema.
--
--   Instead we create two LOGIN roles that own nothing and are granted only what they need:
--
--     secsuite_app       SELECT, INSERT, UPDATE on the application tables.
--                        NO DELETE: nothing in this application deletes a row. Findings are
--                        RESOLVED, risks are MITIGATED/ACCEPTED -- the history is an
--                        append-only audit trail, the same philosophy as the QuickPay
--                        transaction log. If the app can't DELETE, then neither can an
--                        injected query running through the app.
--                        NO DDL: it cannot CREATE, ALTER or DROP anything, because it does
--                        not own the tables and has no CREATE privilege on the schema.
--
--     secsuite_readonly  SELECT only -- for a hypothetical read-only dashboard persona. A
--                        compromised dashboard can leak data but cannot change it.
--
--   The blast radius of an injection is bounded by the privileges of the connecting role.
--   Parameterized queries (SEC-3, SEC-15) prevent the injection; least privilege limits the
--   damage if one slips through anyway. Defense in depth means we want BOTH.
-- ---------------------------------------------------------------------------------------------

-- =============================================================================


-- ---------------------------------------------------------------------------
-- 1. REMOVE DEFAULT PUBLIC PRIVILEGES
-- ---------------------------------------------------------------------------

REVOKE ALL ON ALL TABLES IN SCHEMA public FROM PUBLIC;

REVOKE CREATE ON SCHEMA public FROM PUBLIC;

-- Change "secsuite" if your database has a different name.
REVOKE ALL ON DATABASE secsuite FROM PUBLIC;


-- ---------------------------------------------------------------------------
-- 2. CREATE APPLICATION ROLES
-- ---------------------------------------------------------------------------
-- PostgreSQL does not support:
--
-- CREATE ROLE IF NOT EXISTS ...
--
-- Therefore we use an anonymous PL/pgSQL DO block.
-- ---------------------------------------------------------------------------

DO $$
BEGIN

    IF NOT EXISTS (
        SELECT 1
        FROM pg_roles
        WHERE rolname = 'secsuite_app'
    ) THEN
CREATE ROLE secsuite_app LOGIN;
END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_roles
        WHERE rolname = 'secsuite_readonly'
    ) THEN
CREATE ROLE secsuite_readonly LOGIN;
END IF;

END
$$;


-- ---------------------------------------------------------------------------
-- 3. SET DEVELOPMENT PASSWORDS
-- ---------------------------------------------------------------------------
-- SECURITY:
-- These passwords are for classroom/local development only.
-- Do NOT use hard-coded passwords in production.
-- ---------------------------------------------------------------------------

ALTER ROLE secsuite_app
    WITH LOGIN
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    PASSWORD 'ChangeMe_App_123!';

ALTER ROLE secsuite_readonly
    WITH LOGIN
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    PASSWORD 'ChangeMe_ReadOnly_123!';


-- ---------------------------------------------------------------------------
-- 4. DATABASE AND SCHEMA ACCESS
-- ---------------------------------------------------------------------------

GRANT CONNECT
ON DATABASE secsuite
TO secsuite_app, secsuite_readonly;

GRANT USAGE
ON SCHEMA public
TO secsuite_app, secsuite_readonly;


-- ---------------------------------------------------------------------------
-- 5. APPLICATION ROLE
-- ---------------------------------------------------------------------------
-- Application can:
--
-- SELECT
-- INSERT
-- UPDATE
--
-- Application CANNOT:
--
-- DELETE
-- TRUNCATE
-- CREATE TABLE
-- ALTER TABLE
-- DROP TABLE
--
-- This is an example of LEAST PRIVILEGE.
-- ---------------------------------------------------------------------------

GRANT SELECT, INSERT, UPDATE ON
    analyst,
    asset,
    cve_catalog,
    scan_finding,
    risk_register_entry,
    component,
    component_cve,
    threat_model,
    threat_model_entry,
    threat_intel_alert
    TO secsuite_app;


-- ---------------------------------------------------------------------------
-- Identity column sequences
-- ---------------------------------------------------------------------------
-- Required if your tables use:
--
-- BIGINT GENERATED ... AS IDENTITY
--
-- rather than UUID primary keys.
-- ---------------------------------------------------------------------------

GRANT USAGE, SELECT
             ON ALL SEQUENCES IN SCHEMA public
                 TO secsuite_app;


-- ---------------------------------------------------------------------------
-- 6. READ-ONLY ROLE
-- ---------------------------------------------------------------------------

GRANT SELECT ON
    analyst,
    asset,
    cve_catalog,
    scan_finding,
    risk_register_entry,
    component,
    component_cve,
    threat_model,
    threat_model_entry,
    threat_intel_alert
    TO secsuite_readonly;


-- ---------------------------------------------------------------------------
-- 7. PROTECT SENSITIVE ANALYST COLUMNS
-- ---------------------------------------------------------------------------
-- The read-only dashboard should NOT be able to retrieve:
--
-- password_hash
-- encrypted_contact_email
--
-- Remove table-level SELECT and grant only specific columns.
-- ---------------------------------------------------------------------------

REVOKE SELECT
    ON analyst
    FROM secsuite_readonly;

GRANT SELECT
    (id, username, created_at)
    ON analyst
    TO secsuite_readonly;


-- ---------------------------------------------------------------------------
-- Read-only role intentionally receives NO sequence privileges.
-- ---------------------------------------------------------------------------
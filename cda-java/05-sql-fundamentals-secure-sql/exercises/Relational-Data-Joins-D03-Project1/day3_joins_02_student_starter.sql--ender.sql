/* ============================================================
   CYBER DEVELOPER PROGRAM — SQL TRACK — DAY 3
   ------------------------------------------------------------
   Run day3_pm_01_schema_setup.sql FIRST. This file assumes
   that schema and seed data already exist in your database.

   TODAY'S SCENARIO:
   You're a data analyst supporting a security team. Six
   related tables (asset, vulnerability_scan, sbom_component,
   risk_register, threat_intel_alert, system_log) each hold a
   piece of the picture, but no single table tells the whole
   story. This afternoon you'll connect them with JOINs, and
   also learn several other tools for the same underlying goal:
   finding what's PRESENT, what's MISSING, and what OVERLAPS
   across independent data sources -- often the most important
   part of a security analyst's job.

   
   HOW TO WORK THROUGH THIS FILE:
   Each exercise has a scenario comment, a rubric tag (JN#)
   for reference, and a TODO with a query skeleton. Replace
   with the correct SQL. Run each query as you
   go -- don't wait until the end. If a result looks empty or
   wrong, that's useful information, not a sign to skip it.
   ============================================================ */


/* ============================================================
   PART 1: INNER JOIN WARM-UP
   ------------------------------------------------------------
   Reminder: INNER JOIN only returns rows where the joined
   columns match in BOTH tables. If a row in the left table
   has no match in the right table, it disappears entirely.
   ============================================================ */

-- JN1: List every open vulnerability finding together with the
-- asset's name and criticality. Show cve_id, cvss_score,
-- asset_name, and criticality. Order by cvss_score, highest first.
-- HINT: you only need vulnerability_scan and asset.

SELECT
    v.cve_id,
    v.cvss_score,
    a.asset_name,
    a.criticality
FROM vulnerability_scan v
INNER JOIN asset a
    ON v.asset_id = a.asset_id
WHERE v.status = 'OPEN'
ORDER BY v.cvss_score DESC;



-- JN2: The security architecture team wants a list of every
-- DEPRECATED software component still running, along with the
-- asset it's on and that asset's owning team (so they know who
-- to email). Show component_name, component_version,
-- asset_name, owner_team.

SELECT
    s.component_name,
    s.component_version,
    a.asset_name,
    a.owner_team
FROM sbom_component s
INNER JOIN asset a
    ON s.asset_id = a.asset_id
WHERE s.is_deprecated = TRUE;



-- JN3: Three-table INNER JOIN. Find every vulnerability finding
-- that ALSO has a matching threat intelligence alert (i.e. it's
-- not just something our scanner found -- an outside source has
-- flagged it too). Show asset_name, cve_id, cvss_score,
-- alert_source, and severity (from the alert).
-- HINT: the join condition between vulnerability_scan and
-- threat_intel_alert is on the CVE ID -- but the column names
-- are NOT identical in both tables. Check the schema.

SELECT
    a.asset_name,
    v.cve_id,
    v.cvss_score,
    t.alert_source,
    t.severity
FROM vulnerability_scan v
INNER JOIN asset a
    ON v.asset_id = a.asset_id
INNER JOIN threat_intel_alert t
    ON v.cve_id = t.related_cve_id;


-- STOP AND PREDICT before running JN3: how many rows do you
-- expect back, given there are 7 vulnerability_scan rows and
-- 4 threat_intel_alert rows? Write your guess as a comment,
-- then run the query and compare.
-- My prediction: ______ rows
-- My prediction: 4 rows-- I was wrong it was 3


/* ============================================================
   PART 2: LEFT JOIN — FINDING THE GAPS
   ------------------------------------------------------------
   Reminder: LEFT JOIN keeps EVERY row from the left table,
   even when there's no match in the right table -- the
   unmatched columns come back as NULL. This is how you find
   "assets with no scan," "vulnerabilities with no risk entry,"
   and similar gaps that INNER JOIN would silently hide.
   ============================================================ */

-- JN4: List every asset and how many vulnerability findings
-- it has -- INCLUDING assets with zero findings. Show
-- asset_name and finding_count. Order by finding_count
-- ascending so the zero-finding assets appear first.
-- HINT: you'll need LEFT JOIN + GROUP BY + COUNT, like Day 2.
-- Be careful which column you COUNT -- COUNT(*) behaves
-- differently from COUNT(a_specific_column) when rows are NULL.

SELECT
    a.asset_name,
    COUNT(v.scan_id) AS finding_count
FROM asset a
LEFT JOIN vulnerability_scan v
    ON a.asset_id = v.asset_id
GROUP BY a.asset_name
ORDER BY finding_count ASC;
   


-- JN5: The governance team wants to know which assets have a
-- KNOWN vulnerability finding but NO entry in the risk register
-- at all -- meaning something was found, but nobody has formally
-- assessed the risk yet. Show asset_name and owner_team.
-- HINT: LEFT JOIN asset to risk_register, then filter for the
-- rows where the right-side match came back empty. You'll also
-- need to join in vulnerability_scan to restrict this to assets
-- that actually have a finding.

SELECT DISTINCT
    a.asset_name,
    a.owner_team
FROM asset a
INNER JOIN vulnerability_scan v
    ON a.asset_id = v.asset_id
LEFT JOIN risk_register r
    ON a.asset_id = r.asset_id
WHERE r.asset_id IS NULL;


-- JN6: List every vulnerability finding and, if one exists, its
-- matching threat intel alert's severity. If there's no matching
-- alert, show the literal text 'No external intel' instead of
-- NULL. Show cve_id, cvss_score, and a column called intel_severity.
-- HINT: LEFT JOIN, then COALESCE() to replace NULL with a
-- fallback value.

SELECT
    v.cve_id,
    v.cvss_score,
    COALESCE(t.severity, 'No external intel') AS intel_severity
FROM vulnerability_scan v
LEFT JOIN threat_intel_alert t 
	ON v.cve_id = t.related_cve_id;


-- JN7: Flip the direction of JN3/JN6. Starting from
-- threat_intel_alert this time, find every alert that has NO
-- matching row in vulnerability_scan at all -- meaning outside
-- intel is warning about a CVE that our OWN scanner has not
-- found on any asset yet. Show related_cve_id, alert_source,
-- severity, and description.
-- HINT: the "driving" table (the one after FROM) matters. If
-- you LEFT JOIN from vulnerability_scan like before, you cannot
-- find this. You need to start FROM threat_intel_alert instead.

SELECT
    t.related_cve_id,
    t.alert_source,
    t.severity,
    t.description
FROM threat_intel_alert t
LEFT JOIN vulnerability_scan v ON t.related_cve_id = v.cve_id
WHERE v.scan_id IS NULL;

-- STOP AND REFLECT after running JN7: how is this result
-- different from JN3, and why does the direction of the JOIN
-- (which table comes after FROM) matter here?
-- Your answer: ______

-- JN7 finds threat-intel alerts that our vulnerability scanner has not found,
-- while JN3 finds vulnerabilities that have a matching threat-intel alert.
-- The JOIN direction matters because LEFT JOIN keeps every row from the
-- table after FROM. JN7 starts with threat_intel_alert, so it can find
-- alerts with no matching scanner finding. JN3 starts with
-- vulnerability_scan, so it focuses on scanner findings with matching
-- external intelligence.

-- JN8: Find every asset that has NO system_log entries at all --
-- a monitoring blind spot, regardless of whether that asset has
-- known vulnerabilities. Show asset_name, asset_type, and
-- criticality.

SELECT
    a.asset_name,
    a.asset_type,
    a.criticality
FROM asset a
LEFT JOIN system_log l 
	ON a.asset_id = l.asset_id
WHERE l.log_id IS NULL;


/* ============================================================
   PART 3: EXECUTIVE BLIND-SPOT REPORT (capstone-style)
   ------------------------------------------------------------
   SCENARIO: Leadership has asked for a single report ahead of
   tomorrow's risk committee meeting:

     "Show me every asset where we KNOW about a vulnerability
      (an internal finding, confirmed by external threat intel
      as actively exploited), but where NO formal risk register
      entry exists yet. These are our biggest blind spots --
      known, dangerous, and unmanaged."

   This combines what you built in Parts 1 and 2. Plan it out
   on paper or in comments before you start typing SQL.
   ============================================================ */

-- JN9: Build the blind-spot report described above.
-- Required output columns: asset_name, owner_team, cve_id,
-- cvss_score, alert_source (from threat_intel_alert).
-- Required logic:
--   1. Only include vulnerabilities CONFIRMED by threat intel
--      (an INNER JOIN or matching condition against
--      threat_intel_alert).
--   2. Only include assets with NO risk_register entry
--      (a LEFT JOIN against risk_register, filtered on NULL).
-- Order by cvss_score, highest first.

-- Plan your JOINs here as comments first:
--   Table 1: asset
--   Table 2: vulnerability_scan   (join type: INNER JOIN)
--   Table 3: threat_intel_alert   (join type: INNER JOIN)
--   Table 4: risk_register        (join type: LEFT JOIN)

SELECT
    a.asset_name,
    a.owner_team,
    v.cve_id,
    v.cvss_score,
    t.alert_source
FROM asset a
INNER JOIN vulnerability_scan v
    ON a.asset_id = v.asset_id
INNER JOIN threat_intel_alert t
    ON v.cve_id = t.related_cve_id
LEFT JOIN risk_register r
    ON a.asset_id = r.asset_id
WHERE r.asset_id IS NULL
ORDER BY v.cvss_score DESC;


/* ============================================================
   PART 4: SET OPERATIONS, NULLS, AND DATES
   ------------------------------------------------------------
   Two independent lists of CVE IDs exist in this schema:
   the ones your OWN scanner has found (vulnerability_scan.cve_id)
   and the ones EXTERNAL threat intel has flagged
   (threat_intel_alert.related_cve_id). UNION, INTERSECT, and
   EXCEPT are three different ways to compare those two lists.
   All three require the combined queries to select the SAME
   NUMBER of columns, in compatible data types.
   ============================================================ */

-- JN10: UNION vs UNION ALL. Build a single "watch list" of
-- asset names combining two groups: (a) assets with an OPEN
-- vulnerability finding, and (b) assets running a DEPRECATED
-- SBOM component. Some assets qualify under BOTH conditions.
-- First, write it with UNION (removes duplicates):

SELECT a.asset_name
FROM asset a
JOIN vulnerability_scan v ON a.asset_id = v.asset_id
WHERE v.status = 'Open'
UNION
SELECT a.asset_name
FROM asset a
JOIN sbom_component s ON a.asset_id = s.asset_id
WHERE s.is_deprecated = TRUE;

SELECT a.asset_name
FROM asset a
JOIN vulnerability_scan v ON a.asset_id = v.asset_id
WHERE v.status = 'Open'
UNION ALL
SELECT a.asset_name
FROM asset a
JOIN sbom_component s ON a.asset_id = s.asset_id
WHERE s.is_deprecated = TRUE;

-- Now copy the query above and change UNION to UNION ALL and
-- rerun it. Compare the row counts.
-- UNION returned 5 rows. UNION ALL returned 8 rows.
-- Which asset(s) appear twice under UNION ALL, and why?
-- Your answer: ______

-- auth-service-03, container-api-gateway, and
-- legacy-file-server appear twice because they have both an
-- OPEN vulnerability finding and a DEPRECATED SBOM component.

-- JN11: Build one UNION query that lists every DISTINCT CVE ID
-- known to the organization from EITHER source -- your own
-- scans OR external threat intel. One column, named cve_id,
-- in the final result.

SELECT cve_id 
FROM vulnerability_scan
UNION
SELECT related_cve_id 
FROM threat_intel_alert;


-- JN12: Using the same two column sources as JN11, use
-- INTERSECT to find only the CVE IDs that appear in BOTH lists --
-- confirmed independently by your own scanner AND by external
-- threat intel.
-- STOP AND PREDICT: you already answered a version of this
-- question with a JOIN in JN3. How many CVE IDs do you expect?
-- My prediction: 3

SELECT cve_id FROM vulnerability_scan
INTERSECT
SELECT related_cve_id FROM threat_intel_alert;


-- JN13: Using EXCEPT, find every CVE ID that shows up in
-- threat_intel_alert but is ABSENT from vulnerability_scan --
-- i.e. outside intel is warning about something your own
-- scanning has never found. (You solved this exact question
-- with a LEFT JOIN in JN7 -- this is a second way to get there.)
-- Order matters with EXCEPT: which query goes first decides
-- which list you're subtracting FROM.

SELECT related_cve_id FROM threat_intel_alert
EXCEPT
SELECT cve_id FROM vulnerability_scan;


-- JN14: NULL handling, no JOIN required. vulnerability_scan has
-- a remediation_deadline column that is sometimes NULL. Find
-- every OPEN vulnerability finding that is MISSING a deadline --
-- this is a process gap (an accepted-risk finding having no
-- deadline is normal and should NOT show up here). Show cve_id
-- and status.
-- HINT: you need both a status filter AND a NULL check.

SELECT cve_id, status
FROM vulnerability_scan
WHERE status = 'Open'
  AND remediation_deadline IS NULL;


-- JN15: Dates, part 1. For every vulnerability finding that DOES
-- have a remediation_deadline set, calculate how many days the
-- team was given to fix it (remediation_deadline minus scan_date).
-- Show cve_id, scan_date, remediation_deadline, and a column
-- called sla_days. Order by sla_days ascending (tightest
-- deadlines first).
-- HINT: subtracting one DATE from another in PostgreSQL returns
-- a plain integer number of days.

SELECT
    cve_id,
    scan_date,
    remediation_deadline,
    remediation_deadline - scan_date AS sla_days
FROM vulnerability_scan
WHERE remediation_deadline IS NOT NULL
ORDER BY sla_days ASC;


-- JN16: Dates, part 2. Treat 2024-04-10 as "today" for this
-- exercise. Find every OPEN vulnerability finding whose
-- remediation_deadline has already passed as of that date.
-- Show cve_id, remediation_deadline.
-- STOP AND PREDICT before running: CVE-2024-4444's deadline is
-- exactly 2024-04-10. Will it show up in your result if you use
-- a strict "<" comparison? What about "<="? Try both and note
-- the difference.

SELECT cve_id, remediation_deadline
FROM vulnerability_scan
WHERE status = 'Open'
  AND remediation_deadline <= DATE '2024-04-10';


/* ============================================================
   WRAP-UP / EXIT TICKET
   ------------------------------------------------------------
   Answer briefly in comments before you leave:

   1. In your own words, what's the difference between what
      INNER JOIN and LEFT JOIN return when there's no match?

-- 1. INNER JOIN only returns rows where there is a match in both tables. 
-- If there is no match, that row is left out.
-- LEFT JOIN keeps every row from the left table. If there is no match in 
-- the right table, the right-side columns are returned as NULL.

   2. Why did JN7 require starting FROM threat_intel_alert
      instead of FROM vulnerability_scan? How did JN13 answer
      the same question a different way?

-- 2. JN7 started FROM threat_intel_alert because we wanted to find alerts that our 
-- own vulnerability scanner had NOT found.  A LEFT JOIN keeps every row from the table 
-- after FROM, so starting with threat_intel_alert allowed us to find alerts
-- with no matching vulnerability_scan row.  JN13 answered the same question using EXCEPT. 
-- It took the threat_intel_alert CVE list and subtracted the CVE IDs found
-- by vulnerability_scan, leaving only CVEs known by external intel but absent from our 
-- own scans.	  

   3. In JN14, a NULL remediation_deadline meant something
      different for an 'Open' row than for an 'Accepted Risk'
      row. Why is it important to check the status alongside
      the NULL, rather than just filtering on IS NULL alone?

-- 3. It is important to check the status along with the NULL because a missing remediation 
-- deadline does not always mean there is a problem. An 'Open' vulnerability with no 
-- deadline is a process gap, while an 'Accepted Risk' with no deadline can be normal. 
-- Checking both prevents us from incorrectly reporting accepted risks as missing deadlines.
	  

   4. Name one real finding from today's data (an asset, a gap,
      a blind spot) that you think a security team would
      actually want to act on.

-- 4. One real finding is the legacy-file-server, which has CVE-2024-7777 with a CVSS 
-- score of 6.1. It was found by our vulnerability scanner and confirmed by external threat 
-- intel, but it has no risk-register entry. The security team should formally assess and 
-- manage this vulnerability.	  
   ============================================================ */

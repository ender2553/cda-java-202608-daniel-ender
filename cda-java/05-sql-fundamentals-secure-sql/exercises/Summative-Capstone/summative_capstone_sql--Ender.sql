-- =====================================================================
-- Solstice Health Network — SOC Database
-- Setup script for the PostgreSQL Summative Capstone
-- Run this once per student/lab environment before the exercise begins.
-- =====================================================================

DROP TABLE IF EXISTS ticket CASCADE;
DROP TABLE IF EXISTS event CASCADE;
DROP TABLE IF EXISTS service CASCADE;
DROP TABLE IF EXISTS host CASCADE;
DROP TABLE IF EXISTS app_user CASCADE;

-- ---------------------------------------------------------------------
-- app_user  (named app_user, not "user", to avoid the reserved-word
-- double-quoting trap for beginners — flag this naming choice for
-- students explicitly in the packet)
-- ---------------------------------------------------------------------
CREATE TABLE app_user (
    user_id     INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username    TEXT NOT NULL UNIQUE,
    full_name   TEXT NOT NULL,
    role        TEXT NOT NULL,       -- analyst, senior_analyst, manager, admin
    department  TEXT NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE
);

-- ---------------------------------------------------------------------
-- host
-- ---------------------------------------------------------------------
CREATE TABLE host (
    host_id        INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    hostname       TEXT NOT NULL UNIQUE,
    ip_address     TEXT NOT NULL,
    os             TEXT NOT NULL,
    criticality    TEXT NOT NULL,    -- low, medium, high, critical
    department     TEXT NOT NULL,
    decommissioned BOOLEAN NOT NULL DEFAULT FALSE
);

-- ---------------------------------------------------------------------
-- service
-- ---------------------------------------------------------------------
CREATE TABLE service (
    service_id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    host_id      INTEGER NOT NULL REFERENCES host(host_id),
    service_name TEXT NOT NULL,
    port         INTEGER NOT NULL,
    is_public    BOOLEAN NOT NULL DEFAULT FALSE,
    last_patched DATE NOT NULL
);

-- ---------------------------------------------------------------------
-- event
-- ---------------------------------------------------------------------
CREATE TABLE event (
    event_id    INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    host_id     INTEGER NOT NULL REFERENCES host(host_id),
    service_id  INTEGER REFERENCES service(service_id),   -- nullable
    event_type  TEXT NOT NULL,      -- failed_login, port_scan, malware_detected,
                                     -- unauthorized_access, patch_missing
    severity    TEXT NOT NULL,      -- low, medium, high, critical
    event_time  TIMESTAMP NOT NULL,
    description TEXT NOT NULL,
    resolved    BOOLEAN NOT NULL DEFAULT FALSE
);

-- ---------------------------------------------------------------------
-- ticket
-- ---------------------------------------------------------------------
CREATE TABLE ticket (
    ticket_id   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id    INTEGER NOT NULL REFERENCES event(event_id),
    assigned_to INTEGER NOT NULL REFERENCES app_user(user_id),
    status      TEXT NOT NULL,      -- open, in_progress, closed, escalated
    priority    TEXT NOT NULL,      -- low, medium, high, critical
    opened_at   TIMESTAMP NOT NULL,
    closed_at   TIMESTAMP
);

-- =====================================================================
-- SEED DATA
-- =====================================================================

INSERT INTO app_user (username, full_name, role, department, is_active) VALUES
('jchen',   'Jamie Chen',    'senior_analyst', 'SOC', TRUE),
('rpatel',  'Ravi Patel',    'analyst',        'SOC', TRUE),
('mgarcia', 'Maria Garcia',  'analyst',        'SOC', TRUE),
('twright', 'Tom Wright',    'manager',        'SOC', TRUE),
('klee',    'Kevin Lee',     'analyst',        'SOC', FALSE),   -- offboarded, still has an assigned ticket
('sbrooks', 'Sam Brooks',    'senior_analyst', 'SOC', TRUE),
('aokafor', 'Amara Okafor',  'admin',          'IT',  TRUE),
('dsingh',  'Divya Singh',   'analyst',        'SOC', TRUE);

INSERT INTO host (hostname, ip_address, os, criticality, department, decommissioned) VALUES
('RAD-SRV01',      '10.10.1.11', 'Windows Server 2019',    'high',     'Radiology',        FALSE),
('EHR-DB01',       '10.10.2.5',  'Linux (RHEL 8)',         'critical', 'Patient Records',  FALSE),
('EHR-APP01',      '10.10.2.6',  'Linux (RHEL 8)',         'critical', 'Patient Records',  FALSE),
('BILL-SRV01',     '10.10.3.10', 'Windows Server 2016',    'medium',   'Billing',          FALSE),
('PHARM-SRV01',    '10.10.4.8',  'Windows Server 2019',    'high',     'Pharmacy',         FALSE),
('LAB-SRV01',      '10.10.5.12', 'Linux (Ubuntu 20.04)',   'high',     'Laboratory',       FALSE),
('ADMIN-WKS03',    '10.10.6.44', 'Windows 10',             'low',      'Administration',   FALSE),
('GUEST-AP02',     '10.10.9.2',  'Embedded (Aruba)',       'low',      'Guest Network',    FALSE),
('BACKUP-SRV01',   '10.10.7.3',  'Linux (RHEL 8)',         'critical', 'IT Operations',    FALSE),
('WEB-SRV01',      '10.10.8.20', 'Linux (Ubuntu 22.04)',   'medium',   'Patient Portal',   FALSE),
('LEGACY-FILESRV', '10.10.6.50', 'Windows Server 2008 R2', 'high',     'Administration',   TRUE),
('HR-SRV01',       '10.10.6.15', 'Windows Server 2019',    'medium',   'Human Resources',  FALSE);

INSERT INTO service (host_id, service_name, port, is_public, last_patched) VALUES
(1,  'RDP',        3389, FALSE, '2025-11-01'),
(1,  'SMB',        445,  FALSE, '2025-08-15'),
(2,  'PostgreSQL', 5432, FALSE, '2026-02-01'),
(2,  'SSH',        22,   FALSE, '2026-01-20'),
(3,  'HTTPS',      443,  TRUE,  '2026-02-10'),
(3,  'SSH',        22,   FALSE, '2026-01-20'),
(4,  'RDP',        3389, FALSE, '2025-09-01'),
(5,  'SMB',        445,  FALSE, '2026-01-05'),
(6,  'SSH',        22,   FALSE, '2026-02-15'),
(6,  'HTTPS',      443,  TRUE,  '2026-02-15'),
(7,  'RDP',        3389, FALSE, '2025-07-10'),
(9,  'SSH',        22,   FALSE, '2026-01-30'),
(10, 'HTTPS',      443,  TRUE,  '2026-02-12'),
(11, 'SMB',        445,  FALSE, '2024-03-01'),
(12, 'RDP',        3389, FALSE, '2025-12-01');

INSERT INTO event (host_id, service_id, event_type, severity, event_time, description, resolved) VALUES
(7,  11, 'failed_login',        'low',      '2026-02-01 03:14:00', 'Repeated failed RDP logins from external IP', TRUE),
(7,  11, 'failed_login',        'low',      '2026-02-01 03:16:00', 'Repeated failed RDP logins from external IP', TRUE),
(7,  11, 'failed_login',        'low',      '2026-02-01 03:19:00', 'Repeated failed RDP logins from external IP', TRUE),
(7,  11, 'failed_login',        'medium',   '2026-02-01 03:22:00', 'Failed login count exceeded threshold', FALSE),
(7,  11, 'port_scan',           'low',      '2026-01-28 22:05:00', 'External port scan detected against workstation', TRUE),
(2,  4,  'unauthorized_access', 'critical', '2026-02-03 01:47:00', 'Anomalous query pattern against patient records DB from internal host', FALSE),
(2,  4,  'failed_login',        'medium',   '2026-02-03 01:40:00', 'Multiple failed SSH logins preceding DB anomaly', FALSE),
(3,  5,  'malware_detected',    'critical', '2026-02-05 09:12:00', 'Ransomware signature flagged on EHR application server', FALSE),
(3,  6,  'port_scan',           'medium',   '2026-01-30 14:20:00', 'Internal port scan from Radiology subnet', TRUE),
(1,  1,  'unauthorized_access', 'high',     '2026-02-06 16:03:00', 'Unrecognized RDP session on imaging server', FALSE),
(1,  2,  'patch_missing',       'medium',   '2026-02-07 08:00:00', 'SMB service overdue for patch (>150 days)', FALSE),
(4,  7,  'failed_login',        'low',      '2026-01-25 11:30:00', 'Failed login on billing RDP', TRUE),
(4,  7,  'patch_missing',       'low',      '2026-02-07 08:00:00', 'RDP service overdue for patch', FALSE),
(5,  8,  'malware_detected',    'high',     '2026-02-04 19:45:00', 'Suspicious executable quarantined on pharmacy server', TRUE),
(5,  8,  'failed_login',        'medium',   '2026-02-04 19:30:00', 'Failed SMB auth attempts preceding malware alert', TRUE),
(6,  9,  'unauthorized_access', 'high',     '2026-02-08 13:10:00', 'Unexpected SSH key used to access lab server', FALSE),
(6,  10, 'port_scan',           'low',      '2026-01-29 05:55:00', 'Port scan against public lab results portal', TRUE),
(8,  NULL,'unauthorized_access','low',      '2026-02-02 12:00:00', 'Unregistered device joined guest network', TRUE),
(9,  12, 'failed_login',        'medium',   '2026-02-09 02:11:00', 'Failed SSH logins against backup server', FALSE),
(9,  12, 'unauthorized_access', 'critical', '2026-02-09 02:20:00', 'Backup integrity job disabled without change ticket', FALSE),
(10, 13, 'port_scan',           'medium',   '2026-01-31 20:40:00', 'Port scan against public patient portal', TRUE),
(10, 13, 'unauthorized_access', 'high',     '2026-02-10 07:15:00', 'Session token reuse detected on patient portal', FALSE),
(11, 14, 'malware_detected',    'critical', '2026-02-06 23:58:00', 'Legacy file server flagged despite decommission status', FALSE),
(11, 14, 'patch_missing',       'high',     '2026-02-07 08:00:00', 'SMB service unpatched since 2024', FALSE),
(12, 15, 'failed_login',        'low',      '2026-01-27 10:05:00', 'Failed login on HR RDP', TRUE),
(2,  3,  'patch_missing',       'low',      '2026-02-07 08:00:00', 'PostgreSQL service patch review due', TRUE),
(3,  NULL,'unauthorized_access','medium',   '2026-02-05 09:20:00', 'Follow-up: lateral movement attempt after malware alert', FALSE),
(7,  NULL,'malware_detected',   'low',      '2026-02-11 09:00:00', 'Low-severity adware found on admin workstation', TRUE),
(1,  NULL,'failed_login',       'low',      '2026-02-12 15:40:00', 'Single failed badge-linked login on imaging server', TRUE),
(6,  9,  'failed_login',        'low',      '2026-02-12 16:02:00', 'Single failed SSH login on lab server', TRUE);

INSERT INTO ticket (event_id, assigned_to, status, priority, opened_at, closed_at) VALUES
(4,  2, 'closed',      'medium',   '2026-02-01 03:30:00', '2026-02-01 09:00:00'),
(6,  1, 'escalated',   'critical', '2026-02-03 02:00:00', NULL),
(7,  1, 'in_progress', 'medium',   '2026-02-03 02:05:00', NULL),
(8,  6, 'escalated',   'critical', '2026-02-05 09:20:00', NULL),
(10, 3, 'in_progress', 'high',     '2026-02-06 16:15:00', NULL),
(11, 4, 'open',        'medium',   '2026-02-07 08:10:00', NULL),
(13, 4, 'open',        'low',      '2026-02-07 08:10:00', NULL),
(14, 2, 'closed',      'high',     '2026-02-04 19:50:00', '2026-02-05 10:00:00'),
(16, 6, 'in_progress', 'high',     '2026-02-08 13:20:00', NULL),
(19, 5, 'in_progress', 'medium',   '2026-02-09 02:15:00', NULL),
(20, 1, 'escalated',   'critical', '2026-02-09 02:25:00', NULL),
(22, 3, 'in_progress', 'high',     '2026-02-10 07:20:00', NULL),
(23, 6, 'escalated',   'critical', '2026-02-06 23:59:00', NULL),
(24, 4, 'open',        'high',     '2026-02-07 08:15:00', NULL),
(26, 8, 'closed',      'low',      '2026-02-05 09:25:00', '2026-02-06 11:00:00'),
(2,  1, 'closed',      'low',      '2026-02-01 03:15:00', '2026-02-01 06:00:00'),
(9,  8, 'closed',      'medium',   '2026-01-30 14:25:00', '2026-01-31 09:00:00'),
(15, 8, 'closed',      'low',      '2026-01-29 06:00:00', '2026-01-29 12:00:00'),
(1,  2, 'closed',      'low',      '2026-02-01 03:15:00', '2026-02-01 06:00:00'),
(27, 6, 'closed',      'low',      '2026-02-11 09:15:00', '2026-02-11 11:00:00');

--===============SUMATIVE CAPSTONE POSTGRESQL -- ENDER============
--Part1--Onboarding: Get Oriented--

--1.1

SELECT *
FROM host
WHERE department = 'Patient Records';

--EHR-DB01, EHR-APP01

--1.2

SELECT username, full_name, role
FROM app_user
WHERE is_active = TRUE;


--"jchen"	"Jamie Chen"	"senior_analyst"
--"rpatel"	"Ravi Patel"	"analyst"
--"mgarcia"	"Maria Garcia"	"analyst"
--"twright"	"Tom Wright"	"manager"
--"sbrooks"	"Sam Brooks"	"senior_analyst"
--"aokafor"	"Amara Okafor"	"admin"
--"dsingh"	"Divya Singh"	"analyst"

--1.3

SELECT COUNT(*) AS total_security_events
FROM event;

--30

--1.4

SELECT DISTINCT event_type
FROM event
ORDER BY event_type;

--"failed_login"
--"malware_detected"
--"patch_missing"
--"port_scan"
--"unauthorized_access"

--1.5

SELECT service_name, port
FROM service
WHERE is_public = TRUE
ORDER BY port;

--"HTTPS"	443
--"HTTPS"	443
--"HTTPS"	443

--Part2--Morning Briefing: Aggregates & Grouping--

--2.1

SELECT host_id, COUNT(*) AS event_count
FROM event
GROUP BY host_id
ORDER BY event_count DESC;

--7/	6
--3/	3
--6/	3
--2/	3
--1/	3
--11/   2
--9/	2
--5/	2
--4/	2
--10/   2
--12/   1
--8/	1

--2.2

SELECT host_id, COUNT(*) AS event_count
FROM event
GROUP BY host_id
HAVING COUNT(*) > 2
ORDER BY event_count DESC;

--7/	6
--3/	3
--6/	3
--2/	3
--1/	3

--2.3

SELECT severity, COUNT(*) AS event_count
FROM event
GROUP BY severity
ORDER BY event_count DESC;

--"low"	     13
--"medium"	 8
--"high"	 5
--"critical" 4

--2.4

SELECT status, COUNT(*) AS ticket_count
FROM ticket
GROUP BY status
ORDER BY ticket_count DESC;

--"closed"	    8
--"in_progress"	5
--"escalated"	4
--"open"	    3

--2.5

--Host_id 7 is not necessarily the biggest security concern just because it has the most events. 
--I would need to check the severity and types of its six events, as well as the host's criticality, 
--to determine the actual risk; the later join analysis in this packet will let me examine those details.

--Part3--Relational Investigation: Joins--

--3.1

SELECT
    h.hostname,
    h.department,
    e.event_type,
    e.severity,
    e.event_time
FROM event AS e
JOIN host AS h
    ON e.host_id = h.host_id
WHERE e.severity = 'critical'
ORDER BY e.event_time ASC;

--"EHR-DB01"		"Patient Records"	"unauthorized_access"	"critical"	"2026-02-03 01:47:00"
--"EHR-APP01"		"Patient Records"	"malware_detected"		"critical"	"2026-02-05 09:12:00"
--"LEGACY-FILESRV"	"Administration"	"malware_detected"		"critical"	"2026-02-06 23:58:00"
--"BACKUP-SRV01"	"IT Operations"		"unauthorized_access"	"critical"	"2026-02-09 02:20:00"

--3.2

SELECT
    h.hostname,
    h.department,
    h.decommissioned,
    e.event_type,
    e.severity,
    e.event_time
FROM event AS e
JOIN host AS h
    ON e.host_id = h.host_id
WHERE e.severity = 'critical'
  AND h.decommissioned = TRUE
ORDER BY e.event_time ASC;

--Yes there is a critical event on host marked as decommissioned:
--"LEGACY-FILESRV"	"Administration"	true	"malware_detected"	"critical"	"2026-02-06 23:58:00"

--3.3

SELECT
    h.hostname,
    h.department,
    h.criticality,
    e.event_type,
    e.severity,
    t.priority
FROM ticket AS t
JOIN event AS e
    ON t.event_id = e.event_id
JOIN host AS h
    ON e.host_id = h.host_id
WHERE t.status IN ('open', 'escalated')
ORDER BY h.hostname;

--"BACKUP-SRV01"	"IT Operations"		"critical"	"unauthorized_access"	"critical"	"critical"
--"BILL-SRV01"		"Billing"			"medium"	"patch_missing"			"low"		"low"
--"EHR-APP01"		"Patient Records"	"critical"	"malware_detected"		"critical"	"critical"
--"EHR-DB01"		"Patient Records"	"critical"	"unauthorized_access"	"critical"	"critical"
--"LEGACY-FILESRV"	"Administration"	"high"		"malware_detected"		"critical"	"critical"
--"LEGACY-FILESRV"	"Administration"	"high"		"patch_missing"			"high"		"high"
--"RAD-SRV01"		"Radiology"			"high"		"patch_missing"			"medium"	"medium"

--3.4

SELECT
    t.ticket_id,
    t.status,
    t.priority,
    a.full_name,
    a.is_active
FROM ticket AS t
JOIN app_user AS a
    ON t.assigned_to = a.user_id
ORDER BY t.ticket_id;

--1	 "closed"		"medium"	"Ravi Patel"	true
--2	 "escalated"	"critical"	"Jamie Chen"	true
--3	 "in_progress"	"medium"	"Jamie Chen"	true
--4	 "escalated"	"critical"	"Sam Brooks"	true
--5  "in_progress"	"high"		"Maria Garcia"	true
--6	 "open"			"medium"	"Tom Wright"	true
--7	 "open"			"low"		"Tom Wright"	true
--8	 "closed"		"high"		"Ravi Patel"	true
--9  "in_progress"	"high"		"Sam Brooks"	true
--10 "in_progress"	"medium"	"Kevin Lee"		false
--11 "escalated"	"critical"	"Jamie Chen"	true
--12 "in_progress"	"high"		"Maria Garcia"	true
--13 "escalated"	"critical"	"Sam Brooks"	true
--14 "open"			"high"		"Tom Wright"	true
--15 "closed"		"low"		"Divya Singh"	true
--16 "closed"		"low"		"Jamie Chen"	true
--17 "closed"		"medium"	"Divya Singh"	true
--18 "closed"		"low"		"Divya Singh"	true
--19 "closed"		"low"		"Ravi Patel"	true
--20 "closed"		"low"		"Sam Brooks"	true


--3.5

SELECT
    t.ticket_id,
    t.status,
    t.priority,
    a.full_name,
    a.is_active
FROM ticket AS t
JOIN app_user AS a
    ON t.assigned_to = a.user_id
WHERE t.status <> 'closed'
  AND a.is_active = FALSE
ORDER BY t.ticket_id;

--Ticket 10 is an operational gap because it is still in_progress but is assigned to Kevin Lee, 
--whose analyst account is inactive. This means the ticket may be sitting with an analyst who 
--is no longer available to handle it and should be reassigned to an active analyst.

--Part4--Deeper Analysis: Subqueries

--4.1
SELECT
    host_id,
    hostname,
    department,
    criticality
FROM host
WHERE host_id NOT IN (
    SELECT host_id
    FROM event
    WHERE severity = 'critical'
)
ORDER BY host_id;

--1	"RAD-SRV01"		"Radiology"			"high"
--4	"BILL-SRV01"	"Billing"			"medium"
--5	"PHARM-SRV01"	"Pharmacy"			"high"
--6	"LAB-SRV01"		"Laboratory"		"high"
--7	"ADMIN-WKS03"	"Administration"	"low"
--8	"GUEST-AP02"	"Guest Network"		"low"
--10 "WEB-SRV01"	"Patient Portal"	"medium"
--12 "HR-SRV01"		"Human Resources"	"medium"

--4.2

SELECT
    h.host_id,
    h.hostname,
    COUNT(e.event_id) AS event_count
FROM host AS h
LEFT JOIN event AS e
    ON h.host_id = e.host_id
GROUP BY h.host_id, h.hostname
HAVING COUNT(e.event_id) > (
    SELECT AVG(event_count)
    FROM (
        SELECT COUNT(*) AS event_count
        FROM event
        GROUP BY host_id
    ) AS host_event_counts
)
ORDER BY event_count DESC;

--7	"ADMIN-WKS03"	6
--6	"LAB-SRV01"	3
--1	"RAD-SRV01"	3
--2	"EHR-DB01"	3
--3	"EHR-APP01"	3

--4.3

SELECT
    user_id,
    username,
    full_name,
    role,
    department,
    is_active
FROM app_user
WHERE user_id NOT IN (
    SELECT assigned_to
    FROM ticket
)
ORDER BY user_id;

--7	"aokafor"	"Amara Okafor"	"admin"	"IT"	true
--The subquery found one analyst who is not currently assigned to any ticket: 
--Amara Okafor (`aokafor`). Her account is active, so she is available but currently 
--has no ticket assigned to her.

--Part5--Incident Response Actions--

--5.1  

INSERT INTO event (
    host_id,
    service_id,
    event_type,
    severity,
    event_time,
    description,
    resolved
)
SELECT
    h.host_id,
    s.service_id,
    'phishing_detected',
    'high',
    TIMESTAMP '2026-02-13 10:00:00',
    'Phishing landing page hosted on patient portal',
    FALSE
FROM host AS h
JOIN service AS s
    ON s.host_id = h.host_id
WHERE h.hostname = 'WEB-SRV01'
  AND s.service_name = 'HTTPS';

--Verification--

SELECT
    e.event_id,
    h.hostname,
    s.service_name,
    e.event_type,
    e.severity,
    e.event_time,
    e.description,
    e.resolved
FROM event AS e
JOIN host AS h
    ON e.host_id = h.host_id
JOIN service AS s
    ON e.service_id = s.service_id
WHERE h.hostname = 'WEB-SRV01'
  AND e.event_type = 'phishing_detected';

--5.2

SELECT
    user_id,
    username,
    full_name,
    is_active
FROM app_user
WHERE username = 'sbrooks';

--=========================
INSERT INTO ticket (
    event_id,
    assigned_to,
    status,
    priority,
    opened_at
)
VALUES (
    31,
    6,
    'open',
    'high',
    TIMESTAMP '2026-02-13 10:15:00'
);

--======Verification=============

SELECT
    t.ticket_id,
    t.event_id,
    a.username,
    a.full_name,
    t.status,
    t.priority,
    t.opened_at
FROM ticket AS t
JOIN app_user AS a
    ON t.assigned_to = a.user_id
WHERE t.event_id = 31;

--5.3

--======Indentify=======
SELECT
    t.ticket_id,
    t.status,
    t.priority,
    e.event_id,
    e.event_type,
    e.severity,
    e.resolved
FROM ticket AS t
JOIN event AS e
    ON t.event_id = e.event_id
JOIN host AS h
    ON e.host_id = h.host_id
WHERE h.hostname = 'EHR-DB01'
  AND e.event_type = 'unauthorized_access'
  AND e.severity = 'critical';

--====Update the ticket====

UPDATE ticket
SET
    status = 'closed',
    closed_at = TIMESTAMP '2026-02-13 12:00:00'
WHERE ticket_id = 2;

--=====Mark as resolved======

UPDATE event
SET resolved = TRUE
WHERE event_id = 6;

--=====Verification=======

SELECT
    t.ticket_id,
    t.status,
    t.closed_at,
    e.event_id,
    e.event_type,
    e.resolved
FROM ticket AS t
JOIN event AS e
    ON t.event_id = e.event_id
WHERE t.ticket_id = 2;

--Resolved--
--2	"closed"	"2026-02-13 12:00:00"	6	"unauthorized_access"	true

--5.4
--=======Find orphaned tickets========
SELECT
    t.ticket_id,
    t.status,
    t.priority,
    a.username,
    a.full_name,
    a.is_active
FROM ticket AS t
JOIN app_user AS a
    ON t.assigned_to = a.user_id
WHERE t.status <> 'closed'
  AND a.is_active = FALSE
ORDER BY t.ticket_id;

--10	"in_progress"	"medium"	"klee"	"Kevin Lee"	false

--=========Verify dsingh===============
SELECT
    user_id,
    username,
    full_name,
    is_active
FROM app_user
WHERE username = 'dsingh';

8	"dsingh"	"Divya Singh"	true

--======Reassign orphaned ticket===========
UPDATE ticket
SET assigned_to = 8
WHERE ticket_id = 10;

--=====Verification=======
SELECT
    t.ticket_id,
    t.status,
    t.priority,
    a.username,
    a.full_name,
    a.is_active
FROM ticket AS t
JOIN app_user AS a
    ON t.assigned_to = a.user_id
WHERE t.ticket_id = 10;

--10	"in_progress"	"medium"	"dsingh"	"Divya Singh"	true

--5.5

--======Clean up the duplicate=======
--=====Indentify=======
SELECT
    t.ticket_id,
    t.opened_at,
    t.status,
    t.priority,
    e.event_id,
    e.event_type,
    e.event_time,
    e.description,
    h.hostname
FROM ticket AS t
JOIN event AS e
    ON t.event_id = e.event_id
JOIN host AS h
    ON e.host_id = h.host_id
WHERE h.hostname = 'ADMIN-WKS03'
  AND e.event_type = 'failed_login'
ORDER BY t.opened_at ASC;

--16	"2026-02-01 03:15:00"	"closed"	"low"		2	"failed_login"	"2026-02-01 03:16:00"	"Repeated failed RDP logins from external IP"	"ADMIN-WKS03"
--19	"2026-02-01 03:15:00"	"closed"	"low"		1	"failed_login"	"2026-02-01 03:14:00"	"Repeated failed RDP logins from external IP"	"ADMIN-WKS03"
--1		"2026-02-01 03:30:00"	"closed"	"medium"	4	"failed_login"	"2026-02-01 03:22:00"	"Failed login count exceeded threshold"			"ADMIN-WKS03"

--=====Delete Redundant ticket======
DELETE FROM ticket
WHERE ticket_id = (
    SELECT t.ticket_id
    FROM ticket AS t
    JOIN event AS e
        ON t.event_id = e.event_id
    JOIN host AS h
        ON e.host_id = h.host_id
    WHERE h.hostname = 'ADMIN-WKS03'
      AND e.event_type = 'failed_login'
      AND e.description = 'Repeated failed RDP logins from external IP'
    ORDER BY e.event_time ASC, t.ticket_id ASC
    LIMIT 1
);

--======Verification=======
SELECT
    t.ticket_id,
    t.opened_at,
    e.event_id,
    e.event_time,
    e.description,
    h.hostname
FROM ticket AS t
JOIN event AS e
    ON t.event_id = e.event_id
JOIN host AS h
    ON e.host_id = h.host_id
WHERE h.hostname = 'ADMIN-WKS03'
  AND e.event_type = 'failed_login'
ORDER BY e.event_time ASC;

--16	"2026-02-01 03:15:00"	2	"2026-02-01 03:16:00"	"Repeated failed RDP logins from external IP"	"ADMIN-WKS03"
--1		"2026-02-01 03:30:00"	4	"2026-02-01 03:22:00"	"Failed login count exceeded threshold"			"ADMIN-WKS03"

--Part6--Automation: Stop Repeating Yourself--

--6.1

CREATE OR REPLACE PROCEDURE close_ticket_and_event(
    p_ticket_id INT,
    p_close_time TIMESTAMP
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_event_id INT;
BEGIN
    -- Look up the event connected to this ticket
    SELECT event_id
    INTO v_event_id
    FROM ticket
    WHERE ticket_id = p_ticket_id;

    -- Fail clearly if the ticket does not exist
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Ticket ID % does not exist', p_ticket_id;
    END IF;

    -- Close the ticket
    UPDATE ticket
    SET
        status = 'closed',
        closed_at = p_close_time
    WHERE ticket_id = p_ticket_id;

    -- Mark the related event as resolved
    UPDATE event
    SET resolved = TRUE
    WHERE event_id = v_event_id;
END;
$$;

--===Test on phishing ticket from 5.2====
CALL close_ticket_and_event(
    21,
    TIMESTAMP '2026-02-13 15:00:00'
);

--======Verify on both records=======
SELECT
    t.ticket_id,
    t.status,
    t.closed_at,
    e.event_id,
    e.event_type,
    e.resolved
FROM ticket AS t
JOIN event AS e
    ON t.event_id = e.event_id
WHERE t.ticket_id = 21;

--21	"closed"	"2026-02-13 15:00:00"	31	"phishing_detected"	true

--6.2

CREATE OR REPLACE FUNCTION host_severity_summary(p_host_id INT)
RETURNS TABLE (
    severity TEXT,
    event_count BIGINT
)
LANGUAGE sql
AS $$
    SELECT
        e.severity,
        COUNT(*) AS event_count
    FROM event AS e
    WHERE e.host_id = p_host_id
    GROUP BY e.severity
    ORDER BY e.severity;
$$;

--=======Test host 7==========
SELECT *
FROM host_severity_summary(7);

--"low"	    5
--"medium"	1

--======Test for host 2======
SELECT *
FROM host_severity_summary(2);

--"critical" 1
--"low"	     1
--"medium"	 1

--Although host 7 has more events overall, host 2 should be ranked as the higher 
--security concern because it has a critical event, while host 7's six events are only 
--low or medium severity.

--Part7--End-of-Day Executive Briefing--

--7.1

--=====Create executive_risk_summary======
CREATE OR REPLACE VIEW executive_risk_summary AS
SELECT
    h.hostname,
    h.department,
    h.criticality,
    h.decommissioned,

    -- Number of critical-severity events for this host
    (
        SELECT COUNT(*)
        FROM event AS e
        WHERE e.host_id = h.host_id
          AND e.severity = 'critical'
    ) AS critical_event_count,

    -- Number of currently open or escalated tickets
    (
        SELECT COUNT(*)
        FROM ticket AS t
        JOIN event AS e
            ON t.event_id = e.event_id
        WHERE e.host_id = h.host_id
          AND t.status IN ('open', 'escalated')
    ) AS open_escalated_ticket_count

FROM host AS h;

--========Prioritize=========
SELECT *
FROM executive_risk_summary
ORDER BY
    critical_event_count DESC,
    open_escalated_ticket_count DESC,
    CASE criticality
        WHEN 'critical' THEN 4
        WHEN 'high' THEN 3
        WHEN 'medium' THEN 2
        WHEN 'low' THEN 1
    END DESC,
    hostname;

--"LEGACY-FILESRV"	"Administration"	"high"		true	1	2
--"BACKUP-SRV01"	"IT Operations"		"critical"	false	1	1
--"EHR-APP01"	    "Patient Records"	"critical"	false	1	1
--"EHR-DB01"	    "Patient Records"	"critical"	false	1	0
--"RAD-SRV01"		"Radiology"			"high"		false	0	1
--"BILL-SRV01"		"Billing"			"medium"	false	0	1
--"LAB-SRV01"		"Laboratory"		"high"		false	0	0
--"PHARM-SRV01"		"Pharmacy"			"high"		false	0	0
--"HR-SRV01"		"Human Resources"	"medium"	false	0	0
--"WEB-SRV01"		"Patient Portal"	"medium"	false	0	0
--"ADMIN-WKS03"		"Administration"	"low"		false	0	0
--"GUEST-AP02"		"Guest Network"		"low"		false	0	0

--7.2
--=====Executive Risk Briefing=====
--Executive Summary
--Today's security data shows several significant risks requiring continued attention, particularly
--on hosts with critical-severity events and active tickets.  LEGACY-FILESRV is the highest priority
--host because it has a critical malware-detected event and two open or escalated tickets, while also 
--being marked as decommissioned.  BACKUP-SRV01 EHR-APP01 are also high-priority concerns because each 
--has a critical event and an active open or escalated ticket.  EHR-DB01 has a critical event but no 
--currently open or escalated ticket after its incident was resolved today.

--==========Top 3 Findings==============
--1. LEGACY-FILESRV--Highest Risk
--LEGACY-FILESRV in Administration has one critical event and two open or escalated tickets.  
--The critical event is a malware detection, and the host is marked as decommissioned, making the
--continued security activity especially concerning.  The combination of critical severity, active
--tickets, and decommissioned status makes this the highest-priority host in the executive risk summary.

--2. BACKUP-SRV01--Critical Security Risk
--BACKUP-SRV01 in IT Operations has one critical event and one open or escalated ticket.  The critical
--event is an unauthorized-access incident, and the host itself is classified as critical. Because 
--backup infrastructure can be particularly important to business recovery, unauthorized access to this
--system warrants immediate attention.

--3. EHR-APP01--Critical Patient Records Risk
--EHR-APP01 in Patient Records has one critical malware-detected event and one open or escalated
--ticket.  The host is classified as critical, and its role in the Patient Records enviroment increases 
--the potential business and PHI impact of a compromise.  This should remain a high-priority 
--investigation until the incident is fully contained and resolved.

--============Process Gap and Recommendation==========
--A process gap identified today was that open ticket was assigned to an inactive analyst, Kevin Lee.
--Ticket 10 had to be reassigned to active analyst Divya Singh(dsingh).  Going forward, ticket 
--assignment should be validated against the analyst's active status when tickets are created or 
--reassigned, and a recurring report or automated check should identify any non-closed tickets assigned
--to inactive users.

--========Was the Highest-Event-Count Host Actually the Top Risk?=======
--No.
--ADMIN-WKS03 had the highest event count with six events, but the executive risk summary shows zero
--critical events and zero open or escalated tickets for that host.  In contrast, LEGACY-FILESRV has 
--only two events but has one critical event and two open or escalated tickets, making it the higher
--actual risk.  This demonstrates that security risk should be prioritized using severity, active
--response status, and host criticality rather than raw event volume alone.
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

-- clinic_demo for the Riverside Clinic Scheduler.
-- Run this yourself, once, in pgAdmin's Query Tool or psql, connected to clinic_demo:
--     psql -U postgres -d clinic_demo -f schema.sql
-- The app never runs this file. WARNING: it drops and recreates the clinic tables.
--
-- All data is synthetic: generated patient names, no real people.

CREATE SCHEMA IF NOT EXISTS clinic;
SET search_path TO clinic;

DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS clinicians;

CREATE TABLE clinicians (
    clinician_id VARCHAR(8) PRIMARY KEY,
    display_name VARCHAR(64) NOT NULL
);

CREATE TABLE patients (
    patient_id VARCHAR(12) PRIMARY KEY,
    display_name VARCHAR(64) NOT NULL,
    patient_since DATE NOT NULL
);

CREATE TABLE appointments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    patient_id VARCHAR(12) NOT NULL REFERENCES patients(patient_id),
    clinician_id VARCHAR(8) NOT NULL REFERENCES clinicians(clinician_id),
    starts_at TIMESTAMPTZ NOT NULL,
    reason_code VARCHAR(8) NOT NULL,
    status VARCHAR(12) NOT NULL DEFAULT 'BOOKED',
    CONSTRAINT chk_status CHECK (status IN ('BOOKED', 'CHECKED_IN'))
);

INSERT INTO clinicians (clinician_id, display_name)
SELECT 'CLN-0' || g, 'Dr. Clinician 0' || g
FROM generate_series(1, 6) AS g;

-- PAT-00000001 ... PAT-00200000, named "Patient 000001" ... "Patient 200000".
INSERT INTO patients (patient_id, display_name, patient_since)
SELECT 'PAT-' || lpad(g::text, 8, '0'),
       'Patient ' || lpad(g::text, 6, '0'),
       DATE '2015-01-01' + (g % 3650)
FROM generate_series(1, 200000) AS g;

-- 150,000 upcoming appointments, one every 5 minutes from 2026-10-01.
INSERT INTO appointments (patient_id, clinician_id, starts_at, reason_code)
SELECT 'PAT-' || lpad((1 + (g * 7919) % 200000)::text, 8, '0'),
       'CLN-0' || (1 + g % 6),
       TIMESTAMPTZ '2026-10-01 08:00:00+00' + g * INTERVAL '5 minutes',
       (ARRAY['CHECKUP', 'FLU', 'LAB', 'FOLLOWUP', 'VACCINE'])[1 + g % 5]
FROM generate_series(1, 150000) AS g;

CREATE INDEX idx_appointments_patient ON appointments (patient_id, starts_at);

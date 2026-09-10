-- =====================================================================
-- OpsCore schema (PostgreSQL) — shared by lessons 1, 2, 4, and 5
-- =====================================================================
-- Run this once, in a scratch/lab database, before any of the lesson
-- files. It mirrors the five-table T-SQL OpsCore schema used in class:
-- hosts run services; hosts/services raise events; events open tickets;
-- users own tickets.
--
-- PostgreSQL note: "user" is a reserved word, so the table is named
-- app_users (plural, snake_case — unquoted identifiers are lower-cased
-- automatically, so PascalCase would require double-quoting everywhere;
-- snake_case avoids that overhead entirely).
--
-- HOW TO RUN:
--   createdb opscore_lab
--   psql -d opscore_lab -f 00_opscore_schema.sql
-- =====================================================================

DROP TABLE IF EXISTS event_staging, tickets, events, services, hosts, app_users CASCADE;

CREATE TABLE hosts (
    host_id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    hostname     VARCHAR(128) NOT NULL UNIQUE,
    environment  VARCHAR(20)  NOT NULL,
    os           VARCHAR(64),
    status       VARCHAR(20)  NOT NULL DEFAULT 'active'
);

CREATE TABLE services (
    service_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    host_id      INT NOT NULL REFERENCES hosts(host_id),
    name         VARCHAR(100) NOT NULL,
    criticality  VARCHAR(20)  NOT NULL DEFAULT 'standard'
);

CREATE TABLE app_users (
    user_id        INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username       VARCHAR(64)  NOT NULL UNIQUE,
    team           VARCHAR(64),
    role           VARCHAR(20)  NOT NULL DEFAULT 'member'
                     CHECK (role IN ('member', 'lead', 'manager', 'admin')),
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    -- The ops portal added credential columns to app_users. These are SENSITIVE.
    email          VARCHAR(128),
    password_hash  VARCHAR(256)
);

CREATE TABLE events (
    event_id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    host_id      INT REFERENCES hosts(host_id),
    service_id   INT REFERENCES services(service_id),
    severity     VARCHAR(20) NOT NULL
                   CHECK (severity IN ('info', 'warning', 'error', 'critical')),
    message      VARCHAR(400) NOT NULL,
    occurred_at  TIMESTAMP    NOT NULL DEFAULT now()
                   CHECK (occurred_at <= now())
);

CREATE TABLE tickets (
    ticket_id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    host_id        INT REFERENCES hosts(host_id),
    service_id     INT REFERENCES services(service_id),
    kind           VARCHAR(20) NOT NULL DEFAULT 'incident',
    status         VARCHAR(20) NOT NULL DEFAULT 'open',
    priority       VARCHAR(10) NOT NULL DEFAULT 'p3',
    assignee_user_id INT REFERENCES app_users(user_id),
    opened_at      TIMESTAMP NOT NULL DEFAULT now(),
    resolved_at    TIMESTAMP
);

-- A little seed data so the demo procedures/functions return something.
INSERT INTO hosts (hostname, environment, os, status) VALUES
    ('web-prod-01', 'prod', 'Ubuntu 22.04', 'active'),
    ('web-prod-02', 'prod', 'Ubuntu 22.04', 'active'),
    ('db-prod-01',  'prod', 'Ubuntu 22.04', 'active');

INSERT INTO services (host_id, name, criticality) VALUES
    (1, 'nginx',     'high'),
    (3, 'postgresql','high');

INSERT INTO app_users (username, team, role, email, password_hash) VALUES
    ('amal', 'sre',      'lead',   'amal@example.com',  'x'),
    ('hae',  'sre',      'member', 'hae@example.com',   'x'),
    ('jordan','support', 'manager','jordan@example.com','x');

INSERT INTO events (host_id, service_id, severity, message) VALUES
    (1, 1, 'warning', 'nginx worker restarted'),
    (3, 2, 'error',   'connection pool exhausted');

INSERT INTO tickets (host_id, service_id, kind, status, priority, assignee_user_id) VALUES
    (3, 2, 'incident', 'open', 'p2', 1);

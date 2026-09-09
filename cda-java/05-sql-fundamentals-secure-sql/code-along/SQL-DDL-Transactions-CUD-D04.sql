--DROP TABLE IF EXISTS hosts;

CREATE TABLE hosts (
    host_id      INT GENERATED ALWAYS AS IDENTITY,
    hostname     VARCHAR(64)  NOT NULL,
    environment  VARCHAR(20)  NOT NULL DEFAULT 'unknown',
    tier         VARCHAR(20)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),

	
    CONSTRAINT pk_hosts PRIMARY KEY (host_id),
    CONSTRAINT uq_hosts_hostname UNIQUE (hostname),
    CONSTRAINT chk_hosts_environment CHECK
        (environment IN ('production', 'staging', 'development', 'unknown'))
);



DROP TABLE IF EXISTS services;

CREATE TABLE services (
    service_id  INT GENERATED ALWAYS AS IDENTITY,
    name        VARCHAR(64) NOT NULL,
    owner_team  VARCHAR(64) NOT NULL,
    criticality VARCHAR(20) NOT NULL,
    CONSTRAINT pk_services PRIMARY KEY (service_id),
    CONSTRAINT uq_services_name UNIQUE (name),
    CONSTRAINT chk_services_criticality CHECK
        (criticality IN ('low', 'medium', 'high', 'critical'))
);

DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id  INT GENERATED ALWAYS AS IDENTITY,
    username VARCHAR(32) NOT NULL,
    team     VARCHAR(64) NOT NULL,
    role     VARCHAR(32) NOT NULL,
    active   BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_username UNIQUE (username)
);

INSERT INTO services (name, owner_team, criticality) VALUES
    ('billing-api', 'payments', 'critical'),
    ('search-svc',  'platform', 'medium');
INSERT INTO users (username, team, role) VALUES
    ('ada', 'sre', 'engineer'),
    ('grace', 'sre', 'lead');



DROP TABLE IF EXISTS events;

CREATE TABLE events (
    event_id    INT GENERATED ALWAYS AS IDENTITY,
    host_id     INT NOT NULL,
    service_id  INT NOT NULL,
    severity    VARCHAR(20) NOT NULL,
    message     VARCHAR(400) NOT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT pk_events PRIMARY KEY (event_id),
    CONSTRAINT fk_events_hosts FOREIGN KEY (host_id)
        REFERENCES hosts(host_id) ON DELETE NO ACTION,
    CONSTRAINT fk_events_services FOREIGN KEY (service_id)
        REFERENCES services(service_id) ON DELETE NO ACTION,
    CONSTRAINT chk_events_severity CHECK
        (severity IN ('info', 'warning', 'error', 'critical'))
);


DROP TABLE IF EXISTS tickets;

CREATE TABLE tickets (
    ticket_id        INT GENERATED ALWAYS AS IDENTITY,
    host_id          INT NULL,
    service_id       INT NULL,
    kind             VARCHAR(20) NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'open',
    priority         VARCHAR(10) NOT NULL,
    opened_at        TIMESTAMP NOT NULL DEFAULT now(),
    resolved_at      TIMESTAMP NULL,
    assignee_user_id INT NULL,
    CONSTRAINT pk_tickets PRIMARY KEY (ticket_id),
    CONSTRAINT fk_tickets_hosts FOREIGN KEY (host_id)
        REFERENCES hosts(host_id) ON DELETE SET NULL,
    CONSTRAINT fk_tickets_services FOREIGN KEY (service_id)
        REFERENCES services(service_id) ON DELETE NO ACTION,
    CONSTRAINT fk_tickets_assignee FOREIGN KEY (assignee_user_id)
        REFERENCES users(user_id) ON DELETE SET NULL,
    CONSTRAINT chk_tickets_priority CHECK (priority IN ('P1','P2','P3','P4'))
);



SELECT * FROM hosts;

-- Add an Index - a read-speed lever, with a write-side cost
CREATE INDEX idx_hosts_environment ON hosts(environment);


INSERT INTO hosts (hostname, tier)
VALUES ('db-prod04', 'gold');


INSERT INTO hosts (hostname, environment, tier) -- error: check constraint
VALUES ('db-prod04', 'prd', 'gold');

INSERT INTO hosts (hostname, environment, tier) -- error: uq constraint
VALUES ('db-prod04', 'production', 'gold');

INSERT INTO hosts (hostname, environment, tier)
VALUES ('web-dev01', 'development', 'gold'),
	   ('app-dev01', 'development', 'bronze'),
	   ('web-stg01', 'staging', 'silver');

INSERT INTO hosts (hostname, environment, tier)
SELECT 'web-prod01', environment, tier FROM hosts WHERE host_id = 5;


-- Additive ALTER table: add column
ALTER TABLE hosts ADD COLUMN monitored BOOLEAN NOT NULL DEFAULT true;

-- Verify WHERE clause with SELECT 
UPDATE hosts 
SET environment = 'production', 
	tier = 'silver'
WHERE host_id = 9;


DELETE FROM hosts WHERE host_id = 7;




--=========== Transactions --======================================

CREATE TABLE bank_account (
    account_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_holder VARCHAR(100) NOT NULL,
    balance NUMERIC(12, 2) NOT NULL
);

INSERT INTO bank_account
    (account_holder, balance)
VALUES
    ('Tony Stark', 10000.00),
    ('Peter Parker', 500.00);


SELECT * FROM bank_account;

BEGIN TRANSACTION;

UPDATE bank_account
SET balance = balance - 1000
WHERE account_holder = 'Tony Stark'  


UPDATE bank_account
SET balance = balance + 1000
WHERE account_holder = 'Peter Parker'  


DELETE FROM bank_account;



--Something went wrong
ROLLBACK;
--OR
-- All is good
COMMIT;

SELECT * FROM bank_account;




-- SQL Views 
CREATE OR REPLACE VIEW vw_open_p1_tickets_by_service AS
SELECT
    s.name AS service_name,
    COUNT(*) AS open_p1_count
FROM tickets t
JOIN services s ON t.service_id = s.service_id
WHERE t.status = 'open' AND t.priority = 'P1'
GROUP BY s.name;


SELECT * FROM vw_open_p1_tickets_by_service;



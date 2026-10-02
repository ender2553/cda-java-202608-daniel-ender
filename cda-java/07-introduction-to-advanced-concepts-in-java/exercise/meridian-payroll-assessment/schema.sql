-- payroll_demo for Meridian Payroll.
-- Run this yourself, once, in pgAdmin's Query Tool or psql, connected to payroll_demo:
--     psql -U postgres -d payroll_demo -f schema.sql
-- The app never runs this file. WARNING: it drops and recreates the payroll tables.
--
-- All data is synthetic: generated names, 9xx SSNs (an area number that is never issued).

-- pgcrypto produces the stored bank_account and portal_pin values below.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SCHEMA IF NOT EXISTS payroll;
SET search_path TO payroll, public;

DROP TABLE IF EXISTS payroll_audit;
DROP TABLE IF EXISTS pay_stubs;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS funding_accounts;
DROP TABLE IF EXISTS departments;

CREATE TABLE departments (
    dept_code VARCHAR(3) PRIMARY KEY,
    name VARCHAR(64) NOT NULL
);

CREATE TABLE funding_accounts (
    account_id VARCHAR(8) PRIMARY KEY,
    display_name VARCHAR(64) NOT NULL,
    opening_balance NUMERIC(14,2) NOT NULL,
    balance NUMERIC(14,2) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE employees (
    employee_id VARCHAR(9) PRIMARY KEY,
    first_name VARCHAR(40) NOT NULL,
    last_name VARCHAR(40) NOT NULL,
    dept_code VARCHAR(3) NOT NULL REFERENCES departments(dept_code),
    title VARCHAR(64) NOT NULL,
    annual_salary NUMERIC(10,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    ssn VARCHAR(255) NOT NULL,
    bank_account VARCHAR(255) NOT NULL,
    portal_pin VARCHAR(255) NOT NULL,
    hr_notes TEXT
);

CREATE TABLE pay_stubs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    employee_id VARCHAR(9) NOT NULL REFERENCES employees(employee_id),
    pay_date DATE NOT NULL,
    gross NUMERIC(10,2) NOT NULL,
    net NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (employee_id, pay_date)
);
CREATE INDEX idx_pay_stubs_pay_date ON pay_stubs (pay_date);

CREATE TABLE payroll_audit (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event VARCHAR(32) NOT NULL,
    dept_code VARCHAR(3),
    detail VARCHAR(256),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO departments (dept_code, name) VALUES
    ('ENG', 'Engineering'), ('OPS', 'Operations'), ('FIN', 'Finance'), ('HRS', 'Human Resources'),
    ('SAL', 'Sales'), ('MKT', 'Marketing'), ('SUP', 'Customer Support'), ('LEG', 'Legal');

-- EMP-00001 ... EMP-00600, 75 per department, about 10 inactive.
-- Bank accounts and PINs are stored the way the app's BankCrypto stores them.
-- Employee number g has PIN lpad((g * 7919) % 1000000, 6, '0'): EMP-00001 is 007919, EMP-00002 is 015838.
INSERT INTO employees (employee_id, first_name, last_name, dept_code, title, annual_salary, active,
                       ssn, bank_account, portal_pin, hr_notes)
SELECT 'EMP-' || lpad(g::text, 5, '0'),
       (ARRAY['Alex','Blair','Casey','Dana','Eli','Fran','Gale','Harper','Indira','Jules',
              'Kai','Lena','Milo','Nora','Omar','Priya','Quinn','Rosa','Sam','Tara'])[1 + (g * 3 + g / 20) % 20],
       (ARRAY['Smith','Johnson',E'O''Brien','Garcia','Nguyen',E'O''Neil',E'D''Angelo','Patel','Kim','Lopez',
              'Smith-Jones','Brown','Okafor','Silva','Cohen','Larsen','Ivanov','Haddad','Moreau','Tanaka'])[1 + (g * 7) % 20],
       (ARRAY['ENG','OPS','FIN','HRS','SAL','MKT','SUP','LEG'])[1 + g % 8],
       (ARRAY['Analyst','Specialist','Coordinator','Associate','Senior Analyst','Team Lead'])[1 + g % 6],
       40000 + (g * 1373) % 90000 + (g % 100) / 100.0,
       g % 60 <> 0,
       '9' || lpad(((g * 37) % 100)::text, 2, '0') || '-' || lpad(((g * 13) % 100)::text, 2, '0')
           || '-' || lpad(((g * 7919) % 10000)::text, 4, '0'),
       encode(encrypt(convert_to(substr((((g::bigint * 982451653) % 900000000000) + 100000000000)::text, 1, 10 + g % 3), 'UTF8'),
                      convert_to('MeridianPayroll1', 'UTF8'), 'aes-ecb'), 'base64'),
       encode(encrypt(convert_to(lpad(((g * 7919) % 1000000)::text, 6, '0'), 'UTF8'),
                      convert_to('MeridianPayroll1', 'UTF8'), 'aes-ecb'), 'base64'),
       (ARRAY['Performance plan on file', 'Wage garnishment order on file', NULL, NULL,
              'Medical leave through Q4', NULL, 'Relocation approved', NULL])[1 + g % 8]
FROM generate_series(1, 600) AS g;

-- 260 biweekly pay periods per employee, newest 2026-09-25. The next payday, 2026-10-09, has no stubs yet.
INSERT INTO pay_stubs (employee_id, pay_date, gross, net)
SELECT e.employee_id,
       DATE '2026-09-25' - 14 * k,
       round(e.annual_salary / 26, 2),
       round(round(e.annual_salary / 26, 2) * 0.75, 2)
FROM employees e CROSS JOIN generate_series(0, 259) AS k;

-- The company funding account. opening_balance is set after the stubs exist so that
-- opening - current = sum of stub nets holds from the start.
INSERT INTO funding_accounts (account_id, display_name, opening_balance, balance)
VALUES ('FND-0001', 'Meridian operating payroll', 25000000.00, 25000000.00);

UPDATE funding_accounts
SET opening_balance = balance + (SELECT SUM(net) FROM pay_stubs);

ANALYZE;

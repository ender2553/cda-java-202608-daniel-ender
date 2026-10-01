-- ledger_demo for Lesson 4 (Advanced Java for Performance, Securely).
-- Run this yourself, once, in pgAdmin's Query Tool or psql, connected to ledger_demo:
--     psql -U postgres -d ledger_demo -f schema.sql
-- The app never runs this file. WARNING: it drops and recreates the ledgercore tables.
--
-- It seeds 1,000,000 past transactions, so a request for "all of them" really
-- is too big to hold in memory. Expect it to take 10-30 seconds.

CREATE SCHEMA IF NOT EXISTS ledgercore;
SET search_path TO ledgercore;

DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS account_holders;
DROP TABLE IF EXISTS accounts;

CREATE TABLE accounts (
    account_id VARCHAR(32) PRIMARY KEY,
    balance NUMERIC(19,2) NOT NULL,
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0),
    CONSTRAINT chk_balance_ceiling CHECK (balance <= 100000000.00)
);

-- Not used in this lesson. Kept so the Lesson 3 app still runs against this database.
CREATE TABLE account_holders (
    account_id VARCHAR(32) PRIMARY KEY REFERENCES accounts(account_id),
    display_name VARCHAR(128) NOT NULL,
    tax_id_encrypted VARCHAR(512) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE transactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    from_account VARCHAR(32) NOT NULL,
    to_account VARCHAR(32) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    memo VARCHAR(256),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO accounts (account_id, balance) VALUES
    ('ACC-00000001', 500.00),
    ('ACC-00000002', 100.00),
    ('ACC-00000003', 250.00),
    ('ACC-00000004', 1000.00),
    ('ACC-00000005', 75.00);

-- 1,000,000 transactions of history, one per minute, between different accounts.
INSERT INTO transactions (from_account, to_account, amount, memo, created_at)
SELECT 'ACC-0000000' || (1 + g % 5),
       'ACC-0000000' || (1 + (g % 5 + 1 + (g / 5) % 4) % 5),
       round((1 + random() * 999)::numeric, 2),
       (ARRAY['Rent', 'Groceries', 'Invoice', 'Refund', 'Transfer'])[1 + g % 5],
       TIMESTAMPTZ '2024-01-01 00:00:00+00' + g * INTERVAL '1 minute'
FROM generate_series(1, 1000000) AS g;

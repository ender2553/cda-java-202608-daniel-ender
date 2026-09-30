-- ledger_demo, as it stands after Lesson 2 (Data at Rest — Encryption Basics).
-- Run this yourself, once, in pgAdmin's Query Tool or psql, connected to ledger_demo:
--     psql -U postgres -d ledger_demo -f schema.sql
-- The app never runs this file. WARNING: it drops and recreates the ledgercore tables.
--
-- No account holders are seeded: a tax ID must go through FieldCipher, never plain SQL.
-- Add holders from the app's menu (option 4).

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

CREATE TABLE account_holders (
    account_id VARCHAR(32) PRIMARY KEY REFERENCES accounts(account_id),
    display_name VARCHAR(128) NOT NULL,
    tax_id_encrypted VARCHAR(512) NOT NULL,   -- Base64( IV + AES-GCM ciphertext + tag )
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

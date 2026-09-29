-- LedgerCore schema (PostgreSQL). Runs on every startup unless DB_INIT_MODE=never.
-- Tables are created in DB_SCHEMA (default: ledgercore) via the connection's search_path,
-- so nothing here touches other tables in the same database.
-- Drops first so every run starts from the same clean state.

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

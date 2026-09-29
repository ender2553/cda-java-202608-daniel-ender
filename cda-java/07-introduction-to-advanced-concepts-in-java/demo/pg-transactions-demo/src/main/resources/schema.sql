-- Rebuilt from scratch at the start of every demo scenario so each run starts
-- from the same known state: two accounts holding $600.00 in total.

DROP TABLE IF EXISTS ledger_entries, transfers, accounts CASCADE;

-- Table 1: current balances. The CHECK constraint is Postgres enforcing a
-- business rule for us: no account may go negative.
CREATE TABLE accounts (
    account_id VARCHAR(16)   PRIMARY KEY,
    owner      VARCHAR(64)   NOT NULL,
    balance    NUMERIC(19,2) NOT NULL CONSTRAINT chk_balance_non_negative CHECK (balance >= 0)
);

-- Table 2: one header row per transfer.
CREATE TABLE transfers (
    transfer_id  BIGSERIAL     PRIMARY KEY,
    from_account VARCHAR(16)   NOT NULL REFERENCES accounts (account_id),
    to_account   VARCHAR(16)   NOT NULL REFERENCES accounts (account_id),
    amount       NUMERIC(19,2) NOT NULL CHECK (amount > 0),
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- Table 3: double-entry lines. Every transfer must have exactly two entries
-- (a debit and a credit) that sum to zero.
CREATE TABLE ledger_entries (
    entry_id    BIGSERIAL     PRIMARY KEY,
    transfer_id BIGINT        NOT NULL REFERENCES transfers (transfer_id),
    account_id  VARCHAR(16)   NOT NULL REFERENCES accounts (account_id),
    amount      NUMERIC(19,2) NOT NULL  -- negative = debit, positive = credit
);

INSERT INTO accounts (account_id, owner, balance) VALUES
    ('ACC-1', 'Jordan', 500.00),
    ('ACC-2', 'Sam',    100.00);

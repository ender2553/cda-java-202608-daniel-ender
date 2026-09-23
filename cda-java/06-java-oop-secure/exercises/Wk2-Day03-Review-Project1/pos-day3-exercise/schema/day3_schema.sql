-- QuickPay POS -- Day 3 additive schema.
--
-- Kept in its OWN file, separate from Day 2's schema/schema.sql, for two reasons:
--   1. Day 2's schema.sql is given/unmodified infrastructure from an earlier day, don't
--      touch"
--   2. Apply Day 3's schema independently,
--      and re-apply it (DROP/CREATE during iteration) without touching Day 2's
--      merchant/transaction tables or their data.
--
-- Setup (run schema/day3_schema.sql against the same quickpay_pos database):

-- salt and pin_hash are BYTEA (raw bytes), never TEXT/VARCHAR -- a PBKDF2 salt/hash is
-- binary data, not a printable string, and storing it as BYTEA avoids any encoding-related
-- corruption a text column could introduce.
CREATE TABLE IF NOT EXISTS cashier_account (
    cashier_id  VARCHAR(50) PRIMARY KEY,
    salt        BYTEA NOT NULL,
    pin_hash    BYTEA NOT NULL
);

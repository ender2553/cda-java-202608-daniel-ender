-- QuickPay POS -- PostgreSQL schema
--
-- Setup:
--   createdb quickpay_pos
--   psql quickpay_pos -f schema/schema.sql
--
-- Then point the app/tests at it:
--   export POS_DB_URL=jdbc:postgresql://localhost:5432/quickpay_pos
--   export POS_DB_USER=<your user>
--   export POS_DB_PASSWORD=<your password>

CREATE TABLE IF NOT EXISTS merchant (
    merchant_id   VARCHAR(50) PRIMARY KEY,
    display_name  VARCHAR(200) NOT NULL
);

-- merchant_id is intentionally NOT a foreign key to merchant(merchant_id): a transaction
-- can be recorded for a merchantId string without a corresponding merchant row ever having
-- been registered (Main.java's demo flow never calls MerchantRepository.register(...)
-- before checking out), so a FK constraint here would reject perfectly normal application
-- traffic. merchant_id is still required (NOT NULL) -- it just isn't enforced against the
-- merchant table.
CREATE TABLE IF NOT EXISTS transaction (
    transaction_id  VARCHAR(64) PRIMARY KEY,
    merchant_id     VARCHAR(50) NOT NULL,
    amount          NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    memo            TEXT,
    occurred_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

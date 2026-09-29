-- LedgerCore seed data. Runs after schema.sql on every startup unless DB_INIT_MODE=never.
-- No account holders are seeded: their tax ID must go through FieldCipher, never plain SQL.

INSERT INTO accounts (account_id, balance) VALUES ('ACC-00000001', 500.00);
INSERT INTO accounts (account_id, balance) VALUES ('ACC-00000002', 100.00);

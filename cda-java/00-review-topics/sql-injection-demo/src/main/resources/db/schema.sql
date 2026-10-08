-- =====================================================================
--  sqli_demo  ·  schema "app"  ·  SAFE SANDBOX — fake data only
-- =====================================================================
--  Run this against the sqli_demo database AFTER creating it:
--      CREATE DATABASE sqli_demo;   (see README.md, setup step 1)
--
--  This database is DELIBERATELY insecure, to be attacked in class:
--    * no primary keys, no foreign keys, no unique/not-null constraints
--    * no encryption anywhere — passwords are stored in clear text
--  That is the point. None of this data is real. Nothing here comes from
--  ledger_demo or any other database.
--
--  Re-running this file RESETS the sandbox to a known state — do that after
--  any attack that changes or destroys data (the "vandalise" demos).
-- =====================================================================

DROP SCHEMA IF EXISTS app CASCADE;
CREATE SCHEMA app;
SET search_path TO app;

-- ---------------------------------------------------------------------
-- Application role: sqli_owner.
--   The app connects as this role. It OWNS the schema and tables below, so
--   it can UPDATE / DELETE / DROP them -- which is what makes the "vandalise"
--   demos work. But it is deliberately NOT a superuser, so a stacked payload
--   CANNOT run OS commands (COPY ... TO PROGRAM), read server files
--   (pg_read_file), or read the real Postgres password hashes (pg_authid).
--   Over-privileged enough to teach the lesson; not enough to harm the host.
-- ---------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'sqli_owner') THEN
        CREATE ROLE sqli_owner LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE
            PASSWORD 'sqli_owner_pw';
    END IF;
END
$$;

-- ---------------------------------------------------------------------
-- users: the sensitive table. Clear-text passwords, no constraints.
--   The search box will read this table (UNION) and later vandalise it.
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id       INTEGER,        -- no PRIMARY KEY on purpose
    username TEXT,
    password TEXT,           -- clear text, no hashing/encryption on purpose
    role     TEXT,
    email    TEXT
);

INSERT INTO users (id, username, password, role, email) VALUES
    (1, 'alice', 'sunshine',     'admin', 'alice@example.test'),
    (2, 'bob',   'hunter2',      'staff', 'bob@example.test'),
    (3, 'carol', 'letmein',      'staff', 'carol@example.test'),
    (4, 'admin', 'c0rrecth0rse', 'admin', 'admin@example.test');

-- ---------------------------------------------------------------------
-- products: the public catalog behind the search box. No constraints.
-- ---------------------------------------------------------------------
CREATE TABLE products (
    id       INTEGER,        -- no PRIMARY KEY on purpose
    name     TEXT,
    category TEXT,
    price    NUMERIC(10,2)
);

INSERT INTO products (id, name, category, price) VALUES
    (1, 'Blue Widget',   'Widgets', 9.99),
    (2, 'Red Widget',    'Widgets', 12.50),
    (3, 'Green Gadget',  'Gadgets', 24.00),
    (4, 'Yellow Gadget', 'Gadgets', 19.95),
    (5, 'Steel Gizmo',   'Gizmos',  49.99);

-- ---------------------------------------------------------------------
-- Hand the schema and tables to sqli_owner, so the app (which connects as
-- that role) can read, write, and DROP them -- but still can't touch the
-- rest of the server. Setup runs as a superuser; the APP never does.
-- ---------------------------------------------------------------------
ALTER SCHEMA app OWNER TO sqli_owner;
ALTER TABLE app.users    OWNER TO sqli_owner;
ALTER TABLE app.products OWNER TO sqli_owner;

-- =====================================================================
-- OpsCore Secure Data Access Exercise — STUDENT STARTER FILE
-- Covers: Lesson 1 (SQL Injection), Lesson 2 (Parameterized Queries),
--         
-- =====================================================================
-- SETUP — run these two files IN ORDER before starting any part:
--   1. 00_opscore_schema.sql   (the shared OpsCore schema — provided
--      separately; creates hosts, services, app_users, events, tickets)
--   2. THIS FILE               (adds a "summary" column + seed rows to
--      tickets, and installs the two insecure starter artifacts you
--      will audit and exploit in Part 1)
--
-- Do all your work for Parts 2 and 3 in the TODO blocks below. Do NOT
-- modify the two INSECURE artifacts themselves — you are auditing and
-- exploiting them as-is, then writing SEPARATE, secured replacements.
-- =====================================================================

-- A small scratch table used only for the Part 1 discussion — you will
-- not be asked to damage your own working data.
DROP TABLE IF EXISTS scratch_log;
CREATE TABLE scratch_log (id INT);

-- ---------------------------------------------------------------------
-- Give tickets a searchable free-text field for this exercise.
-- ---------------------------------------------------------------------
ALTER TABLE tickets ADD COLUMN IF NOT EXISTS summary VARCHAR(200);

UPDATE tickets SET summary = 'Database connection pool exhausted, users cannot log in'
    WHERE ticket_id = 1;

INSERT INTO tickets (host_id, service_id, kind, status, priority, assignee_user_id, summary) VALUES
    (1, 1, 'incident', 'open',     'p3', 2, 'Nginx worker restarting intermittently'),
    (2, NULL, 'request', 'open',   'p4', 3, 'New laptop request for onboarding'),
    (NULL, NULL, 'incident', 'resolved', 'p1', 1, 'VPN outage affecting remote team');


-- =====================================================================
-- PART 1 TARGETS (provided — INSECURE ON PURPOSE, for auditing only)
-- =====================================================================

-- Insecure artifact A — ticket lookup by status. Value slot, WHERE clause.
CREATE OR REPLACE FUNCTION usp_lookup_tickets_by_status_INSECURE(p_status TEXT)
RETURNS SETOF tickets
LANGUAGE plpgsql
AS $$
DECLARE
    v_sql TEXT;
BEGIN
    v_sql := 'SELECT * FROM tickets WHERE status = ''' || p_status || '''';
    RAISE NOTICE 'Assembled statement: %', v_sql;
    RETURN QUERY EXECUTE v_sql;
END;
$$;

-- Insecure artifact B — ticket search. Value slot (LIKE) AND identifier
-- slot (ORDER BY) both concatenated.
CREATE OR REPLACE FUNCTION usp_search_tickets_INSECURE(p_keyword TEXT, p_sort_col TEXT)
RETURNS TABLE(ticket_id INT, kind TEXT, status TEXT, priority TEXT, summary TEXT, opened_at TEXT)
LANGUAGE plpgsql
AS $$
DECLARE
    v_sql TEXT;
BEGIN
    v_sql := 'SELECT ticket_id, kind::text, status::text, priority::text, summary::text, opened_at::text '
          || 'FROM tickets WHERE summary ILIKE ''%' || p_keyword || '%'' '
          || 'ORDER BY ' || p_sort_col;
    RAISE NOTICE 'Assembled statement: %', v_sql;
    RETURN QUERY EXECUTE v_sql;
END;
$$;

--SQL code below until TODOs is for opscore word assignment--

SELECT * 
FROM usp_lookup_tickets_by_status_INSECURE(''' OR ''1''=''1');

SELECT * FROM tickets WHERE status = '' OR '1'='1'

SELECT *
FROM usp_search_tickets_INSECURE(
    '%'' UNION SELECT NULL, username, email, NULL, NULL, NULL FROM app_users --',
    'ticket_id'
);

SELECT ticket_id, kind::text, status::text, priority::text, summary::text, opened_at::text
FROM tickets
WHERE summary ILIKE '%%'
UNION SELECT NULL, username, email, NULL, NULL, NULL
FROM app_users
--%' ORDER BY ticket_id

SELECT *
FROM usp_lookup_tickets_by_status(''' OR ''1''=''1');

SELECT *
FROM usp_search_tickets(
    '%'' UNION SELECT NULL, username, email, NULL, NULL, NULL FROM app_users --',
    'ticket_id'
);
-- =====================================================================
-- PART 2 — YOUR WORK: parameterized replacements (Lesson 2)
-- =====================================================================

-- TODO 2.1 — usp_lookup_tickets_by_status(p_status TEXT)
--   A static, parameterized replacement for artifact A. No dynamic SQL
--   is needed at all for this one.
--
-- CREATE OR REPLACE FUNCTION usp_lookup_tickets_by_status(p_status TEXT)
-- RETURNS SETOF tickets
-- LANGUAGE sql
-- AS $$
--     -- your query here
-- $$;

CREATE OR REPLACE FUNCTION usp_lookup_tickets_by_status(p_status TEXT)
RETURNS SETOF tickets
LANGUAGE sql
AS $$
    SELECT *
    FROM tickets
    WHERE status = p_status;
$$;

-- TODO 2.2 — usp_search_tickets(p_keyword TEXT, p_sort_key TEXT)
--   Bind the keyword as a value. Allow-list p_sort_key to one of
--   'opened_at', 'priority', 'status' (default 'opened_at' for
--   anything else), then use format(%I) to compose the identifier.
--
-- CREATE OR REPLACE FUNCTION usp_search_tickets(p_keyword TEXT, p_sort_key TEXT)
-- RETURNS TABLE(ticket_id INT, kind TEXT, status TEXT, priority TEXT, summary TEXT, opened_at TEXT)
-- LANGUAGE plpgsql
-- AS $$
-- DECLARE
--     v_sort_col TEXT;
-- BEGIN
--     -- your allow-list CASE here
--     -- your format(...) + RETURN QUERY EXECUTE here
-- END;
-- $$;

CREATE OR REPLACE FUNCTION usp_search_tickets(p_keyword TEXT, p_sort_key TEXT)
RETURNS TABLE(
    ticket_id INT,
    kind TEXT,
    status TEXT,
    priority TEXT,
    summary TEXT,
    opened_at TEXT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_sort_col TEXT;
BEGIN
    v_sort_col := CASE p_sort_key
        WHEN 'opened_at' THEN 'opened_at'
        WHEN 'priority'  THEN 'priority'
        WHEN 'status'    THEN 'status'
        ELSE 'opened_at'
    END;

    RETURN QUERY EXECUTE format(
        'SELECT ticket_id, kind::text, status::text, priority::text,
                summary::text, opened_at::text
         FROM tickets
         WHERE summary ILIKE $1
         ORDER BY %I',
        v_sort_col
    )
    USING '%' || p_keyword || '%';
END;
$$;

-- TODO 2.3 — tickets_by_priority(p_priorities TEXT[])
--   Accept an array of priorities and return matching tickets using
--   = ANY(...). No string concatenation, no dynamic SQL.
--
-- CREATE OR REPLACE FUNCTION tickets_by_priority(p_priorities TEXT[])
-- RETURNS SETOF tickets
-- LANGUAGE sql
-- AS $$
--     -- your query here
-- $$;

CREATE OR REPLACE FUNCTION tickets_by_priority(p_priorities TEXT[])
RETURNS SETOF tickets
LANGUAGE sql
AS $$
    SELECT *
    FROM tickets
    WHERE priority = ANY(p_priorities);
$$;

-- =====================================================================
-- PART 3 — YOUR WORK: least privilege + a scoped view (Lesson 4)
-- =====================================================================

-- TODO 3.1 — vw_helpdesk_tickets
--   Expose only: ticket_id, kind, status, priority, opened_at,
--   resolved_at. Omit assignee_user_id. Only rows where kind = 'incident'.
--
-- CREATE OR REPLACE VIEW vw_helpdesk_tickets AS
--     -- your query here

CREATE OR REPLACE VIEW vw_helpdesk_tickets AS
SELECT
    ticket_id,
    kind,
    status,
    priority,
    opened_at,
    resolved_at
FROM tickets
WHERE kind = 'incident';

-- TODO 3.2 — a NOLOGIN role with SELECT on the view only
--
-- CREATE ROLE helpdesk_reader NOLOGIN;
-- GRANT SELECT ON vw_helpdesk_tickets TO helpdesk_reader;


-- TODO 3.3 — a LOGIN role for the application, added to the reader role
--
-- CREATE ROLE helpdesk_app WITH LOGIN PASSWORD 'change-me';
-- GRANT helpdesk_reader TO helpdesk_app;


-- TODO 3.4 — prove it. Run each of these, capture the output for your
-- answer sheet, then RESET ROLE before moving on.
--
-- SET ROLE helpdesk_app;
-- SELECT * FROM vw_helpdesk_tickets;   -- should succeed
-- SELECT * FROM tickets;               -- should be denied
-- DELETE FROM tickets;                 -- should be denied
-- RESET ROLE;

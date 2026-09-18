SELECT * FROM host

SELECT * FROM event

GRANT USAGE, CREATE ON SCHEMA public TO secops_app;

GRANT CONNECT ON DATABASE secops_event_exercise_test TO secops_app;

ALTER TABLE event OWNER TO secops_app;

ALTER TABLE host OWNER TO secops_app;

SELECT
    tablename,
    tableowner
FROM pg_tables
WHERE schemaname = 'public'
  AND tablename IN ('event', 'host');

SELECT
    current_database(),
    current_user,
    session_user;

GRANT USAGE, CREATE ON SCHEMA public TO secops_app;

ALTER TABLE event OWNER TO secops_app;
ALTER TABLE host OWNER TO secops_app;
  
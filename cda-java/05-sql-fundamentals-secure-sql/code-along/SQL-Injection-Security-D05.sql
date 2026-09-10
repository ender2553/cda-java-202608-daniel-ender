SELECT 'Secure SQL'

CREATE OR REPLACE FUNCTION demo_find_host_by_name_INSECURE(p_name TEXT)
RETURNS TABLE(host_id INT, hostname VARCHAR, environment VARCHAR, status VARCHAR)
LANGUAGE plpgsql
AS $$
DECLARE
    v_sql TEXT;
BEGIN
    v_sql := 'SELECT host_id, hostname, environment, status '
          || 'FROM hosts WHERE hostname = '''
          || p_name || '''';
    RAISE NOTICE 'Assembled statement: %', v_sql;   -- read this first, always
    RETURN QUERY EXECUTE v_sql;
END;
$$;

-- SELECT .... WHERE hostname = '' OR '1'='1';

SELECT * FROM demo_find_host_by_name_INSECURE('web-prod-01');

SELECT * FROM demo_find_host_by_name_INSECURE(''' OR ''1''=''1');




-- Union based exfiltration - Concatenated LIKE and a concatenated ORDER BY
CREATE OR REPLACE FUNCTION demo_search_events_INSECURE(p_term TEXT, p_sort_col TEXT)
RETURNS TABLE(event_id INT, host_id INT, service_id INT, severity TEXT, message TEXT, occurred_at TEXT)
LANGUAGE plpgsql
AS $$
DECLARE
    v_sql TEXT;
BEGIN
    v_sql := 'SELECT event_id, host_id, service_id, severity::text, message::text, occurred_at::text '
          || 'FROM events WHERE message LIKE ''%' || p_term || '%'' '
          || 'ORDER BY ' || p_sort_col;
    RAISE NOTICE 'Assembled statement: %', v_sql;
    RETURN QUERY EXECUTE v_sql;
END;
$$;


--UNION-based read
SELECT * FROM demo_search_events_INSECURE('nginx', 'occurred_at');
SELECT * FROM demo_search_events_INSECURE(''' UNION SELECT NULL, NULL, NULL, username, email, NULL FROM app_users --', 'occured_at');



-- Procedure code
CREATE OR REPLACE PROCEDURE demo_delete_events_by_severity_INSECURE(p_filter TEXT)
LANGUAGE plpgsql
AS $$
DECLARE
    v_sql TEXT;
BEGIN
    v_sql := 'DELETE FROM events WHERE severity = ''' || p_filter || '''';
    RAISE NOTICE 'Assembled statement: %', v_sql;
    EXECUTE v_sql;
END;
$$;


select * from events;
select * from event_staging;
CALL demo_delete_events_by_severity_INSECURE('info');
CALL demo_delete_events_by_severity_INSECURE('nope''; DROP TABLE event_staging; --');






--=================================================================
--  SECURE: Parameterized / Prepared Statements
--=================================================================
-- SECURED - host lookup.
CREATE OR REPLACE FUNCTION find_host_by_name(p_name TEXT)
RETURNS TABLE(host_id INT, hostname VARCHAR, environment VARCHAR, status VARCHAR)
LANGUAGE sql
AS $$
    SELECT host_id, hostname, environment, status
    FROM hosts
    WHERE hostname = p_name;   -- p_name is bound, never parsed
$$;

SELECT * FROM find_host_by_name('web-prod-01');

SELECT * FROM find_host_by_name(''' OR ''1''=''1');


-- SECURE: Search Events - Allow list
CREATE OR REPLACE FUNCTION search_events(p_term TEXT, p_sort_key TEXT)
RETURNS SETOF events
LANGUAGE plpgsql
AS $$
BEGIN
    IF p_sort_key = 'severity' THEN
        RETURN QUERY
            SELECT * FROM events
            WHERE message LIKE '%' || p_term || '%'
            ORDER BY severity;
    ELSIF p_sort_key = 'event_id' THEN
        RETURN QUERY
            SELECT * FROM events
            WHERE message LIKE '%' || p_term || '%'
            ORDER BY event_id;
    ELSE
        RETURN QUERY
            SELECT * FROM events
            WHERE message LIKE '%' || p_term || '%'
            ORDER BY occurred_at;
    END IF;
END;
$$;



-- Procedure code

-- SECURE
CREATE OR REPLACE PROCEDURE delete_events_by_severity(p_filter TEXT)
LANGUAGE plpgsql
AS $$
BEGIN
	DELETE FROM events WHERE severity = p_filter;
	-- p_filter is bound as literal parameter value. No need for string concatenation.
END;
$$;

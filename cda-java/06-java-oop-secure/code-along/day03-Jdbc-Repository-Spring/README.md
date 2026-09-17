# Day 3 Demo — Ticket Repository (JDBC → JdbcTemplate, PostgreSQL)

Connecting to PostgreSQL, the
Repository pattern, and Spring's JdbcTemplate.

## Setup (do this before running application)

1. Create two local PostgreSQL databases: `secops` (for `Demo.main`) and
   `secops_test` (for the test suite, so tests never touch demo data).
2. Create an app role (if not already exists) and grant it access:
   ```sql
   create role secops_app with login password 'change-me';
   grant all privileges on database secops to secops_app;
   grant all privileges on database secops_test to secops_app;
   ```
3. Add the password to Environment variables so it is never hard-coded (see the JDBC lesson's
   warning about credentials in source):
   ```bash
   SECOPS_DB_PASSWORD=change-me
   ```
4. Run `src/main/resources/schema.sql` then `data.sql` against `secops`
   with `psql` or a client of your choice. The test suite re-runs both
   scripts automatically against `secops_test` before every test.





# Exercise — ServiceRepository (Graded)

## Scenario

The morning's demo built a `TicketRepository` with both raw JDBC and
Spring's JdbcTemplate. This afternoon you'll build a second repository
on your own, following the same pattern: **`ServiceRepository`**,
backed by `ServiceJdbcTemplateRepository`.

A `Service` represents a network service running on a monitored host
(e.g. `nginx` on host 1, port 443). You are implementing the CRUD
operations a SecOps dashboard would use to list, add, update, and
remove services as they're discovered on the network.

## What's already done for you

- `Service.java` and `ServiceStatus.java` — the domain model, complete.
- `ServiceRepository.java` — the interface you must implement. **Do
  not change this file.**
- `config/AppConfig.java` — Spring wiring for `DataSource` and
  `JdbcTemplate`. **Do not change this file.**
- `resources/schema.sql` and `resources/data.sql` — the table
  definition and seed data your tests reset before every run.
- `ServiceJdbcTemplateRepositoryTest.java` — a complete test suite.
  **Do not change this file.** It is your primary feedback loop —
  run it after every method you finish.

## What you're implementing

Everything in `ServiceJdbcTemplateRepository.java`. Each method has a
`// TODO [REPOx]` comment tagging which rubric line it satisfies.
Follow the same techniques used for `TicketJdbcTemplateRepository` in
this morning's demo:

- Use `jdbcTemplate.query(...)` for `findAll` and `findByHostId`.
- Use `jdbcTemplate.queryForObject(...)` for `findById`, and handle
  the case where no row matches.
- Use PostgreSQL's `returning` clause for `add`, and return a **new**
  `Service` object with the generated id — `serviceId` is `final`, so
  you cannot mutate the one you were given.
- Use `jdbcTemplate.update(...)` for `update` and `deleteById`, and
  return `true` only when a row was actually affected.
- Implement the `mapRow` method used by the shared `RowMapper`.

## Setup

1. Create a `secops_exercise` and a `secops_exercise_test` PostgreSQL
   database, and a `secops_app` role with access to both (same steps
   as this morning's demo README).
2. `export SECOPS_DB_PASSWORD=your-password`
3. `Run test` — expect every test to fail with
   `UnsupportedOperationException: TODO` until you implement the
   matching method.



A method that passes its test but does so by holding onto a
`Connection`/`Statement` manually instead of using `JdbcTemplate` does
not receive credit for that method's row in the table above, even if
the test passes.



# Exercise — Security Event Log (Graded)

## Ticket: Security Event Recording

**Reporter:** SecOps Console team
**Component:** Persistence layer
**Priority:** High

### Context

The console's analysts need a durable log of security events
detected across the network -- failed logins, malware signatures,
port scans, unexpected config changes, and so on. This ticket covers
the domain object for a single event and the repository that
persists it against PostgreSQL.

This exercise is entirely from scratch: no model, no interface, and
no implementation are given. You will design and build all of it,
the same way you did with `Host` in this morning's code-along --
except this time on your own, and the criteria below ask for more.

### Acceptance Criteria

**Recording an event**
- Given a host id, an event type, a severity, and a description, when
  a new event is recorded, it is assigned an id by the database and
  can be retrieved afterward with all of its data intact.
- Given no event type, or no severity, recording must fail
  immediately. Unlike a host's risk classification, there is no safe
  guess for what kind of event was detected or how severe it is --
  a missing classification means the event must be rejected, not
  recorded with a made-up default.
- An event's type and severity may only ever be one of a small, fixed
  set of known values each -- never an arbitrary string.
- A blank or missing description must be rejected. A description
  longer than 500 characters must be rejected.
- The moment an event was detected is fixed at the instant it's
  recorded and must never change afterward -- not through any method
  on the object, and not through any reference to a value someone
  else happens to be holding.

**Events don't quietly change after the fact**
- Once an event exists, none of what was originally detected --
  which host, what kind of event, its severity, its description, or
  when it happened -- can be altered. There is no operation, direct
  or indirect, that edits any of these fields on an existing event.
- An event's id is assigned exactly once, the first time it's saved,
  and nothing can change it after that.

**Acknowledging an event**
- An analyst can acknowledge an event to mark it as handled. The
  moment of acknowledgment is recorded permanently at the time it
  happens.
- An event that has already been acknowledged cannot be acknowledged
  again -- attempting to do so must be clearly rejected, not silently
  accepted, and must not overwrite the original acknowledgment time.
- This rule must hold in two places that don't trust each other: in
  the object itself (so business code can enforce it without ever
  touching a database), and in however the repository persists an
  acknowledgment (so a second, concurrent acknowledgment attempt
  against the database can't slip through even if the in-memory rule
  were somehow bypassed).

**Reading events**
- The system can list every event.
- The system can list every event for a given host.
- The system can list every event that has not yet been acknowledged
  -- this is the primary "what needs attention right now" view for
  an analyst, and it should read as its own operation, not as a
  general filter bolted onto `findAll`.
- The system can look up a single event by id.

**Removing events**
- The system can permanently remove an event by id. Removing an
  event that doesn't exist must be reported back to the caller, not
  silently ignored.

### Out of Scope For This Ticket

- Editing an event's description, type, severity, or host after it's
  recorded (intentionally unsupported -- see above).
- Any kind of "undo acknowledgment" operation.

### A Note on Design

You did this once already this morning with `Host`. Ask the same
question for every field on this new object: is there ever a
legitimate reason for it to change after the object is created? For
most fields here, the answer is no -- including, this time, whether
the event has been acknowledged. Think about what that implies for
how "acknowledging" an event has to work if nothing about the object
can be mutated in place.

Also think about where each rule in this ticket is enforced. Some
rules only make sense to check once, at the database, because they
depend on what else already exists there (can two people acknowledge
the same event at the same moment?). Others should never depend on a
database being present at all (should a blank description ever be
allowed to exist as an object, even for a moment, regardless of
whether it's ever saved?). Design accordingly -- and notice that
`SecurityEventTest.java`, one of the two given test files below,
never touches a database. That's not an accident; it's a test of
whether you put the second kind of rule in the right place.

## What's already given

- `config/AppConfig.java` — Spring wiring for `DataSource` and
  `JdbcTemplate`. **Do not change this file.**
- `resources/schema.sql` and `resources/data.sql` — the table
  definitions and seed data your tests reset before every run.
- `src/test/java/learn/secops/models/SecurityEventTest.java` — unit
  tests for your domain object alone, no database required. **Do not
  change this file.**
- `src/test/java/learn/secops/data/SecurityEventRepositoryTest.java`
  — integration tests for your repository. **Do not change this
  file.**
- `Application.java` — a working end-to-end demo, with its body
  commented out. Once your classes compile, uncomment it and run it
  as a second, independent check that your code actually works (the
  given tests are necessary but not sufficient evidence -- a program
  that never runs isn't done).

Read both given test files closely before writing any code. Between
the acceptance criteria above and what the tests actually call,
you have everything you need to determine the exact required shape
of every class and method.

## What you're building

```
src/main/java/learn/secops/models/EventType.java
src/main/java/learn/secops/models/Severity.java
src/main/java/learn/secops/models/SecurityEvent.java
src/main/java/learn/secops/data/SecurityEventRepository.java
src/main/java/learn/secops/data/SecurityEventJdbcTemplateRepository.java
```

Delete the `_START_HERE.md` placeholder in each package once you've
added your files. Implement the repository with `JdbcTemplate`, the
same as every repository this week -- no manual `Connection`/
`Statement` management, and no SQL built by concatenating a variable
into the string, anywhere.

## Setup

1. Create `secops_event_exercise` and `secops_event_exercise_test`
   PostgreSQL databases, and a `secops_app` role with access to both.
2. `export SECOPS_DB_PASSWORD=your-password`
3. Run `schema.sql` then `data.sql` against `secops_event_exercise`.
4. `Run tests` — expect compile errors until your classes exist with
   the right shape, then real test failures until your logic is
   correct.
5. Once `all tests` pass, uncomment `Application.java` and run it.


## Submitting

Submit project with all five files you created, with `test` fully green and
`Application.java` uncommented and working.

# SQL Injection Demo — Safe Sandbox (Spring MVC)

> **Audience: instructor.** This is an **instructor-run live demo** — students
> watch and discuss. This file and the Instructor Guide contain the attack
> payloads, so they are **not** student handouts. Facilitation script, talking
> points, and timing are in `INSTRUCTOR_GUIDE.md`.

A small, **deliberately-insecure** Spring MVC web app for teaching SQL
injection. A product **search box** builds its SQL by string concatenation, so
the very same box can be used to:

1. **Read data it shouldn't** — a `UNION SELECT` pulls the `users` table
   (usernames + clear-text passwords) into the product results.
2. **Vandalise data** — a stacked statement (`'; UPDATE … --`,
   `'; DELETE … --`, `'; DROP TABLE … --`) changes or destroys data.

It uses the architecture the students already know: **REST controller → service
→ repository → POJO**, with Spring JDBC (`JdbcTemplate`). The untrusted search
text is passed *blindly* down through each layer into the concatenated SQL.

> **This is a teaching sandbox.** It is insecure on purpose — no parameterized
> queries, clear-text passwords, and tables with no keys or constraints. It runs
> against a throwaway `sqli_demo` database of fake data. Never write code like
> `ProductRepository`/`UserRepository` in a real app.

---

## Prerequisites

- **Java 21+** and **Maven**
- **PostgreSQL** running locally on port 5432
- A modern web browser

> On Windows, `psql` may not be on your `PATH`. It ships with PostgreSQL, e.g.
> `C:\Program Files\PostgreSQL\18\bin\psql.exe` — use the full path, or add that
> `bin` folder to `PATH`. `psql` will prompt for the **postgres** password (set
> `PGPASSWORD` to avoid the prompt).

> **IDE JDK setup:** this project ships without IDE settings (no committed
> `.idea/`), so the first time you open it in IntelliJ it will ask for a project
> SDK. Pick any installed **JDK 21 or newer** (File → Project Structure →
> Project → SDK; use *Add SDK → Download JDK* if you have none). The build
> targets Java 21, which runs fine on a newer JDK. Command-line users just need
> `java -version` to report 21+.

## Setup

**1. Create the throwaway database** (run once):

```sql
CREATE DATABASE sqli_demo;
```
*Expected:* `CREATE DATABASE`.

**2. Load the sandbox schema and fake data:**

```bash
psql -U postgres -d sqli_demo -f src/main/resources/db/schema.sql
```
*Expected:* `CREATE ROLE` (first run), `CREATE TABLE`, `INSERT 0 4`,
`INSERT 0 5`, and three `ALTER` lines (schema/table ownership).
**Re-run this any time to reset the sandbox** — do it after any "vandalise"
attack, which really does change or drop data. Re-running keeps the existing
`sqli_owner` role and its password (the `IF NOT EXISTS` guard).

> Upgrading from an earlier version of this demo? Drop its leftover role:
> `psql -U postgres -d sqli_demo -c "DROP ROLE IF EXISTS sqli_app;"`

**3. Create your `.env`** (copy the example):

```bash
cp .env.example .env
```

The example points the app at **`sqli_owner`** — a non-superuser role that
`schema.sql` creates and makes the owner of the demo tables, so the vandalise
demos can write/drop them but nothing else — see [Why it's safe](#why-its-safe).
Change the password in both `schema.sql` and `.env` if you like. `.env` is
git-ignored.

## Run

```bash
mvn spring-boot:run
```

(Or `mvn package` then `java -jar target/sql-injection-demo-1.0.0.jar`.)

Then open **http://localhost:8080** in your browser.

## The 2-minute walkthrough

The page has example-search buttons so you don't have to type payloads.

1. **Normal:** click *Normal: Widget*. Two products. Note the printed SQL.
2. **Read:** click *Read: dump users*. The results table now shows the
   `users` rows — usernames and **clear-text passwords** — pulled in by a
   `UNION SELECT`.
3. **Prove it's real:** in **Staff login**, log in as `admin` with the password
   you just saw (`c0rrecth0rse`).
4. **Vandalise:** click *Set all prices to 0.01* (or *Overwrite all passwords*,
   *Delete all products*, *Drop the products table*). The search shows a
   "stacked statement ran" notice — **the write already happened**. Click
   *Normal: Widget* or *Read: dump users* again to see the damage.
5. **Reset:** re-run `schema.sql`.
6. **Show the fix live:** tick **Secure mode** and click the same *Read* /
   vandalise buttons again — now they return *No rows* and change nothing. The
   printed SQL shows a `?` placeholder with the payload bound as a value.

## Why it's safe

Three layers — the first two keep the blast radius inside the sandbox, the third
keeps it on your machine:

1. **Isolated data** — a separate, disposable `sqli_demo` database of fake data,
   reset in one command (`schema.sql`).
2. **Non-superuser account** — the app connects as `sqli_owner`, which *owns the
   two demo tables* (so the vandalise demos run) but is **not** a superuser. A
   stacked payload therefore **cannot** run OS commands (`COPY … TO PROGRAM`),
   read server files, or read the real Postgres password hashes — it's confined
   to those two tables. (This is itself a talking point: *this is exactly why an
   app's DB account must never be a superuser.*)
3. **Localhost only** — `application.properties` sets `server.address=127.0.0.1`,
   so the vulnerable endpoints aren't exposed to the network.

This is **not** "nothing real is reachable" — it's a contained lab. So:

> ⚠ **Don't** point this app at any real database, **don't** expose port 8080,
> and **don't** run the demo while on a shared/classroom network with the bind
> address changed. Run it on your own machine, against `sqli_demo`, as
> `sqli_owner`.

The fix for injection — **parameterized queries** — can be shown live with the
**Secure mode** switch on the page, and as before/after code in
`INSTRUCTOR_GUIDE.md`.

## Fake accounts (sandbox only)

| username | password | role |
|----------|----------|------|
| alice | sunshine | admin |
| bob | hunter2 | staff |
| carol | letmein | staff |
| admin | c0rrecth0rse | admin |

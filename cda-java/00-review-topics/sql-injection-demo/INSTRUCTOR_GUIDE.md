# Instructor Guide — SQL Injection Demo (Spring MVC)

**Format:** instructor-run live demo in a browser (students watch, then discuss).
**Time:** ~22–28 min at full depth; ~15 min on the short path (see below).
**Prep:** app built, `sqli_demo` loaded, `mvn spring-boot:run` started, and
**http://localhost:8080** open *before* class. Do a dry run and then reset
(`schema.sql`) so you start clean.

> Keep this file (and the README) out of any student-facing handout — they hold
> the payloads and the answer key.

**Time budget (full depth):**

| Segment | Time | Core vs. optional |
|---------|------|-------------------|
| 1 — Normal use + show the SQL | ~4 min | all core |
| 2 — UNION read + log in with stolen password | ~7 min | core; the "how did the attacker know?" probing detail is optional |
| 3 — Vandalise (stacked writes) | ~7 min | core: run **Drop the products table** (most vivid) + restore; the other three chips (prices / passwords / delete) and the GET-smell aside are optional |
| 4 — The fix (Secure mode live + code) | ~6 min | core; the non-fixes list is optional |
| Polls + closing check | ~4 min | optional |

**Short on time (~15 min):** Segment 1 → Segment 2 (read + log in with stolen
password) → Segment 3 (one vandalise: drop the table, then restore) → Segment 4
(tick Secure mode, re-run, show the one-line `?` fix). Skip the probing detail
and the non-fixes list.

---

## The app in one slide

A normal-looking "Widget Store" page: a **product search box** and a **staff
login**. Under it, the architecture the students already use:

```
Browser  ──►  ProductController (REST)  ──►  ProductService  ──►  ProductRepository  ──►  PostgreSQL
                 /api/search?q=…              passes q through     builds SQL by  +  concatenation
```

The whole vulnerability is one line in `ProductRepository`:

```java
"SELECT name, category, CAST(price AS TEXT) AS price FROM products WHERE name ILIKE '%" + term + "%'"
```

The search term becomes part of the command. Everything below follows from that.

---

## Learning outcomes

By the end, students can:

1. Explain SQL injection as a failure to separate **code from data** — user
   input concatenated into a SQL string becomes part of the command.
2. Show that one injectable input can both **read** other tables (`UNION`) and
   **change/destroy** data (stacked `UPDATE`/`DELETE`/`DROP`).
3. Explain why **parameterized queries** are the fix, and why escaping,
   blocklisting, or "it's behind a service layer" are not.

---

## The one idea to repeat

> **The database can't tell your data from your commands unless you keep them
> apart.** Concatenation mixes them. A parameter keeps them apart: the command
> is fixed first, then the value is bound in — so a value can never become code.

---

## Segment 1 — Normal use, and show the SQL (~4 min)

Click **Normal: Widget**. Two products appear, and the page prints:

```
SELECT name, category, CAST(price AS TEXT) AS price FROM products WHERE name ILIKE '%Widget%'
```

- Point out: the page shows the **exact SQL** it ran. That printout is the
  lesson for the rest of the session.
- Trace the layers once (controller → service → repository). Ask: *"Where does
  our typed text end up?"* Answer: concatenated into that string.

---

## Segment 2 — Read data you shouldn't: UNION (~7 min)

Click **Read: dump users**. The search box field becomes:
```
' UNION SELECT username, password, role FROM users --
```
and the results table now contains the `users` rows — **usernames and clear-text
passwords** — mixed in with the products.

**Decompose the printed SQL (read it aloud):**
```
… WHERE name ILIKE '%' UNION SELECT username, password, role FROM users --%'
```
- The leading `'` **closes** the search string, so the product filter becomes
  `ILIKE '%'` (matches everything).
- `UNION SELECT …` **staples a second query on**; `--` comments out the trailing
  `%'` the code appended.
- Result: every product **and** every user, in one list (order not guaranteed).

**Talking points:**
- The columns are mislabeled on screen — a password shows under "Category", a
  role under "Price". That mismatch *is* the tell: this data came from a
  different table than the headers claim.
- `UNION` needs the same column count and compatible types as the first query —
  here 3 and 3 (the code `CAST(price AS TEXT)`, so all three are text).
- **Prove the theft is real:** in **Staff login**, log in as `admin` /
  `c0rrecth0rse` (a password you just read). The stolen credentials work.
- Note (students will try it): the **login is injectable too** — a username of
  `' OR '1'='1' --` logs in without a password, because `UserRepository`
  concatenates just like the search. We focus on the search box; converting login
  to parameters is the take-home.
- Clear-text passwords made this a one-step disaster. Hashing would help here —
  but that's a *different* control; injection is still the bug.

**Optional — how did the attacker know the table/columns?** A real attacker
probes: `ORDER BY 1,2,3…` or `UNION SELECT NULL,NULL,…` to find the column
count, `information_schema.tables`/`.columns` for names, and error messages for
type hints. Our payload is pre-tuned to the code so the demo runs cleanly.

---

## Segment 3 — Vandalise data: stacked statements (~7 min)

> This really changes data. You'll reset with `schema.sql` after.

Click **Set all prices to 0.01**. The field becomes:
```
'; UPDATE products SET price = 0.01 --
```
The page shows the SQL and a red notice:
> *A stacked statement ran. The search returned nothing usable — but your extra
> `; UPDATE/DELETE/DROP` already executed on the database. Search again to see
> the damage.*

Now click **Normal: Widget** → every price is `0.01`.

**This is the key accuracy point — teach it explicitly:**
- The `;` ends the SELECT and starts a **second statement**. PostgreSQL (via
  *pgjdbc*, the PostgreSQL JDBC driver) **runs it**. Do **not** tell students
  "the JDBC driver blocks stacked queries" — it doesn't.
- The search's own response was the message *"Multiple ResultSets were returned"*
  — so the attack *looked* like it failed. **It didn't.** That message just means
  `JdbcTemplate.query` called `executeQuery`, which expects exactly one result
  set, and got more than one back (the SELECT plus the extra statement). The
  write had already committed before that error surfaced. An attacker doesn't
  care what the page shows.

**Escalate (pick one or two):**
- **Overwrite all passwords** → then re-run *Read: dump users* to see every
  password is now `pwned`; everyone is locked out and the attacker sets the
  credentials.
- **Delete all products** → *Normal: Widget* returns nothing.
- **Drop the products table** → *Normal: Widget* now errors
  *"relation \"products\" does not exist"*. The feature is gone.

**Restore (do this as the last step of the segment):** re-run `schema.sql`. The
app does **not** need restarting — it reconnects and serves the rebuilt tables.
Show the class the restore so they see the sandbox is disposable: after a *Drop
the products table*, run `schema.sql`, then click *Normal: Widget* → products are
back.

**Aside for a security cohort (optional):** this search is a **GET** that just
changed data. GET requests shouldn't mutate state — they're cached, prefetched,
and logged, and they're trivially fired cross-site (a tag on any page could hit
`localhost:8080/api/search?q=…`). So "injectable" and "a write behind a GET" are
two separate smells stacked on top of each other.

**Ask the room:** *"Same search box. We read every password and dropped a table.
What else is reachable?"* Anything the app's DB account can touch — which is why
that account is a non-superuser here (see Beyond the fix).

---

## Segment 4 — The fix: parameterized queries (~6 min)

**Show it live first.** Tick **Secure mode** on the page and re-run the exact
same buttons:

- *Read: dump users* → **No rows.** The printed SQL is now
  `… WHERE name ILIKE ?` with a `[bound] ? = '%' UNION SELECT … --%'` line — the
  whole payload is a *value*, not code.
- *Drop the products table* → **No rows**, and the table is still there (run
  *Normal: Widget* to confirm). The stacked statement never executes.
- *Staff login* with `admin` / `c0rrecth0rse` still works for real users, and
  *Normal: Widget* still returns products — the fix doesn't break anything.

> The page prints the SQL for whichever path ran: the concatenated string in
> vulnerable mode (which *is* what executed), or the `?` template plus the bound
> value in Secure mode. So "what you see is what ran" holds in both.

> **Secure mode covers the search box only.** The login stays injectable even
> with it on (a username of `' OR '1'='1' --` still gets in) — that's the
> take-home, so don't run the login bypass with Secure mode on, or it looks like
> the fix failed.

**Then show the code.** Open `ProductRepository.java`. The vulnerable method:

```java
public List<Product> search(String term) {
    String sql = "SELECT name, category, CAST(price AS TEXT) AS price "
               + "FROM products WHERE name ILIKE '%" + term + "%'";
    return jdbc.query(sql, MAPPER);
}
```

The fix — a **parameter**, not a concatenated string:

```java
public List<Product> search(String term) {
    String sql = "SELECT name, category, CAST(price AS TEXT) AS price "
               + "FROM products WHERE name ILIKE ?";          // fixed command, ? placeholder
    return jdbc.query(sql, MAPPER, "%" + term + "%");         // value bound separately
}
```

- The SQL is now a **fixed template**. The `%…%` is built around the *value* in
  Java and bound to `?`. The database parses the command first, then binds the
  value — so `' UNION …` or `'; DROP …` is just text to search for. It matches
  no product and nothing else runs.
- `JdbcTemplate` sends a bound query as a `PreparedStatement`. "Parameterized
  query" and "prepared statement" mean the same thing here. Apply the identical
  change to `UserRepository.findByCredentials` (login is injectable too).
- In the shipped code these live side by side: `search()` (vulnerable) and
  `searchSecure()` (the fix), so the Secure-mode toggle can flip between them.
  The real "fix" is the body of `searchSecure()` — the `?` and the bound value.

**Teach the non-fixes (~2 min):**
- **Escaping quotes by hand** (`replace("'", "''")`) — brittle; misses numeric
  contexts and encoding tricks. Let the driver bind parameters instead.
- **Blocklisting** words like `UNION`/`--`/`DROP` — trivially bypassed, breaks
  valid input. Not a defense.
- **"It goes through a service layer"** — our `ProductService` passed the text
  straight through. Layers don't sanitize anything by themselves.
- **Bottom line:** separate code from data with parameters.

---

## Beyond the fix (reference)

- **Parameters bind *values*, not identifiers.** You can't bind a table/column
  name (e.g. a dynamic `ORDER BY` column). For those, check input against an
  **allow-list** of known-good names. (Bridge to the Secure Repository Pattern.)
- **Least privilege** — this demo connects as `sqli_owner`, which *owns* the two
  demo tables (so the vandalism works) but is **not** a superuser. Prove it if
  asked: as `sqli_owner`, `COPY … TO PROGRAM` (OS commands), `pg_read_file`
  (server files) and reads of `pg_authid` (real password hashes) are all
  *permission denied*. In production the app's account should be even narrower
  (e.g. no `DROP`). Least privilege limits the blast radius; it is **not** the
  fix. **Never connect an app as a database superuser.** (Also: this app binds to
  `127.0.0.1` only — don't expose port 8080 or demo on a shared network.)
- **Clear-text passwords** — hashing would blunt Segment 2's payoff, but it's a
  separate control. The injection bug remains.

---

## Facilitation & remote delivery

**Pre-class dry run:**
- [ ] `psql … -f src/main/resources/db/schema.sql` resets the sandbox cleanly.
- [ ] `mvn spring-boot:run` starts; http://localhost:8080 loads.
- [ ] Walk all four segments once, then reset so you start class clean.
- [ ] Confirm the app keeps serving after a `schema.sql` reset **while it's
      running** (drop the table, re-run `schema.sql`, search again → products
      return). No restart needed.

**Screen-share / readability:**
- Zoom the browser so the back row can read the printed SQL and the results.
- The dark SQL block is the punchline — pause on it each time.
- Use the on-page example buttons instead of typing payloads (a typo in a
  payload just looks like an empty result and kills the moment).

**Checks for understanding (predict-then-reveal; great over video chat):**
- Before *Read: dump users*: *"What will appear in the Price column?"*
  (Answer: user roles — data from another table.)
- Before *Set all prices to 0.01*: *"The page will show a message that looks
  like an error. Did the attack work?"* (Answer: yes — check with a normal
  search.)
- Before Segment 4: *"We're behind a service layer. Are we safe?"* (No.)

**Closing check (ask, then reveal):**
> *"Here's a repository method. Safe or not, and why?"*
> ```java
> jdbc.query("SELECT * FROM orders WHERE customer = '" + name + "'", mapper);
> ```
> **Answer:** Unsafe — `name` is concatenated in. Fix: `WHERE customer = ?` and
> pass `name` as a bound argument.

**Optional take-home:** have students convert `UserRepository.findByCredentials`
to parameters and write down one payload each change defeats. Natural bridge to
the **Secure Repository Pattern** demo (`secure-repository-pattern-solution/`).
(The short-on-time path is in the time budget near the top of this guide.)

---

## Code map (where to point)

| File | Role |
|------|------|
| `resources/static/index.html` | The web page: search box, login, example-payload buttons, SQL printout |
| `controller/ProductController.java` | REST `/api/search`; returns the executed SQL + rows (+ any DB error) |
| `controller/AuthController.java` | REST `/api/login` (clear-text compare) |
| `service/ProductService.java` | Passes the raw term straight through — no validation |
| `repository/ProductRepository.java` | **The vulnerability** — concatenated search SQL; the fix goes here |
| `repository/UserRepository.java` | Login lookup — concatenated and clear-text (also injectable) |
| `model/Product.java`, `model/User.java` | POJOs |
| `resources/db/schema.sql` | Fake sandbox data, no keys/constraints (re-run to reset) |

---

## Troubleshooting / FAQ

**Q: The page shows "A stacked statement ran … Multiple ResultSets".** Expected
for a `;`-stacked write through `JdbcTemplate.query`. The SELECT and the write
both ran; `JdbcTemplate` just couldn't hand back two results. The write
committed — prove it with a follow-up search.

**Q: After "Drop the products table", searches error.** Correct — the table is
gone (`relation "products" does not exist`). Re-run `schema.sql` to restore it.

**Q: Why does the SQL say `products`, not `app.products`?** The tables live in
schema `app`, and `application.properties` sets
`spring.datasource.hikari.schema=app`, so the connection's search path is `app`.
In `psql`, `SET search_path TO app;` or qualify as `app.products`.

**Q: Can a payload here damage anything real?** No — it's a *contained lab*, via
three layers: (1) isolated fake data in a throwaway `sqli_demo` you reset with
`schema.sql`; (2) the app connects as `sqli_owner`, which owns the two demo
tables (so vandalism works) but is **not** a superuser, so `COPY … TO PROGRAM`,
`pg_read_file` and `pg_authid` are refused — it's confined to those two tables;
(3) `server.address=127.0.0.1`, so the endpoints aren't on the network. This is
*not* "nothing is reachable" — so don't point the app at a real database, don't
expose port 8080, and don't demo on a shared network.

**Q: Login worked with a stolen password — is login injectable too?** Yes.
`UserRepository.findByCredentials` concatenates just like the search. The demo
focuses on the search box; converting login to parameters is the take-home.

# Instructor Guide: The Secure Repository Pattern (Code-Along)

**Lesson:** Module 1: Advanced Java Security, Lesson 3
**Duration:** 2 hours (120 minutes), following the lesson's Timed Facilitation Flow
**Audience:** adult learners who have finished Lesson 1 (atomic transfers,
commit/rollback) and Lesson 2 (AES-GCM field encryption, externalized keys),
and who can read basic SQL and Java.

> **Instructor-only.** This file is the answer key. It lives only in
> `secure-repository-pattern-solution`. Never copy it into the starter.

See [`README.md`](README.md) for setup and troubleshooting. Learners get
`secure-repository-pattern-starter`. This folder is the finished solution.

---

## Learning Outcomes

By the end of the code-along, learners can:

- Route data access through a repository so validation and parameterization
  live in one auditable place.
- Validate and constrain input at the repository boundary before persistence.
- Handle data-access exceptions securely, with no stack traces, SQL, or
  sensitive detail leaked to callers.
- Map results to objects without exposing sensitive fields.

---

## What Learners Build

Learners work in one class: `repository/AccountHolderRepository.java`. Its
four methods (`create`, `searchByName`, `findById`, `findAll`) start as a
working but insecure first draft:

- SQL built by string concatenation
- no input validation
- `SELECT *` with automatic record mapping
- `catch (Exception ex)` that prints `ex.getMessage()` and swallows the failure

Eight `TODO: Step N of 8` comments walk them to the solution. In this folder,
the same comments read `TODO: (Completed) Step N of 8`, sitting next to the
code that finishes each step.

Everything else is finished and identical in both folders. Learners **read**
these files but don't change them, except `ConsoleMenu` in the extension
exercise:

| File | Why learners look at it |
|---|---|
| `AccountRepository` | A finished repository. `debit` is the worked example for Step 6: `Validate.amount` is the only thing stopping a `-50.00` debit from adding money. |
| `TransactionRepository` | A second finished repository |
| `Validate` | The shared boundary checks learners call in Step 6 |
| `DataAccessFailure` | The one exception a repository lets escape (Step 7) |
| `TransferService` | Lesson 1's transfer, now `@Transactional`. It goes through repositories only. |
| `FieldCipher` | Lesson 2's AES-GCM, now a Spring `@Component` whose key is injected with `@Value` |
| `ConsoleMenu` | The view. It catches only `IllegalArgumentException` and `DataAccessFailure`. Learners add menu option 7 to it in the extension. |

**Spring in one breath**, for learners who haven't used it: Spring Boot reads
`application.properties` (which loads `.env`), builds the connection pool, and
creates every class marked `@Repository`, `@Service`, or `@Component`, passing
each one what its constructor asks for. `JdbcClient` is Spring's short API for
SQL. Its named parameters (`:accountId`) are still bound values in a
`PreparedStatement` underneath.

---

## Before Class

- [ ] Run `schema.sql` against `ledger_demo` in pgAdmin (README, setup step 2).
- [ ] Create `.env` with the class key `4FCL14YY5aI9f04oLJgSYzKsdh789PsdwZB/ykuJPQU=`.
- [ ] Open **the starter** in IntelliJ and let Maven finish downloading. Don't
      leave the first download for classroom Wi-Fi.
- [ ] Run the starter once and add two holders from menu 4. Neither name has
      an apostrophe, so both work in the starter:
      `ACC-00000001` / `Jordan Rivera` / `123-45-6789`, and
      `ACC-00000003` / `Maria Lopez` / `555-12-3456`.
      Learners do the same: it's README setup step 5, and the Step 1 TODO
      reminds them.
- [ ] Rehearse Steps 2, 3, 5, and 7 in the starter. Their "run it first"
      moments carry the lesson.
- [ ] Have `logs/ledger-demo.log` ready to open in IntelliJ next to the Run
      window for Step 7.
- [ ] Keep this folder open in a second IntelliJ window as your answer key.
- [ ] If a learner's `ledger_demo` still has a plaintext tax ID from an
      earlier lab, menu 3 shows "Could not load that account holder" for that
      row. Re-running `schema.sql` fixes it.

---

## Timed Facilitation Flow

The segments and times follow the lesson's Timed Facilitation Flow. In this
code-along, learners write the code **during** segments 2 to 4, so the
25-minute activity slot is used for an independent extension exercise.

| Segment | Time | Steps | Notes |
|---|---|---|---|
| The Repository/DAO as a Security Boundary | 20 min | 1 | Whiteboard (below), then Step 1. Learners add the two practice holders. |
| Centralizing Parameterized Access | 25 min | 2–5 | `O'Brien` breaks the INSERT, then the search and lookup injections, then the sort allow-list. |
| Boundary Input Validation | 20 min | 6 | Open `AccountRepository.debit` first: the negative-amount walkthrough. Then learners add the three patterns. |
| Secure Exception Handling and Safe Result Mapping | 25 min | 7–8 | Put the leaked Step 2 output on the board. Then "log rich, respond thin," then explicit mapping. |
| Activity: Extend the Secure Repository | 25 min | Extension | Learners add `rename(...)` alone, applying all four disciplines. Use it as catch-up time too. |
| Wrap-Up & Reflection | 5 min | none | Close on "validate again at the boundary anyway." |
| **Total** | **120 min** | | |

> **Note for the course maintainer:** the lesson's activity file lists 35
> minutes, but the lesson flow gives it 25. This guide holds to 25.

**If you run short on time,** cut the extension first and use the slot for
catch-up. Never cut Steps 2 or 7: they're the moments learners remember.

---

## Segment 1: The Repository as a Security Boundary (20 min)

### Whiteboard

Draw the "many callers, one repository, one database" picture:

```
  ConsoleMenu ──────┐
  TransferService ──┼──▶  Repositories  ──▶  ledger_demo
  (a future API) ───┘    ┌─────────────┐
                         │ validate    │
                         │ bind values │
                         │ map columns │
                         │ fail safely │
                         └─────────────┘
```

Then erase one arrow and redraw it straight from `TransferService` to the
database. Ask: *"If this class builds its own SQL, which of the four boxes
still protects it?"* None do. The repository only protects the paths that go
through it.

**Misconception to catch early:** "a repository is just a class with JDBC in
it." It's a security boundary only if it's the **only** path in.

### Step 1: Confirm the only path in (5 min)

Learners press **Ctrl+Shift+F** and search for `account_holders`. The only
hits are `AccountHolderRepository` and `schema.sql`. There's no code to write.

Then check that every learner has the two practice holders (README setup
step 5, repeated in the Step 1 TODO). Anyone who skipped setup adds them now
from menu 4. Both work, because neither name has an apostrophe. Have learners
choose **1** and sort by `display_name` to see both holders.

---

## Segment 2: Parameterized Access (25 min)

### Step 2: Parameterize the INSERT, `create` (8 min)

**Run it first:** menu 4, `ACC-00000002`, `Pat O'Brien`, `987-65-4321`.

The starter prints:

```
Database error: PreparedStatementCallback; bad SQL grammar [INSERT INTO account_holders
  (account_id, display_name, tax_id_encrypted, created_at) VALUES ('ACC-00000002', 'Pat O'Brien', ...
Holder saved. Tax ID stored encrypted.
```

Make three points, and **save this output for Step 7**:

1. The apostrophe alone broke the SQL, before anyone even tried an attack.
   Concatenation is *fragile* first and exploitable second.
2. Our SQL is on the screen, ciphertext and all.
3. The menu then said "Holder saved." It wasn't. The catch block swallowed
   the failure.

**Solution:**

```java
jdbc.sql("""
                INSERT INTO account_holders (account_id, display_name, tax_id_encrypted, created_at)
                VALUES (:accountId, :displayName, :taxIdEncrypted, :createdAt)""")
        .param("accountId", accountId)
        .param("displayName", displayName)
        .param("taxIdEncrypted", cipher.encryptField(taxId))
        .param("createdAt", OffsetDateTime.now(ZoneOffset.UTC))
        .update();
```

`ZoneOffset` is already imported in the starter. Rerun with `Pat O'Brien`: it
saves. The apostrophe is now just data.

**Watch for:** a parameter name that doesn't match its `.param(...)` name,
such as `:displayName` in the SQL with `.param("display_name", ...)`. The
starter's catch block prints *No value supplied for the SQL parameter
'displayName'*.

### Step 3: Parameterize the search, `searchByName` (6 min)

**Run it first:** menu 2, search for `zzz' OR 1=1 --`. Every holder comes
back. The `--` turned the rest of the SQL into a comment.

Also try `O'Brien`. It fails the same way the INSERT did.

**Solution** (the columns and `SUMMARY` come in Step 8, so for now keep
`SELECT *` and `.query(HolderSummary.class)`):

```java
return jdbc.sql("SELECT * FROM account_holders WHERE display_name ILIKE :pattern ORDER BY display_name")
        .param("pattern", "%" + namePart + "%")
        .query(HolderSummary.class)
        .list();
```

**Key point:** the `%` wildcards go in the **bound value**, never in the SQL
text. A common wrong fix is `ILIKE '%:pattern%'`, which is a string literal
that contains the text `:pattern`, not a parameter.

After the fix, `zzz' OR 1=1 --` returns "No holders found." It's now a very
odd name to search for, nothing more.

### Step 4: Parameterize the lookup, `findById` (4 min)

**Run it first:** menu 3 with `' OR 1=1 LIMIT 1 --`. It shows the first
holder in the table, with a masked tax ID, even though the learner never
typed an account ID.

Point out that the masking still held. Lesson 2's encryption and the masked
`toString` limit the damage, but the attacker still chose which record to
read.

**Solution:** `WHERE account_id = :accountId` plus
`.param("accountId", accountId)`, before `.query(...)`.

### Step 5: Allow-list the sort column, `findAll` (7 min)

**Run it first:** menu 1, sort by `1/0`:

```
Database error: PreparedStatementCallback; SQL [SELECT * FROM account_holders ORDER BY 1/0]; ERROR: division by zero
```

Postgres **evaluated** our text. Anything that can go in an `ORDER BY` can
be injected here.

Ask the room: *"Can we just use `ORDER BY :sortColumn`?"* No. JDBC binds
**values**, not identifiers such as column or table names. So an identifier
must be checked against a small, known list.

**Solution:**

```java
private static final List<String> SORT_COLUMNS = List.of("account_id", "display_name", "created_at");
...
if (!SORT_COLUMNS.contains(sortColumn)) {
    throw new IllegalArgumentException("Sort column must be one of " + SORT_COLUMNS);
}
// Concatenation is safe here ONLY because sortColumn is now one of three known strings.
```

Bridge to Segment 3: the allow-list *is* boundary validation, just for an
identifier instead of a value.

---

## Segment 3: Boundary Input Validation (20 min)

### Walkthrough first: `AccountRepository.debit` (8 min)

Open `AccountRepository`. A debit runs
`UPDATE ... SET balance = balance + :delta` with `delta = -amount`. The SQL
is perfectly parameterized, so ask: *"What does debiting `-50.00` do?"* It
adds $50. Parameterization stops injection. It doesn't stop nonsense.

Run menu 5: from `ACC-00000001`, to `ACC-00000002`, amount `-50.00`. The
screen shows `Error: Amount must be greater than zero`. That message comes
from `Validate.amount`, and it runs before any SQL does.

**Why the repository?** It's the one place guaranteed to run on every code
path. An upstream check in the menu or an API can be skipped, forgotten, or
missing from a caller added next year.

Keep this segment tight: one idea (constrain values at the boundary), not a
general re-introduction of input validation.

### Step 6: Validate every input (12 min)

**Solution: the constants**

```java
private static final Pattern DISPLAY_NAME = Pattern.compile("[\\p{L} .'-]{1,128}");
private static final Pattern NAME_SEARCH = Pattern.compile("[\\p{L} .'-]{1,64}");
private static final Pattern TAX_ID = Pattern.compile("\\d{3}-\\d{2}-\\d{4}");
```

`Pattern` is already imported in the starter.

**Solution: the calls**, at the top of each method, **before** the `try`:

```java
// create
Validate.accountId(accountId);
Validate.matches(displayName, DISPLAY_NAME, "Name must be 1-128 letters, spaces, apostrophes, periods or hyphens");
Validate.matches(taxId, TAX_ID, "Tax ID must look like 123-45-6789");

// searchByName
Validate.matches(namePart, NAME_SEARCH, "Search text must be 1-64 letters, spaces, apostrophes, periods or hyphens");

// findById
Validate.accountId(accountId);
```

**Watch for:** a `Validate` call placed *inside* the starter's `try`.
`catch (Exception ex)` then catches the `IllegalArgumentException`, prints
it, and carries on. Validation belongs before the `try`. After Step 7, the
narrower `catch (DataAccessException ex)` would let it through anyway, but
the habit matters.

**Talking points:**

- `\p{L}` means "any letter in any language," so `José` and `Zoë` pass.
  Validation that rejects real names is a bug, not security.
- The apostrophe is **allowed**. Validation is about the business rule (what
  a name looks like). Parameterization is what makes the apostrophe safe.
  Defense in depth: both, not either.
- `NAME_SEARCH` excludes `%` and `_`, so a user can't type their own
  wildcards into the `ILIKE`.
- Rerun `zzz' OR 1=1 --` in menu 2. It's now rejected before any SQL runs.

---

## Segment 4: Secure Exceptions and Safe Mapping (25 min)

### Step 7: Handle exceptions securely (13 min)

Put the Step 2 output back on the board. Ask: *"What could an attacker learn
from this?"* Let the room answer: the table name, every column name, the SQL
shape, the ciphertext format, and the fact that concatenation is in use.
Point out that this is **Spring's** message, not the raw driver's: even a
framework's translated exception carries SQL. That's why the repository
still has to wrap it. This weakness has a name: CWE-209, error messages
containing sensitive information.

Then open `DataAccessFailure` and walk through "log rich, respond thin":

- `logged(...)` writes the full cause to `logs/ledger-demo.log`, tagged with
  a short random `ref`.
- The caller gets a generic message plus that `ref`, and nothing else.
- The cause is deliberately **not** attached, so no caller can print
  `getCause()` and leak it anyway.

**Solution** (`create` shown; the other three follow the same shape):

```java
} catch (DuplicateKeyException ex) {
    throw DataAccessFailure.logged("create account holder", "That account already has a holder.", ex);
} catch (DataAccessException ex) {
    throw DataAccessFailure.logged("create account holder", "Could not save the account holder.", ex);
}
```

| Method | Catch | Safe message |
|---|---|---|
| `create` | `DuplicateKeyException`, then `DataAccessException` | "That account already has a holder." / "Could not save the account holder." |
| `searchByName` | `DataAccessException` | "Could not search account holders." |
| `findById` | `DataAccessException \| IllegalStateException` | "Could not load that account holder." |
| `findAll` | `DataAccessException` | "Could not load account holders." |

Both exception types are already imported in the starter. Also delete the
`return List.of();` and `return Optional.empty();` lines from the old catch
blocks.

**Demo the payoff:** menu 4 with `ACC-00000001` again (it already has a
holder). The screen shows
`Error: That account already has a holder. (ref 56b06343)`. In the log,
search for the ref: it names the constraint `account_holders_pkey`, the SQL,
and the `PSQLException`. Put the screen and the log side by side.

**Watch for:**

- `DuplicateKeyException` caught *after* `DataAccessException`. That's a
  compile error ("exception has already been caught"), because it's a
  subclass. The specific catch goes first.
- Forgetting `IllegalStateException` in `findById`. `FieldCipher` throws it
  for a wrong key or tampered ciphertext, and without the catch it would
  escape to the menu and crash the app.
- Keeping `catch (Exception ex)`. That's too broad: it would also hide
  programming bugs, such as a `NullPointerException`, behind a friendly
  database message.

### Step 8: Map results explicitly (12 min)

The IG demo uses a row with an internal-only column. In `account_holders`,
that column is `tax_id_encrypted`: the list and search screens never need
it, but `SELECT *` pulls it into every result anyway.

Ask: *"`HolderSummary` has no tax ID field. So what's wrong with
`SELECT *` + `.query(HolderSummary.class)`?"*

- The ciphertext still crosses the wire and sits in memory on every list
  call.
- `.query(HolderSummary.class)` fills a record from **whatever** columns
  come back, matching them by name. If someone later adds a
  `taxIdEncrypted` field to `HolderSummary`, it fills silently, with no code
  change here that a reviewer would notice.

Safe mapping is a **selection**, not a blind passthrough.

**Solution:**

```java
private static final RowMapper<HolderSummary> SUMMARY = (rs, rowNum) -> new HolderSummary(
        rs.getString("account_id"),
        rs.getString("display_name"),
        rs.getObject("created_at", OffsetDateTime.class));
```

`RowMapper` is already imported in the starter. Then:

- `searchByName` and `findAll`: `SELECT account_id, display_name, created_at`
  and `.query(SUMMARY)`.
- `findById`: `SELECT account_id, display_name, tax_id_encrypted, created_at`.
  It's the only method that reads the tax ID, and it decrypts right there, at
  the boundary.

Close the segment by showing the three safety layers on a tax ID: encrypted
in the column (Lesson 2), selected only by `findById` (Step 8), and masked by
`AccountHolder.maskedTaxId()` and its `toString()` on the way out. Ask why a
record's `toString()` was overridden: the default prints every field, so one
stray log line would leak the full tax ID.

---

## Activity: Extend the Secure Repository (25 min)

Learners add a fifth method **on their own** and wire it to a new menu
option. It uses all four disciplines at once. The task and test inputs (not
the solution) are in the learners' README under "Extension: add `rename`".
Put the task on the board too:

> Add `rename(String accountId, String newDisplayName)` to
> `AccountHolderRepository`, and add menu option **7) Rename account holder**
> to `ConsoleMenu`. It must: validate both inputs, bind every value, handle
> failure with `DataAccessFailure`, and report "No holder for account ..."
> when nothing was updated.

**Test inputs:**

| Input | Expected |
|---|---|
| `ACC-00000001`, `Jordan O'Rivera` | "Holder renamed." Menu 1 shows the new name. |
| `ACC-00000005`, `Nobody` | `Error: No holder for account ACC-00000005` |
| `ACC-00000001`, `x'; DROP TABLE accounts; --` | `Error: Name must be 1-128 letters, ...` |

**Solution:**

```java
public void rename(String accountId, String newDisplayName) {
    Validate.accountId(accountId);
    Validate.matches(newDisplayName, DISPLAY_NAME, "Name must be 1-128 letters, spaces, apostrophes, periods or hyphens");
    int rows;
    try {
        rows = jdbc.sql("UPDATE account_holders SET display_name = :displayName WHERE account_id = :accountId")
                .param("displayName", newDisplayName)
                .param("accountId", accountId)
                .update();
    } catch (DataAccessException ex) {
        throw DataAccessFailure.logged("rename account holder", "Could not rename the account holder.", ex);
    }
    if (rows == 0) {
        throw new IllegalArgumentException("No holder for account " + accountId);
    }
}
```

In `ConsoleMenu`, add `7) Rename account holder` to the menu text, and this
case before `default` (then change the default message to "0-7"):

```java
case "7" -> {
    holders.rename(prompt("Account ID"), prompt("New display name"));
    System.out.println("Holder renamed.");
}
```

**Circulate for:** the `rows == 0` check placed inside the `try`. It works,
but it mixes "the database failed" with "the input matched nothing."
`AccountRepository.changeBalance` shows the clean shape.

Learners who finish early: ask them to explain why `rename` needs no
`@Transactional`, even though `TransferService.transfer` does. `rename` is a
single statement, which is already atomic.

---

## Wrap-Up & Reflection (5 min)

1. Where does the security of this repository live, and what would bypass it?
2. Why was the apostrophe the first problem, before any attack?
3. The menu could validate input too. Why validate again in the repository
   anyway?

Close on question 3. The repository is the one checkpoint every code path
passes through, including the ones nobody has written yet.

Bridge to Lesson 4: the next lesson's streaming and resource-safety work goes
through this same repository. It's the same class, with one more discipline
layered on.

---

## Common Learner Questions

1. **Q: I put all my JDBC code in one class. Doesn't that make it a
   repository?**
   A: Only if it's the only path in. If any other class still builds its own
   SQL against the table, that class skips every check the repository makes.

2. **Q: My `WHERE` clause is parameterized. Is a caller-supplied sort column
   safe too?**
   A: No. JDBC can bind values, not identifiers, so a column name can't be a
   parameter. Check it against an allow-list before it reaches the SQL text.

3. **Q: The menu already checks the amount. Do I still need the repository to
   check it?**
   A: Yes. The repository is the one place guaranteed to run on every code
   path. An upstream check can be skipped, bypassed, or missing from a caller
   added later.

4. **Q: Can I just show `ex.getMessage()` to the user?**
   A: No. As Step 2 showed, even Spring's message contains the SQL, and a raw
   message can include table names, column names, and driver details. Log
   the detail, and return a generic message.

5. **Q: `HolderSummary` has no tax ID field, so why does `SELECT *` matter?**
   A: It still moves the ciphertext on every call, and automatic mapping
   fills whatever fields exist. A field added later would fill silently.
   Name the columns you need.

6. **Q: Is `JdbcClient` with `:name` parameters really a `PreparedStatement`?**
   A: Yes. Spring rewrites `:name` to `?` and binds each value through a
   `PreparedStatement`. What makes it safe is binding, not the syntax.

7. **Q: Why doesn't `DataAccessFailure` keep the original exception as its
   cause?**
   A: So that no caller can leak it by printing `getCause()`. The full cause
   is already in the log, and the `ref` in the message leads straight to it.

8. **Q: Why not use Spring Data JPA and skip the SQL?**
   A: JPA generates safe SQL for simple lookups, but custom queries, sorting,
   and error handling bring back the same four disciplines. Learning them
   with visible SQL is what makes JPA's safety understandable later.

---

## Troubleshooting During Class

The README covers setup errors (Maven reload, JDK, `.env`, the key, the
connection). Problems specific to the code-along steps:

| Symptom | Cause | Fix |
|---|---|---|
| Greyed-out imports at the top of the starter | The imports the TODO steps need are included up front, and IntelliJ greys them out until they're used | That's expected. Don't let IntelliJ's **Optimize Imports** remove them before the steps are done. If one was removed, press **Alt+Enter** on the red name and choose **Import class**. |
| `SORT_COLUMNS`, `DISPLAY_NAME`, or `SUMMARY` "cannot find symbol" | The constant from the class-top TODO wasn't added | Add the constant under its Step comment at the top of the class |
| "exception DuplicateKeyException has already been caught" | Catch order reversed | The specific `DuplicateKeyException` catch goes before `DataAccessException` |
| "missing return statement" after Step 7 | The catch calls `DataAccessFailure.logged(...)` without `throw`, and the old `return` was deleted | Write `throw DataAccessFailure.logged(...)`. `logged` only builds the exception; `throw` is what stops the method. |
| `No value supplied for the SQL parameter 'x'` | A `:name` in the SQL with no matching `.param("name", ...)` | Match the spelling exactly |
| Search returns nothing for a name that exists | `ILIKE '%:pattern%'` in the SQL text | Put the `%` in the bound value: `.param("pattern", "%" + namePart + "%")` |
| After Step 7, menu 3 ends the app with `The demo could not start: ...` | `IllegalStateException` from `FieldCipher` isn't caught in `findById`, so it escapes the menu (usually a row stored with a different key) | Add it to the `findById` catch. Then check `LEDGERCORE_DATA_KEY`, or re-run `schema.sql`. |
| A validation message appears but the method still runs | A `Validate` call is inside the old `catch (Exception)` try | Move the `Validate` calls above the `try` |

---

## Facilitation Tips

- **Let the problem show before the fix.** Every "run it first" line in the
  TODOs exists so learners see the defect for themselves. Resist the urge to
  explain it before they've run it.
- **Code at the learners' pace for Steps 2 to 5.** The syntax is the same
  each time. By Step 4, ask the room to dictate the fix.
- **Keep the log file open next to the Run window from Step 7 on.** Seeing
  the `ref` connect the thin screen message to the rich log line is the
  moment "log rich, respond thin" lands.
- **Circulate for these half-fixes:** a leftover concatenated value in an
  otherwise parameterized query; `Validate` inside the `try`; catch blocks
  that still `return` instead of `throw`; a leftover `SELECT *` after Step 8.
- **Reset if data gets messy:** re-run `schema.sql`, then add the practice
  holders again from menu 4. It takes under a minute.

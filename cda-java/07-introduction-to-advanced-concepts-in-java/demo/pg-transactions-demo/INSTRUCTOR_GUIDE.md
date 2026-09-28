# Instructor Guide: Multi-Table Transactions on PostgreSQL

**Duration:** about 20 minutes (23 with the optional extension), plus about
10 minutes for the whiteboard discussion before it
**Audience:** adult learners who can already read JDBC code (`Connection`,
`PreparedStatement`) and basic SQL.

See [`README.md`](README.md) for setup, commands, and the expected output of
each scenario.

---

## Learning Outcomes

By the end of the demo, learners can:

- Explain why a multi-step write with auto-commit on is a data-integrity
  vulnerability, not just a reliability bug.
- Describe how one transaction makes writes across several tables succeed or
  fail as a unit.
- Explain why rollback has to run on *any* failure, including non-SQL
  exceptions.
- Report a failed transfer safely: full detail to the internal log, a safe
  message to the caller.
- Describe what other sessions can and can't see while a transaction is open.

---

## Whiteboard Discussion (before the demo)

Use this diagram to set up the demo. It shows every object involved in a
transfer and traces each scenario through them, so learners have a map before
they see any output.

![Transfer flow: the application boundary holds TransferDemo, TransferService,
TransferFailedException, System.Logger, and Database. They connect through
JDBC to the transfers, accounts, and ledger_entries tables in PostgreSQL, and a
second psql session reads accounts. Numbered solid lines trace the naive (1),
atomic (2), and visibility (3) scenarios. Lettered circles A to F mark the risk
points listed in the table below.](docs/transaction-flow.svg)

**How to draw it.** Build it up in layers rather than drawing it all at once:

1. **Boundaries.** Draw three boxes: the application (JVM), the PostgreSQL
   database, and a second session (another user or service).
2. **Objects and dotted connections.** Add the Java classes, the JDBC box,
   and the three tables. Dotted lines mean "is connected to" or "is related
   to". The two `FK` lines are the foreign keys that tie a transfer to its
   accounts and ledger lines.
3. **Scenario paths.** Trace each numbered solid line while you describe the
   scenario out loud. For path 1, count the five writes as they reach the
   tables.
4. **Risk points.** Before you add each letter, ask learners where on the path
   something could go wrong. Let them find A and B themselves: most will.
   C through F usually need a nudge.

| Point | Where it sits | What goes wrong | Where the demo shows it | The defense |
|---|---|---|---|---|
| **A** | Inside `TransferService`, between the debit and the credit | Something interrupts execution mid-transfer: an exception, a crash, a restart | `naive` and `atomic` crash steps | Put all five writes inside one transaction boundary |
| **B** | Path 1, between JDBC and the tables | With auto-commit on, each write commits alone, so the half-done transfer stays | `naive`: $525.00 of $600.00 | `setAutoCommit(false)`, then one `commit()` at the end |
| **C** | Path 2, at `accounts` | A database rule (the `CHECK` constraint) fails *after* the header row was already written | `atomic`: the overdraft | Rollback undoes everything since BEGIN, across all three tables |
| **D** | Path 2, where `TransferService` hands off to JDBC | A narrow `catch` skips the rollback, or `setAutoCommit(true)` runs first; either way the partial writes commit | Learner question 1 (the exercise) | Roll back in `finally` whenever `commit()` didn't happen, *then* reset auto-commit |
| **E** | Path 2, from `TransferFailedException` back to `TransferDemo` | The raw `SQLException` tells the caller your constraint, table, and column names | `atomic`: the overdraft's two output lines | Log the detail internally; give the caller a domain exception with a safe message |
| **F** | Path 3, from `psql` to `accounts` | Another session reads the data mid-transfer | `visibility`: 450.00 vs. 500.00 | Uncommitted writes are invisible to other sessions (`READ COMMITTED`) |

Close the whiteboard with a prediction: *"If we crash at A on path 1, what
will the total be?"* Write the guesses up, then run `naive` as step 1 below.

---

## Timed Facilitation Flow

| Step | Scenario | Time | Notes |
|---|---|---|---|
| 0. Whiteboard | none (diagram) | 10 min | See "Whiteboard Discussion" above. End on a prediction for step 1. |
| 1. Break the ledger | `naive` | 5 min | Run the happy path and ask *"Is this code correct?"* Most learners will say yes. Then run the crash and point to $525.00 of $600.00 (points A and B). |
| 2. Find the boundary | none (read code) | 3 min | Open `TransferService.java`. Ask learners where the transaction should start and end, and what has to happen on failure, **before** you show `transfer(...)`. Let them commit to an answer first. |
| 3. Fix it | `atomic` | 7 min | Show that `naiveTransfer` and `transfer` share `writeTransfer(...)`: only the boundary differs. Run the crash, then the overdraft (point C), then the happy path. For the overdraft, compare the `WARNING` log line with the `caller sees` line (point E). |
| 4. Visibility | `visibility` | 5 min | Pause mid-transfer and query from `psql` (point F). Naive: $50 is visibly missing. Atomic: nothing has changed yet. |
| 4a. Optional: row locks | `visibility` + `psql` | 3 min | Cut this first if time is short. See "Row-lock extension" below. |

Each step works on its own, so you can also split the steps across a longer
lecture: run step 1 while motivating atomicity, steps 2 and 3 while teaching
commit and rollback, and step 4 while introducing isolation.

---

## Teaching Notes & Common Misconceptions

### "A crash between two writes is an edge case"

It isn't. Anything that can interrupt execution at the wrong moment is a
realistic trigger: a network blip, a garbage-collection pause, a deploy
restart, a thrown exception. In step 1, point out that the exception *did*
reach the caller, yet the database is still wrong. Knowing that something
failed doesn't undo the damage.

### Rollback undoes the whole transaction, across every table

The overdraft in step 3 is the key moment. Postgres rejects write #2 (the
debit) with a real `CHECK` constraint violation, but write #1 (the `transfers`
header) had **already succeeded**. After the rollback, that row is gone too.
Rollback isn't "undo the statement that failed." It's "undo everything since
BEGIN."

### Report the failure, don't leak it

The overdraft prints two lines on purpose. The `WARNING` line is the internal
log, on stderr: it names the SQLState, the constraint, and the failing row,
which is exactly what whoever is on call needs. The `caller sees` line is the
`TransferFailedException` message, and it's all a user or API client would
get. Ask learners what an attacker learns from `violates check constraint
"chk_balance_non_negative"` (your constraint and table names, plus a hint about
your rules) compared with "could not be completed". Leaking database errors to
the caller is a recognized weakness (CWE-209, error messages containing
sensitive information).

Two details worth pointing out in `transfer(...)`:
- The `catch (SQLException e)` sits *outside* the try-with-resources, so by
  the time it runs, `finally` has already rolled back and the connection is
  closed. The catch handles reporting, never rollback. That split is the
  point of learner question 1.
- The original `SQLException` is kept as the exception's *cause*, so code that
  needs to diagnose the failure still can. It's never in the message.

The simulated crash is an `IllegalStateException`, not a database failure, so
it isn't translated. It propagates as it is, after the rollback.

### Keep the boundary tight

Only the writes that have to succeed together belong inside the transaction.
A useful contrast for learners: folding an unrelated audit-log insert into the
same transaction means an audit failure blocks a legitimate transfer.

### Isolation stays conceptual

Step 4 makes one point: *nobody sees half a transfer.* Postgres's default
isolation level, `READ COMMITTED`, never shows uncommitted data to other
sessions, so there are no dirty reads. Resist turning this into a tour of
isolation levels. The goal is for learners to become suspicious of shared
state, not to memorize the levels.

### Row-lock extension (optional)

During the atomic pause, run this in Terminal B:

```sql
UPDATE accounts SET balance = balance + 1 WHERE account_id = 'ACC-1';
```

It **blocks** until Terminal A commits, because Postgres holds a row lock on
ACC-1. When it finishes, the +1 is applied to the committed balance, so
nothing is lost.

Frame this carefully. It works only because the read and the write happen
together in a single `UPDATE`. A classic **lost update** is a *read-then-write*
pattern: `SELECT` the balance, compute the new value in Java, then
`UPDATE ... SET balance = <computed>`. A row lock doesn't prevent that, because
both sessions read the old balance before either write took the lock. Present
the single-statement case as the contrast that makes the read-then-write
danger clear.

---

## Common Learner Questions

1. **Q: `transfer(...)` already has a `catch (SQLException e)`. Why is the
   rollback in `finally` instead of in that catch?**
   A: A `catch` only covers the exception types it names. The simulated crash
   is an `IllegalStateException`, so `catch (SQLException e)` would never run
   its rollback, but the `finally` block still would. Under the JDBC contract,
   calling `setAutoCommit(true)` in the middle of a transaction **commits** the
   pending writes. So the narrow catch doesn't just skip the cleanup: it
   quietly commits the header, the debit, and the debit entry, which is exactly
   the broken ledger from the `naive` scenario, and it does this with code that
   *looks* transactional.

   The demo tracks whether `commit()` succeeded and rolls back in `finally` if
   it didn't. That covers every way out of the method, including an `Error`
   such as an `AssertionError`, and the original exception keeps propagating,
   so the caller still knows the transfer failed. Point out the order inside
   `finally`, too: rollback *first*, then `setAutoCommit(true)`. Swap them and
   the partial writes get committed.

   A good exercise: have learners make this exact change to `transfer(...)`,
   predict what the `atomic` crash leaves in the ledger, and then run
   `mvn -q exec:java "-Dexec.args=atomic"`:

   ```java
   try {
       long transferId = writeTransfer(connection, fromId, toId, amount, afterDebit);
       connection.commit();
       return transferId;
   } catch (SQLException e) {          // rollback moved here, and narrowed
       connection.rollback();
       throw e;
   } finally {
       connection.setAutoCommit(true); // stays: this is what commits the partial writes
   }
   ```

   Remove the `committed` flag and its `if`, but keep the `finally` with
   `setAutoCommit(true)`. That's the call that does the damage. Leave the
   outer `catch` that logs and throws `TransferFailedException` as it is. After the
   crash, the ledger shows **$525.00 of $600.00: 1 transfer, 1 ledger entry,
   1 unbalanced transfer.** Two things to point out:
   - The overdraft still rolls back correctly, because it *is* an
     `SQLException`. The narrow catch works for the failure its author
     expected and fails silently for the one they didn't.
   - The snapshot labels are fixed text, so the one after the crash still
     says "rollback undid...". Trust the numbers, not the label. That's a
     small lesson of its own about logging that asserts success.

   Have learners restore the original code afterwards.

2. **Q: Can I catch the error and carry on with the rest of the transaction?**
   A: Not in Postgres. Once any statement fails, the whole transaction is
   marked aborted, and every later statement fails with `current transaction
   is aborted, commands ignored until end of transaction block` until you roll
   back. Some embedded databases, such as H2, are more forgiving, which is one
   reason to demo against the real engine.

3. **Q: Why is the successful transfer's ID 3, not 1?**
   A: Run `SELECT transfer_id FROM transfers;` after the `atomic` scenario to
   show it. Sequences aren't rolled back, so the crash and the overdraft each
   used up an ID. IDs are unique but not gap-free, so never treat a gap as
   evidence of a missing record.

4. **Q: Why reset auto-commit in `finally` if the connection is closed right
   after?**
   A: Habit worth building. With a pool, "closed" means "returned for reuse,"
   and a connection left in manual-commit mode silently changes the behavior
   of the next caller's code.

5. **Q: Does using a transaction make the SQL safe from injection?**
   A: No. They're separate defenses. Every write here also uses
   `PreparedStatement` with bound parameters.

---

## Facilitation Tips

- **Rehearse once end to end** with `mvn -q exec:java`. A demo that works
  first time is what makes the $525.00 moment land.
- **Prepare before class:** `docker compose up -d --wait` and `mvn -q compile`.
  The compile step downloads the JDBC driver, so don't leave it for classroom
  Wi-Fi. No Docker on the teaching machine? Follow the README's "Option B" to
  use a locally installed PostgreSQL instead, and do the one-time setup
  (creating the role and database) before class, not live.
- **Teaching remotely:** step 4 needs two terminals. Tile them side by side in
  one shared window before you start, or have a co-presenter run Terminal B.
- **If you're short on time,** cut step 4a first, then shorten step 2 to a
  single question. Keep steps 1 and 3 intact; they carry the core idea.
- **Afterwards:** `docker compose down`.

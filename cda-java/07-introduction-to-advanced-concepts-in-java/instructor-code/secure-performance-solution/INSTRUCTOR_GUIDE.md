# Instructor Guide: Advanced Java for Performance, Securely (Code-Along)

**Lesson:** Module 1: Advanced Java Security, Lesson 4
**Duration:** 2 hours (120 minutes), following the lesson's Timed Facilitation Flow
**Audience:** adult learners who have finished Lessons 1 to 3 (atomic
transfers, field encryption, the secure repository pattern), and who can read
basic Java streams and SQL.

> **Instructor-only.** This file is the answer key. It lives only in
> `secure-performance-solution`. Never copy it into the starter.

See [`README.md`](README.md) for setup and troubleshooting. The lesson quiz
and its answer key (`lesson-04-quiz-answer-key.pdf`) ship with the course
content, not with this project. Don't give the answer key to learners. Learners get
`secure-performance-starter`. This folder is the finished solution.

Whiteboard diagrams for every segment are in [`diagrams.md`](diagrams.md).

---

## Learning Outcomes

By the end of the code-along, learners can:

- Use buffered, streamed I/O to process large data without exhausting memory.
- Manage resources with try-with-resources so streams and connections never
  leak, and explain a leak as an availability (DoS) risk.
- Identify how unbounded input enables DoS, and bound it defensively by
  rejecting, not truncating.
- Apply safe-concurrency practices (immutable values and a thread-safe
  counter), and verify them with a real concurrent test instead of
  single-threaded inspection.

---

## What Learners Build

Learners change three classes. Each starts as a working first draft that is
fine on small data:

| Class | Starts with | Ends with | Steps |
|---|---|---|---|
| `log/TransactionLogExporter` | `Files.readAllLines`; `Files.lines(...).collect(toList())`; a reader and writer that are never closed | `Files.lines` in try-with-resources, a lazy pipeline, the reader and writer in one try-with-resources header | 1–4 |
| `repository/TransactionRepository` | Raw JDBC that never closes its `Connection`; `findRecent(limit)` with no bound | try-with-resources on `Connection`, `PreparedStatement`, `ResultSet`; `MAX_PAGE_SIZE` with `Validate.between` | 5–6 |
| `log/ProcessingStats` | `private volatile int linesProcessed` with `+=` | `AtomicLong` with `addAndGet` | 7 |

Seven `TODO: Step N of 7` comments walk learners to the solution. In this
folder, the same comments read `TODO: (Completed) Step N of 7`, sitting next
to the code that finishes each step.

Everything else is finished and identical in both folders:

| File | Why learners look at it |
|---|---|
| `log/LogFiles` | `generate` and `countDataLines` are finished try-with-resources models. `countDataLines` runs the **File check** after every export (Step 4). |
| `console/ConcurrencyCheck` | Menu 8: 8 threads × 250,000 updates on one shared `ProcessingStats`, released together by a `CountDownLatch`. Its `ExecutorService` is itself closed by try-with-resources (Java 19+). |
| `application.properties` | A **5-connection** Hikari pool, a 3-second wait, and a 2-second leak-detection threshold, so Step 5's leak shows up in five calls. |
| `.run/LedgerDemoApp.run.xml` | The shared IntelliJ run configuration: `-Xmx128m` and `$PROJECT_DIR$` as the working directory. |
| `schema.sql` | Seeds **1,000,000** transactions, so Step 6's unbounded request really is too big. |
| `Validate`, `DataAccessFailure`, `AccountRepository`, `TransferService` | Carried over from Lesson 3. `Validate` gained `between(...)`; `Validate` and `DataAccessFailure.logged` are now public, so the exporter in the `log` package can use them too. |

### Why the counter is `volatile int`, not plain `int`

The lesson IG says "a plain `int`." With a plain `int`, HotSpot's JIT compiler
optimizes menu 8's hot loop after one or two runs, and the lost updates stop
showing up. In testing, the first two runs lost updates, and every later run
reported exactly 2,000,000. A demo that stops failing undercuts the lesson.
`volatile int` stops that optimization, and it lost updates on every one of
16 test runs. It is still not atomic, so the lesson's point holds, and it adds
a second one: **`volatile` gives visibility, not atomicity.** If a learner
asks, "then does a plain `int` work?", the answer is: *it passed our test,
which proves nothing.* This is the same point as reflection question 3.

---

## Before Class

- [ ] Run `schema.sql` against `ledger_demo` in pgAdmin (README setup step 2).
      It takes 10 to 30 seconds. Check `SELECT count(*) FROM ledgercore.transactions;`
      shows `1000000`.
- [ ] Create `.env` in **the starter** (README setup step 3).
- [ ] Open **the starter** in IntelliJ and let Maven finish downloading. Don't
      leave the first download for classroom Wi-Fi.
- [ ] Run **LedgerDemoApp** from the top right. Confirm the menu title says
      `max heap about 128 MB`.
- [ ] Choose **5** and press Enter to generate the 3,000,000-line log (about
      200 MB, a few seconds). Then choose **6** and confirm it crashes with
      `The demo stopped: java.lang.OutOfMemoryError: Java heap space`. The IG
      is explicit: *a demo that doesn't crash on cue undercuts the lesson's
      opening argument.*
- [ ] Open **JConsole** (`jconsole.exe` in your JDK's `bin` folder, next to
      `java.exe`). Practise connecting it to a running `demo.ledger.LedgerDemoApp`
      and opening the **Memory** tab. You use it in Segments 1 and 2.
- [ ] Rehearse Step 5 (the leak), and Step 4's File check mismatch, in the
      starter.
- [ ] Keep this folder open in a second IntelliJ window as your answer key.
- [ ] Check that learners have about 1 GB of free disk space.

---

## Timed Facilitation Flow

The segments and times follow the lesson's Timed Facilitation Flow. In this
code-along, learners write the code **during** segments 1 to 4, so the
25-minute activity slot is used for an independent extension exercise.

| Segment | Time | Steps | Notes |
|---|---|---|---|
| Performance and Safety Are the Same Problem, Not a Tradeoff | 25 min | 1 | Two circles on the board, then the live `OutOfMemoryError`. Ask "who did this to us?" |
| Buffered and Streamed I/O for Large Data | 25 min | 2–3 | JConsole heap graph, the laziness prediction, then the `.collect(...)` trap. |
| Resource Leaks as an Availability Risk | 25 min | 4–5 | The broken `finally` on the board, then the File check mismatch and the pool exhaustion. |
| Bounding Input and Safe Concurrency Under Load | 15 min | 6–7 | "Same failure, different entry point," then lost updates live. Keep it tight. |
| Activity: Streaming and Bounding the Transaction Log | 25 min | Extension | Learners add `findAtLeast(...)` alone, applying all four ideas. Use it as catch-up time too. |
| Wrap-Up & Reflection | 5 min | none | Close on how to *verify*, not assume, correctness under load. |
| **Total** | **120 min** | | |

**If you run short on time,** cut the extension first and use the slot for
catch-up. Never cut Step 1 or Step 5. The crash and the dead pool are the
moments learners remember.

---

## Segment 1: Performance and Safety Are the Same Problem (25 min)

### Whiteboard (5 min)

Draw two separate circles labeled **FAST** and **SAFE**, and ask the room
which one they'd give up under deadline pressure. Then erase both and redraw
**one** circle labeled **STREAMED**:

```
   ( FAST )   ( SAFE )      →        (  STREAMED  )
   "pick one"                         bounded memory,
                                      closed resources,
                                      bounded requests
```

The reframe: availability is the **A** in CIA. Code that exhausts memory,
connections, or file handles is a denial of service, even when nobody is
attacking it.

### Step 1: Reproduce the failure (15 min)

Everyone runs **LedgerDemoApp** from the top right and checks the heap in the
menu title. Walk the room: anyone who sees a number much bigger than 128
started the app another way (README → "The heap isn't 128 MB").

**Run it first:** menu **5** (Enter for 3,000,000 lines), then menu **6**.
Before learners press Enter on 6, have them predict the result. Then:

```
The demo stopped: java.lang.OutOfMemoryError: Java heap space
Full details: logs/ledger-demo.log
```

If you have JConsole attached, the heap line climbs straight to the 128 MB
ceiling just before the crash.

Ask: **"Who did this to us?"** Draw out that nobody did. The log grew. That's
the whole threat model for this lesson: the input is legitimate, and it's
still a DoS.

**Pre-empt "but my data is always small":** LedgerCore's transaction log only
ever grows. Code that is correct on today's file is a liability tomorrow, not
a finished design.

There's no code to write. The TODO's last line asks which part of CIA failed:
**Availability**.

### Discussion (5 min)

Return to the circles. Ask: *"Which fix would make this app both faster and
safer at the same time?"* Let learners guess before you name streaming. Then
ask where else in LedgerCore something grows without limit. Collect answers
on the board: the transactions table, the number of callers, and open
connections. Those are Steps 5 to 7, so leave the list up.

---

## Segment 2: Buffered and Streamed I/O (25 min)

### Step 2: Stream `summarize` (12 min)

**Prediction first:** before anyone runs the fix, ask: *"In
`lines.map(...).reduce(...)`, does `map` run on all 3,000,000 lines first,
and then `reduce`? Or does each line go all the way through before the next
is read?"* Most learners guess the first. The answer is the second: streams
are **lazy** and pull one element at a time through the whole pipeline.
That's why memory stays flat.

**Solution:**

```java
try (Stream<String> lines = Files.lines(log, StandardCharsets.UTF_8)) {
    LogSummary summary = lines.skip(1)                  // the header line
            .map(LogSummary::ofLine)
            .reduce(LogSummary.EMPTY, LogSummary::plus);
    stats.recordLines(summary.transactions());
    return summary;
} catch (IOException | UncheckedIOException ex) {
    throw DataAccessFailure.logged("summarize transaction log", "Could not read the transaction log.", ex);
}
```

`Stream` is already imported in the starter. Rerun menu 6:

```
3,000,000 transactions, totaling 1,501,…
Log lines processed since startup: 3,000,000
```

The total differs per learner, because the log is random.

**With JConsole attached:** the eager version climbed to the ceiling and
crashed. The streamed version stays in a flat, saw-tooth band well under
128 MB. Let the graph do the arguing.

**Why `UncheckedIOException` in the catch:** a stream can't throw a checked
`IOException` from inside `map`, so a read error part-way through the file
arrives as `UncheckedIOException`. It's already in the starter's catch.

### Step 3: The `.collect(...)` trap in `exportForAccount` (13 min)

**Run it first:** menu **7** with `ACC-00000001`. The starter already uses
`Files.lines`, and it still crashes with `OutOfMemoryError`.

Ask: *"We switched to the streaming API. Why didn't it help?"* Point at the
end of the pipeline. `collect(Collectors.toList())` puts every matching line
(about 1,200,000, which is 40% of the log) in one `List`. **The last
operation in the chain decides whether memory was actually saved.** This is
the most common bug in this lesson. Name it explicitly.

**Solution** (inside the existing `try`; Step 4 then moves it into the
try-with-resources header):

```java
Iterator<String> matches = Files.lines(log, StandardCharsets.UTF_8)
        .skip(1)
        .filter(line -> involves(line, accountId))
        .iterator();
BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8);
writer.write(LogFiles.HEADER);
writer.newLine();
long written = 0;
while (matches.hasNext()) {
    writer.write(matches.next());
    writer.newLine();
    written++;
}
stats.recordLines(written);
return new ExportResult(out, written);
```

`Iterator` is already imported in the starter.

**Why `iterator()` and not `forEach`?** `writer.write` throws the checked
`IOException`, and a lambda passed to `forEach` can't throw it. `iterator()`
pulls one line at a time, just like `forEach`, so it's still lazy, and the loop
body can throw normally.

**Watch for:**

- `.toList()` instead of `.collect(Collectors.toList())`. It's the same trap
  with a shorter name.
- Learners who keep the `List<String> matches` and just delete `collect`. That
  won't compile, which is a useful moment: *a `Stream` isn't a collection.*

After Step 3, menu 7 no longer crashes. It does report a line count that
doesn't match the File check, which leads into Segment 3.

---

## Segment 3: Resource Leaks as an Availability Risk (25 min)

### Whiteboard: the broken `finally` (7 min)

Before try-with-resources (Java 7), this was the "correct" way to close
resources. Put it on the board and walk it line by line:

```java
BufferedReader reader = null;
BufferedWriter writer = null;
try {
    reader = Files.newBufferedReader(log);
    writer = Files.newBufferedWriter(out);
    // ... copy lines ...
} finally {
    if (reader != null) {
        reader.close();     // if this throws ...
    }
    if (writer != null) {
        writer.close();     // ... this never runs, and the writer leaks
    }
}
```

1. **One failed `close()` skips the rest.** If `reader.close()` throws,
   `writer.close()` never runs. Getting this right takes a nested
   `try`/`finally` for each resource.
2. **`close()` in `finally` can hide the real error.** If `write` throws
   "disk full" and then `close()` throws too, the `close()` exception
   replaces the original, and the log records the wrong cause.
3. **It's easy to forget,** and nothing warns you.

The lesson IG's first point is that "the null check doesn't cover a throw
before assignment." This guide leaves it out on purpose: when the variable
starts as `null`, the null check *does* handle a throw before assignment.
The real weaknesses are points 1 and 2.

try-with-resources fixes all three. It closes every resource in reverse
order, even when one `close()` fails. A `close()` failure is attached to the
original exception as a **suppressed** exception, so it can't replace it.

### Step 4: Close the reader and the writer (8 min)

**Run it first** (Step 3 done): menu **7** with `ACC-00000001`:

```
Exported 1,200,152 transactions to data\export-ACC-00000001.csv
File check: 1,199,921 transactions on disk. MISMATCH: data was lost.
```

A few hundred lines are missing, and the number varies. The `BufferedWriter`
holds its last few thousand characters in memory until `close()` flushes
them, and nothing ever calls `close()`. *The code reported success, and the
file is wrong.* That's why the menu checks the file itself.

**Solution:**

```java
try (Stream<String> lines = Files.lines(log, StandardCharsets.UTF_8);
     BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
    writer.write(LogFiles.HEADER);
    writer.newLine();
    Iterator<String> matches = lines.skip(1)
            .filter(line -> involves(line, accountId))
            .iterator();
    long written = 0;
    while (matches.hasNext()) {
        writer.write(matches.next());
        writer.newLine();
        written++;
    }
    stats.recordLines(written);
    return new ExportResult(out, written);
} catch (IOException | UncheckedIOException ex) {
    throw DataAccessFailure.logged("export transaction log", "Could not export the transaction log.", ex);
}
```

Rerun the same export. Now the two counts match:

```
Exported 1,200,152 transactions to data\export-ACC-00000001.csv
File check: 1,200,152 transactions on disk. OK
```

The exact count differs per learner, because each learner's log is random.

**The other leak in the same method.** The `Files.lines` stream was never
closed either, so every export leaked an open handle on the log file. This is
IG common question 3: *`Files.lines(path).forEach(...)` leaks a file handle,
even though nothing looks wrong.*

> **Don't promise a live file-handle demo on Windows.** The IG suggests
> watching `lsof -p <pid> | wc -l` climb until "Too many open files." That
> doesn't reproduce here. Windows allows millions of handles per process, and
> when the garbage collector runs, the JVM quietly closes file channels
> nobody references anymore. In testing, the Java process's handle count
> stayed flat across six leaking exports. Use that as a teaching point: *the
> garbage collector sometimes cleans up after you, so this leak hides in
> testing and shows up under load.* A connection pool has no such safety net,
> which is Step 5. On macOS or Linux, `lsof -p <pid> | grep transaction-log`
> can show open handles if you want to try it.

### Step 5: The leaked connection, `totalSentBy` (10 min)

`totalSentBy` uses plain JDBC. The TODO's story is that it was written before
the team adopted `JdbcClient`. Point out that the SQL is fine: it's
parameterized, and the input is validated. Only the lifecycle is broken.

**Run it first:** menu **4** with `ACC-00000001`, five times. Each one works.
Then a sixth time, then menu **1**, then a transfer (menu **2**,
`ACC-00000004` → `ACC-00000005`, `10.00`):

```
Account ID: Error: Could not total that account's transactions. (ref c3ffb7c4)
Error: Could not load accounts. (ref 92e87941)
...
The demo stopped: java.sql.SQLTransientConnectionException: HikariPool-1 -
  Connection is not available, request timed out after 3001ms (total=5, active=5, idle=0, waiting=0)
```

Each failing call pauses for 3 seconds first: that's the pool's wait. Read
`active=5, idle=0` aloud: every connection is checked out, and nobody is
using any of them. Then open `logs/ledger-demo.log` and search for
**`Connection leak detection triggered`**. Hikari logged all five, each with
a stack trace pointing at `totalSentBy`.

Tie it back to the IG: *a leaked pool slot means every other user's
legitimate transfer starts failing.* Menu 4 didn't break, it broke **menu 1
and the transfer**.

> **Why the app crashes on the transfer:** `@Transactional` asks for a
> connection before the method body runs, so the failure is Spring's
> `CannotCreateTransactionException`, not a `DataAccessException`. No
> repository ever sees it, and the menu doesn't catch it. Use it: an
> availability failure doesn't stay where the bug is.

**Solution:**

```java
try (Connection conn = dataSource.getConnection();
     PreparedStatement ps = conn.prepareStatement(
             "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE from_account = ?")) {
    ps.setString(1, accountId);
    try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return rs.getBigDecimal(1);
    }
} catch (SQLException ex) {
    throw DataAccessFailure.logged("total sent by account", "Could not total that account's transactions.", ex);
}
```

Learners restart the app (the leaked connections are freed only when the app
stops), then run menu 4 six or more times, then a transfer. All work.

**Why is `ResultSet` in a nested `try`?** `ps.setString` has to run between
preparing the statement and executing it, so `rs` can't be declared in the
same header. Closing the `Connection` does close its statements and result
sets, but closing each resource yourself is the habit that stays correct
with any driver or pool.

**Watch for:**

- `conn.close()` added as the last line before `return`. It works until the
  query throws, then leaks again. Ask: *"What happens if `executeQuery`
  fails?"*
- Only `conn` in the header, with `ps` and `rs` left outside. It works here,
  but press for the habit.

---

## Segment 4: Bounding Input and Safe Concurrency (15 min)

Keep this segment tight. It exists to set up the extension, where learners
apply all four ideas together.

### Step 6: Bound `findRecent` (7 min)

**Run it first:** menu **3** with `5` works. Then `10000000`:

```
The demo stopped: java.lang.OutOfMemoryError: Java heap space
```

Say it the IG's way: **same failure, different entry point.** The value is
bound as a parameter, so this isn't injection. The caller simply demanded
more than the app can hold, and nothing said no.

Also try `-1` in the starter: `Error: Could not load transactions. (ref …)`.
PostgreSQL rejects a negative `LIMIT`, so the database is doing a check the
repository should have done.

**Solution:**

```java
public static final int MAX_PAGE_SIZE = 100;
...
Validate.between(limit, 1, MAX_PAGE_SIZE, "Show between 1 and " + MAX_PAGE_SIZE + " transactions at a time");
```

The constant goes at the top of the class, and the `Validate.between` call
goes before the `try`, as in Lesson 3. The Step 6 TODO in `findRecent` asks
for both. The starter has no separate class-top TODO, and `totalSentBy` sits
above `findRecent`, so the TODO window lists Step 5 before Step 6.

**Reject, don't truncate** (IG question 4). Someone will propose
`Math.min(limit, MAX_PAGE_SIZE)`. Ask: *"A caller asked for 500 and got 100.
How do they know?"* They don't. Truncation hides the limit and looks like a
data-loss bug. A clear error tells the caller the rule.

### Step 7: The shared counter (8 min)

**Run it first:** menu **8**, three times (the TODO says so too):

```
8 threads x 250,000 updates
Expected: 2,000,000
Actual:   481,822
LOST 1,518,178 updates.
```

The numbers change every run. Draw the lost update on the board:

```
Thread A: read 41 ───────────── add 1 → write 42
Thread B:        read 41 ── add 1 → write 42        (one update lost)
```

Point at the TODO's note that the field is **already `volatile`**. `volatile`
makes every read and write visible to other threads. It doesn't make the
read-add-write sequence one step.

**Solution:**

```java
private final AtomicLong linesProcessed = new AtomicLong();

public void recordLines(long count) {
    linesProcessed.addAndGet(count);
}

public long linesProcessed() {
    return linesProcessed.get();
}
```

`AtomicLong` is already imported in the starter. Rerun menu 8 several times:
`OK: no updates were lost.` every time.

**The bonus question** (in the TODO): an `int` tops out at 2,147,483,647.
At 3,000,000 lines per summary, it overflows after about 715 summaries, and
the counter goes negative. `AtomicLong` fixes both problems.

**Immutability, the other safe-concurrency practice** (IG outcome 4): point
at `LogSummary`. It's a record, so it's immutable. `reduce` never changes a
summary; `plus` builds a new one. Nothing shared is ever written, so there's
nothing to race on. The counter needs `AtomicLong` only because it really is
shared state that changes.

**Alternative learners may suggest:** `synchronized` on both methods. That's
correct too (IG question 5), as long as **both** the write and the read are
synchronized. `AtomicLong` is simpler for a single counter.

---

## Activity: Find Large Transactions in the Log (25 min)

Learners add a method **on their own** and wire it to a new menu option. The
task and test inputs (not the solution) are in the learners' README under
"Extension: find large transactions in the log." Put the task on the board
too:

> Add `findAtLeast(BigDecimal minAmount, int maxResults)` to
> `TransactionLogExporter`, and menu option **9) Find large transactions in
> the log** to `ConsoleMenu`. It must validate both inputs, stream the log in
> try-with-resources, stop at `maxResults`, handle file failure with
> `DataAccessFailure`, and record the lines it returns in `ProcessingStats`.

**Test inputs** (all verified against this solution):

| Minimum amount | How many | Expected |
|---|---|---|
| `990.00` | `5` | Five log lines, each 990.00 or more |
| `990.00` | `1000` | `Error: Show between 1 and 100 transactions at a time` |
| `5000.00` | `10` | `No transactions found.` |
| `-1` | `5` | `Error: Amount must be greater than zero` |

**Solution**, in `TransactionLogExporter` (add `import java.math.BigDecimal;`
and `import java.util.List;`):

```java
/** The first log lines with an amount of at least minAmount, up to maxResults of them. */
public List<String> findAtLeast(BigDecimal minAmount, int maxResults) {
    Validate.amount(minAmount);
    Validate.between(maxResults, 1, 100, "Show between 1 and 100 transactions at a time");
    Path log = LogFiles.requireLog();
    try (Stream<String> lines = Files.lines(log, StandardCharsets.UTF_8)) {
        List<String> found = lines.skip(1)
                .filter(line -> LogSummary.ofLine(line).total().compareTo(minAmount) >= 0)
                .limit(maxResults)
                .toList();
        stats.recordLines(found.size());
        return found;
    } catch (IOException | UncheckedIOException ex) {
        throw DataAccessFailure.logged("search transaction log", "Could not search the transaction log.", ex);
    }
}
```

In `ConsoleMenu`, add `import java.util.List;`, add
`9) Find large transactions in the log` to the menu text, and this case before
`default` (then change the default message to "0-9"):

```java
case "9" -> {
    List<String> found = exporter.findAtLeast(
            parseAmount(prompt("Minimum amount")), parseCount(prompt("How many (1-100)")));
    if (found.isEmpty()) {
        System.out.println("No transactions found.");
    }
    found.forEach(System.out::println);
}
```

**The README's hint question:** *why is `.toList()` safe here when it was the
bug in Step 3?* Because `limit(maxResults)` bounds it: the list can never
hold more than 100 lines. Collecting is not the bug. Collecting an
**unbounded** stream is. `limit` also **short-circuits**: with `990.00`, `5`,
the pipeline stops after a few hundred lines instead of reading 200 MB. Ask
early finishers to time `990.00`, `5` against `5000.00`, `10`, which has to
read the whole file to find nothing.

**Circulate for:**

- `Validate` calls placed after `Files.lines` opens the file. It's not a leak
  here, because try-with-resources still closes the file, but the check
  should run before any resource is opened.
- `.limit` placed **before** `.filter`. It compiles, but it returns only
  matches among the first N lines of the file.
- `LogSummary.ofLine(line).total()` compared with `>=`. `BigDecimal` needs
  `compareTo`.

---

## Wrap-Up & Reflection (5 min)

1. The eager `summarize` passed every test on a small file. What would have
   caught it before production?
2. Step 5's bug was in menu 4, but menu 1 and the transfer were what failed.
   Why does a resource leak show up somewhere other than where the bug is?
3. Menu 8 passed after your fix. How do you know it's correct, and not just
   lucky this run?

Close on question 3, the IG's closing theme: **verify, don't assume.** A
single-threaded test can't show a race. The concurrent check can show that a
race exists, but a pass on one run isn't proof. That's why the fix uses a
type whose guarantee is documented (`AtomicLong`), instead of code that
merely passed a test. The plain-`int` story above is the same lesson in
miniature.

Bridge to the next lesson: the streamed, bounded log reader built here is one
more method that will move into the secure repository from Lesson 3. The
module's four disciplines (atomic transactions, encryption, the repository
boundary, and availability) make up one component, not four.

---

## Common Learner Questions

1. **Q: My code runs fine on my test file. Why would production behave any
   differently?**
   A: Because the test file is small. LedgerCore's log only ever grows. Code
   that works on today's file is a failure that simply hasn't happened yet.

2. **Q: I switched to `Files.lines()`, but I still call
   `.collect(Collectors.toList())` at the end. Is that still lazy?**
   A: No. The last operation decides whether memory was saved. Collecting an
   unbounded stream brings back the whole-file-in-memory problem (Step 3).

3. **Q: Do I need try-with-resources around `Files.lines()` if I chain
   `.forEach()` right after it?**
   A: Yes, always. `Files.lines()` opens a file handle and returns an
   `AutoCloseable` stream. A bare `Files.lines(path).forEach(...)` leaks that
   handle, even though nothing looks wrong.

4. **Q: If I bound the input, should I truncate an over-limit request so it
   still returns something?**
   A: No. Reject it with a clear error. Truncating hides the limit and looks
   like a data-loss bug.

5. **Q: My counter looks correct with one thread. Is an `int` good enough?**
   A: No. `+=` is read, add, write. Two threads can read the same value and
   one update is lost. Use `AtomicLong`, or `synchronized` on both the read
   and the write. Verify with a concurrent test, like menu 8.

6. **Q: The field was `volatile`. Isn't that thread-safe?**
   A: `volatile` means every thread sees the latest value. It does not make
   `+=` one step. Menu 8 lost updates with it.

7. **Q: Doesn't the garbage collector close things for me?**
   A: Sometimes, eventually, for some resources. That's why the file-handle
   leak hides in testing. A connection pool keeps its connections referenced,
   so the garbage collector never frees them (Step 5). Relying on the garbage
   collector is relying on luck.

8. **Q: Why is `limit` safe in the extension but not in `findRecent`'s
   `LIMIT :limit`?**
   A: Both are fine once **you** choose the bound. The bug in `findRecent` was
   that the **caller** chose it, with no maximum.

9. **Q: Doesn't the PostgreSQL driver stream rows too?**
   A: Not by default. It reads the whole result into memory before your code
   sees the first row. That's another reason the bound in Step 6 belongs in
   the repository. (True row streaming needs a fetch size inside a
   transaction. That's out of scope today.)

---

## Troubleshooting During Class

The README covers setup errors (Maven reload, JDK, `.env`, the heap setting,
the connection). Problems specific to the code-along steps:

| Symptom | Cause | Fix |
|---|---|---|
| Step 1 doesn't crash | The app isn't running with `-Xmx128m` (the menu title shows a big heap), or the log is small | Use the **LedgerDemoApp** run configuration (README, "The heap isn't 128 MB"). Regenerate the log with menu 5 and Enter. |
| Greyed-out imports in the starter | Imports the steps need are included up front | Expected. Don't run **Optimize Imports**. If one was removed, **Alt+Enter** on the red name → **Import class**. |
| `unreported exception IOException` inside a lambda | `writer.write` used inside `forEach` | Use the `iterator()` loop from Step 3. |
| `incompatible types: Stream<String> cannot be converted to List<String>` | `collect` deleted but `List<String> matches` kept | Declare `Iterator<String> matches = ... .iterator();` |
| Export still shows MISMATCH after Step 4 | The writer is declared inside the `try` block, not in the `try (...)` header | Both resources go in the header, separated by `;`. |
| Step 5 still fails after the fix | The app wasn't restarted, so the old leaked connections are still checked out | Stop and rerun the app. |
| `cannot find symbol: MAX_PAGE_SIZE` | The constant from Step 6 wasn't added | Add `public static final int MAX_PAGE_SIZE = 100;` at the top of `TransactionRepository`, above `TRANSACTION`. |
| Menu 8 still loses updates after Step 7 | The field is still an `int`, or `recordLines` reads with `get()`, adds, then writes back with `set(...)`, which is the same three steps | Use one call: `linesProcessed.addAndGet(count)`. |
| Out of disk space | Each export writes about 85 MB | Delete the `data` folder's `export-*.csv` files. |

---

## Facilitation Tips

- **Let the crash speak before you explain it.** Step 1 is the lesson's
  strongest moment. Run it, wait, and ask "who did this to us?" before saying
  anything about memory.
- **Have JConsole attached before class starts,** so the heap graph doesn't
  eat into topic time. JConsole's process list shows the app as
  `demo.ledger.LedgerDemoApp`. Reconnect after each crash.
- **Circulate for the two most common incomplete fixes** (from the IG):
  - a read path that streams correctly but still leaks the writer (Step 4's
    MISMATCH is still showing);
  - a bound that truncates with `Math.min` instead of rejecting.
- **Make "other users" concrete in Step 5.** Tell learners that menu 4 is
  one user and menu 2 is another. The user who ran menu 4 saw nothing wrong
  on their first five tries. The user who made the transfer did nothing wrong
  and still got the failure.
- **Reset if data gets messy:** re-run `schema.sql` (10 to 30 seconds) and
  delete the `data` folder. The next menu 5 regenerates the log.

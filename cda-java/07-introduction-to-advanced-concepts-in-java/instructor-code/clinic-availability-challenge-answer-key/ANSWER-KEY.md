# Riverside Clinic Scheduler: Answer Key (Instructor Only)

Do not distribute. The student project is `../clinic-availability-challenge`.
A fully fixed copy of it is in `fixed-project/` in this folder. Diff the two
folders to see every change.

**Exercise:** Module 1, Lesson 4 individual exercise (graded). Students do
about 15 minutes of setup before the session, then 60 minutes of work: the
README gives them a pacing plan and the rubric. The source lesson gives its
own activity only 25 minutes, so the exact menu inputs in the tickets are
there on purpose, to keep reproduction fast.
**Tests:** the four Lesson 4 outcomes, on an app students haven't seen:
streamed I/O, try-with-resources, bounded input, and thread-safe shared state.

Every proof of concept below was run live against the student project with
`-Xmx32m`, and every fix was checked against `fixed-project/`.

---

## Footprint

The exercise is sized for students' own machines:

| | Size |
|---|---|
| Heap | 32 MB (`-Xmx32m`, set in `.run/ClinicApp.run.xml`). The fixed app runs every feature at 24 MB. |
| `clinic_demo` database | 47 MB, including PostgreSQL's own ~8 MB baseline |
| `data/visit-log.csv` | 22 MB (400,000 visits) |
| Largest export (all of 2025) | 14 MB |

---

## Findings

**Core** findings are the six that match a support ticket. A complete
submission has all six. Finding 7 has no ticket and is extra credit.

| # | Core | Ticket | Location | Flaw | Proof of concept (student copy, 32 MB) |
|---|---|---|---|---|---|
| 1 | Yes | 1 | `VisitReport.visitsPerClinician` | `Files.readAllLines` loads the whole 400,000-line log into memory | Menu 5 (Enter), then menu 6: `The app stopped: java.lang.OutOfMemoryError: Java heap space` |
| 2 | Yes | 2 | `VisitReport.exportBetween` | `Files.lines(...)` ends in `.collect(Collectors.toList())`, which holds all ~263,000 matching `Visit` records at once | Menu 7, `2025-01-01` to `2025-12-31`: `OutOfMemoryError` |
| 3 | Yes | 3 | `VisitReport.exportBetween` | The `BufferedWriter` is never closed, so its last buffer is never flushed. The `Files.lines` stream is never closed either, so each export leaks a handle on the log | Menu 7, `2025-03-01` to `2025-03-07`: "Exported 5,040 visits." The file has about 4,735 data rows, and the last one is cut off mid-line (e.g. `47215,2025-03-07T13:50,PAT`) |
| 4 | Yes | 4 | `PatientRepository.findById` | Raw JDBC. `conn`, `ps` and `rs` are closed only on the "found" path. The "not found" `return` and any `SQLException` leak the connection. The pool holds 5 | Menu 2 with five IDs that don't exist (e.g. `PAT-09999999`). All five say "No patient with that ID." The sixth lookup of *any* ID, menu 4, and menu 1 all wait 3 s, then fail: `Could not load that patient. (ref …)`, `That action is unavailable right now. (ref …)`, `Could not search patients. (ref …)`. The log shows `active=5, idle=0` and five `Connection leak detection triggered` warnings pointing at `findById` |
| 5 | Yes | 5 | `AppointmentRepository.findUpcoming` | The caller chooses the page size, with no maximum | Menu 3 with `1000000`: `OutOfMemoryError`. Also `-1` gives `Could not load appointments. (ref …)`, because PostgreSQL rejects a negative `LIMIT` |
| 6 | Yes | 6 | `CheckInCounter.recordCheckIn` | `checkIns++` on a shared `volatile int` isn't atomic. `volatile` gives visibility, not atomicity | Menu 8: "6 desks checked in 50,000 patients each" (300,000), but "Check-ins recorded this morning" is far lower, and different on every run. On the test machine it ranged from about 88,000 to 165,000; the range depends on the CPU. Menu 9 is also short |
| 7 | Extra | none | `PatientRepository.searchByName` | No bound on the number of results. A broad search returns every matching row | Menu 1 with `Patient` (matches all 200,000): `OutOfMemoryError`. This is Finding 5 again, from a different entry point, and nobody filed a ticket for it |

### Things that look suspicious but are correct

Don't take marks off a student who leaves these alone. Give credit to a
student who says *why* each one is fine:

- `VisitLogFiles.generate` and `countDataLines` already use try-with-resources.
- In the fixed report, `Collectors.groupingBy(..., counting())` is a
  `collect` that is safe: it holds 6 counters, not 400,000 lines. A student
  who writes "collect is always wrong" has missed the point of Finding 2.
- `nextBookedFor` uses `LIMIT 1`, a bound the code chose rather than the
  caller.
- `findById`'s "found" path does close all three resources. The flaw is the
  other two paths.

---

## Fixes

Exact code from `fixed-project/`.

### One partial-credit rule for every fix

The **Fix** criterion is worth 6 points. Score every finding on this scale:

| Fix points | When |
|---|---|
| 6 | Complete: as shown below, or an accepted alternative |
| 5 | Minor gap that doesn't affect the ticket or the lesson's failure mode (for example, `synchronized` on the write but not the read) |
| 3 | Passes the ticket, but leaves the same failure on another path: a resource that still leaks on an exception or elsewhere in the method, or a caller-chosen size that is truncated instead of rejected |
| 0–1 | Hides the symptom without fixing the cause (raising the heap, catching `OutOfMemoryError`), or doesn't pass the ticket |

Also:

- **Each finding is scored on its own.** A student who closes the stream in
  Finding 1 but not in Finding 3 earns 6 for Finding 1 and 3 for Finding 3.
- **Right fix, wrong explanation:** score the fix on its merits, and give 0–1
  of the 4 **Location and flaw** points.
- **One write-up block for Findings 2 and 3** (both are in `exportBetween`) is
  fine. Score both findings from it. Give **Proof** credit for Finding 3 if the
  truncated file is described, even inside the Finding 2 block.

### Finding 1: `visitsPerClinician`

```java
try (Stream<String> lines = Files.lines(log, StandardCharsets.UTF_8)) {
    return lines.skip(1)
            .map(Visit::parse)
            .collect(Collectors.groupingBy(Visit::clinicianId, TreeMap::new, Collectors.counting()));
} catch (IOException | UncheckedIOException ex) {
    throw DataAccessFailure.logged("visit report", "Could not read the visit log.", ex);
}
```

Also accepted: `Files.lines` in try-with-resources with a `forEach` that
merges into a `TreeMap`. Or a `BufferedReader` in try-with-resources, with a
`readLine()` loop. Any fix that holds one line at a time is correct.

**Streamed but not closed** (`Files.lines(log)` without try-with-resources):
3 of 6. It passes the ticket, but it leaks a file handle (lesson IG question 3).

### Findings 2 and 3: `exportBetween`

Students often fix these together. Grade them separately.

```java
try (Stream<String> lines = Files.lines(log, StandardCharsets.UTF_8);
     BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
    writer.write(VisitLogFiles.HEADER);
    writer.newLine();
    Iterator<Visit> visits = lines.skip(1)
            .map(Visit::parse)
            .filter(v -> inRange(v, from, to))
            .iterator();
    long written = 0;
    while (visits.hasNext()) {
        writer.write(visits.next().toCsv());
        writer.newLine();
        written++;
    }
    return new ExportResult(out, written);
} catch (IOException | UncheckedIOException ex) {
    throw DataAccessFailure.logged("export visits", "Could not export the visit log.", ex);
}
```

- **Finding 2 is fixed** when nothing collects the unbounded stream. A
  `forEach` that writes through a helper wrapping `IOException` in
  `UncheckedIOException` is also accepted.
- **Finding 3 is fixed** (6 of 6) when **both** the stream and the writer are
  in try-with-resources. The ticket shows only the writer's symptom. The stream
  leak can only be found by reading the code, which is exactly the lesson's
  "`Files.lines` must be closed" point, so it's required for full fix marks.
  - **Location and flaw:** full 4 points if the unclosed writer is named. The
    stream leak doesn't have to be named.
  - Writer closed properly, stream not closed: 3 of 6.
  - `writer.close()` added at the end of the `try` block: 3 of 6. It passes
    the ticket, but it leaks if a write throws, and the stream still leaks.

**Verify:** after the fix, `2025-01-01` to `2025-12-31` exports 262,799
visits (`wc -l` or IntelliJ shows 262,800 lines, including the header), and
the one-week export has 5,041 lines and ends with a complete row.

### Finding 4: `findById`

```java
try (Connection conn = dataSource.getConnection();
     PreparedStatement ps = conn.prepareStatement(
             "SELECT patient_id, display_name, patient_since FROM patients WHERE patient_id = ?")) {
    ps.setString(1, patientId);
    try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
            return Optional.empty();
        }
        return Optional.of(new Patient(
                rs.getString("patient_id"),
                rs.getString("display_name"),
                rs.getObject("patient_since", LocalDate.class)));
    }
} catch (SQLException ex) {
    throw DataAccessFailure.logged("load patient", "Could not load that patient.", ex);
}
```

Also accepted: rewriting it with `JdbcClient` (`jdbc.sql(...).param(...).query(PATIENT).optional()`),
which manages the connection itself. That shows good judgment. Ask the
student to name the root cause in their write-up anyway.

**Partial credit:** adding `conn.close()` before `return Optional.empty();`
earns 3 of 6. It passes the ticket, but it still leaks on `SQLException`.

**Verify:** restart the app, run menu 2 with ten IDs that don't exist, then
menu 4 with `PAT-00031677`. Check-in works.

### Finding 5: `findUpcoming`

```java
public static final int MAX_PAGE_SIZE = 100;
...
Validate.between(limit, 1, MAX_PAGE_SIZE, "Show between 1 and " + MAX_PAGE_SIZE + " appointments at a time");
```

`Validate.between` already exists in the student copy, unused. Any maximum
from 50 to 1,000 is fine if the student justifies it.

**Partial credit:** `Math.min(limit, MAX)` earns 3 of 6, with or without a
message. It stops the crash, but the caller asked for a specific size, and
Lesson 4 says to reject that request, not truncate it. Compare Finding 7,
where the code chooses the bound, so a capped result with a message is fine.

### Finding 6: `CheckInCounter`

```java
private final AtomicLong checkIns = new AtomicLong();

public void recordCheckIn() {
    checkIns.incrementAndGet();
}

public long count() {
    return checkIns.get();
}
```

Also accepted: `synchronized` on **both** methods, or `AtomicInteger` (the
write-up should mention overflow if they chose `int`). `LongAdder` is also
correct.

**Not accepted:** removing `volatile`, or keeping `int` with `volatile` and
arguing that it's thread-safe. A student who claims the fix works because
"menu 8 showed 300,000 once" without running it several times hasn't
verified it. Lesson 4's closing theme is to verify, not assume.

**Verify:** menu 8 shows 300,000 on every run, and menu 9 is an exact
multiple of 300,000, plus any real check-ins.

### Finding 7 (extra credit): `searchByName`

The fixed project fetches one row more than the maximum. If it gets that
extra row, it rejects the search instead of truncating it:

```java
public static final int MAX_SEARCH_RESULTS = 50;
...
            ORDER BY display_name
            LIMIT :limit""")
    .param("pattern", "%" + namePart + "%")
    .param("limit", MAX_SEARCH_RESULTS + 1)
...
if (found.size() > MAX_SEARCH_RESULTS) {
    throw new IllegalArgumentException("More than " + MAX_SEARCH_RESULTS + " patients match. Type more of the name.");
}
```

Also accepted: a plain `LIMIT 50` with a message such as "showing the first
50 matches". Here the *code* sets the bound, not the caller, so telling the
user it was capped is enough.

**The +7 splits:** 3 for finding and explaining it, 2 for the proof, and 2 for
the fix. A silent `LIMIT` with no message earns 1 of the 2 fix points.

---

## Grading Rubric (100 points, plus 10 extra credit)

For each of the six core findings (**15 points each, 90 total**):

| Points | Criterion |
|---|---|
| 4 | **Location and flaw:** the right method, and the flaw explained in Lesson 4 terms (eager load, unbounded collect, unclosed resource, unbounded request, non-atomic update) |
| 3 | **Proof:** the exact menu input and the observed symptom, from before the fix |
| 6 | **Fix:** correct and complete, as defined under Fixes above, including the partial-credit notes |
| 2 | **Verified:** the ticket's steps rerun after the fix, with the result. Several runs for Finding 6 |

**Write-up quality (10 points):** clear and specific. Symptoms are tied to
causes, for example "the pool has 5 connections, so the 6th caller waits."
Short bullet points are fine: the README says so.

Students see this rubric (without the Fixes details) in their README, under
"How you're graded". The maximum is 110.

**Extra credit (up to 10 points):**

- Finding 7 found, proven, and fixed: **+7** (split as described under Finding 7).
- Correctly explains why one of the "looks suspicious but is correct" items
  is fine: **+3**.

**Automatic deductions:**

- The app no longer runs with `-Xmx32m`, or the heap was raised: **−10**,
  once.
- A fix that breaks an unrelated feature (for example, check-in no longer
  works): **−5** per feature.

---

## Grading Workflow

1. Re-run `schema.sql` against `clinic_demo`.
2. Copy your own `.env` into the student's project, and open it in IntelliJ.
3. Check that `.run/ClinicApp.run.xml` still says `-Xmx32m`.
4. Run the ticket checklist below, in order. It takes about 5 minutes.
5. Read `FINDINGS.md` against the rubric. Allow about 15 minutes per student
   in total.

**The checklist assumes a freshly reset database** (step 1). Step 6's
`PAT-00007920` can be checked in only once per reset.

**Ticket checklist** (expected results on a correct submission):

| Step | Input | Expected |
|---|---|---|
| 1 | 5, Enter | `Done.` |
| 2 | 6 | Six clinicians, about 66,600 visits each |
| 3 | 7, `2025-01-01`, `2025-12-31` | `Exported 262,799 visits`. No crash. |
| 4 | 7, `2025-03-01`, `2025-03-07` | `Exported 5,040 visits`. The file has 5,041 lines and a complete last row. |
| 5 | 2 with `PAT-09999999`, six times | `No patient with that ID.` each time, with no delay |
| 6 | 4 with `PAT-00007920` | `Checked in: #1 ...` |
| 7 | 3 with `1000000` | A clear rejection message. No crash. |
| 8 | 8, three times | `300,000` every time |
| 9 | 1 with `Patient` | Extra credit only: a clear message. No crash. |

---

## Common Incomplete Fixes

| What you see | Why it's incomplete | Finding |
|---|---|---|
| `readAllLines` replaced by `Files.lines(...).toList()` | The same eager load | 1 |
| `Collectors.toList()` replaced by `.toList()` | The same unbounded collect | 2 |
| Only `writer.close()` added at the end of the `try`, or only the writer in try-with-resources | Still leaks the `Files.lines` handle, and a plain `close()` also leaks on an exception: 3 of 6 | 3 |
| `Files.lines` without try-with-resources in the report | Streams correctly, but leaks the handle: 3 of 6 | 1 |
| `Math.min(limit, 100)` | Truncates a caller-chosen size instead of rejecting it: 3 of 6 | 5 |
| `synchronized` on `recordCheckIn` only | `count()` isn't synchronized, so a reader can see a stale value. The lost updates are fixed: 5 of 6 | 6 |
| `conn.close()` added before the early `return` in `findById` | Still leaks on `SQLException`: 3 of 6 | 4 |
| Heap raised to make a crash go away | Not a fix. Automatic deduction | any |

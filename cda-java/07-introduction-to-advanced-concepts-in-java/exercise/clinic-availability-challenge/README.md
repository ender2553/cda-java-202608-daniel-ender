# Exercise: Riverside Clinic Scheduler

**Module 1: Advanced Java Security — Lesson 4 individual exercise (graded)**
**Time:** about 15 minutes of setup **before** the session, then 60 minutes of work

Riverside Clinic's front desk uses this console app to find patients, check
them in, and export visit history. It works fine on a quiet day. On a busy one,
it keeps going down.

The clinic has sent you its support tickets, below. Your job is to find what
causes each one, prove it, fix it, and write up what you found. Everything you
need comes from Lesson 4: streamed I/O, try-with-resources, bounded input,
and thread-safe shared state.

This is individual work. The code has no comments or TODOs that point at the
problems. Finding them is part of the exercise.

> **The app runs with a 32 MB heap, on purpose.** The clinic's server is
> small, and a small heap makes problems show up in seconds. Don't raise the
> heap. A fix that only works with more memory isn't a fix.

---

## Before you start

Do this setup **before** the session, so your 60 minutes go to the
exercise. You're ready when step 4 works.

You need PostgreSQL with pgAdmin, IntelliJ IDEA with JDK 21 or newer, and
about **100 MB** of free disk space. While it runs, the whole app uses about
150 MB of your computer's memory: the 32 MB heap, plus the JVM itself.

### 1. Open the project

**File → Open**, choose this folder (the one with `pom.xml`), and click
**Trust Project** if asked. Wait for Maven to finish downloading.

### 2. Create the database

1. In pgAdmin, right-click **Databases → Create → Database…**, name it
   `clinic_demo`, and click **Save**.
2. Right-click **clinic_demo → Query Tool**, open this project's `schema.sql`,
   and click **Execute** (▶ or **F5**). It takes about 10 seconds.

`schema.sql` creates 6 clinicians, 200,000 patients, and 150,000 upcoming
appointments. All of it is made up: the patients are named
"Patient 000001" to "Patient 200000".

**Re-run `schema.sql` whenever you want a fresh start.** Only 150,000 of the
200,000 patients have a booked appointment. Each of those can be checked in
once, until you re-run `schema.sql`.

### 3. Create `.env`

Copy `.env.example` to a new file named `.env` in the same folder, and set
`DB_PASSWORD` to your PostgreSQL password.

### 4. Run it

At the top right of IntelliJ, choose the **ClinicApp** run configuration and
click **Run** ▶. Click inside the Run window before you type.

The menu title must say **`max heap about 32 MB`**. If it shows a bigger
number, open **Run → Edit Configurations → ClinicApp** and set **VM options**
to `-Xmx32m` and **Working directory** to `$PROJECT_DIR$`.

Then choose **5** and press **Enter** to generate the visit log
(`data/visit-log.csv`, 400,000 visits, about 22 MB).

### Useful to know

- **Patients who have a booked appointment:** choose **3** with `5` to see
  five of them, for example `PAT-00007920` and `PAT-00015839`.
- **Error detail goes to `logs/clinic.log`.** When an error shows
  `(ref 1a2b3c4d)`, search the log for that reference.
- **When the app crashes,** the Run window shows `The app stopped: ...`.
  Click **Run** ▶ to start it again.

---

## Support tickets

Each ticket is a real symptom that you can reproduce with the menu.

> **Ticket 1 — Visit report.** "The visit report (menu 6) crashes the app
> every time now. It worked fine when the clinic opened."

> **Ticket 2 — Yearly export.** "IT told us they 'switched the export to
> streaming.' Exporting all of 2025 (menu 7, `2025-01-01` to `2025-12-31`)
> still crashes the app."

> **Ticket 3 — Short export.** "I exported one week (menu 7, `2025-03-01` to
> `2025-03-07`). The app said it exported 5,040 visits. The file has fewer
> rows, and the last row is cut off in the middle."

> **Ticket 4 — Check-in outage.** "Around 10 a.m., check-in (menu 4) stopped
> working at every desk, and then patient search failed too. Restarting the
> app fixed it until the afternoon. The only unusual thing that morning: a new
> receptionist mistyped a few patient IDs in menu 2 and kept getting 'No
> patient with that ID.'"

> **Ticket 5 — Upcoming appointments.** "A manager typed `1000000` into
> Upcoming appointments (menu 3) to see everything, and the app went down for
> everyone."

> **Ticket 6 — Check-in count.** "On busy mornings, the check-in count
> doesn't match the number of patients we saw. The busy-morning simulation
> (menu 8) shows the problem."

The tickets aren't necessarily the whole story. The clinic's staff only report
what they've happened to run into.

---

## What to hand in

1. **Your fixed project.** Every ticket's symptom is gone, and the app still
   runs with `-Xmx32m`. Leave out `target/`, `data/`, `logs/`, and `.env`.
2. **`FINDINGS.md`**, filled in. There's a template in this folder. For each
   problem, give:
   - **Location:** the class and method.
   - **Flaw:** what is wrong, in one or two sentences.
   - **Proof:** the exact menu input and what you saw before your fix.
   - **Fix:** what you changed.
   - **Verified:** what you ran after the fix, and what you saw.

   Short bullet points are fine: two to four lines per field is plenty. If one
   flaw causes more than one ticket, or one ticket has more than one flaw,
   say so.

## How you're graded

| For each problem a ticket describes (6 × 15 = 90 points) | Points |
|---|---|
| **Location and flaw:** the right method, and what's wrong, in Lesson 4 terms | 4 |
| **Proof:** the menu input and what you saw before the fix | 3 |
| **Fix:** correct and complete. It holds with large data and under load, and on every code path, not only the one the ticket shows | 6 |
| **Verified:** you reran the ticket's steps after the fix, and say what you saw | 2 |

| Also | Points |
|---|---|
| A clear, specific write-up | 10 |
| **Extra credit:** a problem that no ticket reports, found, proven, and fixed | up to +10 |
| Raising the heap above 32 MB | −10 |
| A fix that breaks another feature | −5 per feature |

The total is out of 100, and extra credit can take it to 110. Each problem is
scored on its own, so partial work earns partial credit.

## Pacing

A suggested plan for the 60 minutes:

| Minutes | Do this |
|---|---|
| 0–10 | Reproduce all six tickets. Note what you saw for each one. |
| 10–40 | Fix and verify, one ticket at a time. |
| 40–60 | Write up `FINDINGS.md`. Hunt for anything the tickets missed if you have time left. |

**If you run out of time,** stop coding with about 10 minutes left and write up
what you have. A finding with a good proof and an unfinished fix still earns
points.

**Tips:**

- Reproduce every ticket **before** you change any code. Write down what you
  saw.
- After each fix, run the ticket's steps again. For Ticket 6, run it several
  times.
- Restart the app between tests if a previous test crashed or used up
  resources.

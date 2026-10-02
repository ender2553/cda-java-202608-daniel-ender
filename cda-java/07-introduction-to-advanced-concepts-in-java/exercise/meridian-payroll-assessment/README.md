# Summative Assessment: Meridian Payroll

**Module 1: Advanced Java Security, summative assessment for Lessons 1–4 (graded)**
**Format:** individual take-home
**Time:** about 4–6 hours of work if things go smoothly; the plan below
budgets 7, and some people need up to 8. Plus 20 minutes of setup.

Meridian's payroll office uses this console app to look up employees, change
direct-deposit accounts, run payroll, and export pay history. It was written
in a hurry in 2019 and has had no security review since.

That review is now. Meridian has to pass a data-protection audit next month.
The payroll office has sent you its support tickets, and the security team
has sent you the standard the app must meet. Your job:

1. Find what is wrong, prove it, and fix it.
2. Write up what you found in `FINDINGS.md`, and answer its short questions.

Everything you need comes from Lessons 1–4: atomic transactions, encryption
at rest, the secure repository pattern, and streamed, bounded, leak-free
resource use.

This is individual work. The code has no comments or TODOs that point at the
problems. Finding them is part of the assessment.

> **The app runs with a 32 MB heap, on purpose.** A small heap makes memory
> problems show up in seconds. Don't raise it. A fix that only works with more
> memory isn't a fix.

---

## Before you start

You're ready when step 4 works.

You need PostgreSQL with pgAdmin, IntelliJ IDEA with JDK 21 or newer, and
about **150 MB** of free disk space.

### 1. Open the project

**File → Open**, choose this folder (the one with `pom.xml`), and click
**Trust Project** if asked. Wait for Maven to finish downloading. If
IntelliJ picks an older JDK, set **File → Project Structure → Project → SDK**
to 21 or newer.

### 2. Create the database

1. In pgAdmin, connected as the `postgres` user, right-click **Databases →
   Create → Database…**, name it `payroll_demo`, and click **Save**.
2. Right-click **payroll_demo → Query Tool**, open this project's
   `schema.sql`, and click **Execute** (▶ or **F5**). It takes under a minute
   and ends with **Query returned successfully**. It needs the `postgres`
   user because it turns on the `pgcrypto` extension.

`schema.sql` creates 8 departments, 600 employees, about 156,000 past pay
stubs, and one funding account that pays everyone. All of it is made up. The
Social Security numbers all start with 9, a range that is never issued to
real people.

**Re-run `schema.sql` whenever you want a fresh start.** It puts every table
back exactly as it was, including the employee data your migration changes.
Do this before each test that needs a clean ledger. Stop the app first (the
red square in the Run window), because open connections can block it.

**Don't change `schema.sql` or the tables.** The columns are already wide
enough for everything the standard below asks for.

### 3. Create `.env`

Copy `.env.example` to a new file named `.env` in the same folder, and set
`DB_PASSWORD` to your PostgreSQL password. `DB_URL` and `DB_USERNAME` assume
PostgreSQL on this computer, port 5432, user `postgres`; change them if your
install differs. `.env` is git-ignored. Never commit it and never hand it in.

### 4. Run it

At the top right of IntelliJ, choose the **PayrollApp** run configuration
and click **Run** ▶. Click inside the Run window before you type.

The menu title must say **`max heap about 32 MB`** (31 is fine too). If it
shows a bigger number, open **Run → Edit Configurations → PayrollApp** and
set **VM options** to `-Xmx32m` and **Working directory** to `$PROJECT_DIR$`.

Choose **5** (Check funding totals). It should end with `BALANCED`.

**If it won't start,** the Run window shows `The app stopped:` and a reason:

- `password authentication failed`: fix `DB_PASSWORD` in `.env`.
- `Connection refused`: PostgreSQL isn't running. Start it from Windows
  Services, or open pgAdmin and connect.
- `Could not resolve placeholder 'DB_URL'`: `.env` wasn't found. Check it's
  in the project folder, next to `pom.xml`, and that the working directory
  is `$PROJECT_DIR$`.

### Useful to know

- **A test employee:** `EMP-00001`, whose portal PIN is `007919`. Every
  employee's PIN comes from their number, as the comment above the
  `employees` insert in `schema.sql` describes. Use it to test other
  employees.
- **The next payday is `2026-10-09`.** Pay runs suggest it by default. Each
  department can be paid only once per date, so use a later Friday
  (`2026-10-23`, `2026-11-06`) or re-run `schema.sql` for another try.
- **Menu 5 is your ledger check.** The funding account started at its
  opening balance. Every dollar that left it must appear on a pay stub, so
  *opening − current* must equal *the sum of every stub's net pay*.
- **Menu 6 simulates an outage** partway through the next pay runs, until you
  turn it off again.
- **Menu 8 writes a timesheet file** and prints its true totals. Choose the
  sample size (100,000 lines) or the full size (3,000,000 lines, about 80 MB).
- **To look at the raw data,** use pgAdmin's Query Tool, for example
  `SELECT employee_id, ssn, bank_account, portal_pin FROM payroll.employees LIMIT 5;`
- **Logs go to `logs/payroll.log`.**
- **When the app crashes,** the Run window shows `The app stopped: ...`.
  Click **Run** ▶ to start it again.
- **To make a random 32-byte key** for `.env`, run this in PowerShell:
  ```powershell
  $b = New-Object byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
  ```

---

## Support tickets

Each ticket is a real symptom that you can reproduce with the menu.

> **Ticket 1 — Half a payroll.** "The server blipped during last Friday's
> Engineering run. The console said 'Payroll complete', but half of
> Engineering never got paid, and finance says the funding account dropped by
> more than the stubs add up to. The audit log says the run completed." (Menu
> 6 turns on the simulated outage, then menu 4 for `ENG`.)

> **Ticket 2 — Run twice by mistake.** "Someone ran Engineering payroll a
> second time for the same date. Nobody got paid twice, and it said 'Payroll
> complete' again, but menu 5 hasn't balanced since."

> **Ticket 3 — Payday.** "On payday every department runs at once (menu 10).
> Every run reports success, but afterwards menu 5 never balances, and it's
> out by a different amount each time."

> **Ticket 4 — The search box.** "Searching for `O'Brien` (menu 1) fills the
> screen with a long block of technical text. And a contractor showed us that
> the search box can list every employee's Social Security number."

> **Ticket 5 — The auditor's query.** "Our auditor ran a plain `SELECT` on
> `employees` and read every Social Security number. She also noticed that
> when two employees use the same bank account, their `bank_account` values
> are identical, even though they're 'encrypted'."

> **Ticket 6 — The log file.** "The office changed a few employees'
> direct-deposit accounts (menu 3). Later, IT sent `logs/payroll.log` to a
> vendor for troubleshooting. It contained employee Social Security numbers
> and bank account numbers."

> **Ticket 7 — Console freeze.** "After someone checked the funding totals
> (menu 5) six or more times in a row, every option that touches the database
> stopped working for everyone until we restarted the app."

> **Ticket 8 — Pay history export.** "Exporting ten years of pay history
> (menu 7, `2016-01-01` to `2026-12-31`) crashes the app. Exporting six months
> (`2026-04-01` to `2026-09-30`) says 7,800 rows, but the file has fewer and
> the last row is cut off. Finance also says the file has columns they should
> never see."

> **Ticket 9 — Timesheet import.** "Importing the full timesheet file (menu 8,
> size 2, then menu 9) crashes the app. On the sample file, the import's
> totals never match what menu 8 reported, and they change every time we run
> it."

> **Ticket 10 — Existing data.** "Security says all *existing* employee data
> has to meet the standard, not just new changes. Menu 11 just says 'not
> available yet'."

The tickets aren't the whole story. The payroll office only reports what it
has happened to run into. The standard below is what the audit will check.

---

## Meridian data-protection standard (excerpt)

1. **Money moves completely or not at all.** A failure at any point leaves
   every balance and every pay stub as it was before. The caller is told the
   operation did not happen. The audit record never says a run completed
   when it didn't, and a failure to write the audit record never blocks or
   undoes a correct pay run.
2. **Sensitive data is protected at rest.** Values the app must read back are
   encrypted with AES-GCM using a 256-bit key and a fresh random IV for every
   value. The key comes from the environment (`.env`), never from source code
   or a file packaged with the app.
3. **Secrets that are only ever checked are hashed, not encrypted.** Use
   PBKDF2 with HMAC-SHA512, at least 210,000 iterations, and a random 16-byte
   salt per value. Compare in constant time.
4. **Existing data meets the standard too.** Old values are converted in
   place, safely: a failure partway through must not corrupt or lose data,
   and running the conversion again must not damage what is already done.
   If the conversion needs the old encryption key, that key also comes from
   `.env` (for example `PAYROLL_LEGACY_KEY`). Its value goes in your own
   `.env`, never in source code or `.env.example`.
5. **One gate to the database.** All database access goes through repository
   classes. They use bound parameters only, check input before it reaches
   SQL, and return only the fields a caller needs.
6. **Errors tell the user what happened, not how the app works.** No stack
   traces, SQL, or table names on screen. Logs never contain Social Security
   numbers, bank account numbers, PINs, or keys.
7. **The app stays up.** Large data is processed without loading it all into
   memory. Every connection, stream, and file is closed on every path.
   Requests a user can make arbitrarily large have a limit, and requests over
   the limit are rejected with a clear message, never silently cut short.
   Choose limits that still allow normal work: the six-month export in
   Ticket 8 must still succeed. Shared state is safe under concurrent use.

---

## What to hand in

Hand in one `.zip` of this project folder, with `FINDINGS.md` inside it. Your
instructor will tell you where to submit it and by when.

1. **Your fixed project.** Every ticket's symptom is gone, the app meets the
   standard, and it still runs with `-Xmx32m`. Leave out `target/`, `data/`,
   `logs/`, and `.env`.
2. **`FINDINGS.md`**, filled in. There's a template in this folder:
   - **Part A, findings:** one block per problem, with its location, the
     flaw, your proof, your fix, and how you verified the fix.
   - **Part B, short answers:** five questions, a paragraph or two each.
3. **Your `.env.example`**, updated so a grader knows every setting your
   project needs. Use placeholder values only, never a real key or password.

## How you're graded

Your work is graded in five areas. Each area is **Pass** or **Needs
revision**, and the assessment passes when all five areas pass. The grader
checks each item below by running your app and reading your code and
`FINDINGS.md`.

| Area | To pass, you show that… |
|---|---|
| **1. Transactions** (Lesson 1) | A pay run is all-or-nothing, even with the simulated outage or a repeated date, and menu 5 stays balanced. A failed run is reported as failed. Only the writes that must succeed together share the transaction, and the audit record follows rule 1. |
| **2. Data at rest** (Lesson 2) | Sensitive fields are protected as the standard says, each with the right tool (encrypted or hashed). Every key comes from the environment. Every existing row is converted by menu 11, and running it twice is safe. You prove it with the raw values in pgAdmin. |
| **3. Repository boundary** (Lesson 3) | Only repository classes touch the database. Every query uses bound parameters, input is validated at the repository, errors and logs leak nothing, and results carry only the fields their caller needs. |
| **4. Availability** (Lesson 4) | Exports and imports stream under `-Xmx32m`. Every resource is closed on every path. User-chosen sizes are bounded and rejected when too large. Concurrent counts are correct. |
| **5. Findings and analysis** | Every ticket appears in Part A, with proof before and after, and every problem behind it is covered. All five Part B answers are correct and explained in your own words. |

The tickets don't list every problem. The standard is what you're graded
against, so a problem no ticket reports still has to be fixed if it breaks a
rule.

**Needs revision** means you get feedback and can resubmit that area once.

These make an area need revision:
- Raising the heap above 32 MB (area 4).
- Changing `schema.sql` or the tables (area 2).
- A real key or password in any file you hand in (area 2).
- A fix that breaks another feature, for example profiles no longer show or
  a correct PIN is refused (the area of the broken feature).

**With distinction:** for work beyond the standard, such as making Payday
Rush (Ticket 3) balance every time, or a real problem that no ticket and no
rule covers, found, proven, and fixed.

## Suggested plan

| Hours | Do this |
|---|---|
| 0–1 | Reproduce every ticket and note what you saw. Read the code end to end and list every problem you suspect, before you fix anything. |
| 1–1.5 | Area 1: the pay run. |
| 1.5–3 | Area 3: the repository boundary, validation, errors, and results. |
| 3–4.5 | Area 2: encryption, hashing, and the menu 11 migration. |
| 4.5–5.5 | Area 4: exports, imports, limits, counters. |
| 5.5–7 | Re-run every ticket against your fixed app, then finish `FINDINGS.md`. |

**Tips:**

- Reproduce a ticket **before** you change any code. A finding with good
  proof and an unfinished fix still counts for something; a fix you can't
  show was needed counts for less.
- Re-run `schema.sql` before each pay-run test, so an old failure doesn't
  hide a new one.
- Proof means running it. "It should work now" is not verification: show the
  menu output, or the pgAdmin query and its result. A few problems have no
  symptom you can trigger. For those, the proof is the code path you traced,
  quoted in your finding.
- Hashing 600 PINs at 210,000 iterations takes a couple of minutes. If menu
  11 seems slow, it probably hasn't hung. Print some progress.
- After your migration, the app must still work for every employee: profiles,
  PIN checks, and direct-deposit changes.

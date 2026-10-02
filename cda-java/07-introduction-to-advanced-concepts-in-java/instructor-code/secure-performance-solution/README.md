> **Instructor copy: the finished solution.** Every `TODO: (Completed) Step N of 7` in
> `TransactionLogExporter`, `TransactionRepository`, and `ProcessingStats` shows the step
> next to the code that finishes it. The answer key is [`INSTRUCTOR_GUIDE.md`](INSTRUCTOR_GUIDE.md).
> Students get `secure-performance-starter`. Do not share this folder before the session.

# Code-Along: Advanced Java for Performance, Securely

**Module 1: Advanced Java Security — Lesson 4**

Lessons 1 to 3 defended **Integrity** and **Confidentiality**. This lesson
defends **Availability**, the third part of the CIA triad. An app that runs out
of memory, leaks its database connections, or corrupts a counter under load is
down for every user, and no attacker was needed to make that happen.

In this code-along you change three classes:

- `TransactionLogExporter` reads LedgerCore's transaction log, a file that only
  ever grows. You make it **stream** the file instead of loading it, and
  **close** every file it opens.
- `TransactionRepository` gets a **connection that is always returned** to
  the pool, and a **bound** on how many rows a caller can ask for.
- `ProcessingStats` gets a **thread-safe counter**.

The rest of the app is already finished: a numeric console menu, a transfer
service, and the repositories and checks from Lesson 3. It uses **Spring Boot**
for configuration and dependency injection, and **Spring JDBC** for SQL.

> **The starter fails on purpose.** Every class you change works on small data,
> and that's the point. When a step says **"Run it first,"** expect a crash, lost
> data, or a wrong number. That means you're on track, not that your setup is
> broken.

---

## Before you start

You need:

- **PostgreSQL**, running locally, and **pgAdmin** (installed with PostgreSQL).
- **IntelliJ IDEA** with a **JDK 21 or newer**.
- Your PostgreSQL password for the `postgres` user.
- About **1 GB of free disk space**. The practice log file is about 200 MB, and
  each export is about 85 MB.

---

## Setup

### 1. Open the project in IntelliJ

1. **File → Open**, select this project's folder (the one that contains
   `pom.xml`), and click **OK**.
2. If IntelliJ asks, click **Trust Project**.
3. Wait for the progress bar at the bottom right to finish. IntelliJ is
   downloading the Maven dependencies (Spring Boot and the PostgreSQL driver).
   The first time takes a minute or two.
4. Check the JDK: **File → Project Structure → Project → SDK** must be 21 or
   newer.

If the imports in any `.java` file are red after the download finishes, see
[Troubleshooting → Reload the project from Maven](#reload-the-project-from-maven).

### 2. Create the tables in `ledger_demo`

This demo uses the `ledger_demo` database from Lessons 1 to 3. Its tables live
in a schema named `ledgercore`.

1. In pgAdmin, connect to your local server.
2. If you do **not** have a `ledger_demo` database yet: right-click
   **Databases → Create → Database…**, name it `ledger_demo`, and click **Save**.
3. Right-click **ledger_demo → Query Tool**.
4. Click **Open File** (the folder icon), choose this project's `schema.sql`,
   and click **Execute** (▶ or **F5**). It takes 10 to 30 seconds.

> **`schema.sql` resets the `ledgercore` tables.** It drops and recreates
> `accounts`, `account_holders`, and `transactions`. It adds five accounts
> (`ACC-00000001` to `ACC-00000005`) and **1,000,000 past transactions**, so
> the table really is too big to load all at once. Any account holders you
> added in Lesson 3 are removed. The app never runs this file for you.

**Check:** in the Query Tool, run `SELECT count(*) FROM ledgercore.transactions;`.
It shows `1000000`.

### 3. Create your `.env` file

The app reads its connection settings from a file named `.env` in the project
folder. `.env` is git-ignored, so your password is never committed.

1. In IntelliJ's Project view, right-click `.env.example` → **Copy**, then
   right-click the project folder → **Paste**, and name the copy `.env`.
2. Open `.env` and set `DB_PASSWORD` to your own PostgreSQL password:

   ```properties
   DB_URL=jdbc:postgresql://localhost:5432/ledger_demo
   DB_USERNAME=postgres
   DB_PASSWORD=your-postgres-password
   ```

This lesson doesn't use Lesson 2's encryption key, so `.env` has no
`LEDGERCORE_DATA_KEY`.

### 4. Run the app with a 128 MB heap

The project comes with a run configuration named **LedgerDemoApp**. It limits
the app to a **128 MB heap** (the JVM option `-Xmx128m`), so a memory problem
shows up in seconds instead of in production.

1. At the top right of IntelliJ, open the run configuration drop-down and
   choose **LedgerDemoApp**. Then click the green **Run** ▶ button next to it.
2. The menu appears in the **Run** window at the bottom. Click inside the Run
   window before you type.
3. The first line must say **`max heap about 128 MB`**. If it shows a bigger
   number, see [Troubleshooting → The heap isn't 128 MB](#the-heap-isnt-128-mb).
4. Choose **1** (Show account balances). You should see five accounts. Then
   choose **0** to exit.

> **Always start the app with the LedgerDemoApp configuration** at the top
> right, and glance at the heap in the menu title each time. A run
> configuration without `-Xmx128m` gets a much bigger heap, and the crashes in
> Steps 1, 3, and 6 won't happen.

---

## Using the menu

```
=== LedgerCore Demo (max heap about 128 MB) ===
1) Show account balances
2) Transfer funds
3) Show recent transactions
4) Total sent by an account
5) Generate the transaction log file
6) Summarize the transaction log
7) Export one account from the log
8) Run the concurrency check
0) Exit
```

Type the number and press **Enter**, then answer the prompts.

- **Menu 5** writes `data/transaction-log.csv`. Press **Enter** at the prompt
  for the full 3,000,000 lines. You only need to do this once.
- **Menu 7** writes `data/export-ACC-xxxxxxxx.csv`, then counts the lines
  that actually reached the disk (**File check**).

The Run window shows only the menu. **Full error detail goes to
`logs/ledger-demo.log`** in the project folder (the file is created on the
first run). When an error shows `(ref 1a2b3c4d)`, search the log for that
reference to find the full detail.

When the app **crashes**, the Run window shows `The demo stopped: ...`. Click
**Run** ▶ at the top right to start it again. In Steps 1, 3, and 6, a crash is
the expected result. So is Step 5, if you try a transfer after the pool has
run out of connections.

---

## The code-along

Work through the `TODO: Step N of 7` comments **in order**. Most steps say
**"Run it first"**: run the app and try the input shown so you see the
problem before you fix it. Then run it again after the fix.

| Step | What you do | Where |
|---|---|---|
| 1 | Reproduce the `OutOfMemoryError` | `log/TransactionLogExporter`, top of the class |
| 2 | Stream the log instead of loading it | `TransactionLogExporter.summarize` |
| 3 | Remove the `.collect(...)` trap | `TransactionLogExporter.exportForAccount` |
| 4 | Close the reader and the writer | `TransactionLogExporter.exportForAccount` |
| 5 | Close the database connection | `repository/TransactionRepository.totalSentBy` |
| 6 | Add `MAX_PAGE_SIZE`, then bound the request size | `TransactionRepository.findRecent` |
| 7 | Make the shared counter thread-safe | `log/ProcessingStats` |

**Use these as models:** `log/LogFiles` has two finished methods,
`generate` and `countDataLines`, that already use try-with-resources correctly.
`Validate` holds the boundary checks, including `between`, which you use in
Step 6.

**Tip:** in IntelliJ, **View → Tool Windows → TODO** lists every step in the
project. Click a step to jump to it.

**Some imports are grey** until a step uses them (for example `Iterator`,
`Stream`, and `AtomicLong`). That's expected. Don't let IntelliJ's
**Optimize Imports** remove them.

---

## Extension: find large transactions in the log

Do this after Step 7, on your own. It uses all four ideas from this lesson at
once.

Add a method `findAtLeast(BigDecimal minAmount, int maxResults)` to
`TransactionLogExporter`. It returns the first log lines whose amount is at
least `minAmount`, up to `maxResults` of them. Then add menu option
**9) Find large transactions in the log** to `console/ConsoleMenu.java`. This
is the only time you change a file outside the three classes above.

Your `findAtLeast` must:

1. Check both inputs before it opens the file: the amount with
   `Validate.amount`, and the count with `Validate.between` (1 to 100).
2. Stream the log inside try-with-resources.
3. Stop reading as soon as it has found `maxResults` lines.
4. Handle a file failure with `DataAccessFailure`, like `summarize` does.
5. Add the number of lines it returns to `ProcessingStats`.

**Hints:**

- `LogSummary.ofLine(line).total()` already reads a log line's amount as a
  `BigDecimal`. Compare amounts with `compareTo`, not `>=`.
- `ConsoleMenu` already has `prompt`, `parseAmount`, and `parseCount` helpers.
  Case `"6"` is a good model for the new case.
- After `filter`, the stream operation `limit(maxResults)` stops the
  pipeline once it has that many lines. Once the result is bounded, it's safe to
end with `.toList()`. Why is this safe here when it wasn't in Step 3?

**Test it with these inputs:**

| Minimum amount | How many | Expected result |
|---|---|---|
| `990.00` | `5` | Five log lines, each with an amount of 990.00 or more |
| `990.00` | `1000` | `Error: Show between 1 and 100 transactions at a time` |
| `5000.00` | `10` | `No transactions found.` (the largest amount is 999.99) |
| `-1` | `5` | `Error: Amount must be greater than zero` |

---

## Project layout

```
schema.sql                   Creates the ledgercore tables. You run it in pgAdmin.
.env.example                 Template for .env (connection settings)
.run/LedgerDemoApp.run.xml   IntelliJ run configuration: 128 MB heap, project folder as working directory
pom.xml                      Maven build file: Spring Boot and the PostgreSQL driver
src/main/resources/
  application.properties     Reads .env, sets up the connection pool (5 connections) and logging
src/main/java/demo/ledger/
  LedgerDemoApp.java         Starts Spring and the menu
  console/ConsoleMenu.java   The numeric menu (the view)
  console/ConcurrencyCheck   Menu 8: 8 threads updating one shared counter
  service/TransferService    Lesson 1's atomic transfer, using @Transactional
  domain/                    Account and Transaction (data records)
  log/
    TransactionLogExporter   ★ You change this one (Steps 1-4)
    ProcessingStats          ★ You change this one (Step 7)
    LogFiles                 Finished: file locations, log generator, line counter
    LogSummary, ExportResult Data records
  repository/
    TransactionRepository    ★ You change this one (Steps 5-6)
    AccountRepository        Finished repository from Lesson 3
    Validate                 Boundary checks shared by every repository
    DataAccessFailure        Safe exception: log rich, respond thin
data/                        Created by menu 5: the log file and exports (git-ignored)
logs/                        Created on the first run: ledger-demo.log (git-ignored)
```

---

## Troubleshooting

### Reload the project from Maven

Use this when imports such as `org.springframework...` are **red**, you see
**"Cannot resolve symbol"**, or the **LedgerDemoApp** run configuration is
missing.

1. **Reload.** Open the **Maven** tool window (the **m** icon on the right
   edge, or **View → Tool Windows → Maven**) and click **Reload All Maven
   Projects** (the circular-arrows icon). You can also right-click `pom.xml`
   and choose **Maven → Reload project**. Wait for the progress bar to finish.
2. **No Maven window, or `pom.xml` shows as a plain file?** Right-click
   `pom.xml` and choose **Add as Maven Project**. Then reload (step 1).
3. **Dependencies won't download?** Go to **File → Settings → Build, Execution,
   Deployment → Build Tools → Maven** and make sure **Work offline** is **not**
   checked. Check your internet connection, then reload again.
4. **Still red?** In the Maven tool window, expand **Lifecycle** and
   double-click **clean**, then **compile**. The Run window shows the real
   error if one exists.
5. **Last resort:** **File → Invalidate Caches… → Invalidate and Restart**.
   When IntelliJ reopens, reload from Maven (step 1) again.

### The heap isn't 128 MB

The menu title shows your heap limit. If it shows a number much bigger than
128, the app was started without `-Xmx128m`, and the steps that should crash
won't.

1. Open **Run → Edit Configurations…** and select **LedgerDemoApp** under
   **Application**. If it isn't there, click **+** → **Application** and name
   it `LedgerDemoApp`.
2. Set **Main class** to `demo.ledger.LedgerDemoApp`.
3. If there is no **VM options** box, click **Modify options → Add VM options**.
   Type `-Xmx128m` in it.
4. Set **Working directory** to `$PROJECT_DIR$`.
5. Click **OK**, then run **LedgerDemoApp** from the top right.

### "release version 21 not supported" or "invalid source release"

IntelliJ is building with an older JDK. Set **File → Project Structure →
Project → SDK** to 21 or newer. Then go to **Settings → Build, Execution,
Deployment → Build Tools → Maven → Runner** and set **JRE** to the same JDK.
Reload from Maven.

### Errors when the app starts or stops

| You see | What it means | Fix |
|---|---|---|
| `The demo stopped: ... 'url' must start with "jdbc"` | The app can't find `.env`, or `DB_URL` is missing from it | Check that `.env` is in the project folder (next to `pom.xml`), is named exactly `.env` (not `.env.txt`), and has a `DB_URL` line. Then check that the run configuration's **Working directory** is `$PROJECT_DIR$`. |
| `The demo stopped: java.lang.OutOfMemoryError: Java heap space` | The app ran out of its 128 MB | Expected in Steps 1, 3, and 6 before the fix. If it still happens after your fix, compare your code with the step's TODO. |
| `The demo stopped: ... Connection is not available, request timed out` | Every pooled connection was leaked, and then a transfer asked for one | Expected in Step 5 before the fix. Restart the app. |

### Errors in the menu

The menu shows a short message. The cause is in `logs/ledger-demo.log`:
search the log for the `ref` shown in the error message.

| You see | Search the log for the ref; the cause is usually | Fix |
|---|---|---|
| `Error: Could not load accounts. (ref …)` on menu 1 | `password authentication failed` | Fix `DB_PASSWORD` in `.env`, then run again. |
| | `database "ledger_demo" does not exist` | Create the database ([setup step 2](#2-create-the-tables-in-ledger_demo)). |
| | `relation "accounts" does not exist` | Run `schema.sql` against `ledger_demo` ([setup step 2](#2-create-the-tables-in-ledger_demo)). |
| | `Connection to localhost:5432 refused` | PostgreSQL is not running. Start it, or check the port in `DB_URL`. |
| | `Connection is not available, request timed out` | Connections leaked (Step 5). Restart the app. |
| `Error: No transaction log yet. Choose 5 to generate one.` | — | Choose **5** and press **Enter**. |
| Typing does nothing | — | Click inside the **Run** window first. |

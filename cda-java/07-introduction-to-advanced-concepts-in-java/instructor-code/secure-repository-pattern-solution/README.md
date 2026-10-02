> **Instructor copy: the finished solution.** Every `TODO: (Completed) Step N of 8` in
> `AccountHolderRepository` shows the step next to the code that finishes it.
> Students get `secure-repository-pattern-starter`. Do not share this folder before the session.

# Code-Along: The Secure Repository Pattern

**Module 1: Advanced Java Security — Lesson 3**

In this code-along you build one class, `AccountHolderRepository`, into a
security boundary: the only path to the `account_holders` table. By the end,
every query in it is parameterized, every input is validated before it
reaches the database, every failure is handled without leaking internal
detail, and every result is mapped column by column.

The rest of the app is already finished: a numeric console menu, a transfer
service, two other repositories, and Lesson 2's `FieldCipher`. It uses
**Spring Boot** for configuration and dependency injection and **Spring JDBC's
`JdbcClient`** for SQL, so there is no connection or setup code in your way.

> **The starter is insecure on purpose.** `AccountHolderRepository` begins as
> a deliberately insecure first draft. It works, and that's the point. When a
> step says **"Run it first,"** expect to see a failure, a leaked SQL message,
> or even a false "Holder saved." That means you're on track, not that your
> setup is broken.

---

## Before you start

You need:

- **PostgreSQL**, running locally, and **pgAdmin** (installed with PostgreSQL).
- **IntelliJ IDEA** with a **JDK 21 or newer**.
- Your PostgreSQL password for the `postgres` user.

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

This demo uses the `ledger_demo` database from Lessons 1 and 2. Its tables live
in a schema named `ledgercore`.

1. In pgAdmin, connect to your local server.
2. If you do **not** have a `ledger_demo` database yet: right-click
   **Databases → Create → Database…**, name it `ledger_demo`, and click **Save**.
3. Right-click **ledger_demo → Query Tool**.
4. Click **Open File** (the folder icon), choose this project's `schema.sql`,
   and click **Execute** (▶ or **F5**).

> **`schema.sql` resets the `ledgercore` tables.** It drops and recreates
> `accounts`, `account_holders`, and `transactions`, and it adds five
> accounts (`ACC-00000001` to `ACC-00000005`). It adds no account holders:
> a tax ID has to be encrypted by `FieldCipher`, so you add holders from the
> app's menu. The app never runs this file for you.

### 3. Create your `.env` file

The app reads its connection settings from a file named `.env` in the project
folder. `.env` is git-ignored, so your password is never committed.

1. In IntelliJ's Project view, right-click `.env.example` → **Copy**, then
   right-click the project folder → **Paste**, and name the copy `.env`.
2. Open `.env` and fill it in:

   ```properties
   DB_URL=jdbc:postgresql://localhost:5432/ledger_demo
   DB_USERNAME=postgres
   DB_PASSWORD=your-postgres-password
   LEDGERCORE_DATA_KEY=4FCL14YY5aI9f04oLJgSYzKsdh789PsdwZB/ykuJPQU=
   ```

   - `DB_PASSWORD`: your own PostgreSQL password.
   - `LEDGERCORE_DATA_KEY`: the AES-256 key the class shared in Lesson 2.
     Use it exactly as shown.

> **Why is a key in a README?** To keep class simple, everyone shares this one
> practice key. A shared key that is written down is only acceptable for a
> throwaway classroom database. In a real system, every environment gets its
> own key from a secrets manager, and nobody writes it down (Lesson 2).

### 4. Run the app

1. Open `src/main/java/demo/ledger/LedgerDemoApp.java`.
2. Click the green ▶ next to `public class LedgerDemoApp` and choose
   **Run 'LedgerDemoApp.main()'**.
3. The menu appears in the **Run** window at the bottom. Click inside the Run
   window before you type.
4. Choose **6** (Show account balances). You should see five accounts. Then
   choose **0** to exit.

After the first run, the **Run** button at the top right runs the app again.

### 5. Add two practice holders

`schema.sql` adds no account holders, and Steps 3 to 7 need some to work with.
Run the app, choose **4** (Add account holder) twice, and enter:

| Account ID | Display name | Tax ID |
|---|---|---|
| `ACC-00000001` | `Jordan Rivera` | `123-45-6789` |
| `ACC-00000003` | `Maria Lopez` | `555-12-3456` |

Both save correctly in the starter, because neither name has an apostrophe.

**Check:** choose **1** (List account holders) and sort by `display_name`.
Both holders appear. You add a third holder, `Pat O'Brien`, in Step 2.

If you re-run `schema.sql` later, add these two holders again.

---

## Using the menu

```
=== LedgerCore Demo ===
1) List account holders
2) Search holders by name
3) View holder details
4) Add account holder
5) Transfer funds
6) Show account balances
0) Exit
```

Type the number and press **Enter**, then answer the prompts.

The Run window shows only the menu. **Full error detail goes to
`logs/ledger-demo.log`** in the project folder (the file is created on the
first run). When an error shows `(ref 1a2b3c4d)`, search the log for that
reference to find the full detail. You look at this log in Step 7.

---

## The code-along

Open `src/main/java/demo/ledger/repository/AccountHolderRepository.java` and
work through the `TODO: Step N of 8` comments **in order**. Most steps say
**"Run it first"**: run the app and try the input shown so you see the
problem before you fix it. Then run it again after the fix.

| Step | What you do | Where |
|---|---|---|
| 1 | Confirm this class is the only path to `account_holders` | Top of the class |
| 2 | Parameterize the INSERT | `create` |
| 3 | Parameterize the name search | `searchByName` |
| 4 | Parameterize the lookup by ID | `findById` |
| 5 | Allow-list the sort column | `findAll` |
| 6 | Validate every input at the boundary | Constants, then every method |
| 7 | Handle exceptions securely | Every `catch` block |
| 8 | Map results explicitly, with no `SELECT *` | Constants, then every query |

**Use these as models:** `AccountRepository` and `TransactionRepository` are
finished repositories that follow the same pattern. `Validate` holds the
boundary checks, and `DataAccessFailure` is the only exception a repository
lets escape.

**Tip:** in IntelliJ, **View → Tool Windows → TODO** lists every step in the
project. Click a step to jump to it.

---

## Extension: add `rename`

Do this after Step 8, on your own. It uses all four disciplines at once.

Add a method `rename(String accountId, String newDisplayName)` to
`AccountHolderRepository`, then add menu option **7) Rename account holder**
to `console/ConsoleMenu.java`. This is the only time you change a file
outside `AccountHolderRepository`.

Your `rename` must:

1. Validate both inputs before any SQL runs.
2. Bind every value as a parameter.
3. Handle a database failure with `DataAccessFailure`.
4. Report `No holder for account ...` when no row was updated.

**Hint:** `AccountRepository.changeBalance` has the same shape: an `UPDATE`,
then a check of how many rows changed.

**Test it with these inputs:**

| Account ID | New display name | Expected result |
|---|---|---|
| `ACC-00000001` | `Jordan O'Rivera` | "Holder renamed." Menu 1 shows the new name. |
| `ACC-00000005` | `Nobody` | `Error: No holder for account ACC-00000005` |
| `ACC-00000001` | `x'; DROP TABLE accounts; --` | `Error: Name must be 1-128 letters, ...` |

---

## Project layout

```
schema.sql                   Creates the ledgercore tables. You run it in pgAdmin.
.env.example                 Template for .env (connection settings and key)
pom.xml                      Maven build file: Spring Boot and the PostgreSQL driver
src/main/resources/
  application.properties     Reads .env, sets up the database connection and logging
src/main/java/demo/ledger/
  LedgerDemoApp.java         Starts Spring and the menu
  console/ConsoleMenu.java   The numeric menu (the view)
  service/TransferService    Lesson 1's atomic transfer, using @Transactional
  crypto/FieldCipher         Lesson 2's AES-GCM field encryption
  domain/                    Account, AccountHolder, HolderSummary (data records)
  repository/
    AccountHolderRepository  ★ You build this one
    AccountRepository        Finished example
    TransactionRepository    Finished example
    Validate                 Boundary checks shared by every repository
    DataAccessFailure        Safe exception: log rich, respond thin
```

---

## Troubleshooting

### Reload the project from Maven

Use this when imports such as `org.springframework...` are **red**, you see
**"Cannot resolve symbol"**, or there is no green ▶ next to `LedgerDemoApp`.

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

### "release version 21 not supported" or "invalid source release"

IntelliJ is building with an older JDK. Set **File → Project Structure →
Project → SDK** to 21 or newer. Then go to **Settings → Build, Execution,
Deployment → Build Tools → Maven → Runner** and set **JRE** to the same JDK.
Reload from Maven.

### Errors when the app starts

| You see | What it means | Fix |
|---|---|---|
| `The demo could not start: 'url' must start with "jdbc"` | The app can't find `.env`, or `DB_URL` is missing from it | Check that `.env` is in the project folder (next to `pom.xml`), is named exactly `.env` (not `.env.txt`), and has a `DB_URL` line. If all of that is right, open **Run → Edit Configurations → LedgerDemoApp** and set **Working directory** to `$PROJECT_DIR$`. |
| `The demo could not start: LEDGERCORE_DATA_KEY must be a Base64-encoded 32-byte key` | The key line is missing, blank, or mistyped | Copy the key from [step 3](#3-create-your-env-file) again. The key ends with `=`. |

### Errors in the menu

The menu shows a short message. The cause is in `logs/ledger-demo.log`:
search the log for the `ref` shown in the error message.

| You see | Search the log for the ref; the cause is usually | Fix |
|---|---|---|
| `Error: Could not load accounts. (ref …)` on menu 6 | `password authentication failed` | Fix `DB_PASSWORD` in `.env`, then run again. |
| | `database "ledger_demo" does not exist` | Create the database ([setup step 2](#2-create-the-tables-in-ledger_demo)). |
| | `relation "accounts" does not exist` | Run `schema.sql` against `ledger_demo` ([setup step 2](#2-create-the-tables-in-ledger_demo)). |
| | `Connection to localhost:5432 refused` | PostgreSQL is not running. Start it, or check the port in `DB_URL`. |
| `Error: Could not load that account holder. (ref …)` on menu 3 | `Could not decrypt field` | That row was stored with a different key, or before its tax ID was encrypted. Re-run `schema.sql` and add the holder again from menu 4. |
| Typing does nothing | — | Click inside the **Run** window first. |

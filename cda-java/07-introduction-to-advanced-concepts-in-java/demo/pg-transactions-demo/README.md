# Demo: Multi-Table Transactions on PostgreSQL

A small live demo of one idea: **a unit of work that spans several tables
either commits completely or not at all.** It has four Java classes and one
SQL file, and it runs against a real PostgreSQL container.

Using a real database server gives the demo two things an embedded, in-memory
database can't show well:

- Database-enforced rules (a `CHECK` constraint and foreign keys) that cause
  real failures partway through a transfer.
- A **second session** (`psql`) that can watch the tables while the Java
  transaction is still open, so learners can see for themselves that no one
  sees half a transfer.

Facilitation notes (timing, framing, likely questions) are in
[`INSTRUCTOR_GUIDE.md`](INSTRUCTOR_GUIDE.md). This README covers what the demo
does and how to run it.

## The model: one transfer, three tables, five writes

```
accounts        (account_id, owner, balance CHECK >= 0)
transfers       (transfer_id, from_account, to_account, amount)       -- one header per transfer
ledger_entries  (entry_id, transfer_id, account_id, amount)           -- exactly 2 per transfer, summing to 0
```

`TransferService` performs the same five writes in both versions:

| # | Table | Write |
|---|---|---|
| 1 | `transfers` | INSERT header |
| 2 | `accounts` | UPDATE sender balance (debit) |
| 3 | `ledger_entries` | INSERT debit line |
| — | | **`afterDebit` checkpoint: the demo injects a crash or a pause here** |
| 4 | `accounts` | UPDATE receiver balance (credit) |
| 5 | `ledger_entries` | INSERT credit line |

- `naiveTransfer(...)` leaves auto-commit on, so each write commits as soon as it runs.
- `transfer(...)` calls `setAutoCommit(false)`, then `commit()` on success. Its
  `finally` block calls `rollback()` if the commit didn't happen, whatever was
  thrown (`SQLException`, `RuntimeException`, or `Error`), and then restores
  auto-commit.
- A database failure in `transfer(...)` is logged internally with the full
  detail. The caller gets a `TransferFailedException` whose message is safe to
  show a user, so constraint and table names never leak out.

After every step the demo prints a snapshot that checks two invariants:

- **Total money = $600.00.** Money is never created or destroyed.
- **Unbalanced transfers = 0.** Every transfer has two entries that sum to zero.

## Prerequisites

- JDK 21+ and Maven 3.9+
- **One** of:
  - Docker with Compose, installed and running (Option A, the default). On
    Windows and macOS that's Docker Desktop. On Linux, Docker Engine with the
    Compose plugin works too.
  - PostgreSQL 13 or later installed on your machine (Option B)

## Setup, Option A: Docker (default)

```bash
docker compose up -d --wait        # Postgres 17 on localhost:5433, db/user/password: ledger_demo/demo/demo
mvn -q compile                     # also downloads the PostgreSQL JDBC driver
```

You'll know the setup worked when `docker ps` lists `ledger-demo-db` as
`healthy` and `mvn -q compile` finishes with no output.

The credentials are defaults for a throwaway local container. To change them,
copy `.env.example` to `.env`. Docker Compose reads `.env` automatically. For
the Java side, set the same `LEDGER_DB_*` variables in your shell: `export LEDGER_DB_PORT=5434`
in bash, or `$env:LEDGER_DB_PORT = "5434"` in PowerShell.

## Setup, Option B: a locally installed PostgreSQL

Use this if you'd rather not run Docker, or if PostgreSQL is already
installed. The Java app is the same; only where it connects changes. You'll
create a `demo` login and a `ledger_demo` database on your own server, then
point the app at your server's port, usually **5432**. The Docker default is
5433, so the two never clash.

**Windows (PowerShell).** Use the installer from postgresql.org, or run
`winget install PostgreSQL.PostgreSQL.17`. Remember the `postgres` password
you set during install.

```powershell
# 1. Make psql available in this terminal. 17 matches the winget command above;
#    if you installed a different version, use its number (check C:\Program Files\PostgreSQL)
$env:Path += ";C:\Program Files\PostgreSQL\17\bin"

# 2. As the postgres superuser, create the demo login and database (prompts for the postgres password)
psql -h localhost -U postgres -c "CREATE ROLE demo LOGIN PASSWORD 'demo'"
psql -h localhost -U postgres -c "CREATE DATABASE ledger_demo OWNER demo"

# 3. Point the app at your server for this terminal, then build
$env:LEDGER_DB_PORT = "5432"
mvn -q compile
```

**macOS (Homebrew)**, after `brew install postgresql@17` and
`brew services start postgresql@17`. Homebrew makes *your* macOS user the
superuser, so there's no `postgres` login:

```bash
psql -d postgres -c "CREATE ROLE demo LOGIN PASSWORD 'demo'"
psql -d postgres -c "CREATE DATABASE ledger_demo OWNER demo"
export LEDGER_DB_PORT=5432
mvn -q compile
```

**Linux (Debian/Ubuntu)**, after `sudo apt install postgresql`:

```bash
sudo -u postgres psql -c "CREATE ROLE demo LOGIN PASSWORD 'demo'"
sudo -u postgres psql -c "CREATE DATABASE ledger_demo OWNER demo"
export LEDGER_DB_PORT=5432
mvn -q compile
```

You'll know it worked when the first line of any scenario's output says
`Connected to jdbc:postgresql://localhost:5432/ledger_demo`. If it still says
**5433**, `LEDGER_DB_PORT` isn't set in *that* terminal. Environment variables
set this way last only for the current terminal session.

A few things worth knowing:

- **Why `OWNER demo`?** Since PostgreSQL 15, ordinary users can no longer
  create tables in the `public` schema by default. The database owner still
  can, and the demo drops and recreates its tables on every run.
- **Pick a real password if the server is shared.** `demo`/`demo` matches the
  app's defaults and is fine on a server only your machine can reach. Unlike
  the Docker container, though, this server keeps running after class. If
  other machines can connect to it, create the role with a password of your
  own and set `LEDGER_DB_PASSWORD` to match. Either way, drop the role when
  you're done (see Teardown).
- **Everything else is the same.** The scenario commands below don't change.
  Only the `visibility` scenario's second terminal uses `psql` directly
  instead of `docker exec`.

## Scenarios

Every scenario drops and recreates the schema before it runs, so you can repeat
any scenario as many times as you like. Running with no argument
(`mvn -q exec:java`) runs `naive` and then `atomic`.

Keep the quotes around `"-Dexec.args=..."`. They're needed in PowerShell, which
otherwise splits the argument at the `.`, and they're harmless in bash and cmd.

### `naive`: auto-commit breaks the ledger

```bash
mvn -q exec:java "-Dexec.args=naive"
```

1. **Happy path.** $50 moves from ACC-1 to ACC-2. The ledger is consistent.
2. **Crash after the debit.** The ledger shows **$525.00 of an expected
   $600.00**, 2 transfers, 3 ledger entries, and 1 unbalanced transfer. The
   header, the debit, and the debit line all committed, and the credit never
   happened.

### `atomic`: the same writes inside one transaction

```bash
mvn -q exec:java "-Dexec.args=atomic"
```

1. **Same crash.** After the rollback there are 0 transfers and 0 entries, and
   the balances are unchanged.
2. **Overdraft of $1000 from ACC-2.** Postgres rejects the debit with
   `violates check constraint "chk_balance_non_negative"`. The `transfers`
   header row had already been inserted before the failure, and the rollback
   removed it too. Two things print: a `WARNING` from the internal log (on
   stderr) that names the SQLState and the constraint, and the line
   `FAILED, caller sees: Transfer from ACC-2 to ACC-1 could not be completed.
   No money was moved.` That second line is all a caller would ever see.
3. **Happy path.** All five writes commit together.

### `visibility`: what other sessions see mid-transfer

This scenario needs two terminals.

```bash
# Terminal A
mvn -q exec:java "-Dexec.args=visibility"

# Terminal B: run this each time Terminal A pauses
docker exec -it ledger-demo-db psql -U demo -d ledger_demo -c "SELECT * FROM accounts"

# Terminal B, Option B (local install): prompts for the demo password
psql -h localhost -p 5432 -U demo -d ledger_demo -c "SELECT * FROM accounts"
```

With a local install, Terminal A needs `LEDGER_DB_PORT` set and Terminal B
needs `psql` on its `PATH`. Each is a separate terminal session, so set them
in each. The pause message in Terminal A prints both `psql` commands with your
actual port filled in.

The demo pauses twice between the debit and the credit. Press Enter in
Terminal A to continue.

| Pause | What Terminal B sees |
|---|---|
| Naive transfer | ACC-1 = **450.00**, ACC-2 = 100.00. The committed debit is visible to everyone. |
| Atomic transfer | ACC-1 = **500.00**, ACC-2 = 100.00. The uncommitted debit is hidden (`READ COMMITTED`, so no dirty reads). |

## Troubleshooting

| Symptom | Fix |
|---|---|
| `docker compose` fails with `Cannot connect to the Docker daemon` or `error during connect` | Docker isn't running. Windows/macOS: start Docker Desktop and wait until it says it's running. Linux: `sudo systemctl start docker`. Then rerun `docker compose up -d --wait`. |
| `Connection to localhost:5433 refused` | Run `docker compose up -d --wait` and check `docker ps` shows `ledger-demo-db` as healthy. |
| Port 5433 already in use | Set `LEDGER_DB_PORT` in `.env` and export it in your shell, then `docker compose up -d`. |
| `Unknown lifecycle phase ".args=naive"` | PowerShell split the unquoted argument. Quote it: `mvn -q exec:java "-Dexec.args=naive"`. |
| **Option B:** `password authentication failed for user "demo"` | The `demo` role doesn't exist or has a different password. Rerun the `CREATE ROLE` step, or set `LEDGER_DB_PASSWORD` to the password you chose. |
| **Option B:** `database "ledger_demo" does not exist` | Rerun the `CREATE DATABASE ledger_demo OWNER demo` step. |
| **Option B:** `permission denied for schema public` | The database was created without `OWNER demo`. As the superuser, run `ALTER DATABASE ledger_demo OWNER TO demo`. |
| **Option B:** `Connection to localhost:5432 refused` | The server isn't running. Windows: `Get-Service postgresql*` (start it from the Services app). macOS: `brew services list`. Linux: `sudo systemctl status postgresql`. |
| **Option B:** `psql` is not recognized | Add PostgreSQL's `bin` folder to `PATH` for that terminal (see Option B, step 1). |
| The `visibility` pause doesn't wait | Run it from a real terminal, not an IDE run configuration that has no stdin attached. |

## Teardown

**Option A (Docker):**

```bash
docker compose down        # removes the container; data is not persisted between runs
```

**Option B (local install):** remove the demo database and login, running as
the superuser the same way you created them (Windows shown):

```powershell
psql -h localhost -U postgres -c "DROP DATABASE ledger_demo"
psql -h localhost -U postgres -c "DROP ROLE demo"
```

## Layout

```
pg-transactions-demo/
├── README.md                      what the demo does and how to run it
├── INSTRUCTOR_GUIDE.md            timing, framing, talking points
├── docker-compose.yml             Postgres 17 on :5433
├── .env.example                   optional credential/port overrides
├── docs/transaction-flow.svg      whiteboard diagram used in INSTRUCTOR_GUIDE.md
├── pom.xml                        PostgreSQL JDBC driver + exec plugin
└── src/main/
    ├── resources/schema.sql       3 tables + seed ($600 across 2 accounts)
    └── java/demo/transactions/
        ├── Database.java          connection settings + schema reset
        ├── TransferService.java   naiveTransfer vs. transfer (same 5 writes)
        ├── TransferFailedException.java  the safe failure the caller sees
        └── TransferDemo.java      scenarios + consistency snapshot
```

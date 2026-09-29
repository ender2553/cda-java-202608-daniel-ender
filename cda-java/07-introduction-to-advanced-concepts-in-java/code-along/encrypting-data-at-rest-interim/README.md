# LedgerCore — Student Starter (PostgreSQL)

Starter baseline for the **Advanced Java Security** module of *Introduction to
Advanced Concepts in Java — Secure Edition*. LedgerCore is a small account-ledger
service: it transfers funds between accounts, stores account-holder records
(including a sensitive tax ID), and processes an append-only transaction log.

**This project compiles and runs.** The security-critical method bodies are the
insecure/naive starting point each lab fixes — each is marked with a
`// TODO (lab): ...` comment describing what you need to do. Read the comment in
each class before you start; it names exactly what is broken and what "fixed"
looks like.

The project runs against a local **PostgreSQL** database. Connection settings
live in a git-ignored `.env` file, never in source.

## Database setup

1. Have PostgreSQL running locally with a database to use (the defaults assume
   `payments` on `localhost:5432`).
2. Copy `.env.example` to `.env` and fill in your values:

   | Variable | Example | Purpose |
   |---|---|---|
   | `DB_URL` | `jdbc:postgresql://localhost:5432/payments` | JDBC URL — change `payments` to your database name |
   | `DB_USERNAME` | `postgres` | Database user |
   | `DB_PASSWORD` | *(yours)* | Database password |
   | `DB_SCHEMA` | `ledgercore` | Postgres schema LedgerCore's tables go in |
   | `DB_INIT_MODE` | `always` | `always` runs `schema.sql` + `data.sql` on every start; `never` skips them |
   | `LEDGERCORE_DATA_KEY` | `openssl rand -base64 32` | AES-256 key for the encryption lab |

   A real environment variable with the same name always wins over `.env`.
3. Run the app. On startup `db.Database` creates the `ledgercore` schema if needed,
   then runs `src/main/resources/schema.sql` (drop + recreate the three tables)
   and `data.sql` (seed accounts `ACC-00000001` and `ACC-00000002`). This works
   like Spring's `spring.sql.init.mode=always`.

> **Every run resets LedgerCore's tables.** They live in their own `ledgercore`
> schema, so other tables in the same database (for example `public.*`) are
> never touched. To keep data between runs, set `DB_INIT_MODE=never`.

To inspect what is actually stored (for example, to confirm the tax ID is
ciphertext once the encryption lab is done), query Postgres directly:

```sql
SELECT account_id, tax_id_encrypted FROM ledgercore.account_holders;
```

> **Postgres note for the transaction lab:** once any statement fails inside a
> transaction, Postgres marks the whole transaction as aborted and rejects every
> later statement until you `rollback()`. That is one more reason the fixed
> `TransferService` must roll back explicitly on failure.

## What's insecure here (and which lesson fixes it)

| File | What's wrong | Fixed in |
|---|---|---|
| `transactions/TransferService.java` | The debit and credit are two independent, auto-committing writes with nothing tying them together. A failure between them leaves the ledger inconsistent. | Secure Transaction Processing in Java |
| `crypto/FieldCipher.java` | `encryptField`/`decryptField` are a plaintext passthrough — nothing is actually encrypted. | Data at Rest — Encryption Basics in Java |
| `repository/LedgerRepository.java` | Every query is built by concatenating input into SQL text, nothing validates an input before it reaches the database, and a caught exception leaks its raw message to the caller. | The Secure Repository Pattern |
| `logs/TransactionLogProcessor.java` | Loads the entire log file into memory before processing a line, and opens a second stream that is never closed. | Advanced Java for Performance, Securely |

The integrative lab (Secure Advanced Java Lab) asks you to find and fix all four
defect classes in one pass.

## Build and run

Requires JDK 21+ and Maven.

```bash
# Compile
mvn -q compile

# Run the demo app (happy path only — the insecure baselines run, they're just
# not safe under failure, injection, or load)
mvn -q compile exec:java -Dexec.mainClass=com.rti.ledgercore.app.LedgerCoreApp
```

> The `exec:java` invocation above requires the `exec-maven-plugin`, which is not
> declared in `pom.xml` (kept minimal: PostgreSQL driver only, no test dependencies for this
> starter). Run `LedgerCoreApp.main()` directly from an IDE instead.

There are no test sources in this starter — write your own as you fix each
defect (the instructor solution's test suite shows the shape: prove atomicity
with an injected mid-transfer failure, prove the AES-GCM round trip, prove
boundary validation rejects a malformed/injection-style input, prove the log
processor streams and bounds correctly).

## Project layout

```
src/main/java/com/rti/ledgercore/
  domain/          Account, AccountHolder, TransactionRecord (immutable records)
  config/          Env — reads settings from the environment, then .env
  db/              Database, DatabaseConfig — Postgres DataSource + schema.sql/data.sql init
  transactions/    TransferService              <- TODO: make atomic
  crypto/          FieldCipher                   <- TODO: encrypt at rest
  repository/      LedgerRepository              <- TODO: parameterize + validate + handle securely
  logs/            TransactionLogProcessor       <- TODO: stream + bound + leak-free
  app/             LedgerCoreApp (main)
src/main/resources/
  schema.sql       LedgerCore tables (runs on startup)
  data.sql         seed accounts (runs after schema.sql)
```

## Summative assessment

The module's graded summative — **Secure the LedgerCore Transaction Component** — is the integrative lab that applies every defense in this project at once. See [`summative/README.md`](summative/README.md) for the brief, deliverables, and grading.

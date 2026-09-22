# QuickPay POS — Day 2: Persistence & SQL-Injection Prevention

**Cyber Developer Program — Java Secure Coding Foundations**
**Graded Exercise — Part 2 of 4**

## The story

Yesterday you built the cart and payment-method foundation. Today QuickPay POS needs to
actually **save transactions somewhere**. You'll build a small repository layer on top of
a simulated in-memory database, and — critically — you'll fix a real SQL-injection-style
bug in the given starter code.

Your Day 1 code (`Cart`, `LineItem`, `Product`, `CardBrand`, `PaymentMethod`, `CreditCard`,
`GiftCard`, `TerminalRegister`, `Refundable`) is included here **already fully working** —
that's your foundation, not something to redo. All of Day 1's tests still run against this
code and must still pass.

Persistence is now a **real PostgreSQL database**, accessed through Spring's
`JdbcTemplate` — the same pattern used in the dndrpg project (Repository interfaces, a
`jdbc` package with real `JdbcTemplate`-backed implementations, and RowMappers implementing
Spring's actual `org.springframework.jdbc.core.RowMapper<T>` contract against a real
`java.sql.ResultSet`). See "Set up PostgreSQL" below before you start.

## What's already given

- `schema/schema.sql` — PostgreSQL DDL for the `merchant` and `transaction` tables. Run this
  once before starting (see "Set up PostgreSQL" below).
- `com.cyberdev.pos.config.DatabaseConfig` — builds a Spring `DataSource` from the
  `POS_DB_URL`/`POS_DB_USER`/`POS_DB_PASSWORD` environment variables.
- `TransactionRecord`, `TransactionRepository` (interface), `MerchantRepository`
  (interface) — plain data/interface types. `TransactionRecord` has `equals()`/`hashCode()`
  by `transactionId` identity (given, for comparison against today's `Merchant` TODO below).
- `com.cyberdev.pos.exception.DataAccessException` / `DuplicateTransactionException` —
  two more members of Day 1's `PosException` hierarchy. `DataAccessException` wraps any
  SQL/Spring persistence failure; `DuplicateTransactionException` replaces the generic
  `IllegalStateException` a duplicate sale used to throw.
- `InMemoryTransactionRepository` / `InMemoryMerchantRepository`
  (`com.cyberdev.pos.day2.inmemory`) — a no-database-required stand-in for the same
  repository interfaces, used by the service-layer tests (POS2-5/6/7) and by Day 4's
  console demo by default.
- `org.springframework.jdbc.core.RowMapper<T>` — Spring's real one-method interface
  (`T mapRow(ResultSet rs, int rowNum) throws SQLException`), used directly (no invented
  stand-in interface anymore).
- `MerchantRowMapper` (`com.cyberdev.pos.day2.jdbc.mapper`) — a fully-implemented example
  `RowMapper<Merchant>` against a real `ResultSet`. Read it before starting POS2-8;
  `TransactionRecordRowMapper` is the same idea, but you write it.
- `com.cyberdev.pos.day2.support.FakeResultSet` (test support) — a lightweight fake
  `java.sql.ResultSet` used by the provided tests to unit-test your `TransactionRecordRowMapper`
  without needing a live database connection.

## What you need to build (TODOs)

| Tag | File | What to do |
|---|---|---|
| POS2-1 | `jdbc/JdbcTransactionRepository.java` | `save` — parameterized `INSERT` via `JdbcTemplate` |
| POS2-2 | `jdbc/JdbcTransactionRepository.java` | `findById` — parameterized `SELECT` lookup |
| POS2-3 | `jdbc/JdbcTransactionRepository.java` | `searchByMemo` — **fix a real SQL-injection vulnerability** (see below) |
| POS2-4 | `jdbc/JdbcMerchantRepository.java` | `findByMerchantId` — parameterized lookup |
| POS2-5 | `TransactionService.java` | Constructor — accept `TransactionRepository` via **dependency injection** |
| POS2-6 | `TransactionService.java` | `recordSale` — build + save a `TransactionRecord` from a `Cart` |
| POS2-7 | `TransactionService.java` | `recordSale` — reject a duplicate transaction id **before** saving, throw `DuplicateTransactionException` |
| POS2-8 | `jdbc/mapper/TransactionRecordRowMapper.java` | `mapRow` — map a real `ResultSet` row to a `TransactionRecord`, handling a null memo |
| POS2-9 | `Merchant.java` | `equals()`/`hashCode()` by `merchantId` identity |

### About POS2-3 specifically

`searchByMemo` is shipped to you **already "working"** in the sense that it compiles and
returns results for normal input — but it is vulnerable. It builds the **SQL text itself**
by string concatenation (`"...WHERE memo LIKE '%" + keyword + "%'"`) and runs it through
`JdbcTemplate.query(String sql, RowMapper<T>)` with no bind parameters at all. Try
searching for `"nonexistent' OR '1'='1"` against a real Postgres table and see what comes
back. Your fix: route the keyword through `JdbcTemplate.query(String sql, RowMapper<T>,
Object... args)` with a `?` placeholder in the SQL text, e.g. `jdbcTemplate.query("... WHERE
memo LIKE ?", rowMapper, "%" + keyword + "%")` — building the `"%...%"` wildcard string in
Java is fine; concatenating it into the SQL text is not. Do **not** "fix" this by
blacklisting characters like quotes before concatenating; that is not a real fix, is easy
to bypass with a different payload, and will not earn credit even if it happens to pass a
narrow test.

## Key concepts this exercise tests

- **Repository/DAO pattern**: your services talk to an interface, not a concrete database
  class.
- **Parameterized queries vs. string concatenation**: the actual SQL-injection lesson.
- **Constructor-based dependency injection**: `TransactionService` must never construct its
  own repository — it receives one through its constructor (manual IoC, no framework).
- **Fail-closed duplicate rejection**: a repeated transaction id must never be saved twice.
- **RowMapper extraction**: row-to-object mapping now lives in its own class
  (`TransactionRecordRowMapper`/`MerchantRowMapper`), not inline in the repository — this is
  Spring's real `JdbcTemplate.query(sql, rowMapper)` pattern, and it makes the mapping
  logic independently testable (see `FakeResultSet`).
- **Repository/interface swap**: `JdbcTransactionRepository` and
  `InMemoryTransactionRepository` both satisfy the exact same `TransactionRepository`
  interface — that's why `TransactionService` (unchanged from before) doesn't care which one
  it's handed.
- **Entity identity equality**: `Merchant.equals()`/`hashCode()` (POS2-9) is by `merchantId`
  alone — contrast with Day 1's `Product`, which is equal by ALL its fields (a value type).
- **Custom exceptions continue**: repository failures wrap into `DataAccessException`;
  duplicate sales throw `DuplicateTransactionException`. Both extend `PosException`.

## Set up PostgreSQL (required for the JDBC-backed tests)

```
createdb quickpay_pos
run schema/schema.sql from PgAdmin
POS_DB_URL="jdbc:postgresql://localhost:5432/quickpay_pos"
POS_DB_USER="<your postgres user>"
POS_DB_PASSWORD="<your postgres password>"
```

`schema/schema.sql` creates the `merchant` and `transaction` tables `JdbcTransactionRepository`/
`JdbcMerchantRepository` read and write. The POS2-1/2-2/2-3/2-4 tests (and the second half
of POS2-8) connect to this database at test time; if no database is reachable at
`POS_DB_URL`, those specific tests are **skipped**, not failed, so you can still work on and
get feedback from the DI/service/RowMapper-logic tests (POS2-5/6/7/8/9) without Postgres —
but you will need Postgres running to get credit for POS2-1 through POS2-4.

## How to run the tests

```
mvn test
```

Day 1's tests run again automatically (regression check) alongside Day 2's new tests — if
you accidentally broke something in the given Day 1 code, you'll see it here too.

## Grading

100 points total: 80 automated + 20 manual/code-quality. Manual review specifically looks
at whether your POS2-3 fix is a genuine parameterization or a character-blacklist dodge.

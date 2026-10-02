# Meridian Payroll: How the App Is Built

This describes the app as you receive it, so you can find your way around
the code. Setup, tickets, and grading are in `README.md`.

## Libraries and tools

| Library or tool | What it does here |
|---|---|
| **Spring Boot 3.5** | Starts the app, creates each component (`@Component`, `@Service`, `@Repository`), and passes each one the others it needs through its constructor. There's no web server; the app is a console menu. |
| **Spring JDBC** (`JdbcTemplate`, `JdbcClient`) | Runs SQL and turns result rows into Java objects. |
| **HikariCP** | The connection pool Spring Boot uses by default. It keeps up to 5 open database connections and makes a caller wait up to 3 seconds for a free one. |
| **PostgreSQL JDBC driver** | Lets Java talk to PostgreSQL. |
| **`javax.crypto`** | Java's built-in encryption library. |
| **SLF4J / Logback** | Logging. Console logging is turned off, so the menu stays readable. Everything goes to `logs/payroll.log`. |
| **Maven** (`pom.xml`) | Downloads the libraries and builds the app. IntelliJ runs it for you. |

## Folder structure

```
meridian-payroll-assessment/
  README.md              Setup, the support tickets, the standard, and grading
  FINDINGS.md            Your write-up template
  DESIGN.md              This file
  pom.xml                Maven build file: Java version and libraries
  schema.sql             Creates and fills the payroll_demo database (you run it in pgAdmin)
  .env.example           The settings the app reads; copy it to .env
  .gitignore             Keeps .env, build output, logs, and data files out of git
  .run/PayrollApp.run.xml  IntelliJ run configuration: main class, 32 MB heap, working directory
  src/main/resources/
    application.properties  Database connection, pool size, and logging settings
  src/main/java/demo/payroll/
    PayrollApp.java      Starts Spring; prints a short reason if startup fails
    console/
      ConsoleMenu.java   The numeric menu: reads your choices and prints results
      PaydayRush.java    Menu 10: runs all 8 departments' payroll at the same moment
    service/
      PayrollService.java        Runs a department's payroll; reports funding totals
      DirectDepositService.java  Checks the portal PIN and changes a bank account
      MigrationService.java      Menu 11
      FailureInjector.java       Menu 6: makes pay runs fail partway, on purpose
    repository/
      EmployeeRepository.java    Employee search, lookup, and bank-account updates
    crypto/
      BankCrypto.java    Encrypts and decrypts stored account fields
    files/
      DataFiles.java           Where data files live (the data/ folder)
      PayHistoryExporter.java  Menu 7: writes pay stubs for a date range to a CSV file
      TimesheetGenerator.java  Menu 8: writes a test timesheet file and its true totals
      TimesheetImporter.java   Menu 9: reads the timesheet file and totals it
      ImportStats.java         Running count of timesheet lines processed
    domain/
      Employee.java        An employee row
      PayLine.java         One employee's gross and net pay for a run
      PayRunResult.java    What a pay run reports back
      FundingCheck.java    The ledger check behind menu 5
      ExportResult.java    Where an export went and how many rows it wrote
      TimesheetSummary.java  Totals for a timesheet file
```

Files the app creates while running (all git-ignored):
- `logs/payroll.log`
- `data/timesheets.csv`
- `data/pay-history-<from>-to-<to>.csv`

## How the pieces fit

```
You ──▶ ConsoleMenu ──▶ services, repository, files ──▶ PostgreSQL (payroll schema)
                                        │
                                        └──▶ data/ files
```

Spring creates every class marked `@Component`, `@Service`, or `@Repository`
once, when the app starts, and shares that one instance with every class that
asks for it. `ConsoleMenu` runs after startup and calls the other classes for
each menu choice.

## The data

All in schema `payroll`:

| Table | Holds |
|---|---|
| `departments` | 8 departments, such as `ENG` (Engineering) |
| `employees` | 600 employees: name, department, title, salary, whether active, SSN, bank account, portal PIN, HR notes |
| `pay_stubs` | One row per employee per payday: gross and net pay. Each employee can have only one stub per pay date. |
| `funding_accounts` | The one company account (`FND-0001`) that pays everyone: opening balance and current balance |
| `payroll_audit` | A record of pay runs |

**The ledger rule behind menu 5:** money only leaves the funding account to
pay a stub, so *opening balance − current balance* always equals *the sum of
every stub's net pay*.

## Features

| Menu | Feature |
|---|---|
| 1 | Search employees by the start of their last name |
| 2 | Show one employee |
| 3 | Change an employee's direct-deposit account, after checking their portal PIN |
| 4 | Pay every active employee in one department for a pay date |
| 5 | Check the ledger rule above |
| 6 | Turn the simulated outage on or off |
| 7 | Export pay stubs for a date range |
| 8 | Generate a sample or full timesheet file |
| 9 | Import the timesheet file and total it |
| 10 | Payday rush: every department's payroll at once, then the ledger check |
| 11 | Convert existing employee data (not built yet) |

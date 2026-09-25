# SecOps Analyst Suite: Summative Assessment Capstone Requirements and Rubric

**Cyber Developer Program: Java Secure Coding Foundations**
**Summative capstone: one day, 15 graded checkpoints plus two provided security modules,
100 points**

You are finishing a small security-operations console application. It keeps an asset
inventory, records vulnerability-scan findings, runs a risk register, matches an SBOM
against known CVEs, reports STRIDE threat-model coverage, and ingests a third-party
threat-intelligence CSV feed. At the end it correlates everything into one Markdown
**security assessment report**. That report is the graded artifact.

Everything you practised across the QuickPay POS series comes back here: validating
constructors, entity equality, `JdbcTemplate` + `RowMapper`, parameterized SQL and the
injection fix, sealed types with exhaustive switches, constructor injection, PBKDF2,
AES-GCM, and failing closed. This time you work alone, across one bigger code base.

---

## 1. What you receive

```
secsuite-capstone-student/
  REQUIREMENTS.md               <- this file
  pom.xml                       <- Java 21, Spring Boot 3.5, PostgreSQL driver, JUnit 5
  data/threat-intel-feed.csv    <- the feed the pipeline ingests (13 rows, 4 of them deliberately bad)
  schema/schema.sql             <- tables, constraints, indexes (3NF; read the comments)
  schema/dcl.sql                <- least-privilege roles: secsuite_app (no DELETE, no DDL) + secsuite_readonly
  schema/seed.sql               <- the same reference data InMemorySeedLoader loads
  src/main/java/com/cyberdev/secsuite/...
  src/test/java/com/cyberdev/secsuite/...   <- the graded tests (given; do not edit)
```

- **Given and fully working** (marked `GIVEN INFRASTRUCTURE -- not a graded TODO` or
  `GIVEN/PROVIDED IMPLEMENTATION`): all `InMemory*` repositories and `InMemorySeedLoader`,
  the repository interfaces, every model class except `Asset`, the exception hierarchy,
  the Spring Boot profile configuration, `EncryptionKeyConfig`, `ConsoleUI`,
  `CsvLineParser`, the result
  records (`IngestionResult`, `PersistenceResult`, `IngestionSummary`, `SkippedRow`,
  `ComponentVulnerabilities`, `CorrelationHit`), the `Jdbc*` repositories and row mappers
  for analysts, CVEs, scan findings, risks and threat models, and the sealed
  `RiskAssessment` interface. `PasswordHasher`, `EncryptionService`, and `AuthService`
  are also provided: students use their secure behavior but do not implement cryptographic
  primitives or authentication internals.
- **Your work**: every method whose body is
  `throw new UnsupportedOperationException("TODO [SEC-n]: ...")`, plus **SEC-15**, which
  ships as a **working but vulnerable** method. The `INSTRUCTOR NOTE [SEC-n]` and
  `SECURITY CALLOUT` comment directly above each one is the full task description. Read it
  before you write anything.
- Small private helpers next to a TODO (for example `required`, `optional`,
  `parseTimestamp` and `validateIndicatorShape` in `ThreatIntelCsvIngestionService`,
  `escapeMd` and the `append...` section builders in `ReportService`, `runPipeline` in
  `Main`) are **given**. Use them.

The project compiles as delivered. It does not *run* yet: every stub throws at runtime
until you implement it.

**Reference-project boundary.** The RPG Adventure instructor project is a useful reference
for validating models, repository interfaces, JDBC/row-mapper patterns, constructor
injection, sealed types, and parameterized SQL. This capstone's CSV validation, PBKDF2,
AES-GCM, threat correlation, STRIDE coverage, Markdown reporting, and composition-root
requirements are specified here and in the instructor notes; they are not all demonstrated
by that earlier project.

## 2. What "done" looks like

1. `test` reports every graded test green (with the database up, see §3).
2. Run `Main`, log in, and select **7 - Run Full Security Assessment**. The full workflow
   writes **`reports/security-assessment-report.md`**. This file is one of the two artifacts
   you hand in along with your code.
3. The report reflects a fully working pipeline. With the reference data and the shipped
   feed, a correct implementation prints these figures:

   | Report section | Expected |
   |---|---|
   | Header | `Generated` = the run's clock, `Prepared by` = your username, `Scope: 6 assets, 10 catalogued CVEs` |
   | 2. Open Scan Findings | `Total open findings: **8**` (7 seeded + 1 recorded by the scripted scan) |
   | 3. Risk Register | `Total entries: **10**`, sorted by score (25, 20, 20, 20, 15, 12, 9, 8, 6, 2) |
   | 4. SBOM | `Components analysed: **8**, with known vulnerabilities: **5**` |
   | 5. STRIDE | storefront model shows a **coverage gap** for Repudiation and Elevation of Privilege |
   | 6. Alerts | `Total alerts: **13**`; the `<script>` in TI-2026-0004 appears as `&lt;script&gt;` |
   | 7. Correlation | `Active hits: **5**` |
   | 8. Ingestion | run 1: `13 / 9 / 4 / 8 / 1`, run 2: `13 / 9 / 4 / 0 / 9` (rows / parsed / skipped / inserted / duplicates) |

   The console should also print `Alert rows: 5 -> 13 after run 1 -> 13 after replay
   (idempotent)` and `searchByHostname("' OR '1'='1") -> 0 asset(s)`.
4. Use the **`reports/security-assessment-report.md`** to complete your second artifact: **`secOps-executive-summary.docx`**

## 3. Running the tests

```bash
IntelliJ, you can run the test class or method with its green gutter arrow or select Run All Tests.

```


Each graded test carries `@GradedTest(tag = "SEC-n", points = …)`. Until a checkpoint is
done, its tests fail with the stub's message (`UnsupportedOperationException: TODO [SEC-n]:
…`), so the failure tells you which TODO it is waiting for.

| Test class | Checkpoints |
|---|---|
| `model.ModelTests` | SEC-1, SEC-2 |
| `service.ScannerAndRiskTests` | SEC-4, SEC-5 |
| `service.SbomAndThreatModelTests` | SEC-7, SEC-8 (service half) |
| `service.ThreatIntelTests` | SEC-10, SEC-11, SEC-12 |
| `security.SecurityTests` | Provided security regression tests (not graded) |
| `repository.jdbc.RepositoryTests` | SEC-3, SEC-6, SEC-8 (JDBC half), SEC-9, SEC-15 |
| `service.ReportTests` | SEC-16 |
| `PipelineTests` | SEC-17 |

**Dependencies you will notice.** Most tests start from the seeded in-memory data, and the
seed builds `Asset` objects, so **finish SEC-1 first**. Until then nearly everything fails
with `TODO [SEC-1]`. The `ReportTests` also need SEC-2, SEC-7, SEC-8 and SEC-12 (the report
calls those modules). `PipelineTests` is the end-to-end test and turns green last. The
SEC-11 and SEC-12 tests build their own inputs, so an unfinished CSV parser (SEC-10) does
not cost you SEC-11/12 points.

### Database setup (needed for the `[DB]` tests and for SEC-15)

The `RepositoryTests` marked `[DB]` talk to a real PostgreSQL database. If none is
reachable they are **skipped**, not failed. **A skipped test earns no points.** The graded
run is done with the database available, and SEC-15 can only be demonstrated against it.

```pgAdmin
createdb secsuite
Run schema\schema.sql
Run schema\dcl.sql
RUn schema\seed.sql

application-jdbc.properties: SECSUITE_DB_PASSWORD = "ChangeMe_App_123!"
```

The URL and username already default to
`jdbc:postgresql://localhost:5432/secsuite` and `secsuite_app`. Override
`SECSUITE_DB_URL`, `SECSUITE_DB_USER`, or `SECSUITE_DB_PASSWORD` when your local setup
differs; do not edit the committed properties files for personal credentials.

The tests connect as `secsuite_app`, the least-privilege role, which has no `DELETE`. Every
`[DB]` test therefore runs in a transaction that is rolled back afterwards, so the seed data
is never modified and you can re-run the tests as often as you like.

## 4. Running `Main` to produce the graded report

```bash
Click on the green arrow and select Run Main.main()
```

Answer `y` to register an analyst first
(username 3-32 chars `a-z 0-9 . _ -` starting with a letter; password 12-128 chars; a
contact email), then log in with the same credentials. Select **7** to run the deterministic
full assessment used by the expected figures in §2. The report is written to
`reports/security-assessment-report.md` (the directory is created if missing). Options 1-6
run individual workflows and option 0 logs out.

- `SECSUITE_ENCRYPTION_KEY` (Base64 of 32 random bytes, e.g. `openssl rand -base64 32`) sets
  the AES key. If it is unset, a temporary per-run key is used and a warning is printed. That
  is fine for the capstone.
- Scripted login (e.g. for CI):
  `printf 'y\nanalyst1\nCorrect-Horse-42\nanalyst1@example.com\nanalyst1\nCorrect-Horse-42\n' | mvn -q compile exec:java`
- `Main` runs on the `InMemory*` repositories by default. To run the real application with
  PostgreSQL after completing the JDBC checkpoints, set
  `$env:SPRING_PROFILES_ACTIVE = "jdbc"` and the datasource environment variables before
  launching. Do not rewrite `Main.run`; Spring selects the `@Profile("jdbc")` repositories.

---

## 5. Rubric
Code/tests - 50  ;Report and Assessment - 35 ; Quiz - 15

Total - 100 

**Is the time budget realistic?** On a 6 to 7 hour day, 30 min go to setup and the database
check, 10 min to producing the final report, and 15 min to wrap-up. That leaves 5 h 5 min
to 6 h 5 min for the checkpoints, against a suggested 4 h 11 min, so you have 54 minutes
to 1 h 54 min of slack. This is a summative assessment with no code-along, so you will need that
slack. Budget honestly:

- The times assume you read each INSTRUCTOR NOTE first. The notes spell out the shape of a
  correct answer; skipping them costs far more time than reading them.
- SEC-10 is the longest checkpoint. Do not let it eat the afternoon. If you pass 50 minutes,
  move on to SEC-11, SEC-12 and SEC-16 (their tests do not depend on SEC-10) and come back.
- A checkpoint's tests are independent of other checkpoints wherever possible, so partial
  work earns partial points. Leaving SEC-17 (3 points) unfinished still loses you the
  report artifact, so leave it 15 minutes.
- Suggested order: **1 → 2 → 4 → 5 → 7 → 8 → 12 → 10 → 11 → 16 → 17**, which gives
  you a runnable report, then **3 → 6 → 9 → 15** (the JDBC layer). The in-memory pipeline
  that produces the report does not need SEC-3, SEC-6, SEC-9, SEC-15 or the JDBC half of
  SEC-8. Numeric order also works; this order simply gets you to a runnable report sooner.

---

## 6. Checkpoints

Each checkpoint lists its files, the acceptance criteria (every box is checked by a graded
test unless marked *(manual)*), points and suggested time. The INSTRUCTOR NOTE in the code
repeats these criteria with the reasoning and the common mistakes.

### SEC-1: `Asset` validating constructor and entity equality 

**Edit:** `model/Asset.java`: the constructor, `equals`, `hashCode`.

- [ ] `hostname`, `ipAddress` and `ownerTeam` are trimmed **before** validation, and the
      **trimmed** value is what is stored.
- [ ] `id` non-null; `criticality` non-null. A null criticality is **rejected**, never
      defaulted to `LOW`.
- [ ] `hostname` non-blank, at most 253 characters, RFC 1123 shape (dot-separated labels of
      letters, digits and inner hyphens, each 1-63 chars). `web01'; DROP TABLE asset;--`,
      `-web01` and a 64-char label are rejected.
- [ ] `ipAddress` is dotted-quad IPv4 with **every octet 0-255**, matched against the whole
      value: `999.1.1.1`, `10.0.0` and `x10.0.0.1y` are rejected. IPv6 is out of scope.
- [ ] `ownerTeam` non-blank.
- [ ] Every failure throws `ValidationException` (not `IllegalArgumentException` or NPE).
- [ ] `equals`/`hashCode` use **`id` only**: the same id with a different owner is the same
      asset; a different id with identical fields is a different asset. Distinct transient
      assets both using sentinel id `0L` are not equal.

### SEC-2: `Severity.fromCvssScore` 

**Edit:** `model/Severity.java`: `fromCvssScore`.

- [ ] `0.0 → NONE`, `0.1-3.9 → LOW`, `4.0-6.9 → MEDIUM`, `7.0-8.9 → HIGH`, `9.0-10.0 → CRITICAL`.
- [ ] No gaps: a value between published bands belongs to the **lower** band
      (`3.95 → LOW`, `6.95 → MEDIUM`, `8.95 → HIGH`). Compare with `score < 4.0` rather
      than `score <= 3.9`.
- [ ] `< 0.0`, `> 10.0`, `NaN` and ±infinity throw `ValidationException`. Nothing out of
      range is silently bucketed.

### SEC-3: `JdbcAssetRepository` and `AssetRowMapper` 

**Edit:** `repository/jdbc/JdbcAssetRepository.java` (`findById`,
`findByHostname`, `findAll`), `repository/jdbc/mapper/AssetRowMapper.java` (`mapRow`).

The generated-id `save` implementation is given as the pattern for the other JDBC
repositories.

- [ ] Every value is bound through a `?` placeholder. Nothing is concatenated into SQL, not
      even the numeric id.
- [ ] `criticality` is stored by `name()`.
- [ ] `save` inserts only the non-id columns, retrieves the generated id with
      `RETURNING id`, and returns `asset.withId(id)`. It does not mutate the input.
- [ ] `findById`/`findByHostname`: an empty result is `Optional.empty()`. Use
      `query(...)` + empty check, not `queryForObject`, which throws for "not found".
- [ ] `findAll` is `ORDER BY hostname`.
- [ ] Spring's `org.springframework.dao.DataAccessException` is caught and re-thrown as
      `com.cyberdev.secsuite.exception.DataAccessException`. A database outage never
      turns into `Optional.empty()`.
- [ ] `mapRow` reads every column **by name**, converts `id` with
      `rs.getObject("id", Long.class)` and `criticality` with the strict `Criticality.parse`,
      builds the object through the SEC-1 constructor, and **never calls `rs.next()`**.
- [ ] A bad stored value (unknown or NULL criticality, invalid IP) fails with
      `ValidationException`.
- Tests: 2 mapper tests (no database) + 2 `[DB]` tests.

### SEC-4: `ScannerService.recordFinding` 

**Edit:** `service/ScannerService.java`: `recordFinding`.

- [ ] In this order, **before any write**: `assetId` non-null and `cveId` non-blank
      (`ValidationException`); the asset exists (`ValidationException`); the CVE exists in
      the catalog (`ValidationException`); no **OPEN** finding exists for this asset + CVE
      (`DuplicateFindingException`).
- [ ] Only then build the finding with transient id `0L` (status `OPEN`,
      `detectedAt = clock.instant()`), and return the persisted finding returned by `save`.
- [ ] A **RESOLVED** finding for the same asset + CVE is not a duplicate: a regression is
      recorded again.
- [ ] The duplicate check is a lookup **before** `save()`. Catching the storage layer's
      constraint error afterwards does not count (a test double detects it).

### SEC-5: Sealed risk bands, `assess` and `createEntry` 

**Edit:** `event/LowRisk.java`, `MediumRisk.java`, `HighRisk.java`, `CriticalRisk.java`
(compact constructors), `service/RiskRegisterService.java` (`assess`, `createEntry`).
The sealed `RiskAssessment` interface and the exhaustive `remediationWindowDays` switch are
given.

- [ ] Each record rejects a score outside its own band with `ValidationException`:
      **Low 1-6, Medium 7-12, High 13-19, Critical 20-25**.
- [ ] `assess(likelihood, impact)`: both must be 1-5 (else `ValidationException`, never
      clamped); score = `likelihood * impact`; returns the matching record. Boundary cases:
      (2,3)=6 Low, (2,4)=8 Medium, (3,4)=12 Medium, (3,5)=15 High, (4,4)=16 High,
      (4,5)=20 Critical, (5,5)=25 Critical.
- [ ] A Critical rating is **returned**, not thrown.
- [ ] `createEntry`: assess first; fail closed (`ValidationException`, nothing saved) if
      the asset is unknown, if `scanFindingId` is given but unknown **or belongs to a
      different asset**, or if `ownerAnalystId` is given but unknown.
- [ ] **Every** band is persisted, including Low. Build it with transient id `0L` and return
      the persisted entry returned by `save`, with `riskScore = likelihood * impact`,
      status `OPEN`, `createdAt = clock.instant()`, and
      `dueDate = today + remediationWindowDays(band)`: Critical 7, High 30, Medium 90,
      Low 180 days.

### SEC-6: `JdbcComponentRepository` and `ComponentRowMapper` 

**Edit:** `repository/jdbc/JdbcComponentRepository.java` (`findById`, `findAll`,
`linkCve`, `findCveIdsByComponentId`),
`repository/jdbc/mapper/ComponentRowMapper.java`.

The generated-id `save` implementation is given.

- [ ] SEC-3's pattern: parameterized SQL, empty result → `Optional.empty()`, Spring's
      exception wrapped.
- [ ] `findAll` is `ORDER BY application_name, component_name, component_version`, lists
      components with **and without** CVEs, and lists none twice (no `INNER JOIN` to
      `component_cve`).
- [ ] `linkCve` inserts one `component_cve` row.
- [ ] `findCveIdsByComponentId` returns the linked ids `ORDER BY cve_id`, and an **empty
      list** (never null) when there are none.
- [ ] The mapper reads by column name and does not touch CVEs.
- Tests: 1 mapper test + one 2-point `[DB]` test.

### SEC-7: `SbomService.findVulnerableComponents` 

**Edit:** `service/SbomService.java`.

- [ ] One `ComponentVulnerabilities` per component, in `componentRepository.findAll()`
      order.
- [ ] A component with no CVEs **is listed** with an **empty** list. Do not filter it out,
      and do not use null.
- [ ] Each linked id is resolved through `cveCatalogRepository.findById`. A link to a CVE
      missing from the catalog throws `ValidationException` and is never silently dropped.
- [ ] Repository calls only, no SQL (the service must work on the in-memory path).

### SEC-8: STRIDE coverage and threat-model entry persistence 

**Edit:** `service/ThreatModelingService.java` (`strideCoverage`),
`repository/jdbc/JdbcThreatModelEntryRepository.java` (`findByThreatModelId`),
`repository/jdbc/mapper/ThreatModelEntryRowMapper.java`.

The generated-id repository `save` implementation is given.

- [ ] `strideCoverage` returns **all six** `StrideCategory` keys in declaration order
      (S, T, R, I, D, E), including categories with no entries, which map to an **empty
      list**. Build it from the enum (e.g. an `EnumMap` pre-filled from `values()`), not
      from `groupingBy` over the data.
- [ ] An unknown or null threat-model id throws `ValidationException`. It is not reported as
      "zero threats".
- [ ] Repository: enums stored by `name()`, a null `mitigation` bound as NULL,
      `findByThreatModelId` parameterized and ordered, empty list for an unknown model. It
      never invents missing categories.
- [ ] Mapper: strict `StrideCategory.parse` / `ThreatModelEntryStatus.parse` (an unknown
      category fails closed), a NULL mitigation stays null.
- Tests: 3 points service (no database), 1 mapper, 1 `[DB]`.

### SEC-9: `JdbcThreatIntelAlertRepository` and `ThreatIntelAlertRowMapper` 

**Edit:** `repository/jdbc/JdbcThreatIntelAlertRepository.java`
(`findByExternalAlertId`, `findAll`, `findByIndicatorType`, `count`),
`repository/jdbc/mapper/ThreatIntelAlertRowMapper.java`.

The generated-id `save` implementation is given.

- [ ] Every value (all of it third-party feed text) is a bound parameter.
- [ ] NULL `related_cve_id`, `description` and `published_at` are bound and read back as
      null. `Timestamp.from(...)` is only called on a non-null instant, and the mapper
      null-checks `getTimestamp` before `toInstant()`.
- [ ] `findByExternalAlertId` is an **exact** `= ?` match (SEC-11's dedup key); a `%` in the
      argument matches nothing.
- [ ] `findByIndicatorType` binds `indicatorType.name()`; `count()` uses
      `queryForObject("SELECT count(*) …", Long.class)`; lists are
      `ORDER BY external_alert_id`.
- [ ] `save` is a plain INSERT, never an upsert.
- [ ] Mapper: `Severity.parseStoredAlertSeverity` (a stored `NONE` fails closed) and
      `IndicatorType.parse` (an unknown type fails closed).
- Tests: 2 mapper tests + 2 `[DB]` tests.

### SEC-10: CSV threat-intel ingestion, parse and validate 

**Edit:** `service/ThreatIntelCsvIngestionService.java`: `parseFile` and `validateRow`
(the private helpers below them are given).

*File level: throw `IngestionException`, never a raw `IOException`, never an empty result:*

- [ ] null path; missing file or not a regular file (a directory);
- [ ] larger than `MAX_FILE_BYTES` (5 MiB), checked **before** reading;
- [ ] not valid UTF-8; empty file (no header);
- [ ] header not **exactly** `EXPECTED_HEADER` (a leading UTF-8 BOM is tolerated; the right
      column names in the wrong order are rejected).

*Row level: skip the row, record why, keep going:*

- [ ] Blank lines are ignored and are not data rows. Line numbers are **physical and
      1-based**, with the header on line 1.
- [ ] Each other line is split with the given `CsvLineParser` (quoted fields may contain
      commas, `""` is an escaped quote) and validated. A `ValidationException` becomes
      `SkippedRow(lineNumber, claimedExternalId, reason)` and the loop continues. Catch
      `ValidationException` only: a database error is not "a bad row".
- [ ] Row rules: exactly 8 fields; no control characters in any field; required non-blank
      `external_alert_id`, `source`, `indicator_type`, `indicator_value`, `severity`,
      `published_at`; length caps (id/source 100, indicator 512, description 2000);
      `external_alert_id` only `[A-Za-z0-9._:-]`; `indicator_type` ∈
      `IP, DOMAIN, FILE_HASH, CVE`; severity ∈ `LOW, MEDIUM, HIGH, CRITICAL` (**`NONE` is
      rejected**); `published_at` as an ISO-8601 instant, an offset date-time, or a plain
      date (midnight UTC); `indicator_value` matches its type (IPv4 with 0-255 octets,
      domain, 32/40/64-hex hash, CVE id).
- [ ] **CVE reference rule (design decision):** a `related_cve_id`, when present, must be
      well-formed **and exist in `cve_catalog`**. Otherwise the row is **skipped**. It is
      never stored as a dangling reference and never silently nulled. For
      `indicator_type = CVE`, `related_cve_id` is required and must equal
      `indicator_value`.
- [ ] Parsed alerts use transient id `0L`; repositories return a new persisted alert containing
      the generated database identity without mutating the parsed alert. Use
      `ingestedAt = clock.instant()`. `parseFile`
      writes **nothing**.
- [ ] The shipped feed gives 13 data rows: 9 parsed, and 4 skipped at lines 11 (blank
      source), 12 (`URL` type), 13 (`last Tuesday`) and 14 (`CVE-2025-99999` not in the
      catalog).

### SEC-11: Idempotent persistence and deduplication 

**Edit:** `service/ThreatIntelCsvIngestionService.java`: `persistAndDeduplicate`.

- [ ] For each parsed alert, in order: if `findByExternalAlertId` finds it, record the id as
      a duplicate and continue. Otherwise save it and record the **persisted alert returned
      by `save`** as inserted.
- [ ] A duplicate is **not an error** (no exception) and **never overwrites** the stored
      alert (no upsert).
- [ ] The check happens **before** `save()`. Catching the unique-constraint error is not
      the check (a test double detects it).
- [ ] Persisting the same batch twice leaves the row count unchanged the second time. An id
      repeated inside one batch is inserted once.

### SEC-12: Threat-intel correlation 

**Edit:** `service/ThreatIntelAlertService.java`: `correlateWithFindings`.

- [ ] Only alerts of `indicatorType == CVE` (`findByIndicatorType(CVE)`, not `findAll()`).
- [ ] For each, the **OPEN** findings for its CVE (`findOpenByCveId`), giving one
      `CorrelationHit(asset, alert, finding)` **per matching finding**, since one alert can
      hit several assets. An unknown asset fails closed.
- [ ] Sorted by hostname, then external alert id.
- [ ] Zero false positives: a RESOLVED finding is not a hit (TI-SEED-002), and a
      `FILE_HASH`/`IP`/`DOMAIN` alert is never a hit even when its related CVE has an open
      finding (TI-SEED-005). The seed alone yields exactly one hit (web-prod-01 ←
      TI-SEED-001); after the full pipeline there are five.

### SEC-13 and SEC-14: Provided authentication and cryptography

**No student edits are required.** `PasswordHasher`, `EncryptionService`, and `AuthService`. 
They remain in the project as readable examples of:

- PBKDF2-HMAC-SHA256 with a fresh salt and constant-time verification;
- AES-256-GCM with a fresh IV and authenticated decryption;
- normalized registration, encrypted contact information, generic login failures, and a
  dummy-hash timing path that reduces username enumeration.

`SecurityTests` protects this provided behavior against accidental regressions but carries no
graded points. Students should call these services normally and must not replace them with
plaintext storage, reversible password encryption, or hard-coded keys.

### SEC-15: Fix the SQL injection in `searchByHostname` 

**Edit:** `repository/jdbc/JdbcAssetRepository.java`: `searchByHostname`.

This method is **not** a stub. It ships working and **vulnerable** (it concatenates the
keyword into the SQL). Run the SEC-15 tests before you change it and watch them fail, then
fix it.

- [ ] The SQL text is constant; the keyword travels as a bound `?` parameter, with the
      `%…%` wildcards added to the **parameter value**.
- [ ] `searchByHostname("prod")` still returns every host containing `prod`; `null` returns
      an empty list.
- [ ] `x' OR 1=1 --` and `' OR '1'='1` return **no rows** (not the whole table).
- [ ] `x' UNION SELECT id, username, '10.0.0.1', password_hash, 'LOW' FROM analyst --`
      returns no rows. It does not leak analyst rows shaped as assets.
- [ ] `o'brien` returns no rows and causes no SQL error.
- [ ] *(manual)* A blacklist, hand-doubled quotes or regex validation in front of
      concatenation is a near-miss, not a fix. Parameterization is the fix.
- Optional polish (no extra points): escape `%`, `_` and `\` in the keyword and declare
  `ESCAPE '\'`, so the search is a plain substring match like the in-memory version.
- All five points are `[DB]` tests: this vulnerability only exists where there is a query
  language.

### SEC-16: Markdown report generation 

**Edit:** `service/ReportService.java`: `buildMarkdown` and `generateMarkdownReport`.
The eight `append…` section builders and `escapeMd` are given.

- [ ] `buildMarkdown(preparedBy, runs)`: reject a null `preparedBy`
      (`ValidationException`); start with `# SecOps Analyst Suite -- Security Assessment
      Report`, then the header block (`Generated` from the clock as ISO-8601, `Prepared by`,
      `Scope: N assets, M catalogued CVEs`, the fictional-data note), then **all eight
      sections exactly once, in order**: Asset Inventory, Open Scan Findings by Severity,
      Risk Register, SBOM, STRIDE Coverage, Threat Intelligence Alerts, Correlation, CSV
      Ingestion Summary.
- [ ] A section with no data still appears and says `_None._`.
- [ ] **Every** data value is escaped with `escapeMd`, including the username in the
      header: no raw `<script>`, no forged heading from a newline in feed text.
- [ ] `generateMarkdownReport(path, preparedBy, runs)`: reject a null path or analyst
      (`ValidationException`, nothing written); create missing parent directories; write
      exactly `buildMarkdown`'s text as **UTF-8**; return the written path.
- [ ] Any `IOException` is wrapped in `ReportGenerationException`: never escaped raw, never
      swallowed.

### SEC-17: `Main.run`, the composition root 

**Edit:** `Main.java`: `run(ConsoleUI ui, Path reportFile)`. `main`, `runPipeline`,
`resolveClock` and the small helpers are given.

- [ ] Build one shared instance of every `InMemory*` repository. `Main.run` is the
      deterministic, database-free grading seam; the real `main` method uses Spring profiles
      to select in-memory or JDBC repositories.
- [ ] Build every service by constructor injection, all sharing **one** `Clock` from
      `resolveClock()`, and each repository shared by every service that uses it (one
      alert repository for ingestion and correlation, and so on).
- [ ] Load the seed with `InMemorySeedLoader.load(...)`.
- [ ] Run `ui.runLoginGate(authService)`. On failure the `AuthenticationException`
      propagates out of `run` and **nothing after the gate runs** (no report, no
      directories).
- [ ] Hand everything to the given `runPipeline(...)` with the `reportFile` parameter, and
      return its result.

---

## 7. Manual code-quality review 

| Area |  What the reviewer reads for |
|---|---|
| Injection discipline | SEC-3/6/8/9/15: every value parameterized, including SEC-15 (hand-escaping that happens to pass the tests is a near-miss); no SQL in services |
| Fail-closed judgment |  no silent defaults, no swallowed exceptions, no `catch (Exception e) { return … }`; exception types used as documented |
| Dependency injection and design |  no service builds its own repository; no no-arg convenience constructors; exhaustive sealed switches with **no `default`**; shared collaborators remain shared |
| Readability and maintainability |  clear names, small methods, no dead or commented-out attempts, no edits to given tests or provided infrastructure |

**Do not edit the given tests or given infrastructure.** If you think one is wrong, ask
your instructor.

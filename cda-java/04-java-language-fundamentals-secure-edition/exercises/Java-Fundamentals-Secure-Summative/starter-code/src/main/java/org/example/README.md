# Secure Java Fundamentals Capstone — Security Analyst Console

## What this project is

A menu-driven Java console application that simulates a security
analyst's workflow: ingest several untrusted data feeds (a threat-intel
feed, port scan results, CVSS/EPSS vulnerability scores, an SBOM
dependency list, and a legacy pipe-delimited scan log), validate every
field at the trust boundary, build a prioritized risk register, and
generate a durable executive summary report.

The reference classes you're given (`PortScanResult`, `CvssScore`,
`EpssScore`, `SimpleJson`, `ValidationException`, `ParseResult`) are
fully implemented — study them first. Everything else in `src/` has had
its graded logic replaced with a `TODO` stub that throws
`UnsupportedOperationException`. **The project compiles as-is.** Your
job is to implement each TODO until the application runs end-to-end.

> **NOTE ON NUMBERING:** item 2 below was written across two lines in
> the original competency list ("Identify security risks from leaked
> mutable state" / "Apply defensive copying to constructors, getters").
> This README treats those as two separate competencies (2 and 3) so the
> list totals 14 — flag it if that split isn't what was intended.

## Running

SecurityAnalystConsole
# 

Until you implement the TODOs, selecting any menu option other than `0`
(Exit) will throw `UnsupportedOperationException` — this is expected.
That's "red," by design, until you implement the corresponding method.

## The 12 target competencies

 In addition to Java programming fundamentals - (variables and data types, logical branching, loops, arrays, enums, classes, object, methods, etc.) 
 1. Explain the difference between a value and a reference in Java.
 2. Identify security risks from leaked mutable state.
 3. Apply defensive copying to constructors and getters.
 4. Apply immutability to eliminate a class of state-tampering bugs.
 5. Explain why all external input is untrusted and must be validated at
    the trust boundary.
 6. Distinguish allow-list validation from deny-list validation and
    state why allow-list is preferred.
 7. Apply strict validation of type, range, length, and format.
 8. Implement fail-closed parsing that rejects malformed input rather
    than coercing it.
 9. Explain how verbose exceptions and stack traces can leak sensitive
    internal information to an untrusted audience.
10. Apply catch scope discipline — catch specific exceptions, never
    swallow them silently.
11. Design safe error surfaces: generic user-facing messages vs.
    detailed server-side logging.
12. Apply try-with-resources (or try/finally) to release resources
    safely on every exit path, including failure paths.
13. Explain how test-driven development applies to security: writing
        failing tests that encode security requirements *before* the code
        that satisfies them exists.
14. Understand how to write abuse-case tests asserting that invalid or malicious input is
    rejected safely.


Every TODO below is tagged with the competency numbers it exercises.

---

## TODO-by-TODO breakdown

### `model/ThreatIndicator.java`

**User story:** As a security analyst, I need `ThreatIndicator` objects
to be tamper-proof once created, so no other part of the application can
rewrite an indicator's tag list after it's recorded.

| TODO | Acceptance criteria | Competencies |
|---|---|---|
| Constructor | Mutating the caller's list *after* construction must not change this object's state. | 1, 2, 3 |
| `getTags()` | Mutating the list returned by `getTags()` must not change this object's state (defensive copy, or throw on mutation). | 1, 2, 3, 4 |

### `model/Dependency.java`

Same user story and acceptance-criteria pattern as `ThreatIndicator`,
applied to `knownCves`. **Competencies: 1, 2, 3, 4.**

### `model/RiskRegisterEntry.java`

**User story:** As a security analyst, I need to update a risk's status
and append notes over time, without any external code being able to
rewrite the entire notes history directly.

| TODO | Acceptance criteria | Competencies |
|---|---|---|
| Constructor | `relatedCveIds` is set once and defensively copied; never changes after construction. | 1, 2, 3, 4 |
| `getNotes()` | Never returns the live internal list. | 1, 2, 3 |
| `addNote(String)` | The only way `notes` grows; rejects null/blank notes. | 2, 6, 7 |
| `updateStatus(String)` | The only way `status` changes; validates against the allow-listed status set. | 2, 6, 7 |

### `util/InputValidator.java`

**User story:** As the single trust-boundary chokepoint for this
application, every external field must pass through here before it
becomes part of a domain object.

`validateEcosystem(...)` is given, fully implemented, as your reference
example for the allow-list pattern. Every other method below is a TODO.

| TODO | Acceptance criteria | Competencies |
|---|---|---|
| `requireNonBlank` | Shared null/blank guard used by every other method. | 5, 7 |
| `validatePort` | Range `[0, 65535]` inclusive. | 5, 7 |
| `validateProtocol` | Allow-list `{TCP, UDP}`, case-insensitive in, uppercase out. | 5, 6, 7 |
| `validatePortState` | Allow-list `{OPEN, CLOSED, FILTERED}`. | 5, 6, 7 |
| `validateIndicatorType` | Allow-list `{IP, DOMAIN, FILE_HASH}`. | 5, 6, 7 |
| `validateIndicatorValue` | Type-aware format check (IPv4 / domain / SHA-256 regex) plus max length. | 5, 7, 8 |
| `validateConfidence` | Range `[0, 100]` inclusive. | 5, 7 |
| `validateCvssScore` | Range `[0.0, 10.0]` inclusive; explicitly rejects `NaN`. | 5, 7 |
| `validateUnitInterval` | Range `[0.0, 1.0]` inclusive; explicitly rejects `NaN`. | 5, 7 |
| `validateCveId` | Format `CVE-YYYY-NNNN+` via regex. | 5, 7 |
| `validateTextField` | Non-blank plus max length (inclusive boundary). | 5, 7 |

### `parse/ThreatIntelFeedParser.java`, `PortScanResultParser.java`, `SbomDependencyParser.java`, `CvssEpssParser.java`

**User story (all four):** As a security analyst, I need one malformed
record in an untrusted feed to be rejected on its own, without taking
down ingestion of the rest of the feed — but a corrupted top-level
document should fail the entire feed, since there's no reasonable
partial interpretation of it.

| TODO | Acceptance criteria | Competencies |
|---|---|---|
| `parseFeed` / `parseResults` / `parseDependencies` / `parseScores` | Whole-document JSON failure → `ValidationException` for the entire feed. Per-record failure → that record rejected and recorded in `ParseResult`, parsing continues. | 5, 8, 9, 10 |
| `parseRecord` (each parser) | Every field routed through the matching `InputValidator` method before becoming part of a domain object; values are never coerced or defaulted. | 5, 6, 7, 8 |
| `requireString` / `requireInt` / `requireDouble` (each parser) | Missing or wrong-typed field → `ValidationException` naming the field; `requireInt` additionally rejects non-whole numbers rather than truncating. | 7, 8 |

`CvssEpssParserTest` also contains **Bonus TODO #1**: a whole-document
rejection test you write yourself, following the pattern already
demonstrated for the other three parsers.

### `service/RiskRegisterService.java`

**User story:** As a security analyst, I need every vulnerable
dependency turned into a risk register entry whose severity reflects
*both* how bad a vulnerability could be (CVSS) and how likely it is to
actually be exploited (EPSS) — a CVSS 7.5 with 90% EPSS probability is
more urgent than a CVSS 9.8 never seen exploited.

`topRisks(...)` and `filterByStatus(...)` are given, fully implemented.

| TODO | Acceptance criteria | Competencies |
|---|---|---|
| `buildRegister` | A CVE with no matching CVSS record still produces an entry, marked `"UNSCORED"` with a sentinel score outside `[0.0, 10.0]` — never silently defaulted to a numeric 0. | 8 |
| `classifySeverity` | Implements the exact CRITICAL/HIGH/MEDIUM/LOW thresholds (see in-code Javadoc), all boundaries inclusive. | 7 |
| `countBySeverity` | Always returns all five severity buckets, including zero-count ones. | — |

`RiskRegisterServiceTest` also contains **Bonus TODO #2**: an exact
boundary test (`cvss=7.0, epss=0.50 → CRITICAL`) you write yourself.

### `report/ExecutiveReportGenerator.java`

**User story:** As a security analyst, I need a single, readable
executive summary file at the end of a session — a durable File I/O
artifact leadership can review without touching the console app.

| TODO | Acceptance criteria | Competencies |
|---|---|---|
| `writeExecutiveSummary` | Creates the output directory if missing; writer is managed with try-with-resources; on `IOException`, logs full detail via `SecurityLogger` and returns `false` rather than throwing. | 11, 12 |
| `writeHeader` | Title/date header plus dependency counts. | — |
| `writeIngestionStats` | **Security-critical:** writes only accepted/rejected *counts* per feed — never `sampleRejectionReasons` content, since that string is built from untrusted feed data. | 5, 11 |
| `writeSeverityBreakdown` | Lists every severity bucket and its count. | — |
| `writeTopRisks` | Lists the top risks; treats a negative (UNSCORED-sentinel) EPSS value as `0.0` for display rather than showing a nonsensical negative probability. | — |

`ExecutiveReportGeneratorTest` also contains **Bonus TODO #3**: a test
asserting that a rejection-reason string never appears verbatim in the
generated file — directly exercising competency 5.

### `legacy/LegacyAssetProfile.java`, `LegacyDependencyScanner.java`, `LegacyReportArchiver.java`

These three classes each carry a Javadoc comment documenting the
specific bug(s) that existed in the *original* starter code
before it was fixed. Your job is **not** to reproduce those bugs — it's
to write a correct implementation from scratch, using the bug
descriptions as a checklist of mistakes to avoid.

| Class | Bugs documented (avoid these) | Competencies |
|---|---|---|
| `LegacyAssetProfile` | Constructor aliasing instead of copying; getter returning the live list; an unnecessary `setHostname()`. | 1, 2, 3, 4 |
| `LegacyDependencyScanner` | Deny-list instead of allow-list; swallowed exceptions; resource leak; raw stack trace to stdout; score coercion instead of rejection. | 6, 8, 9, 10, 11, 12 |
| `LegacyReportArchiver` | Manually-managed streams leaked on a failure path. | 12 |

### `SecurityAnalystConsole.java`

The menu loop, banner, and file/error-reporting helpers are given,
fully implemented. Each of the eight private "wiring" methods below
needs to call the matching parser/service/report class you implemented
elsewhere in this project.

**User story:** As a security analyst, each menu option should load one
feed (or build the register, or generate the report), tell me how many
records were accepted/rejected, and never crash the whole console on a
single bad record or a single bad file.

| TODO | Competencies |
|---|---|
| `loadThreatIntelFeed`, `loadPortScanResults`, `loadCvssEpssScores`, `loadSbomDependencies` | 9, 10, 11 |
| `loadLegacyDependencyScan` | 9, 10, 11 |
| `buildAndShowRiskRegister` | — |
| `generateExecutiveReport` | 11 |
| `runAbuseCaseDemo` (menu option 8) | 8, 9, 10, 11, 14 |

`runAbuseCaseDemo` reads `data/malicious_sample_feed.json` — a
deliberately hostile file mixing SQL/script-injection-style values,
out-of-range numbers, malformed IPs/domains/hashes, wrong JSON types,
and non-object records — and should show that nearly everything in it
is rejected without crashing the console. This is competency 14 in
action.

---

## Competencies 13 & 14 in context - Security in Testing

Bonus I: (text file, doc, or pdf) - name the file bonus
1. Explain How test-driven development applies to security: writing failing tests that encode security requirements first.
2. Explain the importance of writing Abuse-case tests. 

Bonus II: Override equals and hashcode appropriately.

## Data files

| File | Used by |
|---|---|
| `data/threat_intel_feed.json` | Menu option 1 |
| `data/port_scan_results.json` | Menu option 2 |
| `data/cvss_epss_scores.json` | Menu option 3 |
| `data/sbom_dependencies.json` | Menu option 4 |
| `data/legacy_dependency_scan.log` | Menu option 5 |
| `data/malicious_sample_feed.json` | Menu option 8 (abuse-case demo) |

Every sample file deliberately mixes several valid records with several
invalid ones — expect partial acceptance, not all-or-nothing, from every
feed except the abuse-case demo (which should show *mostly* rejection).

# Audit Target: Source to Sink

This is the audit target for this lesson.

`audit-target-source-to-sink` is an AI-generated Spring Boot reporting service.
A scanner has already flagged a handful of **suspected findings** — the patterns
look dangerous — but a pattern match is only a hypothesis. Your job is to
**confirm or dismiss** each one by tracing untrusted input from its **source**
(where it enters the program) to its **sink** (the dangerous operation), testing
every hop for a sanitizer or other control along the way.

## Suspected findings to resolve

Each endpoint in `ReportController` feeds a service. Work each one:

- **A — `/reports` (`status` param) → `ReportService.findByStatus`** — suspected SQL injection.
- **B — `/reports/export` (`name` param) → `ExportService.readExport`** — suspected path traversal.
- **C — `/reports/rollup` (`period` param) → `ScheduleService.runRollup`** — suspected command injection.
- **D — `/reports/ingest` (request body) → `IngestService.restore`** — suspected insecure deserialization.
- **E — `/reports/ranked` (`sort` param) → `ReportService.rankedBy`** — suspected SQL injection.

## Your task

For each suspected finding, produce a confirm-or-dismiss record:

1. Name the **source** and the **sink**.
2. **Trace** the data flow between them, checking each hop for a sanitizer,
   validator, allow-list, or other control.
3. Decide **confirm** or **dismiss**.
   - For a **confirm**, write the specific **triggering input** (the actual
     string or value) and the unbroken source-to-sink path as evidence.
   - For a **dismiss**, write the **reason** and label it a *false positive*
     (no untrusted input reaches the sink, or a control neutralizes it) or
     *theoretical-only* (reachable in code but blocked by an external control).
4. For every **confirmed** finding, assign a **severity** from exploitability,
   impact, and exposure, and a **block-vs-log** disposition.

Hold the same evidence bar on both decisions: a dismissal needs a written reason,
not a shrug. Not every flagged item is a real vulnerability — that is the point.

There is no answer key in this folder. Produce your records on your own.

## Build

```
mvn -q compile
```

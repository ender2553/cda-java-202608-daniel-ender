Source-to-Sink Security Audit
Project: audit-target-source-to-sink
Framework: Spring Boot 3.2.2 / Java 17
Findings reviewed: A–E

1. Executive Summary

| ID | Vulnerability | Decision | Classification | Severity | Disposition |
|---|---|---|---|---|---|
| A | SQL injection (`status`) | **CONFIRM** | True positive | High | BLOCK |
| B | Path traversal (`name`) | **CONFIRM** | True positive | High | BLOCK |
| C | Command injection (`period`) | **DISMISS** | False positive | N/A | LOG |
| D | Insecure deserialization (body) | **CONFIRM** | True positive | Critical | BLOCK |
| E | SQL injection (`sort`) | **DISMISS** | False positive | N/A | LOG |


Results: 3 confirmed vulnerabilities, 2 dismissed findings.

2. Individual Findings
A — SQL Injection: /reports
Decision: CONFIRM

Severity: HIGH
Disposition: BLOCK

- Source: ReportController.list() — HTTP status request parameter.
- Sink: ReportService.findByStatus() — jdbc.queryForList(sql).

Data-flow trace:

1. Attacker supplies the status query parameter.
2. ReportController.list() receives it without validation.
3. Controller passes it directly to reportService.findByStatus(status).
4. findByStatus() concatenates the value into an SQL statement.
5. jdbc.queryForList(sql) executes the resulting SQL.

Triggering input:

GET /reports?status=' OR '1'='1' --

Resulting SQL:

SELECT id, title, owner, status
FROM reports
WHERE status = '' OR '1'='1' --'

The injected condition evaluates to true, potentially returning every report.

Controls examined:

- No input validation.
- No sanitization.
- No parameterized query.
- No allow-list.
- No interceptor protecting this endpoint.

Conclusion: Confirmed SQL injection. Untrusted input reaches the SQL execution sink without neutralization.

Recommended fix:
public List<Map<String, Object>> findByStatus(String status) {
    String sql = "SELECT id, title, owner, status FROM reports WHERE status = ?";
    return jdbc.queryForList(sql, status);
}

B — Path Traversal: /reports/export
Decision: CONFIRM

Severity: HIGH
Disposition: BLOCK

- Source: ReportController.export() — HTTP name request parameter.
- Sink: ExportService.readExport() — Files.readAllBytes(path).

Data-flow trace:

1. Attacker supplies the name parameter.
2. Controller passes it directly to exportService.readExport(name).
3. ExportService constructs a path:

Path path = Paths.get(EXPORT_DIR + name);

4. No validation or canonicalization occurs.
5. Files.readAllBytes(path) reads the resulting file.

Triggering input:

GET /reports/export?name=../../etc/passwd

Resulting path:

/var/acme/exports/../../etc/passwd

The operating system resolves the traversal segments, potentially accessing:

/etc/passwd

Controls examined:

- No filename allow-list.
- No rejection of ../.
- No normalized-path containment check.
- No validation that the resulting file remains inside the export directory.
- No interceptor protecting this endpoint.

Conclusion: Confirmed path traversal. User-controlled input reaches a filesystem read operation without containment enforcement.

Recommended fix:

public byte[] readExport(String name) throws Exception {
    Path base = Paths.get(EXPORT_DIR).toRealPath();
    Path path = base.resolve(name).normalize();

    if (!path.startsWith(base)) {
        throw new SecurityException("Invalid export path");
    }

    return Files.readAllBytes(path);
}

Also consider restricting filenames to an expected pattern and rejecting symbolic-link escapes.

C — Command Injection: /reports/rollup

Decision: DISMISS

Classification: FALSE POSITIVE

- Source: ReportController.rollup() — HTTP period parameter.
- Sink: ScheduleService.runRollup() — Runtime.getRuntime().exec(cmd).

Data-flow trace:

1. Attacker supplies period.
2. Controller passes it to scheduleService.runRollup(period).
3. Service looks up the value in ROLLUP_SCRIPTS.
4. Only three exact values are accepted:

Map.of(
    "daily", "rollup_daily.sh",
    "weekly", "rollup_weekly.sh",
    "monthly", "rollup_monthly.sh"
);

5. Any other value returns "unknown period".
6. Only the hard-coded script name reaches command construction.

Triggering input attempted:

GET /reports/rollup?period=daily;whoami

The lookup returns null because the input does not match an accepted key.

Execution stops here:

if (script == null) {
    return "unknown period";
}

Controls examined:

- Exact-match allow-list.
- Hard-coded script names.
- Invalid values rejected before command execution.
- No direct concatenation of the original user input into the command.

Conclusion: Dismissed as a false positive. Although the command-execution sink exists, untrusted input cannot reach it as an executable command through this data flow.

Additional observation:

The implementation uses:

String[] cmd = {"/bin/sh", "-c", SCRIPT_DIR + script};

This introduces unnecessary shell interpretation.

Prefer:

String[] cmd = {"/bin/sh", SCRIPT_DIR + script};

This removes the shell interpreter and its command-parsing behavior.

The allow-list should remain in place.

D — Insecure Deserialization: /reports/ingest

Decision: CONFIRM

Severity: CRITICAL
Disposition: BLOCK
- Source: ReportController.ingest() — HTTP request body.
- Sink: IngestService.restore() — ObjectInputStream.readObject().
Data-flow trace:

1. Attacker submits an HTTP request body.
2. Controller receives it as a string:
public String ingest(@RequestBody String payload)

3. Controller passes it directly to:
ingestService.restore(payload);

4. Service Base64-decodes the attacker-controlled input:
byte[] raw = Base64.getDecoder().decode(payload);

5. The decoded bytes are passed to ObjectInputStream.
6. readObject() deserializes the supplied object.
Triggering input:

A Base64-encoded Java serialization stream containing a crafted object graph, potentially using a vulnerable gadget chain available on the application's runtime classpath.

The actual Base64 payload depends on the available gadget classes and runtime dependencies.

Controls examined:

- Base64 decoding is encoding, not sanitization.
- No object-type restrictions.
- No serialization filter.
- No class allow-list.
- No validation before deserialization.
- No safe data-transfer format.

Important network control:

WebConfig registers InternalOnlyInterceptor for:

.addPathPatterns("/reports/ingest");

The interceptor checks:

remote.startsWith("10.0.")

Requests from other addresses receive HTTP 403.

This is an exposure restriction, not a deserialization defense.

An attacker capable of reaching the endpoint from an accepted 10.0.x.x address can still submit malicious serialized data.

Classification: True positive, not theoretical-only. The vulnerable deserialization operation is reachable from the request body, although access is restricted by an IP-based control.

Conclusion: Confirmed insecure deserialization.

Recommended fix:

Avoid native Java deserialization for untrusted request data. Replace it with a constrained format such as JSON and validate the expected fields and types.

If native deserialization is absolutely necessary, use an ObjectInputFilter with a strict class allow-list and resource limits. This is defense in depth, not a substitute for avoiding untrusted native deserialization.

E — SQL Injection: /reports/ranked
Decision: DISMISS

Classification: FALSE POSITIVE

- Source: ReportController.ranked() — HTTP sort request parameter.
- Sink: ReportService.rankedBy() — jdbc.queryForList(sql).

Data-flow trace:

1. Attacker supplies sort.
2. Controller passes it to reportService.rankedBy(sort).
3. Service converts the value to lowercase.
4. The value is checked against a fixed allow-list:

Set.of("created_at", "title", "owner", "status");

5. Values outside the allow-list are replaced with:
column = "created_at";

6. Only the validated column name is concatenated into SQL.

Triggering input attempted:

GET /reports/ranked?sort=id;DROP TABLE reports

The value does not match an allowed column.

It is replaced with:
created_at

Resulting SQL:

SELECT id, title, owner, status
FROM reports
ORDER BY created_at DESC

Controls examined:

- Lowercase normalization.
- Fixed allow-list.
- Invalid values replaced with a safe default.
- Only allow-listed identifiers reach SQL construction.

Conclusion: Dismissed as a false positive. Although SQL string concatenation occurs, the user-controlled identifier is neutralized through strict allow-list validation before reaching the SQL execution sink.

Recommended improvement:

Keep the allow-list. Consider explicitly rejecting invalid values rather than silently defaulting them, depending on application requirements.

3. Final Audit Disposition

| ID | Source | Sink | Result | Evidence |
|---|---|---|---|---|
| A | `status` parameter | SQL execution | **CONFIRMED** | Direct SQL concatenation |
| B | `name` parameter | Filesystem read | **CONFIRMED** | Unvalidated path construction |
| C | `period` parameter | Shell execution | **DISMISSED** | Fixed allow-list blocks injected values |
| D | HTTP request body | Java deserialization | **CONFIRMED** | Untrusted data reaches `readObject()` |
| E | `sort` parameter | SQL execution | **DISMISSED** | Strict column allow-list |


Required remediation priorities

1. D — Critical: Remove unsafe native deserialization.
2. A — High: Replace SQL concatenation with parameterized queries.
3. B — High: Enforce normalized-path containment.
4. C — Improvement: Remove unnecessary shell invocation.
5. E — Maintain: Preserve the existing column allow-list.

Important audit distinction

The two dismissed findings demonstrate why source-to-sink analysis matters:

- C: A dangerous sink exists, but the allow-list prevents attacker-controlled input from reaching it.
- E: SQL concatenation exists, but the allow-list neutralizes the untrusted identifier.
Conversely:
- A: Untrusted input reaches SQL execution directly.
- B: Untrusted input controls the filesystem path.
- D: Untrusted input reaches native object deserialization.

Final verdict: Three confirmed vulnerabilities require remediation before deployment. Two scanner findings are false positives based on the controls present in the supplied code.
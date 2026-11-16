# Audit Target: Remediation

This is an AI-generated Spring Boot document service. The audit has already
happened — the findings below are **confirmed** (each was proven exploitable
during the prior review). Your job in this lesson is not to hunt; it is to
**remediate**.

You start from confirmation, and you take each finding all the way to **closed**
through the loop: **fix &rarr; verify &rarr; re-audit**. A changed line is only
*remediated*; a finding is *closed* only when the original exploit no longer
reproduces and your own diff has been re-audited for anything new.

## Build

```
mvn -q compile
mvn -q test
```

(There is a Maven wrapper-free `pom.xml`; a local Maven and JDK 17 are assumed.)

## Confirmed findings (carried forward from the prior audit)

These are real confirmations, not suspicions. Each was traced to an exploit.

- **F-01 &mdash; SQL injection in document search.**
  `DocumentService.search(...)` builds its query by string-concatenating the
  request parameters. Confirmed: `owner = x' OR '1'='1` returns every row.
  Severity: High.

- **F-02 &mdash; Path traversal in document download.**
  `DocumentService.readDocumentFile(...)` joins the request `filename` onto the
  storage root with no containment check. Confirmed: `filename=../../../etc/passwd`
  reads a file outside the storage root. Severity: High.

- **F-03 &mdash; Permissive security configuration.**
  `SecurityConfig` disables CSRF, permits every request, and registers a
  hardcoded in-memory admin credential. Confirmed: every endpoint is reachable
  unauthenticated, and the credential is committed in source. Severity: High.

- **F-04 &mdash; Missing authorization (object-level) on document access.**
  `DocumentController.getOne(...)` and the download endpoint return any document
  by id/filename with no check that the caller owns it. Confirmed: any caller can
  read any other owner's document. Severity: High.

## Your task

For each confirmed finding:

1. Run the **fix &rarr; verify &rarr; re-audit** loop. Track the finding's state:
   *confirmed &rarr; remediated &rarr; closed*.
2. Apply a **minimal, targeted** fix — change the smallest surface that closes
   the flaw, and no more. Then read your own diff with an auditor's eye.
3. Write a **regression test** that *fails on the original code and passes on
   your fix*. A verbal "it's fixed" is not accepted; the test is the deliverable.
4. **Verify** by re-running the original exploit input and confirming it no
   longer reproduces.
5. **Re-audit your diff** for any newly introduced flaw, regression, or sibling
   flaw left open.
6. For at least one finding, make and justify a **remediate vs.
   reject-and-regenerate** call. Remember: regenerated code is unverified code —
   it re-enters the full loop, it does not skip it.

Watch for the finding whose *obvious* fix introduces a new flaw, and the finding
whose density makes patching the wrong move. Record your closure evidence per
finding: final state, regression test, verification result, and re-audit note.

There is no answer key in this folder. Produce your remediation and evidence on
your own.

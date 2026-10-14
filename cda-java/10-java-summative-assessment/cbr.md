---
title: "Competency-Based Rubric: Secure Java Programming Capstone Review — VitalLedger"
module: "Curriculum-level capstone (no lesson-level module)"
course: "secure-java-programming-supplement"
hours: 102
created: 2026-07-22
last_updated: 2026-07-22
status: active
governing_spec: "curricula-design/design-process-documentation/capstone-design-process.md §8"
standard_source: "Appendix A-31 Cyber Developer Associate (DRAFT 2026-05-08)"
curriculum_slug: cyber-developer-associate
capstone_slug: secure-java-programming-supplement
capstone_course_outline: "courses/secure-java-programming-supplement/course-outline-secure-java-programming-supplement.md"
---

# Competency-Based Rubric: Secure Java Programming Capstone Review — VitalLedger

## Preamble

This is the curriculum-level Competency-Based Rubric (CBR) for the **Cyber Developer Associate** curriculum's **Secure Java Programming Capstone Review** — the **VitalLedger** secure clinical-records build. It follows §8 of `capstone-design-process.md`: it is organized **by OJL competency** (not by deliverable, not by task), rates each competency on a 4-tier scale (§8b), and applies the single **Proficient** pass bar (§8c) with **per-competency revision** (§8d).

This is the **one** course-level rubric for the entire capstone. It is filled once, by the supervisor, at the **T-final Capstone Presentation & Evidence Defense** — consolidating evidence observed across the whole build (the per-phase check-in gates, the two formal design reviews, and the final defense), not scored per phase-lesson.

It tags exactly **3 of the A-31 OJL competencies** — **3.a, 3.b, 3.c** (competency area 3, Secure Application Development) — which this capstone directly evidences. Other A-31 competency areas are exercised in the build but are evidenced as OJL competencies in their home phase courses, not re-tagged here (anti-over-tagging, governing spec §10).

**Capstone form.** A ~102h **facilitated** curriculum-level capstone (`capstone-design-process.md` §3c). From a provided design package, the apprentice builds **VitalLedger**, a secure clinical-records backend that holds protected health information (PHI), across six phases: a secure OOP domain model and encrypted-at-rest data layer (Phase B); a validating, injection-resistant, no-leak service layer with atomic transactions (Phase C); a Spring MVC REST API with authentication and role-based access control (Phase D); a secure LLM summarizer exposed over MCP with PHI minimization and prompt-injection defense (Phase E); and a TDD security test suite plus an AI-assisted security self-audit and remediation (Phase F). The testable contract is the security requirements spec (SEC-001…026). See `course-outline-secure-java-programming-supplement.md` for the per-phase breakdown and `facilitation-plan.md` for the delivery spine.

**Deliverable types** (cited by name in the Evidence field of each competency section):

1. **Service code repository** — VitalLedger end to end in version control: the reference-secure domain model, the encrypted-at-rest parameterized data layer, the validating service layer, the RBAC REST API, and the MCP-exposed LLM summarizer.
2. **Secure data layer** — AES-GCM encryption at rest on every marked PHI field, the secure parameterized repository, and the reference-secure domain entities (SEC-001…008). *Phase B.*
3. **Validating service + secure-transaction layer** — fail-closed allow-list validation, injection defense, no-leak secure exception handling, and the all-or-nothing create-with-audit transaction (SEC-009…014). *Phase C.*
4. **No-leak RBAC REST API** — the nine-endpoint contract with HTTP Basic auth, per-method RBAC under least privilege, secure headers, and RFC-7807 problem responses that disclose no internals (SEC-015…019). *Phase D.*
5. **Secure LLM/MCP summarizer** — PHI-minimized, prompt-injection-defended, role-separated summarization over an MCP-style tool (SEC-020…025). *Phase E.*
6. **Security test suite** — the TDD, exploit-named test suite driving edge-case/boundary inputs against the defenses (SEC-026 and the nine named test classes it asserts). *Phase F.*
7. **AI-assisted security self-audit & remediation writeup** — the audit across all layers, with every finding triaged and closed (remediated + re-verified by a test, or explicitly risk-accepted with written rationale). *Phase F.*
8. **Design-review & defense presentations** — the **Security Design Review** (after Phase C), the **AI-Integration Review** (after Phase E), and the **T-final Capstone Presentation & Evidence Defense** — instructor-observed, on the record, feeding this CBR.

**Not a portfolio rubric.** Per §8f of the governing spec, this CBR signs off the capstone specifically; it is not the full OJL portfolio rubric the apprentice maintains across the program.

## Signoff Header

**Course / curriculum:** Cyber Developer Associate — Secure Java Programming Capstone Review (VitalLedger)
**Capstone:** secure-java-programming-supplement
**Date:** _______________
**Apprentice:** _______________
**Supervisor:** _______________

## Tagged OJL Competencies

### 3.a: Write defensive backend code utilizing proper memory and reference security to prevent unauthorized data access.

**Observable behaviors / demonstrations:**

- Encrypted **every PHI field the spec marks** (`Patient.fullName`, `Patient.dateOfBirth`, `ClinicalRecord.note`) at rest via an AES-GCM converter — a fresh random 96-bit IV per write and a 128-bit authentication tag — so a raw database read returns **ciphertext**, and tampered ciphertext fails to decrypt rather than returning plaintext (SEC-001, SEC-002, SEC-004).
- Enforced **reference security** in the domain model: private fields, no setters, construction-only state, and defensive copies of the role set (in and out), so a caller cannot mutate authorization or reach PHI through a leaked reference; `ClinicalRecord` holds `patientId` as a scalar, not a mutable `Patient` reference (SEC-005).
- Sourced the AES key **only** from the environment with fail-fast startup, no default literal, and no key material in logs or exception messages; stored user passwords only as BCrypt hashes (SEC-003, SEC-007).
- Parameterized **all** persistence by construction (Spring Data derived queries + bound-parameter JPQL) with zero string-concatenated SQL, so input can never alter query structure (SEC-008).
- Kept audit entries **PHI-free** — actor, action, id-based `targetRef`, and timestamp only (SEC-006).

**Evidence (per apprentice's evidence map):** `<secure data layer>`, `<service code repository>` (domain + repository), `<security test suite>` (`EncryptionAtRestTest`, `AesGcmCipherTest`), `<Security Design Review presentation>`.

**Tier:** [ ] Beginning  [ ] Developing  [ ] Proficient  [ ] Advanced

**Tier guidance for this competency:**
- **Beginning** — Any spec-marked PHI field is stored in plaintext, or the domain model leaks a mutable reference to PHI or the role set, or any persistence path concatenates input into a query.
- **Developing** — Encryption is present but incomplete (a marked field missed, IV reused, or the key defaulted in code), or reference security is partial (exposed setters/collections), or parameterization is inconsistent; the data layer is not yet defensibly closed.
- **Proficient** — Every marked PHI field is AES-GCM encrypted at rest, the domain model leaks no mutable references, the key is environment-only with fail-fast, and all persistence is parameterized. Backing tests pass. A supervisor can sign off without follow-up questions.
- **Advanced** — As Proficient, plus tamper-detection and key-fail-fast are proven by test, a live "show me where PHI could leak" attempt is shown defended at the Security Design Review, and defense-in-depth (e.g., free-text SQL-marker scanning atop parameterization) is in place.

**Supervisor notes:** _required if Beginning or Developing_

---

### 3.b: Enforce strict input validation and use secure exception handling to gracefully manage errors without leaking sensitive system information.

**Observable behaviors / demonstrations:**

- Ran **fail-closed, allow-list validation** at the service boundary before any persistence — record-type allow-list, MRN and DOB format regex, and length/business bounds (including boundary cases 120/121 and 8000/8001) — rejecting everything not explicitly permitted, with controller bean-validation as the first gate producing `400 VALIDATION_FAILED` (SEC-009, SEC-010).
- Defended **injection in depth**: parameterized queries plus free-text SQL-meta-character scanning that rejects hostile `note`/`fullName` input; an injection payload in a note is rejected `400` (SEC-011, SEC-008 test).
- Handled exceptions **securely**: every error is an RFC-7807 `problem+json` body with a stable `code` and a `correlationId` and **no** stack trace, SQL, framework/class name, or unexpected-exception message; full detail goes to the server log only; a catch-all maps any unexpected exception to a generic `500` (SEC-012, SEC-013).
- Kept **multi-step operations consistent**: the create-record-with-audit unit is a single all-or-nothing transaction (audit write uses `MANDATORY` propagation) that rolls back entirely on any failure (SEC-014).
- Extended the no-leak posture to the API and the AI integration: per-method RBAC under least privilege with `401`/`403` as no-leak problem bodies and secure headers (SEC-015…019), and PHI-minimized, prompt-injection-defended, role-separated summarization that leaks nothing to the model or in its output (SEC-020…023).

**Evidence (per apprentice's evidence map):** `<validating service + secure-transaction layer>`, `<no-leak RBAC REST API>`, `<secure LLM/MCP summarizer>`, `<security test suite>` (`InputValidatorTest`, `RbacApiTest`, `TransactionRollbackTest`, `PromptGuardTest`), `<Security Design Review presentation>`, `<AI-Integration Review presentation>`.

**Tier:** [ ] Beginning  [ ] Developing  [ ] Proficient  [ ] Advanced

**Tier guidance for this competency:**
- **Beginning** — An input path reaches persistence unvalidated, or an error response leaks internals (stack trace, SQL fragment, class name), or a multi-step operation can leave inconsistent state.
- **Developing** — Validation is present but not fail-closed (deny-by-default) or misses boundary cases; or errors are mostly safe but at least one path leaks; or the create-with-audit unit is not atomic; the layer is not yet defensibly closed.
- **Proficient** — All untrusted input is validated fail-closed at the service boundary, injection is defended in depth, every error path leaks nothing (`problem+json`, no internals), multi-step operations are atomic, and RBAC + secure headers are enforced at the API. Backing tests pass. A supervisor can sign off without follow-up questions.
- **Advanced** — As Proficient, plus a live "trigger an error / make it leak" attempt is shown fully defended at the Security Design Review, boundary tests are mapped to each validation rule, and the secure-integration posture (no-leak, PHI-minimized, prompt-injection-defended summarizer) is demonstrated at the AI-Integration Review.

**Supervisor notes:** _required if Beginning or Developing_

---

### 3.c: Utilize Test-Driven Development (TDD) by actively generating edge-case inputs (e.g., null values) to test boundaries against exploits.

**Observable behaviors / demonstrations:**

- Authored the security test suite the **TDD way** — each test named for the exploit it guards, written **before** the defense is confirmed so it pins the boundary rather than rubber-stamping trusted code.
- **Actively generated hostile edge-case/boundary inputs** — null, empty/blank, oversized, malformed, out-of-range, control characters, injection strings — spanning validation, SQL injection, prompt injection, unauthorized role (`401` + `403`), PHI-in-summary (must be absent), ciphertext-at-rest, crypto tampering / key fail-fast, and transaction atomicity (SEC-026 and the nine named test classes it asserts).
- Kept the full suite **green** (`mvn clean test`) with genuine exploit/boundary coverage — not cosmetic happy-path tests.
- Ran an **AI-assisted security self-audit** across all layers, treating copilot output as reviewable and never trusted, triaged every finding, and closed each — remediated and re-verified by a test, or explicitly risk-accepted with written rationale (nothing left silently open).

**Evidence (per apprentice's evidence map):** `<security test suite>` (the nine named test classes), `<AI-assisted security self-audit & remediation writeup>`, `<service code repository>`, `<T-final Capstone Presentation & Evidence Defense>`.

**Tier:** [ ] Beginning  [ ] Developing  [ ] Proficient  [ ] Advanced

**Tier guidance for this competency:**
- **Beginning** — Tests are cosmetic/happy-path, or absent for the marked exploits, or written only after the fact to confirm existing code; audit findings are unresolved and undocumented.
- **Developing** — Some genuine boundary tests exist but coverage of the named exploit classes is thin, or the TDD authoring order is not evidenced; the audit was run but findings are only partially triaged and closed.
- **Proficient** — The suite is green with genuine edge-case/boundary tests across the exploit classes, each test names the exploit it guards, TDD authoring is evidenced, and every audit finding is closed (fixed + re-verified, or risk-accepted with rationale). A supervisor can sign off without follow-up questions.
- **Advanced** — As Proficient, plus boundary tests are demonstrably mapped to each named exploit, at least one audit finding is shown caught-and-fixed with its guarding test, and the apprentice defends a chosen boundary-test → exploit pairing live at the Evidence Defense.

**Supervisor notes:** _required if Beginning or Developing_

---

## Overall

**Pass bar (governing spec §8c):** The capstone is **Complete** when **every** tagged competency (3.a, 3.b, 3.c) rates **Proficient** or **Advanced**. A rating of **Beginning** or **Developing** on any competency means the capstone is **Beginning Complete**.

**Per-competency revision (governing spec §8d):** Revision is per-competency. When a competency rates Beginning or Developing, **only that competency** is returned to the apprentice for re-evidencing; Proficient and Advanced ratings on the other competencies stand and do not have to be re-defended. The apprentice produces additional evidence targeting only the failing competency, and the supervisor re-rates only that competency.

**Capstone status:** [ ] Complete (all 3 competencies Proficient or Advanced)  [ ] Beginning Complete (at least one competency Beginning or Developing — see returned competencies above)

**Returned for re-evidencing (if any):** _______________

**Supervisor signature / date:** _______________

## Change Log

| Date | Issue | Description |
|---|---|---|
| 2026-07-22 | apprenti-org/design-documentation#1456 (build #1459) | Initial CBR — the one course-level rubric for the VitalLedger capstone; tags A-31 competencies 3.a, 3.b, 3.c; scored at the T-final Evidence Defense. Modeled on the sibling CDA `transaction-validation-service-capstone` CBR. |

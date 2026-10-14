# VitalLedger — Sequenced Issue Backlog

This is the build plan, sequenced as six phase epics (**A–F**). Work them roughly in
order — later epics depend on earlier ones — but issues within an epic can often go in
parallel. Each issue names the design-package section and security requirement(s) it
implements and lists acceptance criteria. This backlog is what gets materialized as
GitHub issues + a project board.

**Design references:** `erd-vitalledger.md` (ERD), `flowcharts.md` (Flow A/B/C),
`api-contract.md` (API), `security-requirements.md` (SEC-001…SEC-026),
`architecture.md` (layers + stack).

**Legend:** each issue lists **Implements** (design section / SEC id) and **Acceptance**.

---

## Epic A — Setup

Stand up the project skeleton, datastore, key handling, and container/secrets posture.

- **A1 — Bootstrap Spring Boot 3.3 / Java 21 project.**
  Create the Maven project with `spring-boot-starter-web`, `-security`, `-data-jpa`,
  `-validation`, H2 (runtime), and test starters.
  *Implements:* architecture.md (tech stack).
  *Acceptance:* `mvn clean test` runs (empty green); Java 21; Spring Boot 3.3.x.

- **A2 — Configure H2 datastore, JPA, and no-leak error properties.**
  In-memory H2, `ddl-auto=create-drop`, `open-in-view=false`, H2 console off, and
  `server.error.include-*=never/false`.
  *Implements:* architecture.md (runtime posture); SEC-012.
  *Acceptance:* app boots against H2; error properties present; console disabled.

- **A3 — Environment-only AES key provider with fail-fast.**
  `EncryptionKeyProvider` reads `vitalledger.aes.key` (from `VITALLEDGER_AES_KEY`); no
  default literal; rejects missing/non-base64/wrong-length keys without leaking
  material.
  *Implements:* architecture.md (crypto); **SEC-003**.
  *Acceptance:* missing key stops startup; wrong length rejected; message names only the
  missing config, never key bytes.

- **A4 — Container + secrets hygiene.**
  Multi-stage Dockerfile (Temurin 21, non-root user), `docker-compose.yml` with
  env-file secrets, `.env.example`, and a `.gitignore` excluding `.env`, `*.pem`,
  `*.key`, `*.p12`.
  *Implements:* architecture.md (runtime/secrets); SEC-003, SEC-024.
  *Acceptance:* image builds and runs as non-root; no secret baked into the image;
  ignore rules present.

- **A5 — Seed demo users at startup.**
  An `ApplicationRunner` that seeds `clinician`/`auditor`/`admin` with BCrypt-hashed
  passwords only if no users exist. (Depends on B1 User + D2 encoder.)
  *Implements:* architecture.md (config); SEC-007.
  *Acceptance:* three demo users seeded once; passwords stored as BCrypt hashes, never
  plaintext.

## Epic B — Secure Domain Model & Data Layer

Build the entities with reference security, encryption at rest, and parameterized
repositories.

- **B1 — Role enum + User entity with reference security.**
  `Role {CLINICIAN, AUDITOR, ADMIN}`; `User` with `username`, BCrypt `passwordHash`, and
  a `Set<Role>` exposed as an unmodifiable defensive copy (in and out).
  *Implements:* erd-vitalledger.md; **SEC-005**, SEC-007.
  *Acceptance:* private fields, no setters; role set cannot be mutated through a leaked
  reference; roles persist via `user_roles` join.

- **B2 — Patient entity with encrypted PHI.**
  `medicalRecordNumber` (non-PHI, unique), `fullName` + `dateOfBirth` (PHI) annotated
  with the encrypting converter.
  *Implements:* erd-vitalledger.md (PHI inventory); **SEC-001**, SEC-005.
  *Acceptance:* PHI fields carry `@Convert`; construction-only state; no setters.

- **B3 — ClinicalRecord entity.**
  `patientId` scalar FK, `recordType` (non-PHI), encrypted `note` (PHI, max 8000),
  `authoredBy`, `createdAt`.
  *Implements:* erd-vitalledger.md; **SEC-001**, SEC-005.
  *Acceptance:* `note` encrypted at rest; holds `patientId` scalar not a `Patient`
  reference; immutable from caller's view.

- **B4 — AuditEntry entity (no PHI).**
  `actor`, `action`, id-based `targetRef`, `occurredAt`; immutable.
  *Implements:* erd-vitalledger.md; **SEC-006**.
  *Acceptance:* no PHI fields; timestamp set on construction; no setters.

- **B5 — AES-GCM cipher + encrypting JPA converter + bootstrap.**
  `AesGcmCipher` (`AES/GCM/NoPadding`, fresh 96-bit IV/write, 128-bit tag),
  `EncryptedStringConverter`, and the static bootstrap that injects the cipher.
  *Implements:* architecture.md (crypto); **SEC-002**, SEC-004.
  *Acceptance:* roundtrip works; IV unique per write; tampered ciphertext fails to
  decrypt; null passthrough.

- **B6 — Secure parameterized repositories.**
  `PatientRepository` (bound-param JPQL `:mrn` + `existsByMedicalRecordNumber`),
  `ClinicalRecordRepository` (`findByPatientId`), `UserRepository`,
  `AuditEntryRepository`. No string SQL.
  *Implements:* architecture.md (repository); **SEC-008**.
  *Acceptance:* all queries parameterized/derived; zero string concatenation.

- **B7 — Data-layer security tests.**
  `AesGcmCipherTest` (roundtrip, IV uniqueness, tamper, key fail-fast) and
  `EncryptionAtRestTest` (raw JDBC read = ciphertext; repository decrypts).
  *Implements:* **SEC-002, SEC-003, SEC-004, SEC-026**.
  *Acceptance:* both test classes green; a raw column read is asserted to be ciphertext.

## Epic C — Secure Service Layer

Add fail-closed validation, secure exceptions, and the transactional create-with-audit.

- **C1 — InputValidator (fail-closed allow-list).**
  Record-type allow-list, MRN/DOB regex, length bounds, and SQL-meta rejection for
  free-text.
  *Implements:* api-contract.md (field rules); **SEC-009, SEC-011**.
  *Acceptance:* rejects everything not explicitly allowed; boundary cases (120/121,
  8000/8001) behave correctly; messages never echo rejected values.

- **C2 — AuditService with MANDATORY propagation.**
  `record(actor, action, targetRef)` under `@Transactional(propagation = MANDATORY)`;
  `recent()` read-only.
  *Implements:* flowcharts.md (Flow A); **SEC-014**, SEC-006.
  *Acceptance:* audit write fails if no active transaction; entries carry no PHI.

- **C3 — PatientService.**
  Validate → uniqueness check → persist → audit, all `@Transactional`.
  *Implements:* flowcharts.md (Flow A); SEC-009, SEC-014.
  *Acceptance:* duplicate MRN → `ValidationException`; create writes a `PATIENT_CREATE`
  audit entry atomically.

- **C4 — ClinicalRecordService (transactional create-with-audit).**
  Validate → verify patient exists → save record → write `RECORD_CREATE` audit in the
  same transaction; plus list/get/delete.
  *Implements:* flowcharts.md (Flow A); **SEC-014**.
  *Acceptance:* record + audit are one atomic unit; missing patient → `NotFound`;
  delete writes a `RECORD_DELETE` audit.

- **C5 — Caller-safe custom exceptions.**
  `ValidationException` (400) and `ResourceNotFoundException` (404) with messages
  authored to be safe to return.
  *Implements:* api-contract.md (error model); SEC-012.
  *Acceptance:* messages contain no internal detail; map to the correct status.

- **C6 — Service-layer security tests.**
  `InputValidatorTest` (null/blank/oversized/allow-list/SQL-meta boundaries) and
  `TransactionRollbackTest` (audit failure rolls back the record insert).
  *Implements:* **SEC-009, SEC-011, SEC-014, SEC-026**.
  *Acceptance:* both green; rollback test proves atomicity.

## Epic D — Spring MVC REST API & Access Control

Expose the nine endpoints with DTO validation, RBAC, secure headers, and no-leak errors.

- **D1 — Request/response DTOs with bean validation.**
  `CreatePatientRequest` (`@Pattern` MRN/DOB, `@Size` name), `CreateRecordRequest`
  (`@Size` note/type), and response records + `MeResponse`, `SummaryResponse`,
  `AuditResponse`.
  *Implements:* api-contract.md; **SEC-010**, SEC-019.
  *Acceptance:* invalid DTO → `400 VALIDATION_FAILED` before service runs.

- **D2 — SecurityConfig (authn/authz/headers).**
  HTTP Basic, `BCryptPasswordEncoder`, `@EnableMethodSecurity`, stateless session, CSRF
  disabled (token-less API), and secure headers (HSTS, nosniff, frame-deny, CSP,
  no-referrer).
  *Implements:* flowcharts.md (Flow B); **SEC-016, SEC-017, SEC-018**.
  *Acceptance:* `/api/**` requires auth; headers present; stateless.

- **D3 — DomainUserDetailsService.**
  Load users from the store; map roles to `ROLE_*` authorities; compare only the BCrypt
  hash.
  *Implements:* flowcharts.md (Flow B); SEC-007, SEC-016.
  *Acceptance:* valid creds authenticate; unknown user → generic failure; no plaintext
  comparison.

- **D4 — No-leak error handling.**
  `GlobalExceptionHandler` (RFC-7807, stable `code` + `correlationId`, catch-all → 500,
  parser message not echoed), `ProblemWriter`, and 401/403 entry points.
  *Implements:* api-contract.md (error model); **SEC-012, SEC-013**.
  *Acceptance:* no stack/SQL/class names in any body; correlation id present; malformed
  body → generic `MALFORMED_REQUEST`.

- **D5 — Controllers with per-method RBAC.**
  `AuthController`, `PatientController`, `RecordController`, `AuditController` — all nine
  endpoints with `@PreAuthorize`, correct status/`Location` semantics.
  *Implements:* api-contract.md (all endpoints); **SEC-015, SEC-019**.
  *Acceptance:* every endpoint enforces its documented role; creates return `201` +
  `Location`; delete returns `204`.

- **D6 — RBAC + API security tests.**
  `RbacApiTest`: 401/403 cases, auditor-cannot-create, clinician-cannot-audit,
  only-admin-deletes, SQL-injection-in-note → 400, validation problem shape, secure
  headers present, happy-path CRUD + summarize.
  *Implements:* **SEC-008, SEC-010, SEC-012, SEC-015, SEC-016, SEC-017, SEC-019, SEC-026**.
  *Acceptance:* class green; asserts no exception/class names leak in error bodies.

## Epic E — Secure LLM Summarizer via MCP

Build the redaction → guard → role-separated stub-model → egress pipeline behind an
MCP-style tool.

- **E1 — LLM client abstraction + local stub.**
  `LlmClient` interface, `LlmMessage` (SYSTEM/USER/ASSISTANT), immutable `LlmRequest`,
  and `StubLlmClient` (no network, no key; treats input as inert data).
  *Implements:* architecture.md (llm); **SEC-024**, SEC-022.
  *Acceptance:* default client makes no outbound call; needs no API key; deterministic
  output.

- **E2 — PhiRedactor.**
  Strip patient name + DOB; mask SSN/phone/email/MRN patterns.
  *Implements:* flowcharts.md (Flow C step 1); **SEC-020**.
  *Acceptance:* redacted text contains no name/DOB/identifier patterns.

- **E3 — PromptGuard.**
  Detect and neutralize instruction-override markers; fence content as delimited data;
  strip fence-breakout attempts.
  *Implements:* flowcharts.md (Flow C step 2); **SEC-021**.
  *Acceptance:* known injection markers collapse to `[neutralized]`; content is fenced.

- **E4 — SummarizerService pipeline.**
  Redact → sanitize → fence → role-separated request (fixed SYSTEM + USER data) →
  stub complete → egress re-scrub → `SummaryResponse(..., phiRedactedBeforeModel=true)`.
  *Implements:* flowcharts.md (Flow C); **SEC-020, SEC-021, SEC-022, SEC-023**.
  *Acceptance:* summary contains no PHI even with an injection-laden note; steps run in
  the specified order.

- **E5 — MCP tool + registry.**
  `McpTool` contract, `McpToolRegistry` (tools/list + tools/call routing),
  `SummarizerTool` (`summarize_clinical_record` with `name`/`description`/`inputSchema`).
  *Implements:* architecture.md (mcp); api-contract.md endpoint 8; **SEC-025**.
  *Acceptance:* tool advertised with schema via `list()`; `call()` validates and routes
  by name.

- **E6 — LLM/MCP security tests.**
  `PhiRedactorTest`, `PromptGuardTest`, `SummarizerServiceTest` (no-PHI-in-summary,
  injection-neutralized-and-leaks-no-PHI), `McpToolRegistryTest`
  (tool-advertised-with-schema).
  *Implements:* **SEC-020, SEC-021, SEC-022, SEC-023, SEC-025, SEC-026**.
  *Acceptance:* all green; no network in the summarizer test path.

## Epic F — Security Self-Audit / Tests / Evidence

Prove the whole thing and produce the graded evidence.

- **F1 — Full suite green.**
  `mvn clean test` passes with all nine test classes.
  *Implements:* **SEC-026**.
  *Acceptance:* build is green; no skipped security tests.

- **F2 — Security self-audit against SEC-001…SEC-026.**
  Walk every requirement, confirm a passing verification exists, and record any gap.
  *Implements:* security-requirements.md (all).
  *Acceptance:* every SEC id has a named passing test or a documented design-review
  confirmation.

- **F3 — CBR → evidence map.**
  Author `SOLUTION-NOTES.md`-style mapping from each CBR element (3.a / 3.b / 3.c +
  LLM/MCP) to its source files and tests.
  *Implements:* security-requirements.md (traceability).
  *Acceptance:* every CBR element maps to source evidence + test evidence.

- **F4 — Secrets-hygiene verification.**
  Confirm no real key or PHI is committed; tests use a synthetic key + synthetic PHI;
  ignore rules hold.
  *Implements:* architecture.md (secrets posture); SEC-003, SEC-024.
  *Acceptance:* repo scan finds no real secret/PHI; `.env`/key patterns git-ignored.

- **F5 — README + run/build/docker docs.**
  Tech stack, build/test, run with `VITALLEDGER_AES_KEY`, Docker, demo users, API
  table, and security-design summary.
  *Implements:* architecture.md; api-contract.md.
  *Acceptance:* a new developer can build, test, and run from the README alone.

---

## Backlog summary

| Epic | Theme | Issues |
|---|---|--:|
| A | Setup | 5 |
| B | Secure Domain Model & Data Layer | 7 |
| C | Secure Service Layer | 6 |
| D | Spring MVC REST API & Access Control | 6 |
| E | Secure LLM Summarizer via MCP | 6 |
| F | Security Self-Audit / Tests / Evidence | 5 |
| **Total** | **6 epics** | **35** |

Every SEC-001…SEC-026 requirement is claimed by at least one issue, and every issue
traces to a design section. Build A→F; do not skip the tests in each epic — the tests
are the deliverable that proves the security posture.

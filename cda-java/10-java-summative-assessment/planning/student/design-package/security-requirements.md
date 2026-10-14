# VitalLedger — Security Requirements Specification

These are the testable security requirements your implementation must satisfy. Each has
a stable ID (`SEC-NNN`), a statement, the design artifact it derives from, and how it is
verified. They are grouped by the Competency-Based Rubric (CBR) element they demonstrate
so the graded evidence maps cleanly.

**How to read a requirement:** the statement is the contract; "Verify by" names a test
(or observable behavior) that must exist and pass. A requirement is met only when its
verification passes — evidence before assertion.

**CBR mapping:**
- **3.a** — Secure data layer (OOP domain model, reference security, encryption at rest, secure repository)
- **3.b** — Secure service + API layer (validation, injection defense, secure exceptions, secure transactions, RBAC API)
- **3.c** — Security tests (TDD, edge-case/boundary against exploits)
- **LLM/MCP** — PHI minimization + prompt-injection posture for the AI summarizer

---

## CBR 3.a — Secure data layer

| ID | Requirement | Derives from | Verify by |
|---|---|---|---|
| SEC-001 | The PHI fields `Patient.fullName`, `Patient.dateOfBirth`, and `ClinicalRecord.note` are encrypted at rest via a JPA attribute converter; no PHI is persisted in plaintext. | ERD PHI inventory; Flow A | `EncryptionAtRestTest` — raw JDBC read of PHI columns returns ciphertext |
| SEC-002 | Encryption uses AES in GCM mode with a fresh random 96-bit IV per write and a 128-bit authentication tag; tampered ciphertext fails to decrypt rather than returning plaintext. | Architecture (crypto) | `AesGcmCipherTest` — roundtrip, IV uniqueness, tamper detection, null passthrough |
| SEC-003 | The AES key is supplied **only** from the environment (`VITALLEDGER_AES_KEY`); there is no default literal, the app fails fast at startup if the key is missing or the wrong length, and key material is never logged, printed, or placed in an exception message. | Architecture (crypto); secrets hygiene | `AesGcmCipherTest.missingKey_failsFast_withoutLeakingMaterial`, `wrongKeyLength_failsFast` |
| SEC-004 | A raw database read of any PHI column returns ciphertext; the repository transparently decrypts on read for authorized callers. | ERD; Flow A | `EncryptionAtRestTest` |
| SEC-005 | Domain entities enforce reference security: private fields, no setters, construction-only state, and defensive copies of the role set (in and out) so a caller cannot mutate authorization or PHI through a leaked reference. `ClinicalRecord` holds `patientId` as a scalar, not a mutable `Patient` reference. | ERD; Architecture (domain) | Design review; exercised throughout the test suite |
| SEC-006 | Audit entries contain **no PHI** — only actor, action, an id-based `targetRef`, and a timestamp. | ERD; Flow A/B | Design review; `RbacApiTest` audit read returns no PHI |
| SEC-007 | User passwords are stored only as BCrypt hashes; plaintext passwords are never persisted, loaded, or compared. | Architecture (security) | `RbacApiTest` auth cases; `DomainUserDetailsService` uses stored hash only |
| SEC-008 | All persistence is parameterized by construction (Spring Data derived queries + bound-parameter JPQL); there is zero string-concatenated SQL, so input can never alter query structure. | Architecture (repository); Flow A | `RbacApiTest.sqlInjectionInNote_isRejected_400`; `InputValidatorTest` |

## CBR 3.b — Secure service + API layer

| ID | Requirement | Derives from | Verify by |
|---|---|---|---|
| SEC-009 | Fail-closed, allow-list validation runs at the service boundary before any persistence: record-type allow-list, MRN and DOB format regex, and length/business bounds. Everything not explicitly permitted is rejected. | API contract field rules; Flow A | `InputValidatorTest` (incl. boundaries 120/121, 8000/8001, allow-list) |
| SEC-010 | Controller bean-validation is the first gate on every request DTO (`@NotBlank`, `@Pattern`, `@Size`), producing a `400 VALIDATION_FAILED` before the service runs. | API contract; DTOs | `RbacApiTest.validationError_returnsProblemJson_withoutStackTrace` |
| SEC-011 | Free-text fields (`fullName`, `note`) are scanned for SQL meta-characters / statement markers and rejected as defense in depth on top of parameterized queries. | API contract; SEC-008 | `InputValidatorTest.rejectsSqlInjectionMarkersInFreeText`; `RbacApiTest.sqlInjectionInNote_isRejected_400` |
| SEC-012 | Every error response is an RFC-7807 `application/problem+json` body with a stable `code` and a `correlationId`; no stack trace, SQL, framework/class name, or unexpected-exception message ever appears in the body. Full detail goes to the server log only. | API contract error model | `RbacApiTest.validationError_returnsProblemJson_withoutStackTrace` (asserts no exception/class names in body) |
| SEC-013 | A catch-all handler maps any unexpected exception to a generic `500 INTERNAL_ERROR`; malformed-body parser messages are never echoed to the caller. | API contract error model | Design review; catch-all + `MALFORMED_REQUEST` handler |
| SEC-014 | The create-record-with-audit operation is a single all-or-nothing transaction; the audit write uses `MANDATORY` propagation so it can never commit outside the business transaction, and a failure in either step rolls back the whole unit. | Flow A; Architecture (service) | `TransactionRollbackTest.auditFailure_rollsBackTheRecordInsert` |
| SEC-015 | RBAC is enforced per method with `@PreAuthorize` under least privilege: CLINICIAN authors/reads/summarizes, AUDITOR reads records + audit log, ADMIN deletes. A clinician cannot read the audit log; an auditor cannot create; only an admin deletes. | API contract roles; Flow B | `RbacApiTest` (auditor-cannot-create 403, clinician-cannot-audit 403, only-admin-deletes) |
| SEC-016 | Authentication is HTTP Basic verified against BCrypt hashes; unauthenticated requests get `401 AUTH_REQUIRED` and bad credentials get `401` — both as no-leak problem bodies. | Flow B; Architecture (security) | `RbacApiTest.unauthenticated_isRejected_401`, `badCredentials_areRejected_401`, `authenticated_me_reportsRoles` |
| SEC-017 | Responses carry secure headers: HSTS, `X-Content-Type-Options: nosniff`, frame-deny, a restrictive CSP (`default-src 'none'; frame-ancestors 'none'`), and `Referrer-Policy: no-referrer`. | Flow B; Architecture (security) | `RbacApiTest.secureHeaders_arePresent` |
| SEC-018 | The API is stateless (no session); CSRF token flow is deliberately disabled for this token-less API because there is no cookie-based session to protect. | Architecture (security) | Design review; `SecurityConfig` stateless + csrf disabled |
| SEC-019 | The nine-endpoint REST contract is coherent: correct verbs, `201 Created` + `Location` on creates, `204 No Content` on delete, and the documented role gates. | API contract | `RbacApiTest` happy-path CRUD + summarize |

## LLM / MCP — Secure summarizer

| ID | Requirement | Derives from | Verify by |
|---|---|---|---|
| SEC-020 | PHI is minimized/redacted **before** any model call: the patient's name and DOB are stripped and SSN/phone/email/MRN patterns are masked. The model physically never receives a direct identifier. | Flow C step 1; Architecture (llm) | `PhiRedactorTest`; `SummarizerServiceTest.summary_containsNoPhi` |
| SEC-021 | Prompt-injection defense neutralizes instruction-override markers in record content and fences the content as delimited data, so a note cannot hijack the prompt. | Flow C step 2; Architecture (llm) | `PromptGuardTest`; `SummarizerServiceTest.promptInjectionInRecord_isNeutralized_andLeaksNoPhi` |
| SEC-022 | Role separation: a fixed trusted instruction rides in a SYSTEM message; untrusted record content is confined to a USER message and never concatenated into the system role. | Flow C step 3; Architecture (llm) | `SummarizerServiceTest` |
| SEC-023 | The model output is re-scrubbed for identifiers on egress (defense in depth) before being returned. | Flow C step 4 | `SummarizerServiceTest.summary_containsNoPhi` |
| SEC-024 | The default LLM client is a local stub: no network call and no API key. A real provider would read its credential only from the environment (`vitalledger.llm.api-key`) and is not wired by default. | Architecture (llm); secrets hygiene | All summarizer tests run with no network; `StubLlmClient` |
| SEC-025 | The summarizer is exposed as an MCP-style tool advertising `name`, `description`, and an `inputSchema` (tools/list), and is invoked by a validated argument map (tools/call). | Architecture (mcp); API endpoint 8 | `McpToolRegistryTest.summarizerToolIsAdvertisedWithSchema` |

## CBR 3.c — Security tests (TDD)

| ID | Requirement | Derives from | Verify by |
|---|---|---|---|
| SEC-026 | The full test suite is green (`mvn clean test`) and covers exploit/boundary cases: null/blank/oversized/malformed input, SQL injection, prompt injection, unauthorized role (401 + 403), PHI-in-summary (must be absent), ciphertext-at-rest, crypto tampering / key fail-fast, and transaction atomicity. | All flows + all SEC above | The named tests: `InputValidatorTest`, `RbacApiTest`, `PromptGuardTest`, `PhiRedactorTest`, `SummarizerServiceTest`, `EncryptionAtRestTest`, `AesGcmCipherTest`, `TransactionRollbackTest`, `McpToolRegistryTest` |

---

## Traceability at a glance

| CBR element | Requirements |
|---|---|
| 3.a Secure data | SEC-001 … SEC-008 |
| 3.b Secure service + API | SEC-009 … SEC-019 |
| LLM/MCP posture | SEC-020 … SEC-025 |
| 3.c Security tests | SEC-026 (asserts the others) |

Every requirement traces to a design artifact (ERD, flowcharts, API contract, or
architecture) and to at least one verification. If you add a requirement, add its
verification; a requirement with no test is not done.

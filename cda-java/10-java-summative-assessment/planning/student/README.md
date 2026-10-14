# VitalLedger — Secure Clinical-Records Backend (Student Starter)

This is the **student starter skeleton** for the **Cyber Developer Associate**
secure-development capstone. You will fork this repo and build VitalLedger yourself,
phase by phase, from the provided design package and the issue backlog.

VitalLedger is a small backend that stores **PHI** (protected health information) for a
clinic: patients, clinical records, users, and an audit trail. Because it holds PHI, a
breach is existential — so every layer has a security job. Your task is to implement
those security controls: encryption at rest, a parameterized secure repository, a
fail-closed validating service tier, an RBAC-protected REST API with no-leak error
handling, and a PHI-minimizing / prompt-injection-hardened LLM/MCP summarizer.

> **What's already here vs. what you build.** The project structure, class/interface
> declarations, fields, Spring annotations, DTOs, domain entities, repository
> interfaces, and the crypto **key provider** + config wiring are provided so the
> project compiles from day one. Every method that carries real security logic has its
> body replaced with a `// TODO(phase X): …` marker (non-void methods throw
> `UnsupportedOperationException("TODO(phase X)")`). You fill those in.

## How you work: the design package + the issue backlog

- **`design-package/`** — the provided design package: the scenario, the domain model,
  the security requirements, the API contract, and the Competency-Based Rubric (CBR)
  your work is graded against. Read it first. (It is delivered to you separately and
  dropped into this folder.)
- **Issue backlog** — the build is broken into issues, grouped by phase. Work them in
  order; each issue tells you which `TODO(phase X)` markers it closes.

## Build order (phases)

Implement in this order — each phase builds on the last:

| Phase | Focus | You implement (marked `TODO(phase …)`) |
|-------|-------|----------------------------------------|
| **A — Setup**              | Get the project building and running | project builds, key/config wiring understood, app boots with a key set |
| **B — Data + Encryption**  | Secure data layer | AES-GCM cipher, the JPA encrypting converter (domain entities + repository interfaces are given) |
| **C — Service**            | Secure service tier | fail-closed validation, exceptions, transactional create-with-audit, audit service |
| **D — API**                | Secure REST API | controllers, HTTP Basic + BCrypt auth, `@PreAuthorize` RBAC, secure headers, no-leak RFC-7807 error handling |
| **E — LLM / MCP**          | Secure summarizer | PHI redactor, prompt-injection guard, stub LLM client, MCP tool + registry, summarizer pipeline |
| **F — Tests**              | Security test suite | **you author the security tests** — evidence for CBR item 3.c |

Find the work with:

```bash
grep -rn "TODO(phase" src/main/java
```

## Tech stack

Java 21 · Spring Boot 3.3 (Spring MVC + Spring Security) · Spring Data JPA · H2
(in-memory) · Bean Validation · JUnit 5.

## Build & test

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)   # macOS; use any Java 21 toolchain
mvn test                                            # compile + run tests
```

The skeleton **compiles** out of the box. Tests are **RED until you implement the
code** — that is expected. A starter harness lives in
`src/test/java/com/vitalledger/ExampleSmokeTest.java`; the full security suite is your
Phase F deliverable.

## Run

The AES key is **required** and loaded **only** from the environment — the app fails
fast at startup without it (this behavior is provided; do not weaken it).

```bash
export VITALLEDGER_AES_KEY=$(openssl rand -base64 32)   # 32-byte AES-256 key, base64
mvn spring-boot:run
```

### Docker

```bash
cp .env.example .env         # then set VITALLEDGER_AES_KEY in .env
docker compose up --build
```

## Environment variables

Copy `.env.example` to `.env` (git-ignored) and set:

| Variable | Required | Purpose |
|----------|----------|---------|
| `VITALLEDGER_AES_KEY`     | **Yes** | Base64 AES key (16/24/32 raw bytes → AES-128/192/256) for encryption at rest. Generate with `openssl rand -base64 32`. App fails fast if unset. Binds to `vitalledger.aes.key`. |
| `VITALLEDGER_LLM_API_KEY` | No       | Only needed if you replace the local stub LLM client with a real provider. Blank → the no-network `StubLlmClient` is used. Binds to `vitalledger.llm.api-key`. |

Never commit real key material. `.env` and key/cert patterns are already git-ignored.

## Target API (what you will build)

| Method & path                          | Role(s)                    | Purpose                          |
|----------------------------------------|----------------------------|----------------------------------|
| `GET  /api/auth/me`                    | any authenticated          | identity + granted roles         |
| `POST /api/patients`                   | CLINICIAN, ADMIN           | create patient                   |
| `GET  /api/patients/{id}`              | CLINICIAN, AUDITOR, ADMIN  | read patient                     |
| `POST /api/patients/{id}/records`      | CLINICIAN                  | create record (+audit, atomic)   |
| `GET  /api/patients/{id}/records`      | CLINICIAN, AUDITOR, ADMIN  | list a patient's records         |
| `GET  /api/records/{id}`               | CLINICIAN, AUDITOR, ADMIN  | read one record                  |
| `DELETE /api/records/{id}`             | ADMIN                      | delete a record (+audit)         |
| `POST /api/records/{id}/summarize`     | CLINICIAN, ADMIN           | secure LLM/MCP summary           |
| `GET  /api/audit`                      | AUDITOR, ADMIN             | read the audit log (no PHI)      |

Demo users (`clinician` / `auditor` / `admin`) are seeded at startup once your auth and
password-encoder wiring is in place — see `config/SeedDataRunner`. These are clearly
labeled **demo** credentials for a reference app, not production secrets.

## Secrets hygiene

- `.gitignore` excludes `.env`, `*.pem`, `*.key`, `*.p12`, and credential patterns.
- No real secret or real PHI is committed. The test config uses a clearly-labeled
  **synthetic** test key and synthetic PHI only.

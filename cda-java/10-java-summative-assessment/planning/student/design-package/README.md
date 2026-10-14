# VitalLedger — Provided Design Package

The authoritative specification for the VitalLedger secure clinical-records capstone. You implement the code from this design; you do not invent the schema, endpoints, or security rules.

| File | What it gives you |
|---|---|
| `erd-vitalledger.md` | Entity-relationship diagram + data dictionary. **PHI fields that must be encrypted at rest are marked.** |
| `flowcharts.md` | The three key control flows: secure record create-with-encryption (+audit); authenticated/authorized (RBAC) access; the LLM-summarizer path with PHI redaction before the model call + prompt-injection defense. |
| `api-contract.md` | The REST API contract — every endpoint (method, path, required role, request/response schemas) and the problem/error model. |
| `security-requirements.md` | The security requirements spec (SEC-001…), stated as testable requirements traceable to the assessed competencies. |
| `architecture.md` | Layer/component architecture and the tech stack. |
| `issue-backlog.md` | The sequenced build backlog — six phase epics (A–F). Work these on your project board. |

Start with `architecture.md` and `erd-vitalledger.md`, then work the `issue-backlog.md` epics in order, checking each issue against the `security-requirements.md`.

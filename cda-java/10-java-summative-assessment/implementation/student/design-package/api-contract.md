# VitalLedger — REST API Contract

The API you will build. Nine endpoints across four controllers. Everything under
`/api/**` requires authentication (HTTP Basic); each endpoint additionally requires the
role(s) listed. All error responses use one shared RFC-7807 problem shape (see
[Error model](#error-model)).

**Base URL:** `http://localhost:8080`
**Auth:** HTTP Basic on every `/api/**` request (stateless — every request
re-authenticates). Passwords are checked against a BCrypt hash.
**Content types:** requests and success bodies are `application/json`; error bodies are
`application/problem+json`.

## Roles

| Role | Capabilities |
|---|---|
| `CLINICIAN` | Create/read patients and records; summarize a record |
| `AUDITOR` | Read patients, read records, read the audit log (no writes, no summarize) |
| `ADMIN` | Everything, including deleting a record |

## Endpoint summary

| # | Method & path | Required role(s) | Purpose |
|--:|---|---|---|
| 1 | `GET /api/auth/me` | any authenticated | Identity + granted roles |
| 2 | `POST /api/patients` | CLINICIAN, ADMIN | Create a patient |
| 3 | `GET /api/patients/{id}` | CLINICIAN, AUDITOR, ADMIN | Read a patient |
| 4 | `POST /api/patients/{patientId}/records` | CLINICIAN | Create a record (+audit, atomic) |
| 5 | `GET /api/patients/{patientId}/records` | CLINICIAN, AUDITOR, ADMIN | List a patient's records |
| 6 | `GET /api/records/{id}` | CLINICIAN, AUDITOR, ADMIN | Read one record |
| 7 | `DELETE /api/records/{id}` | ADMIN | Delete a record (+audit) |
| 8 | `POST /api/records/{id}/summarize` | CLINICIAN, ADMIN | Secure LLM/MCP summary |
| 9 | `GET /api/audit` | AUDITOR, ADMIN | Read the audit log (no PHI) |

---

## 1. GET /api/auth/me

Returns the authenticated caller's identity and granted authorities.

- **Auth:** any authenticated user.
- **Request:** none.
- **Response 200:**

```json
{ "username": "clinician", "roles": ["ROLE_CLINICIAN"] }
```

- **Errors:** `401 AUTH_REQUIRED`.

## 2. POST /api/patients

Create a patient. PHI fields (`fullName`, `dateOfBirth`) are encrypted at rest.

- **Auth:** CLINICIAN or ADMIN.
- **Request body:**

```json
{
  "medicalRecordNumber": "MRN-100427",
  "fullName": "Jane Q. Public",
  "dateOfBirth": "1980-02-29"
}
```

| Field | Type | Required | Rule |
|---|---|:--:|---|
| medicalRecordNumber | string | yes | matches `^MRN-[0-9]{4,10}$`; unique |
| fullName | string | yes | non-blank, max 120 chars, no SQL-meta |
| dateOfBirth | string | yes | ISO date `^[0-9]{4}-[0-9]{2}-[0-9]{2}$` |

- **Response 201 Created** with `Location: /api/patients/{id}`:

```json
{
  "id": 1,
  "medicalRecordNumber": "MRN-100427",
  "fullName": "Jane Q. Public",
  "dateOfBirth": "1980-02-29"
}
```

- **Errors:** `400 VALIDATION_FAILED` (bad format, oversized, SQL-meta, or duplicate
  MRN), `401 AUTH_REQUIRED`, `403 FORBIDDEN`.

## 3. GET /api/patients/{id}

Read a patient (decrypted PHI returned only to authorized roles).

- **Auth:** CLINICIAN, AUDITOR, or ADMIN.
- **Path param:** `id` (Long).
- **Response 200:** same shape as the create response above.
- **Errors:** `404 NOT_FOUND`, `401 AUTH_REQUIRED`, `403 FORBIDDEN`.

## 4. POST /api/patients/{patientId}/records

Create a clinical record for a patient. Runs as an atomic **create-with-audit**
transaction; the note is encrypted at rest.

- **Auth:** CLINICIAN only.
- **Path param:** `patientId` (Long).
- **Request body:**

```json
{ "recordType": "PROGRESS_NOTE", "note": "Patient stable; continue current plan." }
```

| Field | Type | Required | Rule |
|---|---|:--:|---|
| recordType | string | yes | non-blank, max 40; must be in allow-list `PROGRESS_NOTE`, `LAB_RESULT`, `PRESCRIPTION`, `DISCHARGE_SUMMARY`, `RADIOLOGY` |
| note | string | yes | non-blank, max 8000 chars, no SQL-meta |

- **Response 201 Created** with `Location: /api/records/{id}`:

```json
{
  "id": 10,
  "patientId": 1,
  "recordType": "PROGRESS_NOTE",
  "note": "Patient stable; continue current plan.",
  "authoredBy": "clinician",
  "createdAt": "2026-07-21T15:04:05Z"
}
```

- **Errors:** `400 VALIDATION_FAILED` (bad type/oversized/SQL-meta), `404 NOT_FOUND`
  (patient missing), `401 AUTH_REQUIRED`, `403 FORBIDDEN`.

## 5. GET /api/patients/{patientId}/records

List a patient's records, newest first.

- **Auth:** CLINICIAN, AUDITOR, or ADMIN.
- **Path param:** `patientId` (Long).
- **Response 200:** JSON array of record objects (shape as in #4 response).
- **Errors:** `404 NOT_FOUND` (patient missing), `401 AUTH_REQUIRED`, `403 FORBIDDEN`.

## 6. GET /api/records/{id}

Read a single clinical record.

- **Auth:** CLINICIAN, AUDITOR, or ADMIN.
- **Path param:** `id` (Long).
- **Response 200:** one record object (shape as in #4 response).
- **Errors:** `404 NOT_FOUND`, `401 AUTH_REQUIRED`, `403 FORBIDDEN`.

## 7. DELETE /api/records/{id}

Delete a record; writes an audit entry.

- **Auth:** ADMIN only.
- **Path param:** `id` (Long).
- **Response 204 No Content** (empty body).
- **Errors:** `404 NOT_FOUND`, `401 AUTH_REQUIRED`, `403 FORBIDDEN`.

## 8. POST /api/records/{id}/summarize

Produce a PHI-minimized summary through the secure MCP-style summarizer tool.

- **Auth:** CLINICIAN or ADMIN.
- **Path param:** `id` (Long).
- **Request:** none (record id comes from the path).
- **Response 200:**

```json
{ "recordId": 10, "summary": "SUMMARY: ...", "phiRedactedBeforeModel": true }
```

`phiRedactedBeforeModel` is always `true` — it documents that direct identifiers were
stripped before the model call.

- **Errors:** `404 NOT_FOUND`, `401 AUTH_REQUIRED`, `403 FORBIDDEN`.

## 9. GET /api/audit

Read the audit log, newest first. Audit rows carry **no PHI**.

- **Auth:** AUDITOR or ADMIN. (A CLINICIAN is forbidden here.)
- **Response 200:**

```json
[
  {
    "id": 5,
    "actor": "clinician",
    "action": "RECORD_CREATE",
    "targetRef": "record:10",
    "occurredAt": "2026-07-21T15:04:05Z"
  }
]
```

- **Errors:** `401 AUTH_REQUIRED`, `403 FORBIDDEN`.

---

## Error model

Every error is an RFC-7807 `application/problem+json` body with a **stable `code`** and
a **`correlationId`**. Internal detail (stack traces, SQL, class names, parser
messages) is written to the server log only, keyed by the correlation id — never to the
response body.

```json
{
  "type": "about:blank",
  "title": "Validation failed",
  "status": 400,
  "detail": "recordType is not an allowed category",
  "code": "VALIDATION_FAILED",
  "correlationId": "5f3e9c2a-1b4d-4e77-9a1c-2f0b8c7d6e5a"
}
```

### Stable error codes

| Code | HTTP | When |
|---|:--:|---|
| `AUTH_REQUIRED` | 401 | Missing or invalid credentials |
| `FORBIDDEN` | 403 | Authenticated but lacks the required role |
| `VALIDATION_FAILED` | 400 | Bean-validation or service allow-list/format/length/SQL-meta failure |
| `MALFORMED_REQUEST` | 400 | Unparseable request body (parser message is **not** echoed) |
| `NOT_FOUND` | 404 | Patient or record does not exist |
| `BAD_REQUEST` | 400 | Other illegal-argument input |
| `INTERNAL_ERROR` | 500 | Catch-all; nothing internal escapes to the body |

**No-leak guarantees:** `detail` strings are authored to be caller-safe; error bodies
never contain `Exception`/framework class names, stack traces, or SQL. The
`MALFORMED_REQUEST` and `INTERNAL_ERROR` responses carry only a generic message plus
the `correlationId` a caller can quote to support.

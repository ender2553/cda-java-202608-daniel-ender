# Diagnose Failing Cross-Origin Requests — Worksheet

## Your task

Each file in `fixtures/` captures one failing cross-origin request: a console
error plus the request/response headers from the Network panel. For each case,
work through the diagnostic checklist and write your answer below.

For each case:

1. Classify the failure as one of:
   - **Missing/mismatched `Origin`–`Access-Control-Allow-Origin`**
   - **Failed preflight** (missing `Access-Control-Allow-Methods` /
     `Access-Control-Allow-Headers`)
   - **Credentials/wildcard conflict**
   - **Not actually a CORS failure** (something else is going on, and the
     console message is misleading)
2. State the specific evidence from the Network panel capture that supports
   your classification — name the exact header (present, missing, or
   mismatched) that tells you.
3. State the correct fix, in your own words. The fix must be something the
   **server** does. Do not propose disabling CORS in the browser, installing
   a "CORS unblock" extension, or adding a proxy — the lesson explicitly
   calls these anti-patterns.

---

## Case 1 — `case-1-missing-allow-origin.txt`

**Classification:**

**Evidence:**

**Fix:**

---

## Case 2 — `case-2-failed-preflight.txt`

**Classification:**

**Evidence:**

**Fix:**

---

## Case 3 — `case-3-credentials-wildcard-conflict.txt`

**Classification:**

**Evidence:**

**Fix:**

---

## Case 4 — `case-4-404-not-cors.txt`

**Classification:**

**Evidence:**

**Fix:**

One of the four cases above is not really a CORS problem at all — the console
message is misleading. Which case is it, and why does the console still show
a CORS-shaped error message even though the real problem is something else?

**Answer:**

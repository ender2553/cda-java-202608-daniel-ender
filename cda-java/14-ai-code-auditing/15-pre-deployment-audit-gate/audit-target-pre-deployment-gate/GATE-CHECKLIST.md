# Pre-Deployment Audit Gate — Checklist Template

Use this template to render a gate decision for the coupon service. Fill every
field. The decision is only as strong as the evidence behind it, and a sign-off
is an accountable act: a named person attesting this AI-generated code is safe
to deploy.

Evaluate the sections **in order**. Entry criteria are preconditions — if they
are not all met, the gate is not even evaluated (there is nothing to decide
yet). That is different from *gated and failed*.

---

## Entry Criteria (preconditions — all must be true to evaluate the gate)

- [ ] Audit performed across the changed AI-generated code
- [ ] All findings confirmed (exploitability proven; false positives discarded)
- [ ] Every confirmed finding is either **verified closed** or **explicitly accepted with rationale**

> If any box above is unchecked, STOP. The gate is *not eligible to be evaluated*.
> Name what is missing.

---

## Findings Disposition (one row per finding)

| ID | Location / Class | Severity | Disposition (FIXED / ACCEPTED / REJECTED / OPEN) | Evidence reference |
|----|------------------|----------|--------------------------------------------------|--------------------|
|    |                  |          |                                                  |                    |
|    |                  |          |                                                  |                    |
|    |                  |          |                                                  |                    |
|    |                  |          |                                                  |                    |

Disposition rules:
- **FIXED** — cite the verification evidence (regression test fails-on-old /
  passes-on-new, re-run exploit trace, re-audited diff).
- **ACCEPTED** — record the rationale **explicitly**. A silent acceptance is not
  acceptable; an accepted risk is invisible unless it is written down.
- **REJECTED** — note that the code was regenerated and re-audited.
- **OPEN** — an unremediated confirmed finding. Any **OPEN blocking** finding
  forces a FAIL.

---

## Pass / Fail Conditions (the decision)

- [ ] No open blocking finding remains  → eligible to **PASS**
- [ ] Any open blocking finding remains → **FAIL** (name the blocker below)

Blocking finding(s), if any: ______________________________________________

---

## Sign-Off

```
Auditor: ______________________    Decision: PASS / FAIL    Date: ____________

Evidence references: ______________________________________________________

Accepted-risk log (explicit rationale per accepted finding):
  ________________________________________________________________________
```

> The sign-off line is not a formality. Leaving it unsigned with a named blocker
> is the correct outcome when a blocking finding is still open.

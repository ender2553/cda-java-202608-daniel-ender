# Audit Target: Pre-Deployment Gate

This is an AI-generated Spring Boot coupon service that has been audited and
partly remediated. Your job is **not** to re-audit it from scratch — it is to
stand at the **pre-deployment gate** and decide, with evidence, whether it ships.

## What you have

- The coupon service code (`CouponController`, `CouponService`, config, resources).
- `AUDIT-RECORD.md` — the carried-forward findings-and-remediation record (the
  input to the gate). Some findings claim a remediation; one does not.
- `GATE-CHECKLIST.md` — a checklist **template** with entry criteria, a findings
  disposition table, pass/fail conditions, and a sign-off block. Fill it in.

## Build

```
mvn -q compile
```

## Your task

1. **Verify the record against the code.** Open the source and confirm each
   disposition claim in `AUDIT-RECORD.md` actually holds. Do not accept a
   "fixed" label on faith.
2. **Apply the gate checklist.** Evaluate entry criteria first (preconditions),
   then the findings dispositions, then the pass/fail conditions.
3. **Render a gate decision:** *pass with sign-off*, or *fail with the blocking
   finding(s) named*.
4. **Justify the decision** against your own checklist, citing the evidence
   reference for each disposition (fixed / accepted / rejected / open).
5. **Record any accepted-risk decision explicitly**, with rationale. A silent
   acceptance is not acceptable.

Be honest about what the evidence supports. If a confirmed finding is still
open and blocking, the defensible answer is **fail** — name the blocker and
leave the sign-off line unsigned. Rubber-stamping a pass over an open blocker is
the exact failure this exercise exists to drill.

There is no answer key in this folder. Produce your gate decision on your own.

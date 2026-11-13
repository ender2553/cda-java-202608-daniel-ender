# Carried-Forward Audit Record (input to the gate)

This is the findings-and-remediation record carried forward from the prior
audit of the coupon service. You are gating a **real** audit, not a hypothetical.
Read it, confirm it against the code in this project, and then apply your gate
checklist to it.

Some entries claim a remediation. Part of your job is to confirm the claim holds
against the actual code in this folder before you accept it at the gate — do not
take a "fixed" label on faith.

| ID | Location / Class | Severity | Remediation claim |
|----|------------------|----------|-------------------|
| F-01 | `CouponService.findByCode` — SQL injection | High | Claimed FIXED: query parameterized with `?` binding. |
| F-02 | `CouponController.lookup` — verbose error/stack returned to client | Low | Claimed ACCEPTED: service is internal-only, behind the partner gateway. |
| F-03 | `CouponService.PROMO_API_KEY` — hardcoded partner API key (live secret) committed in source | High | No remediation recorded. |
| F-04 | (prior draft) hallucinated `JdbcTemplate.queryForOptional(...)` call | Medium | Claimed REJECTED: method does not exist; code regenerated to use `queryForMap` and re-audited. |

Your task is to verify each disposition against the code, render the gate
decision, and sign or refuse to sign — naming any blocker.

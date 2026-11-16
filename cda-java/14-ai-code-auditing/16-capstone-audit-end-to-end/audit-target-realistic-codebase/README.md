# Audit Target: Realistic Codebase (End-to-End Audit)

This is a realistic, AI-generated Spring Boot order/invoice service — larger and
messier than any single earlier exercise. A pull request landed with this whole
codebase at once: controllers, services, a repository, models, and configuration.
Some of it is fine. Some of it is not. Your job is to run the **entire audit**,
end to end, and decide — with evidence — whether it ships.

This is a single, continuous workflow:

**review → findings → confirmation → remediation → documented sign-off**

Nothing here is a new technique. What is new is running every move together on
one codebase without dropping one.

## What's in the codebase

```
src/main/java/com/acme/shop/
  web/      OrderController, InvoiceController
  service/  OrderService, AccountService, InvoiceService, CartService
  repo/     OrderRepository
  config/   SecurityConfig, TokenConfig
  model/    Order
src/main/resources/  application.properties, schema.sql, data.sql
```

## Build

```
mvn -q compile
```

## Your task — run all five stages

1. **Scope and review.** Open the audit naming all four finding families as
   things to look for:
   - **hallucinations** (APIs/methods that do not exist),
   - **configuration** flaws,
   - **Dependency Injection** vulnerabilities (bean scope/lifetime, shared
     state),
   - **vulnerability classes** (injection, traversal, secrets, access control,
     weak crypto, info exposure).
   Work the codebase methodically — do not chase the first flaw you see.
2. **Produce a findings list** spanning the whole codebase, as one running record.
3. **Confirm** each candidate with a trace. Mark it **confirmed** or **false
   positive** — not every suspicious-looking line is exploitable in context.
   Assign a severity and **prioritize**.
4. **Remediate** confirmed findings through **fix → verify → re-audit**, or
   **reject-and-regenerate** where a unit is too broken to patch safely.
   Re-audit each fix; a finding is closed only when the original exploit no
   longer reproduces and the diff introduced nothing new.
5. **Document and sign off.** Assemble one audit record linking every finding,
   its disposition (fixed / accepted / rejected / false-positive), and its
   evidence. Record accepted risks **explicitly** with rationale. Then render the
   **pre-deployment gate decision**: *pass with attestation*, or *fail with the
   blocking findings named*.

Complete all stages. A partial audit that skips remediation or the gate decision
is incomplete. A pass that would not survive a later incident review is, for this
lab, a fail.

There is no answer key in this folder. Produce your audit and gate decision on
your own.

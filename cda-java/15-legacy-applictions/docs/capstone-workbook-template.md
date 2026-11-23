# Capstone Workbook

> One workbook per team, built stage by stage on the course repository (`legacy-monolith`,
> `integration`, `new-service`). Each stage picks up the previous stage's sections and adds its own.

## 1. "Done" bar
_Committed in planning, graded in validation. A short checklist: the integration performs its
function; the surface delta accounts for every change; every high-priority register entry maps to a
passing test; residual risk is documented with detection and rollback._

- [ ]
- [ ]
- [ ]
- [ ]

## Planning stage

### 2. Scenario summary
_One sentence each._

- **Business driver:**
- **Integration seam** (the specific data and operations that cross the boundary):
- **Constraints** (what must not break):

### 3. Data-flow sketch and pre-integration surface baseline
_What the new service reads, writes, and triggers in the monolith, with each trust boundary marked.
Record the baseline here too — endpoints, ports, privileges, and trust relationships before any
integration code — in the same form you will use for the post-integration snapshot._

### 4. Pre-integration threat model
_Reference your completed `threat-model-template.md` and `inventory-template.md`. Scope strictly to
the seam: threat actors, trust boundaries, data flows._

### 5. Predicted booby-trap classes
_At least three, each tied to a specific feature of this monolith, not the generic taxonomy._

| # | Trap class | Where in this system | Why you expect it |
|---|-----------|----------------------|-------------------|
|   |           |                      |                   |

### 6. Candidate secure pattern
_One of strangler fig, anti-corruption layer, API gateway, or sidecar, with a two-sentence fit
rationale. Flagged as a candidate to validate; discovery may revise it._

### 7. Integration plan
_Numbered sequence across the discover-and-neutralize, implementation, and validation stages: the
input each consumes, the artifact it produces, and who owns the handoff._

## Discover-and-neutralize stage

### 8. Integration risk register
_Reference your completed `risk-register-template.md`. Each entry: location (file · method), trap
class, precise trigger condition, resulting vulnerability, likelihood × integration-impact score, and
bucket (neutralize now / build carefully around / monitor in production)._

### 9. Neutralization log
_One row per high-priority trap._

| ID | Action (mitigate / isolate / contain) | Rationale | Applied now? | Residual risk |
|----|---------------------------------------|-----------|--------------|---------------|
|    |                                       |           |              |               |

### 10. Discovery summary
_The handoff gate. Every high-priority entry appears in exactly one list._

- **Neutralized (action applied):**
- **Design constraints for implementation** (instructions someone else could follow blind):
- **Deferred to production monitoring:**

## Implementation stage

### 11. Constraint → control mapping
_One line per design constraint naming the enforced seam control that satisfies it._

| Design constraint | Seam control (class · method) | Register entry it protects |
|-------------------|-------------------------------|----------------------------|
|                   |                               |                            |

### 12. Working integration
_How the new service reaches the monolith — only through the seam: authentication, least-privilege
credential, contract validation, anti-corruption mapping. Record the register re-check: for each
neutralized trap, the code path that would re-arm it does not exist._

### 13. Post-integration surface snapshot
_Endpoints, ports, service identities and privileges, trust relationships — in the same form as the
baseline in section 3._

## Validation stage

### 14. Test evidence
_One security-regression test per high-priority register entry, abuse cases for the contract and both
seam directions, the captured pass/fail run, and at least one re-arm-and-revert showing a test go red._

| Register entry | Test (class · method) | Result | Re-armed and went red? |
|----------------|-----------------------|--------|------------------------|
|                |                       |        |                        |

### 15. Surface delta
_Baseline (section 3) against snapshot (section 13). Every difference gets a disposition._

| Change | Kind (endpoint / port / privilege / trust) | Disposition (justified-and-kept / removed) | Justification |
|--------|---------------------------------------------|--------------------------------------------|---------------|
|        |                                             |                                            |               |

### 16. Residual risk
_For each deferred or accepted risk: the risk in one sentence, the detection signal, the rollback path._

| Risk | Detection signal | Rollback path |
|------|------------------|---------------|
|      |                  |               |

### 17. Self-grade and reflection
_Walk section 1 line by line and mark each pass or fail. Then: which discovery step caught the most
dangerous trap, and what would skipping it have cost?_

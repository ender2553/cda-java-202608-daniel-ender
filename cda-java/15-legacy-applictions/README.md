# Safe Hybrid Development / Legacy Integration — Student Starter

A small multi-module Java / Spring Boot workspace used across this security course. A
legacy monolith is being integrated with a newer service through an integration seam.
Your job, over the course, is to understand the system, find its weaknesses, secure the
seam, and prove your fixes with tests.

## Module map

| Module | What it is | Runs on | Package |
|--------|-----------|---------|---------|
| `legacy-monolith` | Accounts/ledger monolith exposing `/reports/*` (the seam provider). | 8080 | `com.rti.legacy` |
| `integration` | Anti-corruption seam library: authenticator, contract validator, legacy→service mapper. Interfaces only in the starter (impls are stubs). | — (library) | `com.rti.integration` |
| `new-service` | Reporting/notification consumer that pulls summaries from the monolith across the seam. Client is a stub in the starter. | 8081 | `com.rti.newservice` |

## Build

From the project root:

```
mvn -q -DskipTests compile
```

Build and install all modules (both services depend on the `integration` module, so it must be in your local Maven repository):

```
mvn -q -DskipTests install
```

## Run

Start the monolith (seam provider):

```
mvn -q -pl legacy-monolith spring-boot:run
```

Then, in another terminal, start the new service:

```
mvn -q -pl new-service spring-boot:run
```

- Monolith: `http://localhost:8080/reports/A-100`, `http://localhost:8080/reports/search?filter=...`
- New service: `http://localhost:8081/notify/A-100`

> In the starter, the new service's seam client and the integration impls are
> unbuilt stubs — calling `/notify/*` throws until you wire them.

## Your task across the course

1. **Inventory** the system and fill in `docs/inventory-template.md`. The
   `legacy-monolith/README.md` is intentionally sparse and partly wrong — verify it.
2. **Threat-model** the boundaries (`docs/threat-model-template.md`) and record findings
   in `docs/risk-register-template.md`.
3. **Remediate** the flaws you find in the monolith.
4. **Secure the seam**: build real `integration` implementations and wire
   `new-service`'s `MonolithClient` through them with least-privilege credentials.
5. **Verify** with a regression test per fix. The capstone runs on this same repository:
   capture each stage's artifacts in `docs/capstone-workbook-template.md`.

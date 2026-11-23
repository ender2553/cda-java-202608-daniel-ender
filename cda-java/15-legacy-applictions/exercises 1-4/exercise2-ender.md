Map the Trust Boundaries of a Hybrid — Completed Worksheet

Step 2–3: Boundaries and Owners

| Boundary (between what and what?) | Who authenticates the caller? | Who authorizes the action? | Who validates the data crossing? |
|---|---|---|---|
| **1. Customers → Order Management System** (Internet boundary) | Order Management System authenticates customers for account/order actions. | Order Management System determines what each customer is allowed to do. | Order Management System validates incoming requests, inputs, and customer/order data. |
| **2. Notification Service → Order API** (Internal service boundary) | **Nobody explicitly identified**; the API is internal and not internet-accessible, but internal network location alone isn't authentication. | Order API/monolith; however, dedicated permissions for the Notification Service are not specified. | Order API should validate requests, but dedicated service-level validation isn't specified. |
| **3. Notification Service → ORDERS_DB** (Database trust boundary) | Database authenticates the Notification Service using the shared `oms_app` account. | **Insufficient separation**; Notification Service inherits the privileges of the monolith's database account. | Database enforces its own constraints, but application-level validation and least-privilege controls are not established. |
| **4. Notification Service → Email/SMS Provider** (External vendor boundary) | Provider authenticates the Notification Service using its provider credentials (assumed). | Notification Service controls which messages it submits; provider controls what the account is permitted to send. | Notification Service should validate recipients and message content; provider may also enforce limits and checks. |

Step 4: The Seam
Circle boundary #3 — Notification Service ↔ ORDERS_DB.

This is the most dangerous seam because the new microservice connects directly to the legacy production database using the monolith's shared credentials.

In one sentence, what does an attacker gain by crossing it?
An attacker who compromises the Notification Service could inherit the monolith's database privileges, allowing unauthorized access to sensitive customer, order, payment, and other production data, potentially including modification or deletion.

Step 5: Earned or Assumed?
What does the Notification Service implicitly trust about the legacy side?
- It trusts the database schema to remain stable.
- It trusts the shared oms_app credentials to provide appropriate access.
- It trusts the order_status table to contain accurate and safe information.
- It trusts that querying the database directly won't interfere with the monolith.
- It trusts the Order API to return the correct order and customer information.
- It trusts that the legacy system has no unexpected side effects or hidden behavior relevant to these interactions.
Is that trust earned or assumed? Why?
The trust is mostly assumed. The Notification Service shares production database credentials with the monolith, has direct access to sensitive data, and depends on an internal API that wasn't designed for external service integration. There is no demonstrated least-privilege separation, explicit service authentication, or documented contract establishing that these trust assumptions are safe.

Step 6: A Boundary We Almost Missed
The internal network is still a trust boundary. Just because the Order API isn't reachable from the internet doesn't mean the Notification Service should automatically be trusted. Internal services can be compromised, credentials can be stolen, and network location alone does not establish identity or authorization.

What the diagram teaches us

| Security problem | Why it matters |
|---|---|
| Shared database credentials | A compromise of the new service can expose privileges intended for the monolith. |
| Direct database access | Bypasses the monolith's intended application-level controls and contracts. |
| Internal API trust | Internal network access is not proof of a trusted caller. |
| Sensitive data exposure | Notification Service retrieves customer names, items, and delivery addresses. |
| Third-party integration | Customer information and message content leave the retailer's trust boundary. |
| Excessive privileges | The Notification Service may have more database access than its function requires. |

Overall conclusion
The primary weakness is not that the retailer has a monolith and a microservice; it's that the new service crosses legacy trust boundaries without clearly defined authentication, authorization, data validation, or least-privilege controls.

The safest design would be to replace direct database access with a narrowly scoped, authenticated integration mechanism—such as a dedicated read-only API or limited event stream exposing only the order-status data required by the Notification Service.
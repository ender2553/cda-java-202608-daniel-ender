Threat-Model a Provided Monolith — Completed Worksheet

1. Assets

What is worth protecting?

1. Customer data — personal information, order history, addresses, and account details.
2. Financial assets — payment tokens, account credit, refunds, billing records, and financial transactions.
3. Employee data — employee records, payroll information, and administrative credentials.
4. RETAIL_DB — integrity, availability, and confidentiality of the shared database.
5. Loyalty points and rewards — accurate point balances, redemptions, and prevention of fraudulent rewards.
6. Authentication credentials — customer passwords, staff credentials, and database credentials.
7. Business operations — storefront availability, billing accuracy, and store operations.


 Threat actors

| Actor | What they want / could abuse | Access they have |
|---|---|---|
| **External attacker** | Steal customer data, take over accounts, manipulate points, abuse account credit, or disrupt operations. | Internet-facing Storefront and the new public Loyalty API. |
| **Malicious customer** | Fraudulently redeem points, manipulate balances, or obtain unauthorized account credit. | Their own customer account, public Storefront, and public Loyalty API. |
| **Compromised staff account / insider** | Issue unauthorized credits, manipulate refunds, or access sensitive customer or employee information. | Internal Admin Console; potentially broad privileges through shared staff accounts. |
| **Loyalty Service (new)** | Could unintentionally or maliciously read, alter, or expose data beyond its intended function. | Direct database access using `app_owner`, plus access to the public API and reward-redemption logic. |
| **Third-party accounting firm** | Could misuse or expose financial information if compromised or acting maliciously. | Daily sales and ledger CSVs transferred over plain FTP. |
| **Compromised monolith** | Exploit shared privileges to access or modify other modules' data. | Monolith modules share a process and use `app_owner` against RETAIL_DB. |

3. Trust boundaries

Perimeter boundaries
- Internet ↔ Storefront
- Internet/mobile app ↔ Public Loyalty API
- Internal network ↔ Admin Console
- Northwind network ↔ External accounting firm (FTP transfer)

Module boundaries
- Storefront ↔ Admin Console
- Storefront ↔ Billing
- Billing ↔ Catalog
- Loyalty Service ↔ Monolith modules
- Nightly Batch ↔ Finance/Payroll
- Loyalty Service ↔ Loyalty API

Although the monolith's modules are logical boundaries, they are weak security boundaries because they share a process and database account.

Data boundaries
- Monolith ↔ RETAIL_DB
- Loyalty Service ↔ RETAIL_DB
- Billing ↔ Payment processor
- Nightly Batch ↔ Accounting firm
- Loyalty Service ↔ Customer reward/account-credit records

Privilege boundaries
- Customer ↔ Customer-owned account and rewards
- Staff ↔ Admin Console administrative functions
- Supervisor ↔ Credits over $20
- Monolith ↔ Shared app_owner database privileges
- Loyalty Service ↔ Database owner-level privileges
- Loyalty Service ↔ Publicly accessible redemption operations

The new seam

Loyalty Service ↔ RETAIL_DB and the customer account-credit mechanism.

This is the most concerning new integration boundary because the new service is outside the monolith but receives the same unrestricted database privileges as the legacy application.

4. The new flows

| New flow | Boundaries it crosses | Shared stores it touches | Assets it can now reach |
|---|---|---|---|
| **L1 — Loyalty Service → RETAIL_DB (nightly reads)** | Container/service boundary, database boundary, privilege boundary | `orders`, `order_items`, `RETAIL_DB` | Customer purchase history, order details, potentially other data accessible through `app_owner`. |
| **L2 — Loyalty Service → RETAIL_DB (reward point updates)** | Service-to-database boundary, data-integrity boundary | `customer.reward_points` | Customer reward balances and account records. |
| **L3 — Customer/mobile app → Loyalty API → Loyalty Service → RETAIL_DB (redemption)** | Internet perimeter, API authentication boundary, authorization boundary, service/database boundary | `customer.reward_points`, `customer.account_credit` | Reward balances, account credit, customer accounts, and monthly billing calculations. |

5. Threats: Does the integration make this worse?

| Threat | Integration makes it worse? | Trap class | Why |
|---|---|---|---|
| **Excessive database privileges (`app_owner`)** | **In scope** | Privilege escalation | The new service inherits owner-level access to all database tables, including payment tokens, payroll, and employee data it doesn't need. |
| **Fraudulent reward redemption** | **In scope** | Implicit contract / privilege abuse | The public API can convert points into account credit, creating a new way to manipulate financial value if authorization or validation is weak. |
| **Unauthorized account-credit modification** | **In scope** | Shared state / privilege escalation | The new service modifies `account_credit`, which is used by Billing to calculate monthly charges and refunds. |
| **Database compromise from service compromise** | **In scope** | Privilege escalation | Compromising the Loyalty Service could provide broad database access through the shared owner credentials. |
| **Weak staff authentication (six-character minimum, no MFA, shared login)** | **Out of scope** | Authentication weakness | This existing weakness isn't introduced by Loyalty, although the monolith's shared privileges make its overall impact severe. |
| **Plain FTP to accounting firm** | **Out of scope** | Insecure data transfer | Existing nightly exports are still sent over unencrypted FTP; Loyalty doesn't create or modify this flow. |
| **Monolith modules share a process and database account** | **Out of scope (existing weakness), but impact worsens** | Shared state / privilege escalation | The monolith already has poor internal isolation, but the new service expands the number of components able to access the shared database privileges. |

6. Top three (in-scope only)

| Rank | Threat | Trap class | What the design must do about it |
|---|---|---|---|
| **1** | Loyalty Service has unrestricted `app_owner` access to RETAIL_DB. | Privilege escalation | Give the Loyalty Service its own database identity with least-privilege permissions. Restrict it to only the required order data and reward-related operations; no access to payroll, employee records, payment tokens, or unrelated tables. |
| **2** | Fraudulent or unauthorized reward redemption creates account credit. | Privilege abuse / implicit contract | Authenticate customers, authorize account ownership, validate point balances, enforce redemption limits, and make redemption atomic and auditable. |
| **3** | Loyalty Service directly modifies shared customer financial data. | Shared state / side effect | Do not allow unrestricted direct writes to `account_credit`. Use a narrowly scoped, validated redemption interface that safely applies credits and records the transaction. |

7. Safest attach point and residual risk

Where should the Loyalty Service attach?

The safest attach point is through a narrowly scoped, authenticated integration API or event stream provided by the monolith, rather than direct database access. The service should receive only the order data it needs to calculate points, and reward redemptions should go through a dedicated operation that validates the customer, available points, redemption amount, and account-credit transaction.

Recommended architecture:
- Earn points: The monolith publishes order-completed events containing only the necessary order information. The Loyalty Service consumes these events rather than polling the database.
- Read rewards: The Loyalty API retrieves reward balances through a dedicated, authorized interface.
- Redeem rewards: The Loyalty Service requests redemption through a dedicated monolith operation, which validates and applies the account credit atomically.
- Database access: If direct access cannot be avoided, use a dedicated database identity with restricted read/write permissions—never app_owner.
- Public API: Require customer authentication, ownership checks, input validation, rate limiting, and audit logging.

What can't you confirm from the diagram alone?
- Whether the Loyalty API properly authenticates customers.
- Whether authorization checks ensure customers can access only their own reward balances.
- Whether reward redemptions are atomic and protected against race conditions or replay.
- Whether point balances can become negative or be redeemed multiple times.
- Whether the database supports separate accounts and granular permissions.
- Whether the monolith has existing APIs or event mechanisms suitable for rewards integration.
- Whether the new service securely stores credentials, logs, and customer data.
- Whether reward calculations correctly handle refunds, canceled orders, and duplicate events.
- Whether the Loyalty Service is monitored, rate-limited, and audited.

Overall conclusion

The biggest risk is giving a new, internet-connected service unrestricted access to the legacy monolith's database. The Loyalty Service creates a new attack path into sensitive customer, financial, employee, and payroll data while also introducing a new mechanism for changing account credit.

The goal isn't necessarily to modernize or replace the entire monolith. It's to create the smallest, safest seam possible—a controlled integration boundary with limited data, limited privileges, explicit validation, and auditable financial operations.

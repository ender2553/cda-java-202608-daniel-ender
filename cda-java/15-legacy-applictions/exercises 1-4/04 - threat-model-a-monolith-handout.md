# Threat-Model a Provided Monolith — Group Handout

**Lesson 4: Threat-Modeling a Legacy Monolith** · One copy per group, with the diagram `threat-model-a-monolith-diagram.svg`

Your goal is a **one-page, integration-scoped, prioritized threat model**: where can the new service attach with the least new exposure, and which threats does this integration make worse?

## The Legacy System: Northwind Retail Platform

Northwind has sold outdoor gear online and in 14 stores since 2008. Everything runs in one monolith and one database.

| Module | What it does today |
|---|---|
| **Storefront** | Public website. Customers browse, check out and use **My Account** (order history, saved addresses, current account credit). |
| **Admin Console** | Used by store and office staff on the internal network. Includes refunds, user management and **Issue Credit**, which lets customer-service staff give a customer goodwill credit. Issue Credit caps each credit at $50, requires a supervisor to approve anything over $20, and records who issued it. Staff log in with a username and a password of at least 6 characters; there is no multi-factor login, and the returns desk in each store shares one login. |
| **Billing** | On the 1st of each month, builds each customer's statement: purchases on account, minus `customer.account_credit`. It charges the remaining balance to the card on file through the payment processor and then sets `account_credit` back to 0. If the credit is larger than the balance, Billing refunds the difference to the card. |
| **Catalog** | Products, prices and images, managed by merchandising staff. |
| **Finance & Payroll** | General ledger and employee pay. |
| **Nightly Batch Jobs** | At 1:00 a.m., exports the day's sales and ledger entries as a CSV to the outside accounting firm over plain FTP. |

**How the modules reach data:** All six modules run in one process and call each other directly. Every module connects to **RETAIL_DB** with the same database account, `app_owner`, which has owner rights on every table, including `payment_tokens`, `employees` and `payroll`.

### Flows on the diagram

| Flow | From → To | What moves |
|---|---|---|
| **F1** | Customers ↔ Storefront | Browsing, orders, account details, saved cards (internet, HTTPS) |
| **F2** | Staff ↔ Admin Console | Staff logins, refunds, credits, user changes (internal network) |
| **F3** | All modules ↔ RETAIL_DB | Every read and write in the system, as `app_owner` |
| **F4** | Billing → Payment processor | Monthly card charges and refunds |
| **F5** | Nightly Batch → Accounting firm | Daily sales and ledger CSV (plain FTP) |

## The New Service: Loyalty Service

Northwind is launching a rewards program. A new team is building the **Loyalty Service**, a separate microservice in its own container. Customers earn 1 point per dollar spent. Every night, the Loyalty Service reads the last 24 hours of `orders` and `order_items` directly from RETAIL_DB to award points, and it keeps each customer's balance up to date in `customer.reward_points` so it shows up on My Account. Customers check and redeem points through a new **public Loyalty API** used by the Northwind mobile app. When a customer redeems, they choose how many points to convert (100 points = $1), and the Loyalty Service adds that amount to `customer.account_credit` so the next monthly statement applies it. To hit the launch date, the team plans to connect to RETAIL_DB with the existing `app_owner` account "because it already has access to every table we need."

---

## Worksheet

### 1. Assets

What is worth protecting here? List at least four.

______________________________________________________________________

### 2. Threat actors

List every actor. Include the new service, and write down the **new access** it would have.

| Actor | What they want / could abuse | Access they have |
|---|---|---|
| | | |
| | | |
| | | |
| | | |

### 3. Trust boundaries

On the diagram, mark the outer perimeter **and** the boundaries hidden inside the monolith. Then list them here.

- Perimeter boundaries: ______________________________________________
- Module boundaries: ________________________________________________
- Data boundaries: __________________________________________________
- Privilege boundaries: ______________________________________________
- The new seam: ____________________________________________________

### 4. The new flows

Draw the Loyalty Service and every flow it needs in the empty box on the diagram. For each new flow:

| New flow | Boundaries it crosses | Shared stores it touches | Assets it can now reach |
|---|---|---|---|
| | | | |
| | | | |
| | | | |

### 5. Threats: does the integration make this worse?

List the threats you found, including real weaknesses that this integration **doesn't** change.

| Threat | Integration makes it worse? (in scope / out of scope) | Trap class | Why |
|---|---|---|---|
| | | | |
| | | | |
| | | | |
| | | | |
| | | | |

### 6. Top three (in-scope only)

| Rank | Threat | Trap class | What the design must do about it |
|---|---|---|---|
| 1 | | | |
| 2 | | | |
| 3 | | | |

### 7. Safest attach point and residual risk

Where should the Loyalty Service attach to give the least new exposure? What can't you confirm from the diagram alone?

______________________________________________________________________

______________________________________________________________________

# Map the Trust Boundaries of a Hybrid — Diagram & Worksheet

**Lesson 2: Monoliths, Microservices & Hybrids** · Pair handout (one copy per pair)

A printable version of the diagram is in `map-the-trust-boundaries-hybrid-diagram.svg`. Draw your boundaries directly on it.

## The Scenario

An online retailer has run its **Order Management System** since 2009. It is a single monolith that handles the storefront, orders, payments and customer accounts. Everything it knows lives in one database, **ORDERS_DB**.

This year a new team built a **Notification Service** that emails or texts customers when their order status changes (confirmed, shipped, delivered).

## The Diagram

```
        Customers (web & mobile)
                   │
                   │ (1)
                   ▼
┌──────────────────────────────────────┐              ┌───────────────────────────┐
│ ORDER MANAGEMENT SYSTEM              │              │ NOTIFICATION SERVICE      │
│ legacy monolith · running since 2009 │              │ new microservice · 2026   │
│                                      │              │                           │
│ • Web storefront                     │              │ • Status watcher          │
│ • Order API                          │◀─────(2)─────┤ • Message builder         │
│ • Payments module                    │              │ • Sender                  │
│ • Customer accounts module           │              │                           │
└──────────────────┬───────────────────┘              └──────┬─────────────┬──────┘
                   │                                         │             │ (4)
                   ▼                                         │             ▼
┌──────────────────────────────────────┐                     │    Email / SMS provider
│ ORDERS_DB  (shared database)         │◀────────(3)─────────┘
│                                      │
│ customers        addresses           │
│ orders           order_status        │
│ payments         card_tokens         │
└──────────────────────────────────────┘
```

## What Each Connection Does

1. **Customers → Order Management System.** Customers browse, place orders and manage their accounts through the web storefront and mobile app over the internet.
2. **Notification Service → Order API.** When it builds a message, the Notification Service calls the monolith's Order API to get the order's items, the customer's name and the delivery address. The Order API was built in 2011 for the monolith's own storefront. It sits on the internal network and is not reachable from the internet.
3. **Notification Service → ORDERS_DB.** Every 30 seconds, the Status Watcher queries the `order_status` table directly to find orders whose status has changed. To save setup time, it connects with the same database account the monolith uses (`oms_app`).
4. **Notification Service → Email / SMS provider.** The Sender hands finished messages to a third-party email and text-message service.

---

## Worksheet

### Step 2–3: Boundaries and owners

List every trust boundary you marked. For each one, name who is responsible for each check, or write "nobody" if that's the honest answer.

| Boundary (between what and what?) | Who authenticates the caller? | Who authorizes the action? | Who validates the data crossing? |
|---|---|---|---|
| | | | |
| | | | |
| | | | |
| | | | |
| | | | |

### Step 4: The seam

Circle the seam on your diagram. In one sentence, what does an attacker gain by crossing it?

______________________________________________________________________

______________________________________________________________________

### Step 5: Earned or assumed?

What does the Notification Service implicitly trust about the legacy side?

______________________________________________________________________

______________________________________________________________________

Is that trust **earned** or **assumed**? Why?

______________________________________________________________________

______________________________________________________________________

### Step 6: A boundary we almost missed

______________________________________________________________________

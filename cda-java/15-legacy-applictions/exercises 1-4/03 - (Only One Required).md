# Dissect a Booby Trap — Group Cards

**Lesson 3: The "Booby Trap" Problem** · One card per group (cut along the lines)

Each card has three parts: how the legacy code is used **today**, a short **legacy snippet** in pseudocode, and the **new service** that is about to start calling it. Your group runs the four-part dissection on your card, then adds one discovery step:

1. **Hidden dependency:** point to the exact line or behavior.
2. **Trigger:** new input, new privilege, new frequency/timing, or new data path. Name the specific change.
3. **Resulting vulnerability:** name the exploitable condition, not just "risky."
4. **Trap class:** shared state, implicit contract, side effect, privilege/escalation, or timing/concurrency.
5. **Discovery step:** the one check that would have caught this before go-live.

Some cards hide more than one problem.

---

## Card 1 — Gift Card Redemption

**Today:** In-store registers call this when a cashier swipes a gift card. One register, one card, one swipe at a time. It has worked without complaint for 11 years.

```
function redeem_gift_card(card_id, amount):
    card = db.load_card(card_id)
    if card.balance >= amount:
        card.balance = card.balance - amount
        db.save_card(card)
        return "APPROVED"
    return "DECLINED"
```

**New service:** The new **Mobile Wallet** lets customers pay online with a gift card stored in the app. It runs as eight load-balanced instances so checkout stays fast during sales. When a customer taps "Pay," the app retries automatically if it doesn't get a response within two seconds, and nothing stops a customer from opening checkout in several tabs or devices at once. Each payment request calls `redeem_gift_card` directly.

---

## Card 2 — Account Balance Lookup

**Today:** Bank tellers call `get_balance` when a customer at the counter asks for their balance. Right after that, the teller usually opens the Transfer screen, which fills in the "from" account automatically. The whole application runs as one process per server, and `screen_memory` is shared by every request on that server.

```
GLOBAL screen_memory = {}

function get_balance(account_id):
    account = db.load_account(account_id)
    screen_memory["last_account"] = account
    audit.write("BALANCE_VIEWED", account_id)
    return account.balance

function prefill_transfer_form():
    return screen_memory["last_account"]
```

**New service:** The new **Spending Insights** service builds a weekly money summary for every customer. To get current balances, it calls `get_balance` for each account, about 4,000 calls a minute during business hours, running on the same servers the tellers use. The team picked `get_balance` because "it's just a read," and it's already used in production.

---

## Card 3 — Apply Discount Code

**Today:** The web checkout calls this only after its own page logic has confirmed that the code exists, hasn't expired, and that the cart meets the code's minimum spend. Codes are typed by customers but always pass through that checkout page first.

```
function apply_discount(order, code):
    discount = db.find_discount(code)
    order.total = order.total - discount.amount
    order.discount_code = code
    db.save_order(order)
    return order
```

**New service:** The new **Partner Marketplace** integration lets a partner website sell our products. Partner orders arrive through an API, and the partner passes along whatever discount code their customer typed on the partner's site. To reuse existing logic, the integration calls `apply_discount` for each incoming order before sending it to fulfillment. Orders over a partner's site can be as small as a single $4 item.

---

## Card 4 — Customer Email Address

**Today:** A customer can change their email only through the Account Settings screen, which requires them to be logged in, re-enter their password, and click a confirmation link sent to the new address. Elsewhere in the monolith, the "Forgot password" page uses the same column.

```
// Account Settings screen (after login, password and confirmation)
function change_email(customer_id, new_email):
    db.customers[customer_id].email = new_email

// "Forgot password" page
function send_reset_link(email):
    customer = db.customers.find_by_email(email)
    if customer exists:
        send_mail(customer.email, make_reset_link(customer.id))
```

**New service:** The new **Marketing Preferences** service lets customers choose where they get offers. Every newsletter has a "Manage preferences" link like `/preferences?customer=48213`, which opens a page without logging in so customers don't have to remember a password. When someone enters a new address on that page, the service writes it straight into `db.customers[customer_id].email`, because "that's where we keep the customer's email."

---

## Card 5 — Save Scanned Document

**Today:** Scanning workstations in the back office call this to save scanned mail. The workstations run as the operating-system account `scanner`, which is allowed to write only inside `/data/incoming`. Staff choose the folder from a fixed list; the scanner software names files automatically (`scan-000123.pdf`).

```
function save_document(folder, filename, contents):
    path = folder + "/" + filename
    write_file(path, contents)
    return path
```

**New service:** The new **Customer Portal** lets customers upload proof-of-address documents. It runs on the application server as the OS account `portal_svc`. To keep setup simple, `portal_svc` has write access to `/opt/portal` (the portal's own code, configuration, and page templates) and to all of `/data`. For each upload, the portal calls `save_document` with the folder `/data/incoming/<customer_id>` and the filename exactly as it arrived from the customer's browser.

---

## Group Dissection Sheet

**Card #:** ______

| Part | Our answer |
|---|---|
| **Hidden dependency** (point to the line) | |
| **Trigger** (which of the four, and what exactly changed) | |
| **Resulting vulnerability** (the named, exploitable condition) | |
| **Trap class** | |
| **Discovery step** (the one check that would have caught it) | |

**Did you find a second hidden dependency on your card? If so, what is it?**

______________________________________________________________________

______________________________________________________________________

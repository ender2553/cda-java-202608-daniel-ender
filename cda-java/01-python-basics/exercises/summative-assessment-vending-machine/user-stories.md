# Vending Machine — User Stories & Acceptance Criteria

These are the same user stories referenced as comments in
`vending-machine-starter.py`. Use this document as your spec while you
work, and check off each acceptance criterion as you satisfy it.

---

## US1 — Main Menu Navigation
**As a** customer, **I want** to see a main menu of options **so that** I can
choose what to do with the vending machine.

**Acceptance Criteria**
- [ ] Given the program is running, the main menu displays 5 options: View
      Products, Feed Money, Purchase Item, Check Balance, Exit.
- [ ] Given the menu is displayed, when I enter a number from 1–5, the
      program performs the matching action.
- [ ] When I enter something that isn't a number, the program shows an error
      message and asks again — it does not crash.
- [ ] When I enter a number outside 1–5, the program shows an error message
      and asks again.

## US2 — Feed Money
**As a** customer, **I want** to insert money into the machine **so that** I
build up a balance I can spend on items.

**Acceptance Criteria**
- [ ] Given I select "Feed Money," when I enter a valid positive amount, it
      is added to my balance and my new balance is displayed.
- [ ] When I enter something that isn't a number, the program shows an error
      message and asks again — my balance is unchanged.
- [ ] When I enter zero or a negative number, the program shows an error
      message and asks again — my balance is unchanged.
- [ ] When money is successfully added, a record is appended to
      `transaction_log.txt`.

## US3 — Purchase an Item
**As a** customer, **I want** to buy an item using my balance **so that** I
receive the item and can still use any leftover balance to buy more.

**Acceptance Criteria**
- [ ] Given I select "Purchase," the current product list is shown before I
      choose a code.
- [ ] When I enter "B," I return to the main menu with no changes made.
- [ ] When I enter a code that isn't on the menu, an error message is shown
      and nothing changes (no crash).
- [ ] When the item I select has 0 stock, a message tells me it's out of
      stock and nothing changes.
- [ ] When my balance is less than the item's price, a message tells me
      exactly how much more I need, and nothing changes (no deduction).
- [ ] When my balance covers the price, the price is deducted from my
      balance, the item's stock drops by 1, and I see a confirmation
      message with my remaining balance.
- [ ] I can purchase a second (and third, etc.) item in the same session
      without restarting the program, as long as my balance covers it.
- [ ] Every successful purchase is appended to `transaction_log.txt`.

## US4 — Check Balance
**As a** customer, **I want** to check my current balance at any time
**so that** I know how much more I can spend.

**Acceptance Criteria**
- [ ] Given I select "Check Balance," my current balance is displayed,
      formatted as currency (e.g., `$3.50`).

## US5 — Exit & Receive Change
**As a** customer, **I want** to receive any unused balance back when I'm
done **so that** I don't lose money I didn't spend.

**Acceptance Criteria**
- [ ] Given I select "Exit," if my balance is greater than 0, it is
      displayed and returned to me as change.
- [ ] Given I select "Exit," if my balance is 0, no change message is shown.
- [ ] Exiting always saves the current inventory to `inventory.txt`.
- [ ] Exiting always records a final entry in `transaction_log.txt`, whether
      or not change was returned.

## US6 — Load Inventory from File
**As a** machine operator, **I want** the program to load product data from
a file **so that** stock and pricing persist between runs.

**Acceptance Criteria**
- [ ] Given `inventory.txt` exists, when the program starts, every line is
      parsed into the inventory dictionary (code, name, price, stock).
- [ ] Given `inventory.txt` does NOT exist, when the program starts, a
      `FileNotFoundError` is caught, a default inventory is used, and the
      file is created so it exists on the next run.

## US7 — Save Inventory to File
**As a** machine operator, **I want** stock changes saved back to the file
**so that** the next run reflects what's already been sold.

**Acceptance Criteria**
- [ ] Whenever inventory changes (a purchase, or program exit), the current
      inventory is written to `inventory.txt`.
- [ ] If the file can't be written, an `IOError` is caught and a warning is
      printed instead of crashing the program.

## US8 — Transaction Log
**As a** machine operator, **I want** every feed-money and purchase event
recorded **so that** I have an audit trail of activity.

**Acceptance Criteria**
- [ ] Every feed-money, purchase, and exit event appends a new line to
      `transaction_log.txt` — existing entries are never erased.
- [ ] If the log file can't be written, an `IOError` is caught and a warning
      is printed — a failed log write never crashes a sale.

---

## General Acceptance Criteria
**All money amounts must display with two fixed decimal places for cents (e.g. 5.65 ; 3.00, etc.)**

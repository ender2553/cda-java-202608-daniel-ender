# Findings: Meridian Payroll

**Name:**

---

## Part A: Findings

Write one block per problem. Copy the block as many times as you need: most
tickets have more than one problem behind them, and some problems have no
ticket at all. Write "Ticket: none" for those. If one problem explains more
than one ticket, say so in the Ticket field.

Short bullet points are fine: two to four lines per field is plenty.

### Finding 1

- **Ticket:**
- **Standard rule** (1–7):
- **Location** (class and method):
- **Flaw:**
- **Proof** (menu input or query, and what you saw before the fix):
- **Fix** (what you changed):
- **Verified** (what you ran after the fix, and what you saw):

---

### Finding 2

- **Ticket:**
- **Standard rule** (1–7):
- **Location** (class and method):
- **Flaw:**
- **Proof** (menu input or query, and what you saw before the fix):
- **Fix** (what you changed):
- **Verified** (what you ran after the fix, and what you saw):

---

### Finding 3

- **Ticket:**
- **Standard rule** (1–7):
- **Location** (class and method):
- **Flaw:**
- **Proof** (menu input or query, and what you saw before the fix):
- **Fix** (what you changed):
- **Verified** (what you ran after the fix, and what you saw):

---

(Add more findings here.)

---

## Part B: Short answers

A paragraph or two each, in your own words. Refer to this app's code and
data, not just definitions.

**B1. Concurrent pay runs.** Ticket 3 happens when departments run payroll
at the same moment. Explain, step by step with two departments, how the
funding balance ends up wrong even though no error is ever thrown. Name this
kind of problem. Then explain what a *dirty read* is, and how it differs.
Describe one way to prevent the Ticket 3 problem. (Fixing it in code is
optional, for distinction.)

**B2. Transaction boundaries.** For the pay run, which writes did you put
inside one transaction, and what did you deliberately leave outside? Why? For
your menu 11 migration, how big is each transaction, and why that size rather
than one transaction for all 600 employees, or one per field?

**B3. Encrypt or hash?** For each of these, say whether it should be
encrypted, hashed, or left as is, and why: `ssn`, `bank_account`,
`portal_pin`, `annual_salary`, `hr_notes`, `last_name`. Base each answer on
whether the app ever needs the original value back.

**B4. Keys and rotation.** Why is a key in a git-ignored config file packaged
with the app still not externalized? Suppose `PAYROLL_FIELD_KEY` leaks next
year. Describe, step by step, how you would rotate to a new key without
losing data or taking the app down. What does your stored format need to make
that possible?

**B5. "We use HTTPS."** A vendor says Meridian's data is safe because every
connection uses TLS. Explain where TLS's protection ends. Then, for each of
Ticket 5's and Ticket 6's problems, say whether TLS would have prevented it,
and why.

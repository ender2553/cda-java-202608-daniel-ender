# "Legacy or Not?" — System Descriptions

**Lesson 1: What Makes a System "Legacy"?** · Group handout (one copy per group)

Read all five descriptions as a group. For each one, decide whether it is **legacy (yes / no / borderline)** and name the hallmark(s) behind your call:

- **Scale:** how big it is and how much of the business depends on it
- **Age:** how long it has been running and changing
- **Tight coupling:** how much it shares with other systems (data, state, side effects)
- **Undocumented behavior:** what it does that nobody has written down or fully understands

Then rank all five by **integration risk**: how dangerous it would be to connect a new service to it.

---

## System 1 — PayCore (Payroll)

PayCore has run payroll for the company's 3,000 employees every two weeks for **14 years**. It is a Java monolith of about **400,000 lines**, maintained by a five-person team; two of them helped build the original version.

Six years ago the team put a **versioned REST API** in front of the monolith. Every outside system now goes through it, and nothing reads PayCore's database directly. Architecture and API docs are updated with every release. About **70% of the code is covered by automated tests**, and the team keeps a change log that explains why each major change was made.

---

## System 2 — RateSync (Shipping Rates)

RateSync calculates shipping costs and is called by the checkout page on **every order**. It is **six months old** and about **2,000 lines** of code.

A contractor built it, and their contract ended in month four. Nobody on staff has changed it since. It has **no automated tests**. The README is still the default project template. The deployment config sets two environment variables that no one is sure are still used. RateSync has had no incidents in production.

---

## System 3 — StockLedger (Inventory)

StockLedger tracks warehouse inventory. It is **seven years old**, about **60,000 lines**, and maintained by a three-person team. It has up-to-date design docs, reasonable test coverage, and talks to other systems only through a **message queue** with documented message formats.

Its third-party dependencies **haven't been upgraded in five years**. Its web framework version is past end-of-life, its logging library has publicly known vulnerabilities, and its XML parser is two major versions behind. Upgrades keep getting deferred because "it works and nothing's broken."

---

## System 4 — Customer Profile API

The Customer Profile API is a **one-year-old** Spring Boot service with **90% test coverage**, published OpenAPI docs, and a CI pipeline that runs on every commit. Its own code is clean and well reviewed.

It reads and writes **directly to the company's 20-year-old CUSTOMER database**, which the order-management monolith also uses. That database has triggers and stored procedures that fire on updates. For example, changing a customer's address quietly recalculates their tax region and pushes a record to an older billing system. **None of those triggers are documented**, and the API team doesn't own the database schema.

---

## System 5 — nightly_reconcile.pl

`nightly_reconcile.pl` is a **300-line Perl script** that runs at 2:00 a.m. on a single virtual machine. It matches the day's bank deposits against open invoices and flags mismatches to the finance team. **If it doesn't run, the month-end close can't happen.**

An employee who has since left the company wrote it about nine years ago, and at least three people have patched it since. It contains hardcoded account numbers with **no comments explaining them**. It writes directly to two production database tables. It also drops an output file on a shared drive, and **no one is sure what else reads that file**.

---

## Group Worksheet

| System | Legacy? (yes / no / borderline) | Hallmark(s) behind the verdict | Risk rank (1 = riskiest) | One-sentence reason for the rank |
|---|---|---|---|---|
| 1 — PayCore | | | | |
| 2 — RateSync | | | | |
| 3 — StockLedger | | | | |
| 4 — Customer Profile API | | | | |
| 5 — nightly_reconcile.pl | | | | |

**Single attribute that most influenced our ranking:** ______________________

**A system where "legacy" and "high integration risk" didn't line up, and why:**

______________________________________________________________________

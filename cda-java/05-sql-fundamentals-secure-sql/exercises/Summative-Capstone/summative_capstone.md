# PostgreSQL Summative Capstone
## Solstice Health Network — SOC Analyst Post Training Day

**Environment:** PostgreSQL, `summative_capstone_setup.sql` already loaded.
**Deliverable:** A single answer document containing your SQL for every numbered task, its output, and the written Executive Briefing at the end (Part 7).

---

## Scenario

It's your first day post training as a junior SOC analyst at **Solstice Health Network**, a regional hospital system. The security operations database tracks five things: the **hosts** on the network, the **services** running on them, **security events** detected on those services, the **tickets** opened to track response, and the **analysts** (`app_user`) who work them.

Your shift lead has handed you a list of questions from the CISO, the IT manager, and Compliance. You'll work through them in order, building toward a short executive briefing at the end of the day. Nothing here requires you to write anything you haven't already learned — you'll use single-table queries, aggregates, `GROUP BY`/`HAVING`, joins, subqueries, and `INSERT`/`UPDATE`/`DELETE`.

**Two ground rules real analysts follow, and so will you:**
1. Never trust a summary number without knowing what's behind it. If a host has "a lot" of events, check what kind before deciding it matters.
2. A flag that says something is inactive, closed, or decommissioned doesn't mean it's actually harmless — check the data, not just the label.

---

## Schema Reference

```
app_user(user_id, username, full_name, role, department, is_active)
host(host_id, hostname, ip_address, os, criticality, department, decommissioned)
service(service_id, host_id → host, service_name, port, is_public, last_patched)
event(event_id, host_id → host, service_id → service [nullable], event_type,
      severity, event_time, description, resolved)
ticket(ticket_id, event_id → event, assigned_to → app_user, status, priority,
       opened_at, closed_at [nullable])
```

- `criticality` and `severity` values: `low`, `medium`, `high`, `critical`
- `event_type` values in use: `failed_login`, `port_scan`, `malware_detected`, `unauthorized_access`, `patch_missing`
- `ticket.status` values: `open`, `in_progress`, `escalated`, `closed`
- `app_user` is named `app_user`, not `user` — `user` is a reserved word in PostgreSQL and would need to be double-quoted everywhere, so the team avoided it.

---

## Part 1 — Onboarding: Get Oriented (~30–45 min)

Before touching any multi-table logic, get comfortable with what's actually in the database. Write and run a query for each:

1.1. List every host in the `Patient Records` department.

1.2. List all analysts (`app_user`) who are currently active, showing username, full name, and role.

1.3. How many total security events are in the `event` table?

1.4. List the distinct `event_type` values that actually appear in the data.

1.5. List every service running on a public-facing port (`is_public = TRUE`), showing service name and port.

---

## Part 2 — Morning Briefing: Aggregates & Grouping (~60–75 min)

Your shift lead wants a volume picture before anyone jumps to conclusions.

2.1. For each `host_id`, count how many events have been logged against it. Order from most to fewest.

2.2. Using `HAVING`, narrow that same result to only hosts with **more than 2** events.

2.3. Count how many events fall into each `severity` level.

2.4. Count how many tickets currently sit in each `status`.

2.5. **Stop and look at your 2.1 results before moving on.** One host has noticeably more events than any other. Write one or two sentences: is that host necessarily the biggest security concern? What would you need to check to answer that — and which later part of this packet lets you check it?

---

## Part 3 — Relational Investigation: Joins (~75–90 min)

Numbers alone don't tell the CISO what's actually happening. Bring in host and ticket context.

3.1. List hostname, department, event type, severity, and event time for every **critical**-severity event, ordered chronologically (earliest first). *(join `event` + `host`)*

3.2. Check `host.decommissioned` against your Part 3.1 results. Is there a critical event on a host marked as decommissioned? Write the query that finds it directly — don't just eyeball the list.

3.3. List every ticket with status `open` or `escalated`, showing hostname, host department, host criticality, event type, severity, and ticket priority. *(join `ticket` + `event` + `host`)*

3.4. List every ticket, along with the assigned analyst's full name and whether that analyst is currently active (`is_active`). *(join `ticket` + `app_user`)*

3.5. Using your 3.4 query (or a fresh one), find any ticket that is **not closed** and is assigned to an **inactive** analyst. This is a real operational gap — untriaged work sitting with someone who's already gone.

---

## Part 4 — Deeper Analysis: Subqueries (~60 min)

4.1. Using a subquery, list every host that has **never** had a critical-severity event.

4.2. BONUS: Using a subquery to compute the average number of events per host, list the hosts whose event count is **above** that average.

4.3. Using a subquery, find any analyst in `app_user` who does **not** currently appear as `assigned_to` on any ticket.

---

## Part 5 — Incident Response Actions (~60 min)

Late morning brings new developments. Handle each with the appropriate statement — write out the exact SQL you run for each.

5.1. **New detection.** A phishing landing page was found hosted on the patient portal server (`WEB-SRV01`), using its HTTPS service. Insert a new `event`: type `phishing_detected`, severity `high`, timestamp `2026-02-13 10:00:00`, description `'Phishing landing page hosted on patient portal'`, not yet resolved.

5.2. **Open the ticket.** Open a new ticket for the event you just inserted, assigned to `sbrooks`, status `open`, priority `high`, opened at `2026-02-13 10:15:00`. (Look up her `user_id` — don't hardcode a number you haven't verified.)

5.3. **Close out a resolved incident.** The critical unauthorized-access ticket on `EHR-DB01` (from Part 3.1) has been resolved. Update that ticket's status to `closed` with a close time of `2026-02-13 12:00:00`, **and** mark the underlying event as `resolved`.

5.4. **Reassign orphaned work.** Reassign the ticket you flagged in 3.5 (the inactive-analyst ticket) to `dsingh`.

5.5. **Clean up a duplicate.** Two tickets were opened two minutes apart for the same RDP brute-force burst on `ADMIN-WKS03` — one for the very first failed-login event in that burst, one for the second. The shift lead confirms the first is redundant. Find it and delete it. (Don't delete by a hardcoded ID — write a query that identifies it from the data, then delete it.)

---

## Part 6 — Automation: Stop Repeating Yourself (~60–75 min)

You've now hand-written the same kind of fix more than once today. A real SOC doesn't retype the same multi-step logic every time — it wraps it once and reuses it. You'll build one **procedure** (for an action with side effects) and one **function** (for a reusable lookup).  Be sure to avoid SQL injection pitfalls.

6.1. **Procedure — automate the Part 5.3 close-out.** In Part 5.3 you closed a ticket with two separate statements: update the ticket, then update its event. Write a `PROCEDURE` named `close_ticket_and_event(p_ticket_id INT, p_close_time TIMESTAMP)` that does both in one call. It should look up the ticket's `event_id` itself — don't make the caller pass it in. If someone calls it with a `ticket_id` that doesn't exist, it should raise a clear error instead of silently doing nothing.

Test it: use your procedure to close the phishing ticket you opened in Part 5.2, with a close time of `2026-02-13 15:00:00`. Confirm both the ticket and its event updated correctly.

6.2. **Function — reusable severity breakdown.** Write a `FUNCTION` named `host_severity_summary(p_host_id INT)` that returns a table of `severity` and `event_count` for whatever host ID you pass in — the same shape of answer as Part 2.3, but scoped to one host on demand instead of hardcoded for all of them.

Test it against host 7 (the high-volume host from Part 2.1) and host 2 (`EHR-DB01`). Compare the two results in one sentence: does the breakdown change how you'd rank these two hosts?

---

## Part 7 — End-of-Day Executive Briefing (~45–60 min)

7.1. **Build the report's foundation: a view.** Rather than re-running a pile of joins every time someone asks "how are we doing," create a `VIEW` named `executive_risk_summary` that gives one row per host with: hostname, department, criticality, decommissioned status, a count of that host's **critical**-severity events, and a count of its currently **open or escalated** tickets. This view is the thing you (or the next analyst) will query every day going forward — build it so it doesn't need editing to be reused tomorrow.

Query your view, ordered to surface the highest-risk hosts first, and use its output as the backbone of the briefing below.

7.2. Using everything you found today — including your `executive_risk_summary` view — write a short briefing for the CISO. Include:

1. **Executive summary** (3–5 sentences): what's the overall security posture picture from today's data?
2. **Top 3 findings**, ranked by actual risk — not by raw event count. For each, name the host/ticket involved and why it matters. Pull these straight from your view's ranked output.
3. **One process gap** you identified today (hint: Part 3.5) and what you recommend to prevent it going forward.
4. **One explicit statement** addressing the Part 2.5 question: was the highest-event-count host actually the top risk? Justify your answer using your view's output, not just intuition.

Submit your SQL answer file (including your procedure, function, and view definitions) and this briefing together.

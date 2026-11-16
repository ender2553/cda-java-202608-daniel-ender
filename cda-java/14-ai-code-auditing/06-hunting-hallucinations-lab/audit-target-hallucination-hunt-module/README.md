# Lab: Hunt the Hallucinations — Full Module Audit

This is a complete AI-generated Python module: a small metrics-aggregation
pipeline that fetches records from an API, normalizes them with pandas, rolls
them up, and stores the result in S3. A copilot produced the whole thing,
manifest included. It has **not** been reviewed.

Your job is an end-to-end hallucination audit and a defensible findings list.

## Scope

Hunt every hallucination in the module:

- invented APIs and non-existent methods,
- real names used with a fabricated signature or wrong semantics,
- hallucinated or slopsquatted dependencies,
- calls that exist in some version but not the one the manifest **pins**.

At least one package-hallucination/slopsquat plant and one real-library /
wrong-version plant are present. At least one dependency and several calls are
genuinely fine and must survive your audit **unflagged**.

## Method — run the checklist on every symbol and dependency

**exists → provenance → version → signature → behavior**

Work **breadth first, then depth**: sweep all files once to list candidates
(unfamiliar imports, unusual method names, version-sensitive calls), then go
back and confirm each candidate down the chain against an authority external to
the code (PyPI, official docs, the pinned-version changelog, type stubs).

## Deliverable — the findings list

For every item, fill one row:

| Item | Location | Type | Authority Checked | Evidence | Decision |
|------|----------|------|-------------------|----------|----------|

`Decision` is **verify**, **reject**, or **unresolved**. A confirmed *verify* on
a clean item counts as much as a confirmed *reject* — blanket rejection fails
the audit. An honestly *unresolved* item (naming what you could not confirm and
which authority would settle it) beats a confidently wrong one.

## Rules of engagement

- Every finding needs a **named authority and captured evidence**. A flag
  without confirmation is an opinion, not an audit result.
- Treat every dependency as untrusted. **Do not run any install command** to
  test a package — the dangerous slopsquat case is the install that *succeeds*.
- A clean import or compile proves existence only, never correct usage.

## Files under audit

- `requirements.txt` — the AI-suggested dependency manifest.
- `api_client.py` — fetches metric records from the API.
- `transform.py` — normalizes records into a pandas DataFrame.
- `storage.py` — uploads artifacts to S3.
- `pipeline.py` — wires the stages together.

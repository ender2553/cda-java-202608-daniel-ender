# Audit Target: Hallucinated APIs

This is a small, AI-generated Python project for ingesting webhook events and
exposing them over a minimal Flask app. It was produced by a copilot from the
prompts shown at the top of each file and has **not** been reviewed.

## Your task

Audit the code for **AI hallucinations** in its API usage: calls that read
naturally but reference functions, methods, parameters, or configuration keys
that do not exist or do not work as the surrounding code assumes. The code uses
real libraries (`requests`, `datetime`, `hashlib`, `json`, `os`, `flask`), so
some calls are genuine and some are invented or misused.

For each suspect symbol, run a first audit pass and record:

1. **Symbol** — the exact call, attribute, parameter, or config key.
2. **Authority** — the external source of truth that should define it (official
   docs, the module/class reference, type stubs).
3. **Confirmation** — the check you ran against that authority and what it showed.
4. **Decision** — **confirm** (real and used correctly) or **reject**
   (hallucinated: invented symbol, wrong signature, or fabricated config key).

Note the *form* of each hallucination you find: an invented API / non-existent
method, a real name used with a fabricated signature, or a fabricated
configuration key that fails silently.

## Rules of engagement

- Do not rewrite the code. The deliverable is an evidence-backed decision per
  item, not a corrected version.
- A clean-looking line is not a cleared line — confirm every unfamiliar symbol
  against an authority external to the code itself.
- Not everything here is broken. A confirmed *clear* is as much a result as a
  confirmed *reject*.

## Files

- `event_ingest.py` — fetches, parses, hashes, and writes out events.
- `web_app.py` — a minimal Flask app exposing the events.

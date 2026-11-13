# Audit Target: Hallucinated Dependencies

This AI-generated mini-project is a token-auth API client. The assistant
produced the `requirements.txt` and the importing modules from the prompts at
the top of each file. None of it has been reviewed.

## Your task

Audit the **dependencies and their usage** for hallucinations and supply-chain
risk. Run the full verification chain on every package and every import:

**exists → provenance → version → signature → behavior**

For each item, decide:

- Does the **package exist** on PyPI under that exact name?
- Is it the **package actually intended**, or a plausible look-alike with no
  provenance (a possible **slopsquat**)?
- Does the API the code calls exist **in the pinned version**, or only in some
  other version?
- Is a real package being **used wrongly** (wrong argument, wrong call)?

## Deliverable

Produce a findings list. For every item record: **item & location**,
**authority checked** (PyPI, official docs, the pinned-version changelog, type
stubs), the **evidence** that authority produced, and a **confirm-or-reject**
decision.

## Rules of engagement

- Treat every package as untrusted until confirmed. **Do not run any install
  command** (`pip install ...`) to "see if it works" — a successful install is
  exactly the slopsquat failure mode.
- Audit APIs against the **pinned version** in `requirements.txt`, not the
  latest docs online.
- Not everything is broken. A confirmed **keep** on a real, correct dependency
  is as much a result as a reject.

## Files

- `requirements.txt` — the AI-suggested dependency manifest.
- `auth.py` — issues and verifies JWTs.
- `client.py` — HTTP client (search, order, HTML scrape, fan-out).

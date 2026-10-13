# Secure LLM API Integration — Student Starter Code

Your working copy for the **Secure LLM API Integration** course. It is one Python/
FastAPI microservice that you harden lesson by lesson, treating the model boundary
as an untrusted edge. The package imports and runs from day one — but the
implementations are intentionally **naive and insecure**. Each module has
`TODO(Lesson N)` markers telling you what to fix and where.

## Set up

```bash
python -m venv .venv && . .venv/bin/activate
pip install -e ".[dev]"

cp .env.example .env          # then fill in your own values
python -m pytest              # capstone tests start skipped — you implement them
```

## What you build, lesson by lesson

| Lesson | File | What you do |
|---|---|---|
| 1 — Anatomy of an API Call | `examples/trace_round_trip.py` | Run it; annotate the request/response round trip |
| 2 — Safely Parsing JSON | `src/secure_llm_service/response_handler.py` | Replace trusting access with defensive parsing |
| 3 — API Keys & Secrets | `src/secure_llm_service/config.py` | Remove the hardcoded default; load from env, fail fast |
| 5 — Strict Data Sanitization | `src/secure_llm_service/prompt_builder.py` | Separate roles; implement `is_safe_input()` |
| 6 — Preventing Leakage | `src/secure_llm_service/output_filter.py` | Add redaction passes + counts + length guard |
| 7 — Architecting the Service | `src/secure_llm_service/llm_client.py`, `app.py` | Plan where each control lives |
| 8 — Capstone | all of the above + `tests/test_security.py` | Assemble the secure service; implement the 5 security tests |

## How to work

- Search the code for `TODO(` to find every task.
- The capstone test suite (`tests/test_security.py`) is your acceptance check.
  Implement each test against the provided local stub server (`tests/conftest.py`),
  then make your service pass all five.
- A finished service should: load secrets only from the environment
  (`grep -r 'INSECURE-DEFAULT' src` returns nothing), parse responses defensively,
  reject prompt-injection and oversized input, redact PII from output, and survive
  a 429 and a timeout without leaking internals.

> This is the **student** repo. The complete reference solution lives in the
> separate instructor repo and is not distributed here.

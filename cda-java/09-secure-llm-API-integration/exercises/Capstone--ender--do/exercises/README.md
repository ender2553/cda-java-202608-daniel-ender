# Exercise Starters

Deliberately **vulnerable** starter files for the hands-on activities. Each is
self-contained (no API key or network needed — the provider is stubbed) and
runnable with `python <file>` so you can watch the flaw before you fix it.

| Lesson | Starter | You produce | The flaw to fix |
|---|---|---|---|
| L2 — Safely Parsing JSON Responses | `L2-safely-parsing/naive_handler.py` | a hardened handler (4 gates) | assumes response shape; crashes on a missing-`choices` / truncated / wrong-typed body |
| L3 — API Key Management & Secrets | `L3-secrets/insecure_app.py` | a refactored `app.py` + `.env` | hard-codes the key and prints it |
| L5 — Strict Data Sanitization | `L5-sanitization/vulnerable_summarizer.py` | `hardened_summarizer.py` | raw document concatenated into the prompt; any tool call executed unchecked |
| L6 — Preventing Data Leakage | `L6-leakage/leaky_service.py` | `safe_service.py` | returns raw output, logs full prompt/response, puts a full record (email/account/token) in the prompt |

> These are starting points, not reference solutions. The hardened reference for
> the whole service lives in `src/secure_llm_service/` (and in the instructor
> repo). Work each fix using only the techniques taught in that lesson.

Fake placeholder secrets (`sk-FAKE…`) are used throughout — never replace them
with a real key.

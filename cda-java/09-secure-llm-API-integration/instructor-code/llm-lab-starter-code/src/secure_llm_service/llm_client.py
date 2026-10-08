"""Talk to the LLM provider.

STARTER — Lesson 7 (architecture) and Lesson 8 (capstone build).

This minimal version makes a request with NO timeout and NO retry, and would leak
detail on failure. Your job:
  - Make this the ONLY module that holds the API key or calls the provider.
  - Add a hard timeout on every request (configurable via the Config).
  - Retry on HTTP 429, honoring Retry-After, up to cfg.max_retries.
  - Raise a generic, typed LLMUnavailableError on timeout/transport/error status
    — no URL, provider name, or stack detail surfaced to callers.
"""
from __future__ import annotations

import httpx

from .config import Config
from .response_handler import LLMResult, parse_chat_completion


class LLMUnavailableError(RuntimeError):
    """Raised when the provider cannot be reached or fails after retries."""


def complete(messages: list[dict], cfg: Config) -> LLMResult:
    # TODO(Lesson 8): add timeout, retry-on-429, structured request-id logging,
    #   and generic typed errors. This naive call has none of those protections.
    url = f"{cfg.api_base}/v1/chat/completions"
    resp = httpx.post(
        url,
        json={"model": cfg.model, "messages": messages},
        headers={"Authorization": f"Bearer {cfg.api_key}"},
    )
    return parse_chat_completion(resp.json(), expected_model=cfg.model)

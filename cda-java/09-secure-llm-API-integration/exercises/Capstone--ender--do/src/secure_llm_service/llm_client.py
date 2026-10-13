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
import time
import uuid
import logging

import httpx

from .config import Config
from .response_handler import LLMParseError, LLMResult, parse_chat_completion

logger = logging.getLogger("llm_service")

class LLMUnavailableError(RuntimeError):
    """Raised when the provider cannot be reached or fails after retries."""


def complete(messages: list[dict], cfg: Config) -> LLMResult:
    # COMPLETE(Lesson 8): add timeout, retry-on-429, structured request-id logging,
    #   and generic typed errors. This naive call has none of those protections.

    request_id = str(uuid.uuid4())
    url = f"{cfg.api_base}/v1/chat/completions"


    chars = 0
    for message in messages:
        chars += len(message.get("content", ""))

    prompt_tokens = (chars + 3) // 4

    for attempt in range(cfg.max_retries + 1):
        logger.info(
            "LLM request",
            extra={
                "request_id": request_id,
                "model": cfg.model,
                "prompt_tokens_estimate": prompt_tokens,
                "timestamp": time.time(),
            },
        )

        try:
            resp = httpx.post(
                url,
                json={"model": cfg.model, "messages": messages},
                headers={"Authorization": f"Bearer {cfg.api_key}"},
                timeout=cfg.timeout_seconds,
            )
        except httpx.RequestError as e:
                logger.error(
                    "LLM request failed",
                    extra={
                        "request_id": request_id,
                        "error_type": type(e).__name__,
                    },
                )
                raise LLMUnavailableError("LLM service unavailable") from None

        if resp.status_code == 429 and attempt < cfg.max_retries:
                wait = cfg.retry_backoff_seconds

                try:
                     retry_after = int(resp.headers.get("Retry-After", ""))
                     if retry_after >= 0:
                         wait = retry_after
                except ValueError:
                    pass

                logger.warning(
                    "LLM request rate limited",
                    extra={
                        "request_id": request_id,
                        "error_type": "LLMUnavailableError",
                    },
                )

                time.sleep(wait)
                continue

        if resp.status_code != 200:
            logger.error(
                "LLM request failed",
                extra={
                    "request_id": request_id,
                    "error_type": "LLMUnavailableError",
                },
            )
            raise LLMUnavailableError("LLM provider returned an error.")

        try:
                payload = resp.json()
        except ValueError:
                logger.error(
                    "LLM request failed with non-JSON response",
                    extra={
                        "request_id": request_id,
                        "error_type": "LLMParseError",
                    },
                )
                raise LLMParseError("Invalid Response") from None

        try:
                return parse_chat_completion(payload, expected_model=cfg.model)
        except LLMParseError:
                logger.error(
                    "LLM request failed to parse response",
                    extra={
                        "request_id": request_id,
                        "error_type": "LLMParseError",
                    },
                )
                raise

    raise LLMUnavailableError("Upstream service unavailable.")
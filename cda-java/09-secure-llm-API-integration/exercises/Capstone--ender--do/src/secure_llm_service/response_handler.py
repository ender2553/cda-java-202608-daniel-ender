"""Parse provider responses into a typed result.

STARTER — Lesson 2 (Safely Parsing JSON Responses).

This naive version trusts the response shape with direct key access — a malformed
or hostile response crashes with KeyError/TypeError deep in the call stack. Your
job in Lesson 2: parse defensively. Validate every field's presence and type, use
.get() with checks, and raise LLMParseError (whose message never contains the raw
response body) on any unexpected shape.
"""
from __future__ import annotations

from dataclasses import dataclass
from typing import Any


class LLMParseError(RuntimeError):
    """Raised when a provider response cannot be safely parsed."""


@dataclass(frozen=True)
class LLMResult:
    content: str
    model: str
    finish_reason: str


def parse_chat_completion(
    payload: Any, *, expected_model: str | None = None
) -> LLMResult:
    # COMPLETE (Lesson 2): SECURITY ISSUE — assumes and trusts the response shape.
    #   Validate types, use .get() with checks, raise LLMParseError on bad shape,
    #   and never put the raw response body into an error message.
    if not isinstance(payload, dict):
        raise LLMParseError("Provider response must be an object")

    choices = payload.get("choices")
    if not isinstance(choices, list) or not choices:
        raise LLMParseError("Provider response has invalid choices")

    first_choice = choices[0]
    if not isinstance(first_choice, dict):
        raise LLMParseError("Provider response has invalid choice")

    message = first_choice.get("message")
    if not isinstance(message, dict):
        raise LLMParseError("Provider response has invalid message")

    content = message.get("content")
    if not isinstance(content, str):
        raise LLMParseError("Provider response has invalid content")

    model = payload.get("model")
    if model is None:
        model = expected_model

    if not isinstance(model, str) or not model.strip():
        raise LLMParseError("Provider response has invalid model")

    finish_reason = first_choice.get("finish_reason")
    if finish_reason is None:
        finish_reason = "unknown"

    if not isinstance(finish_reason, str):
        raise LLMParseError("Provider response has invalid finish reason")

    return LLMResult(
        content=content,
        model=model,
        finish_reason=finish_reason,
    )

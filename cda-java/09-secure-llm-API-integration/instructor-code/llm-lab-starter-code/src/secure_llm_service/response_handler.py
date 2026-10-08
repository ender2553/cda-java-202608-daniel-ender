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


def parse_chat_completion(payload: Any, *, expected_model: str | None = None) -> LLMResult:
    # TODO(Lesson 2): SECURITY ISSUE — assumes and trusts the response shape.
    #   Validate types, use .get() with checks, raise LLMParseError on bad shape,
    #   and never put the raw response body into an error message.
    content = payload["choices"][0]["message"]["content"]
    return LLMResult(
        content=content,
        model=payload.get("model", expected_model or "unknown"),
        finish_reason=payload["choices"][0].get("finish_reason", "unknown"),
    )

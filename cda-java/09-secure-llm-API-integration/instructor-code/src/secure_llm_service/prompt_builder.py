"""Compose provider messages from a system prompt and user input.

STARTER — Lesson 5 (Strict Data Sanitization to Prevent Prompt Injection).

This naive version concatenates the system instructions and untrusted user text
into a single string — exactly the pattern that invites prompt injection. Your job
in Lesson 5:
  - Separate instructions from data using the provider's message roles
    (distinct system and user objects), not one concatenated blob.
  - Implement is_safe_input(): reject role-override tokens, over-long input, and
    control characters, returning a reason code — never echo the input.
  - Make build_messages() reject unsafe input via UnsafeInputError.
"""
from __future__ import annotations


class UnsafeInputError(ValueError):
    """Raised when user input fails sanitization. Carries only a reason code."""

    def __init__(self, reason_code: str):
        super().__init__(reason_code)
        self.reason_code = reason_code


def is_safe_input(text: str, *, max_chars: int = 2000) -> tuple[bool, str]:
    # TODO(Lesson 5): implement sanitization. This stub accepts everything.
    #   Return (False, "INPUT_TOO_LONG" | "CONTROL_CHARS" | "INJECTION_PATTERN")
    #   as appropriate; otherwise (True, "OK").
    return True, "OK"


def build_messages(
    system_prompt: str, user_input: str, *, max_chars: int = 2000
) -> list[dict]:
    # TODO(Lesson 5): SECURITY ISSUE — instructions and untrusted data are merged
    #   into one string. Separate them into distinct role objects, and reject
    #   unsafe input (raise UnsafeInputError) before building the messages.
    blob = f"{system_prompt}\n\nUser: {user_input}"
    return [{"role": "user", "content": blob}]

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
    """Validate that user input is safe to send to the model.

    Returns:
      (True, "OK") when the input passes validation.
      (False, reason_code) when the input fails validation.
    """
    if not isinstance(text, str):
        return False, "CONTROL_CHARS"

    if len(text) > max_chars:
        return False, "INPUT_TOO_LONG"

    if any(
        (ord(char) < 32 and char not in "\t\n") or ord(char) == 127
        for char in text
    ):
        return False, "CONTROL_CHARS"

    import re

    injection_patterns = [
        r"ignore\s+(all\s+)?(previous|prior|above|earlier)\s+instructions?",
        r"disregard\s+(all\s+)?(previous|prior|above|earlier)\s+instructions?",
        r"forget\s+(all\s+)?(previous|prior|above|earlier)\s+instructions?",
        r"\b(system|assistant)\s*:",
        r"<\s*/?\s*(system|assistant|developer)\s*>",
        r"\[\s*(system|assistant|developer)\s*\]",
        r"\b(override|bypass)\s+(the\s+)?(system\s+)?instructions?\b",
        r"reveal\s+(the\s+)?(system\s+prompt|hidden\s+instructions?)",
    ]

    for pattern in injection_patterns:
        if re.search(pattern, text, flags=re.IGNORECASE):
            return False, "INJECTION_PATTERN"

    return True, "OK"


def build_messages(
    system_prompt: str, user_input: str, *, max_chars: int = 2000
) -> list[dict]:
    """Build separate system and user messages after validating user input."""
    safe, reason_code = is_safe_input(user_input, max_chars=max_chars)

    if not safe:
        raise UnsafeInputError(reason_code)

    return [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_input},
    ]

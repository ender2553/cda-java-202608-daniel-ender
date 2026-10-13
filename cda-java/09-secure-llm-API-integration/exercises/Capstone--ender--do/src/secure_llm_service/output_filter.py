"""Filter model output before returning it."""

from __future__ import annotations

import re
from dataclasses import dataclass, field


@dataclass
class FilterResult:
    text: str
    counts: dict = field(default_factory=dict)
    truncated: bool = False


# Compile patterns once for efficient reuse.
EMAIL_PATTERN = re.compile(
    r"\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}\b",
    re.IGNORECASE,
)

PHONE_PATTERN = re.compile(
    r"(?<!\w)(?:\+?1[-.\s]?)?"
    r"(?:\(\d{3}\)|\d{3})[-.\s]?"
    r"\d{3}[-.\s]?\d{4}(?!\w)"
)

CREDIT_CARD_PATTERN = re.compile(
    r"(?<!\d)(?:\d[ -]?){12,18}\d(?!\d)"
)

API_KEY_PATTERN = re.compile(
    r"\b(?:"
    r"sk-[A-Za-z0-9_-]{8,}"
    r"|pk-[A-Za-z0-9_-]{8,}"
    r"|api[_-]?key\s*[:=]\s*[A-Za-z0-9_-]{8,}"
    r"|Bearer\s+[A-Za-z0-9._~+/-]{8,}=?"
    r")\b",
    re.IGNORECASE,
)


def filter_output(
    text: str,
    *,
    max_output_length: int = 8000,
) -> FilterResult:
    """Redact sensitive information and enforce the output length limit."""

    if not isinstance(text, str):
        raise TypeError("Output must be a string")

    if max_output_length < 0:
        raise ValueError("max_output_length must not be negative")

    counts = {
        "email": 0,
        "phone": 0,
        "credit_card": 0,
        "api_key": 0,
    }

    text, counts["email"] = EMAIL_PATTERN.subn(
        "[REDACTED_EMAIL]", text
    )

    text, counts["phone"] = PHONE_PATTERN.subn(
        "[REDACTED_PHONE]", text
    )

    text, counts["credit_card"] = CREDIT_CARD_PATTERN.subn(
        "[REDACTED_CC]", text
    )

    text, counts["api_key"] = API_KEY_PATTERN.subn(
        "[REDACTED_KEY]", text
    )

    # Truncate after redaction.
    truncated = len(text) > max_output_length

    if truncated:
        marker = "[TRUNCATED]"

        if max_output_length >= len(marker):
            text = text[:max_output_length - len(marker)] + marker
        else:
            text = marker[:max_output_length]

    return FilterResult(
        text=text,
        counts=counts,
        truncated=truncated,
    )
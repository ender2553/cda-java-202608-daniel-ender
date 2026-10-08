"""Filter model output before returning it.

STARTER — Lesson 6 (Preventing Sensitive Data Leakage).

This naive version returns the model output unchanged — a leaky service. Your job
in Lesson 6:
  - Add compiled-regex redaction passes for emails ([REDACTED_EMAIL]), US phone
    numbers ([REDACTED_PHONE]), card-shaped numbers ([REDACTED_CC]), and API-key
    patterns ([REDACTED_KEY]).
  - Return a per-field redaction COUNT (safe to log) — never the redacted values.
  - Add a max_output_length guard that truncates with a [TRUNCATED] marker.
"""
from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class FilterResult:
    text: str
    counts: dict = field(default_factory=dict)
    truncated: bool = False


def filter_output(text: str, *, max_output_length: int = 8000) -> FilterResult:
    # TODO(Lesson 6): SECURITY ISSUE — returns text unchanged (no redaction, no
    #   length guard). Implement the redaction passes and the truncation guard.
    return FilterResult(text=text, counts={}, truncated=False)

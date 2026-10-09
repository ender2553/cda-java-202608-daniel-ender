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

# Compile the patterns once so they can be reused efficiently. 
EMAIL_PATTERN = re.compile( 
    r"\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}\b", 
    re.IGNORECASE, 
    ) 

PHONE_PATTERN = re.compile( 
    r"(?<!\w)(?:\+?1[-.\s]?)?" 
    r"(?:\(\d{3}\)|\d{3})[-.\s]?" 
    r"\d{3}[-.\s]?\d{4}(?!\w)" ) 

CREDIT_CARD_PATTERN = re.compile( 
    r"(?<!\d)(?:\d[ -]?){12,18}\d(?!\d)" 
    ) 

API_KEY_PATTERN = re.compile( 
    r"\b(?:" 
    r"sk-[A-Za-z0-9_-]{8,}" 
    r"|" 
    r"pk-[A-Za-z0-9_-]{8,}" 
    r"|" 
    r"api[_-]?key\s*[:=]\s*[A-Za-z0-9_-]{8,}" 
    r"|" 
    r"Bearer\s+[A-Za-z0-9._~+/-]{8,}=*" 
    r")\b", 
    re.IGNORECASE, 
)

def filter_output(
        text: str, *, max_output_length: int = 8000
        ) -> FilterResult:
    # COMPLETE(Lesson 6): SECURITY ISSUE — returns text unchanged (no redaction, no
    #   length guard). Implement the redaction passes and the truncation guard.
    
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
    # Redact email addresses. 
    text, counts["email"] = EMAIL_PATTERN.subn( 
       "[REDACTED_EMAIL]", text 
    ) 
    
    # Redact US phone numbers. 
    text, counts["phone"] = PHONE_PATTERN.subn( 
       "[REDACTED_PHONE]", text 
    ) 
    
    # Redact card-shaped numbers. 
    text, counts["credit_card"] = CREDIT_CARD_PATTERN.subn( 
       "[REDACTED_CC]", text 
    ) 
    
    # Redact API-key patterns. 
    text, counts["api_key"] = API_KEY_PATTERN.subn( 
       "[REDACTED_KEY]", text 
    ) 
    
    # Truncate only after redaction, so sensitive values are never returned. 
    truncated = len(text) > max_output_length 
    if truncated: 
      marker = "[TRUNCATED]" 
    
      if max_output_length >= len(marker): 
        text = text[:max_output_length - len(marker)] + marker 
        
      else: 
         # Preserve the configured maximum length. 
         text = marker[:max_output_length]
    
    return FilterResult(
        text=text, 
        counts={}, 
        truncated=False)

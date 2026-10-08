"""Configuration loading.

STARTER — Lesson 3 (API Key Management and Secrets Handling).

This naive version hardcodes a fallback credential and reads config with silent
defaults. Your job in Lesson 3:
  - Read every secret from the environment ONLY (no literal default value).
  - Raise ConfigError listing any missing required variables — never a value.
  - Ensure the key never appears in a default, a print(), or an exception message.
"""
from __future__ import annotations

import os
from dataclasses import dataclass


class ConfigError(RuntimeError):
    """Raised when required configuration is missing or invalid."""


@dataclass(frozen=True)
class Config:
    api_key: str
    api_base: str
    model: str
    timeout_seconds: float = 10.0
    max_retries: int = 2
    retry_backoff_seconds: float = 5.0
    max_input_chars: int = 2000
    max_output_length: int = 8000

    @property
    def masked_key(self) -> str:
        return "***" if self.api_key else ""


def load_config(env: dict | None = None) -> Config:
    env = os.environ if env is None else env
    # TODO(Lesson 3): SECURITY ISSUE — hardcoded fallback credential + silent
    #   defaults. A missing key should fail loudly, not fall back to a literal.
    #   Replace the defaults below with env-only reads and a fail-fast check.
    return Config(
        api_key=env.get("LLM_API_KEY", "INSECURE-DEFAULT-KEY-REPLACE-IN-LESSON-3"),
        api_base=env.get("LLM_API_BASE", "https://api.your-provider.example").rstrip("/"),
        model=env.get("LLM_MODEL", "your-model-name"),
        timeout_seconds=float(env.get("LLM_TIMEOUT_SECONDS", "10")),
        max_retries=int(env.get("LLM_MAX_RETRIES", "2")),
        retry_backoff_seconds=float(env.get("LLM_RETRY_BACKOFF_SECONDS", "5")),
        max_input_chars=int(env.get("LLM_MAX_INPUT_CHARS", "2000")),
        max_output_length=int(env.get("LLM_MAX_OUTPUT_LENGTH", "8000")),
    )

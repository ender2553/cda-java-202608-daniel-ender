"""Secure LLM Service — STUDENT STARTER.

The public surface mirrors the finished service so the package imports and runs
from day one. The implementations are intentionally naive/insecure — each module
carries TODO markers for the lesson that hardens it. Build toward the secure
reference as you progress through the course; assemble and test it in the capstone.
"""
from .config import Config, ConfigError, load_config
from .llm_client import LLMUnavailableError, complete
from .output_filter import FilterResult, filter_output
from .prompt_builder import UnsafeInputError, build_messages, is_safe_input
from .response_handler import LLMParseError, LLMResult, parse_chat_completion

__all__ = [
    "Config",
    "ConfigError",
    "load_config",
    "build_messages",
    "is_safe_input",
    "UnsafeInputError",
    "complete",
    "LLMUnavailableError",
    "parse_chat_completion",
    "LLMResult",
    "LLMParseError",
    "filter_output",
    "FilterResult",
]

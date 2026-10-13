"""Trace one annotated request/response round trip to an LLM API.

Run this against a real provider (or a local stub) to *see* the untrusted edge:
the exact request you send, and the raw envelope you get back. Notice that the
body is whatever the remote service chose to return — your code must validate it,
not trust it.

    LLM_API_KEY=... LLM_API_BASE=https://api.provider.example LLM_MODEL=... \
        python examples/trace_round_trip.py "Say hello in one short sentence."

The key is read from the environment only. This script prints the request shape
and the response status + parsed content; it never prints the API key.
"""
from __future__ import annotations

import sys

import httpx

from secure_llm_service.config import load_config
from secure_llm_service.prompt_builder import build_messages
from secure_llm_service.response_handler import parse_chat_completion

SYSTEM = "You are a helpful assistant."


def main() -> int:
    prompt = sys.argv[1] if len(sys.argv) > 1 else "Say hello in one short sentence."
    cfg = load_config()
    messages = build_messages(SYSTEM, prompt, max_chars=cfg.max_input_chars)

    url = f"{cfg.api_base}/v1/chat/completions"
    body = {"model": cfg.model, "messages": messages}

    # --- request side (annotate) ---
    print("REQUEST")
    print(f"  POST {url}")
    print("  Authorization: Bearer ***   <- key present, never printed")
    print(f"  model: {cfg.model}")
    print(f"  messages: {messages}")

    with httpx.Client(timeout=cfg.timeout_seconds) as client:
        resp = client.post(
            url,
            json=body,
            headers={
                "Authorization": f"Bearer {cfg.api_key}",
                "Content-Type": "application/json",
            },
        )

    # --- response side (annotate) ---
    print("\nRESPONSE")
    print(f"  status: {resp.status_code}   <- check before trusting the body")
    result = parse_chat_completion(resp.json(), expected_model=cfg.model)
    print(f"  parsed content: {result.content!r}")
    print(f"  finish_reason: {result.finish_reason}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

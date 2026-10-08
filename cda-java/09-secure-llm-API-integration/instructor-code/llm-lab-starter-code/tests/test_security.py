"""Security integration suite — CAPSTONE DELIVERABLE (Lesson 8).

Implement these five tests against the local stub LLM server (see conftest.py,
which is already provided). Each currently skips — replace the skip with a real
test. The suite must exercise the full service stack (not mocked at the HTTP
layer) and cover:

  1. a prompt-injection attempt  -> rejected, reason code INJECTION_PATTERN
  2. an oversized input          -> rejected, reason code INPUT_TOO_LONG
  3. a PII-bearing response      -> redacted before return; redaction count logged
  4. a 429 from the provider     -> retried, then succeeds
  5. a provider timeout          -> LLMUnavailableError, no internal detail leaked

Fixtures available from conftest.py: `cfg` (a Config pointed at the stub) and
`stub_server` (yields (base_url, STUB); set STUB["mode"] / STUB["content"]).
"""
import pytest


def test_prompt_injection_is_rejected(cfg):
    pytest.skip("TODO(capstone): assert an injection input -> 400, reason INJECTION_PATTERN")


def test_oversized_input_is_rejected(cfg):
    pytest.skip("TODO(capstone): assert an over-long input -> 400, reason INPUT_TOO_LONG")


def test_pii_in_response_is_redacted(cfg, stub_server):
    pytest.skip("TODO(capstone): stub a PII response; assert it is redacted and counted")


def test_rate_limit_is_retried_then_succeeds(cfg, stub_server):
    pytest.skip("TODO(capstone): stub 429-then-ok; assert final 200 and two calls")


def test_timeout_raises_unavailable_without_leaking(cfg, stub_server):
    pytest.skip("TODO(capstone): stub a timeout; assert LLMUnavailableError, no leak")

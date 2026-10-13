"""Test fixtures: a real local stub LLM server (not an HTTP-layer mock).

The stub is a genuine HTTP server on an ephemeral port, so the service exercises
its full network stack — timeouts, retries, and JSON parsing all run for real.
Tests steer the stub through the module-level STUB dict (mode, content).
"""
from __future__ import annotations

import json
import threading
import time
from http.server import BaseHTTPRequestHandler, HTTPServer

import pytest

from secure_llm_service.config import Config

# Per-test stub state. Reset by the stub_server fixture.
STUB = {"mode": "ok", "content": "Hello, how can I help?", "calls": 0}


class _StubHandler(BaseHTTPRequestHandler):
    # HTTP/1.1 with explicit Content-Length + Connection: close on every reply.
    protocol_version = "HTTP/1.1"

    def log_message(self, *args):  # silence stderr access logs
        pass

    def _reply(self, status: int, body: bytes, extra_headers: dict | None = None):
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.send_header("Connection", "close")
        for key, value in (extra_headers or {}).items():
            self.send_header(key, value)
        self.end_headers()
        self.wfile.write(body)

    def do_POST(self):  # noqa: N802 (BaseHTTPRequestHandler API)
        # Always drain the request body — leaving it unread resets the connection.
        length = int(self.headers.get("Content-Length", 0) or 0)
        if length:
            self.rfile.read(length)

        STUB["calls"] += 1
        mode = STUB["mode"]

        if mode == "timeout":
            # Sleep past the client timeout so the client raises before we reply.
            time.sleep(2.0)

        if mode == "429-then-ok" and STUB["calls"] == 1:
            self._reply(429, b'{"error": "rate_limited"}', {"Retry-After": "0"})
            return

        payload = {
            "model": "stub-model",
            "choices": [
                {
                    "message": {"role": "assistant", "content": STUB["content"]},
                    "finish_reason": "stop",
                }
            ],
        }
        self._reply(200, json.dumps(payload).encode())


@pytest.fixture
def stub_server():
    """Start a fresh stub server; yield (base_url, STUB) and shut it down after."""
    STUB.update({"mode": "ok", "content": "Hello, how can I help?", "calls": 0})
    server = HTTPServer(("127.0.0.1", 0), _StubHandler)
    port = server.server_address[1]
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    try:
        yield f"http://127.0.0.1:{port}", STUB
    finally:
        server.shutdown()
        server.server_close()


@pytest.fixture
def cfg(stub_server):
    """A Config pointed at the stub, with fast timeouts and zero retry back-off."""
    base, _ = stub_server
    return Config(
        api_key="sk-test-key-not-a-real-secret",
        api_base=base,
        model="stub-model",
        timeout_seconds=1.0,
        max_retries=2,
        retry_backoff_seconds=0.0,
        max_input_chars=2000,
        max_output_length=8000,
    )

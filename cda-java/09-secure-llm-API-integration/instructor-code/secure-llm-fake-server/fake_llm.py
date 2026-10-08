"""Fake LLM server for the Secure LLM API Integration lessons.

A stand-in for a real model provider. It speaks the same OpenAI-style
chat-completions format that Ollama and OpenAI use, so the secure_llm_service
app cannot tell the difference. No install, no API key, no cost: it uses only
the Python standard library.

Run it in its own terminal:

    python fake_llm.py                 # normal replies
    python fake_llm.py --mode slow     # trigger the client timeout
    python fake_llm.py --mode error    # provider returns HTTP 500
    python fake_llm.py --mode ratelimit  # provider returns HTTP 429 every time
    python fake_llm.py --mode broken   # provider returns a malformed body

Stop it with Ctrl+C. To simulate "provider is down", just stop it.

Replies are canned, chosen by keywords in your message, so every security
control in the app can be demonstrated on demand:

    "contact"         -> reply leaks an email and phone number  (output redaction)
    "payment"         -> reply leaks a test credit card number  (output redaction)
    "secret"          -> reply leaks an API-key-shaped string   (output redaction)
    anything else     -> reply echoes your message back, so any email or phone
                         number you type also comes back and gets redacted
"""
from __future__ import annotations

import argparse
import json
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

MODES = ("normal", "slow", "error", "ratelimit", "broken")

# All values below are fake: reserved example domain, 555 phone number,
# a published test card number, and a made-up key.
CANNED_REPLIES = [
    (
        ("contact",),
        "Sure! Here is a sample contact card:\n"
        "Name: Jordan Rivera\n"
        "Email: jordan.rivera@example.com\n"
        "Phone: (555) 123-4567",
    ),
    (
        ("payment", "credit card"),
        # The comma keeps the redaction pattern from swallowing the next space.
        "For testing payments you can use the card number 4111 1111 1111 1111, "
        "with any future expiry date.",
    ),
    (
        ("secret",),
        "The service is configured with the key sk-DEMO1234567890abcdef. "
        "(A real model should never reveal this.)",
    ),
]

ECHO_LIMIT = 500


def choose_reply(user_text: str) -> str:
    lowered = user_text.lower()
    for keywords, reply in CANNED_REPLIES:
        if any(word in lowered for word in keywords):
            return reply
    echoed = user_text[:ECHO_LIMIT]
    return (
        f'[fake LLM] You said: "{echoed}"\n'
        "This is a canned reply from the fake LLM server. Try a message "
        'containing "contact", "payment" or "secret" to see output redaction.'
    )


class FakeLLMHandler(BaseHTTPRequestHandler):
    protocol_version = "HTTP/1.1"
    mode = "normal"
    delay = 15.0

    def log_message(self, *args):  # replaced by the summary line in do_POST
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
        # Always drain the request body; leaving it unread resets the connection.
        length = int(self.headers.get("Content-Length", 0) or 0)
        raw = self.rfile.read(length) if length else b""

        if self.path.rstrip("/") != "/v1/chat/completions":
            self._reply(404, b'{"error": "not_found"}')
            return

        try:
            request = json.loads(raw)
            messages = request["messages"]
            roles = [m["role"] for m in messages]
            user_text = next(m["content"] for m in reversed(messages) if m["role"] == "user")
        except (ValueError, KeyError, TypeError, StopIteration):
            self._reply(400, b'{"error": "bad_request"}')
            return

        # Log the shape of the request, never its content or the key value,
        # the same rule the secure service follows.
        has_key = self.headers.get("Authorization", "").startswith("Bearer ")
        print(
            f"request: roles={roles} user_chars={len(user_text)} "
            f"auth_header={'present' if has_key else 'MISSING'} mode={self.mode}",
            flush=True,
        )

        if self.mode == "slow":
            print(f"  sleeping {self.delay:g}s (slow mode)", flush=True)
            time.sleep(self.delay)
        elif self.mode == "error":
            self._reply(500, b'{"error": "internal_server_error"}')
            return
        elif self.mode == "ratelimit":
            self._reply(429, b'{"error": "rate_limited"}', {"Retry-After": "1"})
            return
        elif self.mode == "broken":
            self._reply(200, b'{"unexpected": "shape"}')
            return

        payload = {
            "model": request.get("model", "fake-model"),
            "choices": [
                {
                    "message": {"role": "assistant", "content": choose_reply(user_text)},
                    "finish_reason": "stop",
                }
            ],
        }
        try:
            self._reply(200, json.dumps(payload).encode())
        except (BrokenPipeError, ConnectionResetError):
            # The client gave up (timeout) before we answered.
            print("  client disconnected before the reply was sent", flush=True)


def main() -> None:
    parser = argparse.ArgumentParser(description="Fake OpenAI-style LLM server")
    parser.add_argument("--port", type=int, default=9999)
    parser.add_argument("--mode", choices=MODES, default="normal")
    parser.add_argument(
        "--delay", type=float, default=15.0, help="seconds to wait in slow mode"
    )
    args = parser.parse_args()

    FakeLLMHandler.mode = args.mode
    FakeLLMHandler.delay = args.delay
    server = ThreadingHTTPServer(("127.0.0.1", args.port), FakeLLMHandler)
    print(
        f"Fake LLM listening on http://127.0.0.1:{args.port} (mode: {args.mode}). "
        "Press Ctrl+C to stop.",
        flush=True,
    )
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        server.server_close()
        print("Fake LLM stopped.")


if __name__ == "__main__":
    main()

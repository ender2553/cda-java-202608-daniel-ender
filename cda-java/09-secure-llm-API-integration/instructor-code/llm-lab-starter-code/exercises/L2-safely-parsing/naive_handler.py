"""Starter for the Lesson 2 activity: harden a naive LLM response handler.

This is the NAIVE version you are asked to harden. It assumes the response
always has the documented success shape and reaches straight for the content —
so a missing `choices` key, a truncated body, or a wrong-typed field crashes it
or lets bad data flow inward. Your job (see the activity brief) is to write the
four sample responses and rebuild this so each control point fails closed:
outer parse -> envelope -> schema -> bounds.

Run `python naive_handler.py` to watch the naive version succeed on a good
response and crash on a bad one.
"""


class Response:
    """Minimal stand-in for an HTTP response object with a .json() method."""

    def __init__(self, body):
        self._body = body

    def json(self):
        # In real life this can raise json.JSONDecodeError on a non-JSON body.
        return self._body


def naive_handle(response):
    """The naive handler — assumes shape, no gates."""
    data = response.json()
    return data["choices"][0]["message"]["content"]


if __name__ == "__main__":
    good = Response({
        "choices": [{"message": {"content": "Here is the summary."}, "finish_reason": "stop"}],
    })
    print("good response ->", naive_handle(good))

    # An error envelope arrived with HTTP 200 but no `choices` — the naive
    # handler raises KeyError instead of failing closed.
    error_at_200 = Response({"error": {"type": "invalid_request_error", "message": "bad model"}})
    print("error-at-200 ->", end=" ")
    print(naive_handle(error_at_200))  # <-- crashes here; this is the vulnerability to fix

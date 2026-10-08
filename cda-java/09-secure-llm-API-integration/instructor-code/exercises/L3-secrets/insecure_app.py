"""Starter for the Lesson 3 activity: secure key-loading refactor.

This script does everything wrong with a secret: it hard-codes the API key as a
string literal and then prints it. Your job (see the activity brief) is to
refactor it so the key loads from the environment after `load_dotenv()`, fails
fast with a `ValueError` that names the variable but not its value when the key
is absent, and never appears in output, logs, or version control.

Run `python insecure_app.py` to see the leak the refactor must remove.
"""

# VULNERABILITY 1: a secret hard-coded in source (and committable to git).
# The value below is a fake placeholder — never paste a real key anywhere.
api_key = "sk-FAKE000000000000000000000000000000000000"

# VULNERABILITY 2: the key is written straight to stdout.
print(f"Using key: {api_key}")


def call_llm(prompt):
    """Pretend to call the provider. The point is only that it needs the key."""
    if not api_key:
        raise RuntimeError("no key")
    return f"(response to: {prompt})"


if __name__ == "__main__":
    print(call_llm("Summarize this paragraph."))

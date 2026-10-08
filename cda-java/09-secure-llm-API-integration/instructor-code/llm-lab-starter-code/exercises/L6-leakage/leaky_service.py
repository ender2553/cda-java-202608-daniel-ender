"""Starter for the Lesson 6 activity: add output filtering and logging discipline.

This service leaks sensitive data in three ways:
  1. it returns the model's raw text response with no inspection;
  2. it logs the full prompt and response at DEBUG;
  3. it passes a full customer record — email, account number, bearer token —
     into the prompt context.

Your job (see the activity brief) is to close all three channels and save the
result as `safe_service.py`: add an output filter that redacts emails and bearer
tokens, minimize the context with an allow-list, and replace raw-payload logging
with redacted structured metadata.

Run `python leaky_service.py` to watch all three leaks happen.
"""

import logging

logging.basicConfig(level=logging.DEBUG, format="%(levelname)s %(message)s")
logger = logging.getLogger("leaky_service")

# A full customer record. Only some of this is needed to answer a question, but
# the leaky version puts all of it into the prompt.
CUSTOMER = {
    "name": "Dana Lopez",
    "city": "Toledo",
    "email": "dana.lopez@example.com",          # sensitive
    "account_number": "4471-9920-1183",          # sensitive
    "bearer_token": "Bearer sk-FAKE0000token0000example0000",  # secret
}


def build_prompt(record, question):
    # VULNERABILITY 3: the entire record (incl. email, account, token) goes in.
    return f"Customer record: {record}\n\nQuestion: {question}\nAnswer:"


def call_model(prompt):
    """Stand-in for the provider. It reflects record fields back, the way a
    model coaxed by an extraction prompt might surface PII it was handed."""
    return (
        "Dana is in Toledo. For reference, contact dana.lopez@example.com "
        "and auth header Bearer sk-FAKE0000token0000example0000."
    )


def handle(question):
    prompt = build_prompt(CUSTOMER, question)
    logger.debug("prompt=%s", prompt)        # VULNERABILITY 2: raw prompt logged
    response = call_model(prompt)
    logger.debug("response=%s", response)    # VULNERABILITY 2: raw response logged
    return response                          # VULNERABILITY 1: raw output returned


if __name__ == "__main__":
    print("returned to caller ->", handle("Where is the customer located?"))

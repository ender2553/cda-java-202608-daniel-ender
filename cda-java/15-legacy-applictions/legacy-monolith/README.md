# legacy-monolith

Accounts/ledger service. Runs on port 8080.

## Endpoints

- `GET /reports/{accountId}` — returns the current balance for an account.
- `GET /reports/search?filter=...` — returns a list of accounts. The `filter` is
  applied to the account owner name only.

## Notes

Account data is seeded at startup. Reads are safe to call concurrently.

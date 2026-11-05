# Add Authorization Handling and Secure Token Storage

## Scenario

This is a pure single-page app calling a third-party API. There is no
first-party backend session for this client, so an `HttpOnly` cookie is not
an available option — the bearer token has to be attached explicitly by your
code, and stored somewhere your code can read it back from.

## Your task

`app.js` has three problems. Fix all three:

- **No `Authorization` header is ever attached.** `loadProfile()` calls
  `fetch('/api/user')` with no header at all, so it always gets a `401`
  regardless of whether a token is stored. Attach `Authorization: Bearer
  <token>` using the stored token.
- **The token is stored in `localStorage`** — the easiest option to wire up,
  which is exactly why it's the trap here. Given the scenario above (no
  `HttpOnly` cookie available), reason between `localStorage` and in-memory
  storage and implement whichever is actually the least-exposed viable
  option for this scenario. Don't default to whichever was easiest to reach
  for.
- **On a `401`, the code silently retries the same request with the same
  token** (capped at 3 attempts only so the broken starter doesn't hammer the
  server forever). Replace this with clean handling: on `401`, clear the
  stale token and force the user through an explicit re-authentication step
  — never a silent retry using a credential you already know is bad.

## Files

- `index.html` — the runnable page (Log in / Load profile / Clear token
  buttons).
- `app.js` — the token storage, attachment, and `401`-handling logic; this
  is what you fix.

## Run it

From this folder:

```
node ../../../../../../_server/serve.js
```

Then open `http://localhost:8080`.

## Acceptance criteria

- Click **Log in**, then **Load profile** — the profile loads successfully
  (the `Authorization` header is present on the request; check the Network
  panel).
- Click **Clear token (simulate expiry)**, then **Load profile** again — you
  see a clean "session expired, please log in again" state, not a silent
  retry loop and not a generic error.
- The token is never written to `localStorage`. Confirm in DevTools →
  Application → Local Storage that no auth token appears there after fixing
  the storage choice.
- No `console.log` anywhere in the app prints the raw token value.

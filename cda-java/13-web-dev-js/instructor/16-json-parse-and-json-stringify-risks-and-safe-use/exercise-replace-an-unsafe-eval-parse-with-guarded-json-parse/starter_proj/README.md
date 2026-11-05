# Replace an Unsafe `eval` Parse with Guarded `JSON.parse`

## Your task

You are given a small page that "parses" a response body with `eval()` and
logs the current session with no thought for what it contains. Open
`index.html` in a browser and work the exercise:

- Locate `parseResponse` in `app.js`. It parses by wrapping the text in
  parentheses and calling `eval()` on the result — the historical shortcut
  this lesson warns about.
- Replace the `eval()` call with `JSON.parse`, wrapped in a `try/catch` that
  returns `{ ok: true, data }` on success and `{ ok: false, error }` on
  failure, instead of throwing out of the function.
- Test your replacement against the provided hostile response string.
  Confirm the original `eval` version executes it (the arbitrary-code-execution
  marker fires), and your `JSON.parse` version safely rejects it with a
  caught `SyntaxError` instead.
- Add a `JSON.stringify` replacer to `logSession` so the `authToken` field is
  never present in logged output, even though the in-memory `currentSession`
  object still has it.
- Verify the replacer by inspecting the actual logged string in the page
  (or the console), not just by reading the code.

Everything here is local and benign — the "hostile" payload only flips a
visible marker and logs a console warning. Nothing contacts the network.

## Files

- `index.html` — the runnable page: parse buttons, a result panel, an
  arbitrary-code-execution marker, and a session-logging panel.
- `app.js` — the vulnerable script: `eval`-based `parseResponse` and a
  replacer-free `logSession`, with `TODO` markers where you harden.

## Acceptance criteria

- Clicking **Parse hostile response** with the starter code flips the
  arbitrary-code-execution marker to *FIRED* and logs a console warning —
  proof `eval` ran code, not just data.
- After your fix, clicking the same button no longer fires the marker; the
  result panel shows a caught error instead of an executed payload.
- Clicking **Parse benign response** still returns the parsed object
  correctly, both before and after your fix.
- After your fix, clicking **Log session (unsafe)** and inspecting the
  logged output shows no `authToken` substring anywhere in the string, while
  `userId` and `displayName` are still present.
- Runnable by opening `index.html` directly in a browser, with no build step
  or server.

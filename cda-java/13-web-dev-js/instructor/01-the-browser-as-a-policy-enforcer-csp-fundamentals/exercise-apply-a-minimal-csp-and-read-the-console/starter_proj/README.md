# Apply a Minimal CSP and Read the Console

## Your task

`index.html` ships with **no** Content Security Policy at all: an inline `<script>` sets the status
text, an `<img>` loads from a partner asset host, and a third-party `<script src>` loads a widget —
none of it is currently governed by any policy.

1. Start the server from this project root: `node ../../../../../../_server/serve.js`
2. Create a `headers.json` file in this project root applying a minimal baseline policy:
   ```json
   { "/": { "Content-Security-Policy": "default-src 'self'" } }
   ```
3. Restart the server (headers are read once at startup) and reload `http://localhost:8080/` with
   DevTools open to the **Console** tab.
4. For every violation message you see, write down: which directive fired, and which resource was
   blocked (the exact URL or `inline`).
5. **Do not** add source-list entries, `'unsafe-inline'`, or anything else to make the violations
   disappear. The point of this exercise is reading and correctly interpreting what the browser
   blocks — writing a working policy is a later skill.

## Files

- `index.html` — the dashboard page (inline script, cross-origin image, cross-origin script).
- `headers.json` — **you create this.** Not present in the starter.

## Acceptance criteria

- `headers.json` applies exactly `default-src 'self'` at `/`.
- You can name all three violations from the Console: the inline `<script>` block, the cross-origin
  `<img>`, and the cross-origin `<script src>` — each with its blocked resource and the directive
  that fired (`default-src`, since no more specific directive is set).
- The page's markup and the policy string are unchanged from what you started with — only
  `headers.json` was added.

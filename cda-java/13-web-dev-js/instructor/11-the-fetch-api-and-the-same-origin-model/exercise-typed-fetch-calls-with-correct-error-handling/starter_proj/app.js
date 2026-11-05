// Order Status Dashboard — STARTER (incomplete error handling)
//
// This page calls four endpoints on the shared dev server:
//   - /api/echo             -> 200, real success
//   - /api/does-not-exist   -> 404, but the catch-all still returns a valid
//                              JSON body ({ error: 'no such endpoint' })
//   - /api/crash            -> 500, but ALSO a valid JSON body
//   - a bad port            -> genuinely unreachable (network-level failure)
//
// The bug this starter demonstrates: response.json() succeeds on the 404 and
// the 500 case too, because both endpoints still return well-formed JSON.
// A fetch() call only REJECTS on a real network failure — never on an HTTP
// error status. If you never check response.ok, a 404 or a 500 looks
// identical to a real success once the body parses cleanly.

const TARGETS = [
  { label: 'success (200)',        url: '/api/echo' },
  { label: 'not found (404)',      url: '/api/does-not-exist' },
  { label: 'server error (500)',   url: '/api/crash' },
  { label: 'unreachable',          url: 'http://localhost:8099/api/echo' }
];

const resultsEl = document.getElementById('results');

function renderRow(label, kind, detail) {
  const row = document.createElement('div');
  row.className = 'row';
  const l = document.createElement('div');
  l.className = 'label';
  l.textContent = `${label} → ${kind}`;
  const d = document.createElement('div');
  d.textContent = typeof detail === 'string' ? detail : JSON.stringify(detail);
  row.append(l, d);
  resultsEl.append(row);
}

async function runOne(target) {
  // COMPLETE: this only distinguishes "the fetch() promise rejected" from
  // "it resolved" — it never checks response.ok, so an HTTP error with a
  // parseable JSON body (404, 500) is reported as a success below.
  //
  // Updated: response.ok is now checked before reading the response body.

  try {
    const response = await fetch(target.url);

    // Check HTTP status BEFORE reading the response body.
    if (!response.ok) {

      // Distinguish a missing resource from a server error.
      if (response.status === 404) {
        renderRow(
          target.label,
          'HTTP error 404',
          'Resource not found.'
        );
        return;
      }

      if (response.status === 500) {
        renderRow(
          target.label,
          'HTTP error 500',
          'Internal server error.'
        );
        return;
      }

      // Handle any other unsuccessful HTTP status.
      renderRow(
        target.label,
        `HTTP error ${response.status}`,
        'Request unsuccessful.'
      );
      return;
    }

    // COMPLETE: check response.ok (and branch on response.status) BEFORE
    // treating this as success data.
    //
    // Updated: response.ok and response.status have now been checked above.
    // JSON is only parsed after confirming a successful HTTP response.

    const data = await response.json();

    renderRow(target.label, 'success', data);

  } catch (err) {
    // COMPLETE: this single catch block is the only place any failure is
    // handled — it can't tell a real network failure (unreachable URL)
    // apart from an HTTP error, because HTTP errors never land here.
    //
    // Updated: HTTP errors are handled above. This catch handles genuine
    // network failures, such as the unreachable URL.

    renderRow(target.label, 'network failure', err.message);
  }
}

document.getElementById('run-btn').addEventListener('click', async () => {
  resultsEl.innerHTML = '';
  for (const target of TARGETS) {
    await runOne(target);
  }
});


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
  // TODO: this only distinguishes "the fetch() promise rejected" from
  // "it resolved" — it never checks response.ok, so an HTTP error with a
  // parseable JSON body (404, 500) is reported as a success below.
  try {
    const response = await fetch(target.url);
    const data = await response.json();
    // TODO: check response.ok (and branch on response.status) BEFORE
    // treating this as success data.
    renderRow(target.label, 'success', data);
  } catch (err) {
    // TODO: this single catch block is the only place any failure is
    // handled — it can't tell a real network failure (unreachable URL)
    // apart from an HTTP error, because HTTP errors never land here.
    renderRow(target.label, 'request failed', err.message);
  }
}

document.getElementById('run-btn').addEventListener('click', async () => {
  resultsEl.innerHTML = '';
  for (const target of TARGETS) {
    await runOne(target);
  }
});

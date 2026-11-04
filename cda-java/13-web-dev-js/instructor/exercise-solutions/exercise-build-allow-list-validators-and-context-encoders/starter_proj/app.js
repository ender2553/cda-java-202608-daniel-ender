// Validators & Encoders Console — wires validators.js to a live page.
//
// Two things happen here:
//   1. A manual-entry section lets you type into each field and watch the
//      matching encoder/validator run live.
//   2. A "Run bypass-payload suite" button runs every payload in
//      BYPASS_PAYLOADS against the field it targets and reports PASS/FAIL —
//      PASS means the payload was rendered inert (or rejected) by the
//      correct technique for that context.

function $(id) {
  return document.getElementById(id);
}

// ---------------------------------------------------------------------------
// Bypass-payload suite. Each entry targets one field/technique.
// ---------------------------------------------------------------------------
const BYPASS_PAYLOADS = [
  { field: 'displayName', value: '<script>alert(1)</script>', label: 'script tag' },
  { field: 'displayName', value: '<img src=x onerror=alert(1)>', label: 'onerror image' },
  { field: 'bio', value: '"><img src=x onerror=alert(1)>', label: 'attribute breakout + tag' },
  { field: 'bio', value: '" onmouseover="alert(1)', label: 'attribute breakout handler' },
  { field: 'search', value: 'javascript:alert(1)', label: 'javascript scheme in query' },
  { field: 'search', value: 'x&admin=true', label: 'query parameter injection' },
  { field: 'userId', value: '../../etc/passwd', label: 'path traversal identifier' },
  { field: 'userId', value: "admin'; DROP TABLE users; --", label: 'non-allow-listed identifier' },
  { field: 'status', value: 'deleted', label: 'non-enum status' },
  { field: 'age', value: -5, label: 'negative age' },
  { field: 'age', value: 999, label: 'out-of-range age' },
  { field: 'joinDate', value: '2023-02-30', label: 'non-existent calendar date' },
  { field: 'joinDate', value: 'not-a-date', label: 'malformed date string' },
];

// ---------------------------------------------------------------------------
// Manual-entry live demo — HTML body context via encodeForHtml, inserted
// through innerHTML on purpose so you can see the encoded tag show up as
// literal text instead of executing.
// ---------------------------------------------------------------------------
function renderDisplayName() {
  const raw = $('displayNameInput').value;
  $('displayNamePreview').innerHTML = 'Hello, ' + Validators.encodeForHtml(raw) + '!';
}

// Attribute context — build a `title="..."` attribute by string
// concatenation, encoded with encodeForAttribute so a quote in the input
// cannot break out of the attribute.
function renderBio() {
  const raw = $('bioInput').value;
  $('bioPreview').innerHTML =
    '<span title="' + Validators.encodeForAttribute(raw) + '">Hover for bio</span>';
}

// URL context — build a search link's query string with encodeForUrl.
function renderSearch() {
  const raw = $('searchInput').value;
  $('searchPreview').innerHTML =
    '<a href="/search?q=' + Validators.encodeForUrl(raw) + '">Run search</a>';
}

// Structured fields — allow-list validation, fail closed.
function renderStructured() {
  const results = [
    tryValidate('userId', () => Validators.validateIdentifier($('userIdInput').value)),
    tryValidate('status', () => Validators.validateStatus($('statusInput').value)),
    tryValidate('age', () => Validators.validateInteger($('ageInput').value, { min: 0, max: 130 })),
    tryValidate('joinDate', () => Validators.validateDate($('joinDateInput').value)),
  ];
  $('structuredPreview').textContent = results.join(' | ');
}

function tryValidate(name, fn) {
  try {
    return name + ': accepted (' + fn() + ')';
  } catch (err) {
    return name + ': REJECTED — ' + err.message;
  }
}

// ---------------------------------------------------------------------------
// Why regex tag-stripping fails (LO5) — demonstration only, never used as a
// defense. A `>` character inside a quoted attribute value is a normal,
// legal part of that attribute to a real HTML parser, but it ends the naive
// regex's match early — "a tag broken across an unexpected attribute
// boundary," exactly the failure category this lesson names. The regex
// truncates at the first `>` (inside `title="a>b"`) and leaves the dangerous
// `onerror=alert(1)` fragment behind as leftover text instead of removing
// the whole tag.
// ---------------------------------------------------------------------------
function renderRegexFailureDemo() {
  const payload = '<img title="a>b" src=x onerror=alert(1)>';
  const stripped = Validators.stripTagsNaive(payload);
  $('regexFailureOut').textContent =
    'stripTagsNaive(' + JSON.stringify(payload) + ') => ' + JSON.stringify(stripped) +
    ' — "onerror=alert(1)" survives the strip.';
}

// ---------------------------------------------------------------------------
// Bypass-payload suite runner.
// ---------------------------------------------------------------------------
function encodedValueIsSafe(encodedValue, { checkQuote = false } = {}) {
  // Check the ENCODED PAYLOAD ITSELF, not the full rendered string (which
  // also contains legitimate raw `<`/`"` from the trusted template around
  // it). Encoding preserves every character as text — the literal substring
  // "onerror=" may still legitimately appear post-encoding — what matters is
  // that no raw `<`, `>` (or, in an attribute, `"`) survived to let the
  // browser's parser treat any of it as markup or break out of the
  // attribute.
  if (/[<>]/.test(encodedValue)) return false;
  if (checkQuote && encodedValue.includes('"')) return false;
  return true;
}

function runSuite() {
  const rows = [];

  for (const payload of BYPASS_PAYLOADS) {
    let pass;
    let detail;

    if (payload.field === 'displayName') {
      const encoded = Validators.encodeForHtml(payload.value);
      const rendered = 'Hello, ' + encoded + '!';
      pass = encodedValueIsSafe(encoded);
      detail = rendered;
    } else if (payload.field === 'bio') {
      const encoded = Validators.encodeForAttribute(payload.value);
      const rendered = '<span title="' + encoded + '">Hover for bio</span>';
      pass = encodedValueIsSafe(encoded, { checkQuote: true });
      detail = rendered;
    } else if (payload.field === 'search') {
      const rendered = '<a href="/search?q=' + Validators.encodeForUrl(payload.value) + '">Run search</a>';
      pass = !rendered.includes('javascript:alert') && !rendered.includes('&admin=true');
      detail = rendered;
    } else if (payload.field === 'userId') {
      pass = wasRejected(() => Validators.validateIdentifier(payload.value));
      detail = pass ? 'rejected (fail closed)' : 'WAS ACCEPTED';
    } else if (payload.field === 'status') {
      pass = wasRejected(() => Validators.validateStatus(payload.value));
      detail = pass ? 'rejected (fail closed)' : 'WAS ACCEPTED';
    } else if (payload.field === 'age') {
      pass = wasRejected(() => Validators.validateInteger(payload.value, { min: 0, max: 130 }));
      detail = pass ? 'rejected (fail closed)' : 'WAS ACCEPTED';
    } else if (payload.field === 'joinDate') {
      pass = wasRejected(() => Validators.validateDate(payload.value));
      detail = pass ? 'rejected (fail closed)' : 'WAS ACCEPTED';
    }

    rows.push({ ...payload, pass, detail });
    if (!pass) {
      console.error('[bypass-suite] FAILED:', payload.field, payload.label, detail);
    }
  }

  renderResultsTable(rows);
  return rows;
}

function wasRejected(fn) {
  try {
    fn();
    return false; // did not throw -- bypass accepted, fail-closed violated
  } catch {
    return true;
  }
}

function renderResultsTable(rows) {
  const tbody = $('resultsBody');
  tbody.textContent = '';
  for (const row of rows) {
    const tr = document.createElement('tr');

    const fieldCell = document.createElement('td');
    fieldCell.textContent = row.field;
    tr.appendChild(fieldCell);

    const labelCell = document.createElement('td');
    labelCell.textContent = row.label;
    tr.appendChild(labelCell);

    const statusCell = document.createElement('td');
    statusCell.textContent = row.pass ? 'PASS' : 'FAIL';
    statusCell.className = row.pass ? 'pass' : 'fail';
    tr.appendChild(statusCell);

    tr.appendChild(document.createElement('td')).textContent = row.detail;

    tbody.appendChild(tr);
  }
}

// Wire up live inputs.
$('displayNameInput').addEventListener('input', renderDisplayName);
$('bioInput').addEventListener('input', renderBio);
$('searchInput').addEventListener('input', renderSearch);
$('userIdInput').addEventListener('input', renderStructured);
$('statusInput').addEventListener('input', renderStructured);
$('ageInput').addEventListener('input', renderStructured);
$('joinDateInput').addEventListener('input', renderStructured);
$('runSuiteBtn').addEventListener('click', runSuite);

renderDisplayName();
renderBio();
renderSearch();
renderStructured();
renderRegexFailureDemo();

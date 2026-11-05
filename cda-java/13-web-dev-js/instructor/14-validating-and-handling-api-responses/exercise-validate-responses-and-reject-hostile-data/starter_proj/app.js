// Order Feed — STARTER (no response validation)
//
// Four sources are loaded: the live mock API's /api/hostile route (returns
// a non-JSON body) and three local fixtures (valid, shape-invalid, and a
// hostile field carrying markup instead of a plain name).
//
// This starter never checks Content-Type before parsing, never validates
// shape/type before use, and renders straight into the DOM with innerHTML —
// so a "successful" 200 with the wrong body, a wrong-typed field, and a
// markup-carrying field all cause real problems instead of a clean,
// safe failure.
const SOURCES = [
  { label: 'live: /api/hostile (non-JSON)', url: '/api/hostile' },
  { label: 'fixture: valid',                url: 'fixtures/valid.json' },
  { label: 'fixture: shape-invalid',        url: 'fixtures/shape-invalid.json' },
  { label: 'fixture: hostile-field',        url: 'fixtures/hostile-field.json' }
];

const resultsEl = document.getElementById('results');

async function loadAndRender(source) {
  const row = document.createElement('div');
  row.className = 'row';
  resultsEl.append(row);

  try {
    // TODO: no Content-Type check before parsing — the /api/hostile source
    // returns text/html, so response.json() throws here instead of failing
    // with a clear, specific error.
    const response = await fetch(source.url);
    const order = await response.json();

    // TODO: no shape/type validation before use — "optimistic destructuring"
    // assumes order.customer.name and order.total are the right shape and
    // type. shape-invalid.json's total is a string, so .toFixed() throws.
    //
    // TODO: rendering with innerHTML means hostile-field.json's markup is
    // parsed and RUN, not shown as text — even though nothing here checked
    // whether that field was safe to render.
    row.innerHTML = `<span class="label">${source.label}</span><br>${order.customer.name} — $${order.total.toFixed(2)}`;
  } catch (err) {
    // Whatever broke, the failure surfaces as a raw, unhelpful error.
    row.innerHTML = `<span class="label">${source.label}</span><br>ERROR (unhandled): ${err.message}`;
  }
}

document.getElementById('run-btn').addEventListener('click', async () => {
  resultsEl.innerHTML = '';
  for (const source of SOURCES) {
    await loadAndRender(source);
  }
});

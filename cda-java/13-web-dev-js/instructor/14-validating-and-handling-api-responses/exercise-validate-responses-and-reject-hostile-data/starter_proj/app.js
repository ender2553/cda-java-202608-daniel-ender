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

async function fetchJson(url) {
  const response = await fetch(url);
  if(!response.ok){
    throw new Error(`Request Failed with status ${response.status}`);
  }
  const contentType = response.headers.get('content-type') || '';
  if (!isJsonContentType(contentType)){
    throw new Error(`Expected JSON, recieved: ${contentType}`);
  } 
  return response.json();
}



function renderLabel(row, label){
  const l = document.createElement('div');
  l.className = 'label';
  l.textContent = label;
  row.append(l);
}

function renderOrderSafely(row, order){
  const line = document.createElement('div');
  const nameNode = document.createTextNode(order.customer.name);
  const totalNode = document.createTextNode(` - $${order.total.toFixed(2)}`);
  line.append(nameNode, totalNode);
  row.append(line);
}

function renderFailure(row, message){
  const line = document.createElement('div');
  line.textContent = message;
  row.append(line);
}

async function loadAndRender(source) {
    const row = document.createElement('div');
    row.className = 'row';
    resultsEl.append(row);
    try {
      const order = await fetchJson(source.url);
      if (!validateOrder(order)) {
         renderFailure(row, 'Unable to Display this order');
         return;
      }

      renderOrderSafely(row, order);
    } catch (err){
      console.error(`[${source.label} load failed:` + err);
      renderFailure(row, 'UNabled to load this order. Please Try Again.');
    }
}
document.getElementById('run-btn').addEventListener('click', async () => {
  resultsEl.innerHTML = '';
  for (const source of SOURCES) {
    await loadAndRender(source);
  }
});

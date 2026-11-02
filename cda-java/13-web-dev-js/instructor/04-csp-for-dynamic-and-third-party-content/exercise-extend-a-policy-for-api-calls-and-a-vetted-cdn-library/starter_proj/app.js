// Metrics dashboard data layer: one same-origin call (already covered by
// connect-src 'self'), one partner-API call (needs a connect-src extension),
// and one call to an unvetted analytics collector that should NEVER be added
// to the policy — it stays blocked, on purpose.
const log = document.getElementById('log');
const lines = [];

function print(line) {
  lines.push(line);
  log.textContent = lines.join('\n');
}

async function loadSameOrigin() {
  try {
    const res = await fetch('/api/echo');
    const body = await res.json();
    print(`same-origin /api/echo -> ${res.status} ${JSON.stringify(body)}`);
  } catch (err) {
    print(`same-origin /api/echo -> blocked/failed: ${err.message}`);
  }
}

async function loadPartnerApi() {
  // Legitimate, vetted partner endpoint. TODO: needs a connect-src entry.
  try {
    const res = await fetch('https://partner-api.example.com/metrics');
    print(`partner API -> ${res.status}`);
  } catch (err) {
    print(`partner API -> blocked/failed: ${err.message}`);
  }
}

async function loadUnvetted() {
  // Deliberately NOT vetted. This call is expected to stay blocked forever —
  // do not add this origin to connect-src.
  try {
    const res = await fetch('https://sketchy-analytics.example/beacon');
    print(`unvetted origin -> ${res.status} (should not happen)`);
  } catch (err) {
    print(`unvetted origin -> blocked/failed (expected): ${err.message}`);
  }
}

loadSameOrigin();
loadPartnerApi();
loadUnvetted();

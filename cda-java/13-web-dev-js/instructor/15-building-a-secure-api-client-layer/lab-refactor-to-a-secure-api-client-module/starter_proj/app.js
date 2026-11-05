// Client Dashboard — STARTER (scattered inline fetch calls)
//
// Three widgets, three independent, duplicated request implementations.
// Each one re-attaches the Authorization header by hand (copy-pasted, easy
// to forget at a fourth call site), applies validation inconsistently (the
// profile widget checks nothing; the fixture-check widget blindly parses
// and innerHTML-renders, same bug as the L14 starter), has no timeout, no
// retry policy, and reports errors in a different shape each time. Nothing
// here checks the endpoint list against connect-src-policy.json at all.
//
// TODO (the whole point of this lab): replace all of this with ONE wrapper
// module that injects auth/base URL/headers once, enforces validation on
// every call, applies uniform timeout/retry/error handling, and is checked
// against connect-src-policy.json.

const AUTH_TOKEN = 'demo-token'; // TODO: duplicated in every widget below

// ---- Widget 1: Profile ----
document.getElementById('profile-btn').addEventListener('click', async () => {
  const el = document.getElementById('profile-result');
  try {
    // TODO: auth header hand-attached here, again.
    const response = await fetch('/api/user', {
      headers: { 'Authorization': `Bearer ${AUTH_TOKEN}` }
    });
    // TODO: no response.ok check, no shape validation.
    const data = await response.json();
    el.textContent = JSON.stringify(data);
  } catch (err) {
    el.textContent = `Error: ${err.message}`;
  }
});

// ---- Widget 2: Ping ----
document.getElementById('ping-btn').addEventListener('click', async () => {
  const el = document.getElementById('ping-result');
  try {
    // TODO: auth header hand-attached here too — third copy of the same
    // three lines, and it will only get more copies as widgets are added.
    const response = await fetch('/api/echo', {
      headers: { 'Authorization': `Bearer ${AUTH_TOKEN}` }
    });
    if (response.ok) {
      const data = await response.json();
      el.textContent = JSON.stringify(data);
    } else {
      // TODO: a THIRD, differently-shaped error report from a THIRD widget.
      el.textContent = `ping failed: status ${response.status}`;
    }
  } catch (err) {
    el.textContent = `ping error: ${err.message}`;
  }
});

// ---- Widget 3: Fixture check ----
const FIXTURES = ['fixtures/valid.json', 'fixtures/shape-invalid.json', 'fixtures/hostile-field.json'];

document.getElementById('fixtures-btn').addEventListener('click', async () => {
  const container = document.getElementById('fixtures-result');
  container.innerHTML = '';
  for (const url of FIXTURES) {
    const row = document.createElement('div');
    row.className = 'row';
    container.append(row);
    try {
      // TODO: no content-type check, no shape validation, and innerHTML
      // rendering — the hostile fixture's payload will execute here, same
      // as the unfixed L14 starter.
      const response = await fetch(url);
      const order = await response.json();
      row.innerHTML = `<span class="label">${url}</span><br>${order.customer.name} — $${order.total.toFixed(2)}`;
    } catch (err) {
      row.innerHTML = `<span class="label">${url}</span><br>ERROR (unhandled): ${err.message}`;
    }
  }
});

// TODO: nothing on this page ever reads connect-src-policy.json or checks
// it against the endpoints these widgets actually call.
document.getElementById('policy-result').textContent =
  'Not implemented — the wrapper you build should check its endpoint list against connect-src-policy.json and flag any mismatch.';

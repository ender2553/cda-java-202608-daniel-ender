// Profile Viewer — STARTER (incomplete auth handling)
//
// Scenario: a pure single-page app calling a third-party API. There is no
// first-party backend session for this client, so an HttpOnly cookie is not
// an available option — the token has to be attached explicitly by this
// code and stored somewhere this code can read it back from.
//
// The mock API's /api/user route returns 401 without an Authorization
// header, and 200 with any Authorization header present (it does not
// validate the token's value — the point of this exercise is attaching and
// storing the credential correctly, not building a real auth backend).

const statusEl = document.getElementById('status');

// TODO: localStorage is the "easiest to wire up" option, which is exactly
// why it's a trap here. Given the scenario above (no HttpOnly cookie
// available), is localStorage actually the least-exposed choice you have?
// Reconsider against an in-memory alternative before you fix loadProfile().
function storeToken(token) {
  localStorage.setItem('authToken', token);
}
function getToken() {
  return localStorage.getItem('authToken');
}
function clearToken() {
  localStorage.removeItem('authToken');
}

document.getElementById('login-btn').addEventListener('click', () => {
  // Simulates a successful login response issuing an opaque bearer token.
  const fakeToken = 'demo-token-' + Math.random().toString(36).slice(2);
  storeToken(fakeToken);
  statusEl.textContent = 'Logged in. Token stored.';
});

document.getElementById('expire-btn').addEventListener('click', () => {
  clearToken();
  statusEl.textContent = 'Token cleared (simulating an expired session).';
});

async function loadProfile(attempt = 1) {
  // TODO: this request never attaches the Authorization header at all, so
  // it always gets a 401 back from /api/user regardless of whether a token
  // is stored. Add the header using the stored token.
  const response = await fetch('/api/user');

  if (response.status === 401) {
    // TODO: this silently retries the exact same request with the exact
    // same (missing/expired) token instead of forcing the user through
    // re-authentication. (Capped at 3 attempts here only so the broken
    // starter doesn't hammer the server forever — the real bug is that it
    // retries AT ALL instead of failing safe into a re-authenticate state.)
    // A retry loop against an invalid token wastes requests and never
    // actually fixes anything — replace this with a clean "session
    // expired, please log in again" state instead.
    statusEl.textContent = `Got 401 — retrying (attempt ${attempt})...`;
    if (attempt >= 3) {
      statusEl.textContent = 'Still 401 after 3 silent retries. Stuck.';
      return;
    }
    return loadProfile(attempt + 1);
  }

  const data = await response.json();
  statusEl.textContent = `Profile: ${JSON.stringify(data)}`;
}

document.getElementById('load-btn').addEventListener('click', () => {
  loadProfile();
});

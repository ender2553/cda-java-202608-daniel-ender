"use strict";

// ---------------------------------------------------------------------------
// SECURITY FRAMING: This page runs entirely in your browser against local,
// seeded strings. The only "attack" is a benign marker (a visible on-page
// flag plus a console message). Nothing here contacts the network.
// ---------------------------------------------------------------------------

// A well-formed JSON response body.
const BENIGN_RESPONSE = '{"id":42,"name":"Ada Lovelace","active":true}';

// NOT valid JSON, but valid JavaScript: a call expression. This is the shape
// of the historical eval-parsing vulnerability -- the parser below wraps
// whatever it is given in parens and evals it, so any valid JS expression
// runs, not just JSON data.
const HOSTILE_RESPONSE = "window.__evalExecuted()";

window.__evalExecuted = function () {
  const el = document.getElementById("exec-marker");
  el.textContent = "FIRED -- arbitrary code ran during \"parsing\"";
  el.classList.remove("clean");
  el.classList.add("bad");
  console.warn("[demo] eval-based parser executed attacker-controlled code.");
  return { poisoned: true };
};

// ==== TODO (Step 1/2): replace this eval-based "parser" ====================
// This is the historical shortcut: JSON syntax is a subset of JS syntax, so
// wrapping the text in parens and calling eval() "works" for well-formed
// JSON -- but eval() runs ANY valid JavaScript the string contains, not just
// JSON data. Replace this with JSON.parse, wrapped in a try/catch that
// returns a clear { ok, data } / { ok, error } result instead of throwing
// out of the function.
function parseResponse(text) {
  const data = eval("(" + text + ")"); // VULNERABLE: executes arbitrary JS
  return { ok: true, data };
}
// =============================================================================

function renderResult(result) {
  const el = document.getElementById("result");
  el.textContent = JSON.stringify(result);
}

document.getElementById("btn-benign").addEventListener("click", function () {
  renderResult(parseResponse(BENIGN_RESPONSE));
});
document.getElementById("btn-hostile").addEventListener("click", function () {
  renderResult(parseResponse(HOSTILE_RESPONSE));
});

// --- Session logging -------------------------------------------------------

const currentSession = {
  userId: 42,
  displayName: "Ada",
  authToken: "eyJhbGciOiJIUzI1NiJ9.session-secret-do-not-leak",
};

// ==== TODO (Step 4/5): add a JSON.stringify replacer ========================
// This logging function currently serializes the session object exactly as
// it sits in memory, which means authToken -- a secret -- goes out with it.
// Add a replacer function as JSON.stringify's second argument that omits the
// authToken field (return undefined for that key) so it never appears in
// logged output, even though the in-memory object still has it.
function logSession(session) {
  const serialized = JSON.stringify(session); // VULNERABLE: no replacer
  document.getElementById("logged-output").textContent = serialized;
  console.log("[log]", serialized);
  return serialized;
}
// =============================================================================

document.getElementById("btn-log").addEventListener("click", function () {
  logSession(currentSession);
});

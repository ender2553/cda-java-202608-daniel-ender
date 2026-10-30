"use strict";

// ---------------------------------------------------------------------------
// SECURITY FRAMING: This page runs entirely in your browser against local,
// seeded data. The only "attack" is a benign marker (a visible on-page flag
// plus a console message). Nothing here contacts the network or a real site.
// ---------------------------------------------------------------------------

// --- Seeded untrusted payloads (imagine each arrived as a server response) ---

// Well-formed and harmless.
const BENIGN_PAYLOAD =
  '{"user":"Ada","role":"engineer","bio":"Builds reliable ledger software."}';

// Hostile: carries a __proto__ prototype-pollution attempt. The parse is
// innocent (JSON.parse stores "__proto__" as an ordinary own key); the naive
// recursive merge below is what follows that key onto the prototype.
const HOSTILE_PAYLOAD =
  '{"__proto__":{"polluted":true},"user":"Mallory","role":"admin",' +
  '"bio":"just an admin"}';

// Valid shape, no forbidden keys — but the bio VALUE is an XSS payload.
// Shape/key checks would pass this; only safe RENDERING stops it.
const TRICKY_PAYLOAD =
  '{"user":"Eve","role":"engineer",' +
  '"bio":"<img src=x onerror=window.__xssFired()>"}';

// Benign marker fired by the XSS payload's onerror handler.
window.__xssFired = function () {
  const el = document.getElementById("xss-marker");
  el.textContent = "XSS FIRED (onerror executed at the write)";
  el.classList.remove("clean");
  el.classList.add("bad");
  console.warn("[demo] XSS marker fired — an untrusted value executed at the DOM write.");
};

// STEP 1 — the parser. Confirm this is JSON.parse (NOT eval / new Function).
function parsePayload(str) {
  // NOTE: this is the safe parser, but it is not wrapped in try/catch yet.
  // Step 1 of the exercise asks you to add a try/catch so malformed input is
  // rejected rather than throwing uncaught.
  return JSON.parse(str);
}

// VULNERABLE object handling: a naive recursive merge that follows keys onto
// the prototype. THIS is the prototype-pollution sink (the parse is innocent).
function unsafeMerge(target, source) {
  for (const key of Object.keys(source)) {
    if (typeof source[key] === "object" && source[key] !== null) {
      unsafeMerge((target[key] ??= {}), source[key]); // walks INTO target.__proto__
    } else {
      target[key] = source[key];
    }
  }
}

function handlePayload(str) {
  const parsed = parsePayload(str);

  // ==== TODO (Step 3): HARDEN THE OBJECT HANDLING ====================
  // Replace the unsafeMerge below with defensive handling that:
  //   1. validates the expected shape { user, role, bio } and REJECTS mismatches
  //   2. rejects (or strips) the keys __proto__, constructor, prototype
  //   3. builds the untrusted map with Object.create(null)
  //   4. reads fields with Object.hasOwn(...) rather than `key in ...`
  const state = {};
  unsafeMerge(state, parsed); // <-- the vulnerable sink; remove it when hardening
  // ===================================================================

  renderProfile(state);
  updatePollutionProbe();
}

// VULNERABLE render: writes an untrusted value through a markup sink.
function renderProfile(data) {
  const out = document.getElementById("profile");
  // TODO (Step 5 / render debt): data.bio is attacker-controlled text. Writing
  // it through innerHTML hands it to the HTML parser, so an embedded onerror
  // payload executes here. It must be written with textContent (or safe node
  // construction), not innerHTML.
  out.innerHTML =
    "<strong>" + data.user + "</strong> (" + data.role + ")<br>" + data.bio;
}

// Probe a brand-new empty object to reveal whether the prototype is polluted.
function updatePollutionProbe() {
  const el = document.getElementById("probe");
  const polluted = {}.polluted; // reads through the prototype chain
  if (polluted === undefined) {
    el.textContent = "CLEAN — ({}).polluted is undefined";
    el.classList.remove("bad");
    el.classList.add("clean");
  } else {
    el.textContent = "POLLUTED — ({}).polluted is " + polluted;
    el.classList.remove("clean");
    el.classList.add("bad");
    console.warn("[demo] Prototype polluted — every object now reports polluted =", polluted);
  }
}

document.getElementById("btn-benign").addEventListener("click", function () {
  handlePayload(BENIGN_PAYLOAD);
});
document.getElementById("btn-hostile").addEventListener("click", function () {
  handlePayload(HOSTILE_PAYLOAD);
});
document.getElementById("btn-tricky").addEventListener("click", function () {
  handlePayload(TRICKY_PAYLOAD);
});

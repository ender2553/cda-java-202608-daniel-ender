// ============================================================================
// Support Ticket Dashboard -- VULNERABLE STARTER
// ----------------------------------------------------------------------------
// This page works: it renders a ticket, formats a note, follows a source link,
// computes a priority score, and applies saved settings. It is ALSO riddled
// with course-spanning flaws. Open index.html and watch the Attack Monitor:
// several payloads fire on load, and two more are "armed" (click to fire).
//
// Your job (see README.md): secure every path end-to-end WITHOUT breaking the
// features, then prove each payload is inert. A vetted sanitizer (DOMPurify)
// is already bundled as purify.min.js -- wire it into the rich-text note.
// ============================================================================

// --- Benign, local exploit marker ------------------------------------------
// Payloads call this instead of doing anything harmful. It flips a monitor row
// to "FIRED" and logs to the console. No network, no real damage.
window.reportXss = function (label) {
  const row = document.querySelector('[data-vector="' + label + '"] .status');
  if (row) {
    row.textContent = "FIRED";
    row.className = "status fired";
  }
  console.error("[XSS] payload executed:", label);
};

function $(id) {
  return document.getElementById(id);
}

// ===========================================================================
// FLAW 1 -- HTML-string sink for the message (innerHTML)
// The message is user text, but it is written as markup, so the <img onerror>
// runs.
// ===========================================================================
function renderMessage(ticket) {
  $("message").innerHTML = ticket.message; // VULNERABLE
}

// ===========================================================================
// FLAW 2 -- rich-text note via innerHTML, no sanitizer
// DOMPurify is bundled but unused here, so the note's payloads run.
// ===========================================================================
function renderNote(ticket) {
  $("note").innerHTML = ticket.note; // VULNERABLE
}

// ===========================================================================
// FLAW 3 -- source link built from a raw, unvalidated URL
// A javascript: URL becomes an executable href. (Click the link to fire it.)
// ===========================================================================
function renderSourceLink(ticket) {
  const link = $("sourceLink");
  link.setAttribute("href", ticket.sourceUrl); // VULNERABLE
  link.textContent = "Open source";
  // Reflect the danger in the monitor without needing a click:
  if (link.protocol === "javascript:") {
    const cell = document.querySelector(
      '[data-vector="source-link-href"] .status'
    );
    cell.textContent = "ARMED (click)";
    cell.className = "status armed";
  }
}

// ===========================================================================
// FLAW 4 -- dynamic execution via eval() for the priority weighting
// The "flexible formula" is attacker-controllable, so eval runs their code.
// ===========================================================================
function computePriority(ticket, severity) {
  const weight = eval(ticket.weightingExpr); // VULNERABLE (runs arbitrary code)
  return severity * weight;
}

// ===========================================================================
// FLAW 5 -- unvalidated JSON + a recursive merge = prototype pollution
// JSON.parse trusts the shape, and the naive deep-merge walks a __proto__ key
// straight onto Object.prototype.
// ===========================================================================
function naiveMerge(target, source) {
  for (const key in source) {
    if (source[key] && typeof source[key] === "object") {
      if (!target[key]) target[key] = {};
      naiveMerge(target[key], source[key]); // recurse -- follows __proto__
    } else {
      target[key] = source[key];
    }
  }
  return target;
}

function loadSettings(jsonText) {
  const DEFAULTS = { theme: "light", pageSize: 10 };
  const parsed = JSON.parse(jsonText); // VULNERABLE (no shape/key checks)
  const settings = naiveMerge({ ...DEFAULTS }, parsed);

  // Detect the pollution so you can SEE it fire: a brand-new empty object now
  // carries an "isAdmin" property it never should have.
  if ({}.isAdmin === true) {
    reportXss("proto-pollution");
  }
  return settings;
}

// ===========================================================================
// Wire everything up on load.
// ===========================================================================
function main() {
  const t = INCOMING_TICKET;

  $("customer").textContent = t.customer; // (this one is already safe)
  renderMessage(t);
  renderNote(t);
  renderSourceLink(t);

  const score = computePriority(t, SEVERITY);
  $("priority").textContent = String(score);

  const settings = loadSettings(UNTRUSTED_SETTINGS_JSON);
  $("theme").textContent = settings.theme;
  $("pageSize").textContent = String(settings.pageSize);
  $("status").textContent = t.status; // (no validation yet)
}

document.addEventListener("DOMContentLoaded", main);

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
  $("message").textContent = ticket.message; // FIXED
}

// ===========================================================================
// FLAW 2 -- rich-text note via innerHTML, no sanitizer
// DOMPurify is bundled but unused here, so the note's payloads run.
// ===========================================================================
function renderNote(ticket) {
  $("note").innerHTML = DOMPurify.sanitize(ticket.note, {
    ALLOWED_TAGS: ["b", "a"],
    ALLOWED_ATTR: ["href", "title"]
  });                                       //FIXED
}

// ===========================================================================
// FLAW 3 -- source link built from a raw, unvalidated URL
// A javascript: URL becomes an executable href. (Click the link to fire it.)
// ===========================================================================
function renderSourceLink(ticket) {
  const link = $("sourceLink");
  link.textContent = "Open source";

  try {
    const url = new URL(ticket.sourceUrl, window.location.href);

    if (url.protocol === "http:" || url.protocol === "https:") {
      link.href = url.href;
    } else {
      link.href = "#";
    }
  } catch {
    link.href = "#";                          //FIXED
  }
}

// ===========================================================================
// FLAW 4 -- dynamic execution via eval() for the priority weighting
// The "flexible formula" is attacker-controllable, so eval runs their code.
// ===========================================================================
function computePriority(ticket, severity) {
  const WEIGHT_TABLE = {
    low: 1,
    medium: 2,
    high: 3
  };

  const weight = WEIGHT_TABLE[ticket.weighting];

  if (typeof weight !== "number") {
    throw new Error("Invalid priority weighting");
  }                                       
                                            //FIXED
  return severity * weight;
}

// ===========================================================================
// FLAW 5 -- unvalidated JSON + a recursive merge = prototype pollution
// JSON.parse trusts the shape, and the naive deep-merge walks a __proto__ key
// straight onto Object.prototype.
// ===========================================================================
function containsDangerousKeys(object) {
  const dangerousKeys = ["__proto__", "constructor", "prototype"];

  if (object === null || typeof object !== "object") {
    return false;
  }

  for (const key of Object.keys(object)) {
    if (dangerousKeys.includes(key)) {
      return true;
    }

    if (containsDangerousKeys(object[key])) {
      return true;
    }
  }

  return false;
}

function loadSettings(jsonText) {
  const DEFAULTS = {
    theme: "light",
    pageSize: 10
  };

  let parsed;

  try {
    parsed = JSON.parse(jsonText);
  } catch {
    return DEFAULTS;
  }

  // Require a plain JSON object.
  if (
    parsed === null ||
    typeof parsed !== "object" ||
    Array.isArray(parsed)
  ) {
    return DEFAULTS;
  }

  // Reject dangerous property names.
  // Safe, explicitly validated settings can still be applied.
  const hasDangerousKeys = containsDangerousKeys(parsed);

  const validTheme = ["light", "dark"].includes(parsed.theme);
  const validPageSize = [10, 25, 50].includes(parsed.pageSize);

  const settings = {
    theme: validTheme ? parsed.theme : DEFAULTS.theme,
    pageSize: validPageSize ? parsed.pageSize : DEFAULTS.pageSize
  };

  if (hasDangerousKeys) {
    console.warn("Dangerous settings properties rejected.");
  }

  return settings;
}                                         //FIXED

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
  const ALLOWED_STATUSES = ["open", "pending", "closed"];

const status = ALLOWED_STATUSES.includes(t.status)
  ? t.status
  : "open";

$("status").textContent = status;
}

document.addEventListener("DOMContentLoaded", main);

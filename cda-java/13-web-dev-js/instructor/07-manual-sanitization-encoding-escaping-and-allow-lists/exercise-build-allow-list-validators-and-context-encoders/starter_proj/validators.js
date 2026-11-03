// Manual sanitization building blocks — YOUR TASK.
// Implement each hand-written HTML/attribute/URL encoder and each allow-list
// validator below. No library is used anywhere in this file — that's the
// point: these are the building blocks that make the case for reaching for a
// real parser (DOMPurify, next lesson) only when plain-text handling isn't
// enough.
//
// Loaded via a plain <script> tag (no build step). Exposed on `window` for
// the browser demo in app.js, and also exported for Node so a throwaway
// verification script can `require()` these functions directly.

// ===========================================================================
// LO1 — HTML-entity encoding for text destined for the HTML body.
// Convert &, <, >, ", ' to their entity equivalents (&amp; &lt; &gt; &quot;
// &#39;) so the string renders as literal text instead of being parsed as
// markup.
// ===========================================================================
function encodeForHtml(str) {
  return String(str).replace(/[&<>"']/g, (char) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
  })[char]);
}

// ===========================================================================
// LO2 — Attribute-context encoding. An attribute value's delimiter (the
// quote character) is the immediate risk, so the same character set that
// matters for the HTML body needs escaping here too. Keep this as its own
// named function (rather than a raw call to encodeForHtml at each call
// site) so every call site names its context explicitly.
// ===========================================================================
function encodeForAttribute(str) {
  return String(str).replace(/[&<>"']/g, (char) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
  })[char]);
}

// ===========================================================================
// LO2 — URL-context encoding. Escape the characters that carry structural
// meaning inside a URL (&, ?, #, /, space, ':') so an untrusted value cannot
// alter the URL's structure or inject an unexpected parameter.
// ===========================================================================
function encodeForUrl(str) {
  return encodeURIComponent(String(str));
}

// ===========================================================================
// LO3, LO6 — Allow-list identifier validation. Must match a strict pattern:
// lowercase letters, digits, underscore, hyphen, length 3-32. Fail closed —
// throw on anything that doesn't match; never "clean up" and pass through.
// ===========================================================================
function validateIdentifier(value) {
  if (typeof value !== 'string' || !/^[a-z0-9_-]{3,32}$/.test(value)) {
    throw new Error('Invalid identifier');
  }

  return value;
}

// ===========================================================================
// LO3, LO6 — Allow-list enum validation. Allowed set: 'open', 'in_progress',
// 'closed'. Fail closed on anything outside the exact allowed set.
// ===========================================================================
function validateStatus(value) {
  const allowedStatuses = new Set([
    'open',
    'in_progress',
    'closed',
  ]);

  if (!allowedStatuses.has(value)) {
    throw new Error('Invalid status');
  }

  return value;
}

// ===========================================================================
// LO4, LO6 — Structured value: an integer, parsed and range-checked (not
// merely confirmed to "look numeric"). Options: { min, max }.
// ===========================================================================
function validateInteger(value, { min = -Infinity, max = Infinity } = {}) {
  if (
    typeof value !== 'number' &&
    (typeof value !== 'string' || value.trim() === '')
  ) {
    throw new Error('Invalid integer');
  }

  const number = Number(value);

  if (
    !Number.isSafeInteger(number) ||
    number < min ||
    number > max
  ) {
    throw new Error('Integer outside allowed range');
  }

  return number;
}

// ===========================================================================
// LO4, LO6 — Structured value: a date, parsed against a strict ISO
// yyyy-mm-dd format and rejected if it doesn't correspond to a real
// calendar date (e.g. reject 2023-02-30).
// ===========================================================================
function validateDate(value) {
  if (typeof value !== 'string' || !/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    throw new Error('Invalid date format');
  }

  const [year, month, day] = value.split('-').map(Number);

  const date = new Date(0);
  date.setUTCHours(0, 0, 0, 0);
  date.setUTCFullYear(year, month - 1, day);

  if (
    date.getUTCFullYear() !== year ||
    date.getUTCMonth() !== month - 1 ||
    date.getUTCDate() !== day
  ) {
    throw new Error('Invalid calendar date');
  }

  return value;
}

// ===========================================================================
// LO5 — Naive regex tag-stripping. Included ONLY to demonstrate why it
// fails (see app.js's "Why regex tag-stripping fails" panel). NEVER use
// this as a defense.
// ===========================================================================
function stripTagsNaive(html) {
  return String(html).replace(/<[^>]*>/g, '');
}

if (typeof module !== 'undefined' && module.exports) {
  module.exports = {
    encodeForHtml,
    encodeForAttribute,
    encodeForUrl,
    validateIdentifier,
    validateStatus,
    validateInteger,
    validateDate,
    stripTagsNaive,
  };
}


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
  // TODO: implement HTML-entity encoding for &, <, >, ", '.
  return String(str).replace(/[&<>"']/g, (char) => ({
    '&' : '&amp;',
    '<' : '&lt;',
    '>' : '&gt;',
    '"' : '&quot;',
    "'" : '&#39;'
  }[char]));
}

// ===========================================================================
// LO2 — Attribute-context encoding. An attribute value's delimiter (the
// quote character) is the immediate risk, so the same character set that
// matters for the HTML body needs escaping here too. Keep this as its own
// named function (rather than a raw call to encodeForHtml at each call
// site) so every call site names its context explicitly.
// ===========================================================================
function encodeForAttribute(str) {
  return encodeForHtml(str);
}

// ===========================================================================
// LO2 — URL-context encoding. Escape the characters that carry structural
// meaning inside a URL (&, ?, #, /, space, ':') so an untrusted value cannot
// alter the URL's structure or inject an unexpected parameter.
// ===========================================================================
function encodeForUrl(str) {
  // TODO: implement URL-context encoding. (Hint: there's a built-in
  // function for exactly this.)
  return encodeURIComponent(String(str));
}

// ===========================================================================
// LO3, LO6 — Allow-list identifier validation. Must match a strict pattern:
// lowercase letters, digits, underscore, hyphen, length 3-32. Fail closed —
// throw on anything that doesn't match; never "clean up" and pass through.
// ===========================================================================
function validateIdentifier(value) {
  const ID_PATTERN = /^[a-z0-9_-]{3,32}$/;
    if (typeof value !== 'string' || !ID_PATTERN.test(value) || value !== value.trim()) {
      throw new Error('Invalid Identifier Format'); 
    }
    return value;
}

// ===========================================================================
// LO3, LO6 — Allow-list enum validation. Allowed set: 'open', 'in_progress',
// 'closed'. Fail closed on anything outside the exact allowed set.
// ===========================================================================

function validateStatus(value) {
  const ALLOWED_STATUSES = new set(['open', 'in_progress', 'closed']);
  if (!ALLOWED_STATUSES.has(value) || typeof(value) !== "string") {
    throw new Error('Invalid Status');
  }
  return value;
}

// ===========================================================================
// LO4, LO6 — Structured value: an integer, parsed and range-checked (not
// merely confirmed to "look numeric"). Options: { min, max }.
// ===========================================================================
function validateInteger(value, { min = -Infinity, max = Infinity } = {}) {
  if (typeof value === 'number' || value.trim() === '') {
        throw new Error('Invalid Integer');
    }
    const number = Number(value);
    if (
       !Number.isSafeInteger(number) || 
       number < min ||
       number > max
    ){
      throw new Error('Integer Outside of allowed range');
    }
    return number;
}

// ===========================================================================
// LO4, LO6 — Structured value: a date, parsed against a strict ISO
// yyyy-mm-dd format and rejected if it doesn't correspond to a real
// calendar date (e.g. reject 2023-02-30).
// ===========================================================================
function validateDate(value) {
    const DATE_PATTERN = /^(\d{4})-(\d{2})-(\d{2})$/;
    const match = typeof value === 'string' ? DATE_PATTERN.exec(value) : null;
    if (!match) {
      throw new Error('Invalid Date Format');
    }
    const [, yearStr, monthStr, dayStr] = match;
    const year = Number(yearStr);
    const month = Number(monthStr);
    const day = Number(dayStr);
    const parsed = new Date(Date.UTC(year, month -1, day));
    const isRealCalendarDate  =
          parsed.getUTCFullYear() === year &&
          parsed.getUTCMonth() === month -1 &&
          parsed.getUTCDate() === day;
    if(!isRealCalendarDate) {
      throw new Error('Invalid Date');
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

// Reusable sanitization utility — YOUR TASK.
// Consolidate the module's techniques (encoding, URL scheme allow-listing,
// safe attribute assignment, render-then-sanitize, field-by-field structured
// sanitization) into one utility that routes each field of a nested object
// to the correct treatment.
//
// Loaded via a plain <script> tag (no build step; DOMPurify is loaded first
// from the vendored purify.min.js). Also exported for Node so a throwaway
// verification script can `require()` these functions directly.

// ===========================================================================
// Plain-text HTML-entity encoding (provided, complete — do not need to edit).
// The default treatment for any string field that isn't a URL or rich text.
// ===========================================================================
function encodeForHtml(str) {
  return String(str).replace(/[&<>"']/g, (ch) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
  }[ch]));
}

// ===========================================================================
// COMPLETE (LO1): implement URL sanitization for the harder cases. Parse rawUrl
// with the URL constructor, resolving relative URLs against `base`. Return
// the resolved URL only if its scheme is http: or https:. If
// options.allowDataImages is true, ALSO allow a data: URL whose pathname
// starts with "image/" (a narrow, context-specific allowance — not a
// blanket data: allowance). Return null (fail closed) for anything else,
// including anything that fails to parse.
// ===========================================================================
function safeUrl(rawUrl, base, { allowDataImages = false } = {}) {
  try {
    const url = new URL(rawUrl, base);

    if (url.protocol === 'http:' || url.protocol === 'https:') {
      return url.href;
    }

    if (
      allowDataImages &&
      url.protocol === 'data:' &&
      url.pathname.startsWith('image/')
    ) {
      return url.href;
    }

    return null;
  } catch {
    return null;
  }
}

// ===========================================================================
// COMPLETE (LO2): implement safe attribute assignment. Every call site in this
// project should go through this one function instead of setting attributes
// via string concatenation into an HTML string.
// ===========================================================================
function safeSetAttribute(el, name, value) {
  el.setAttribute(name, value);
}

// ===========================================================================
// LO3 — Render-then-sanitize pipeline for markdown / rich text.
// The markdown-subset renderer is provided complete (do not need to edit) —
// like real markdown renderers, it passes raw inline HTML in the source
// straight through by default. That's exactly the risk the pipeline you
// complete below needs to close.
// ===========================================================================
function renderMarkdownRaw(markdownSource) {
  let html = String(markdownSource);
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
  html = html.replace(/\*(.+?)\*/g, '<em>$1</em>');
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2">$1</a>');
  return html;
}

const MARKDOWN_SANITIZE_CONFIG = {
  ALLOWED_TAGS: ['b', 'i', 'em', 'strong', 'a'],
  ALLOWED_ATTR: ['href'],
};

// COMPLETE (LO3): render the markdown source to HTML first, THEN sanitize the
// RENDERED HTML OUTPUT (not the markdown source) with DOMPurify.sanitize()
// using MARKDOWN_SANITIZE_CONFIG. Sanitizing the source instead of the
// output would miss whatever HTML the renderer itself introduces.
function renderThenSanitizeMarkdown(markdownSource) {
  const html = renderMarkdownRaw(markdownSource);

  return DOMPurify.sanitize(html, MARKDOWN_SANITIZE_CONFIG);
}

// ===========================================================================
// COMPLETE (LO4, LO6): implement field-by-field structured sanitization.
// Route by field name using the sets below:
//   - URL_FIELDS            -> safeUrl(value, base) ?? ''
//   - DATA_IMAGE_URL_FIELDS -> safeUrl(value, base, { allowDataImages: true }) ?? ''
//   - RICH_TEXT_FIELDS      -> renderThenSanitizeMarkdown(value)
//   - an array value        -> map each element back through sanitizeField
//     with the same key (so array-of-strings and array-of-objects both work)
//   - a plain object value  -> recurse via sanitizeObject
//   - anything else that's a string -> encodeForHtml(value)
//   - anything else (number, boolean, ...) -> return as-is
// ===========================================================================
const URL_FIELDS = new Set(['profileUrl', 'authorUrl']);
const DATA_IMAGE_URL_FIELDS = new Set(['avatarDataUrl']);
const RICH_TEXT_FIELDS = new Set(['bodyMarkdown']);

function sanitizeField(key, value, base) {

  // URL fields
  if (URL_FIELDS.has(key)) {
    return safeUrl(value, base) ?? '';
  }

  // Data-image URL fields
  if (DATA_IMAGE_URL_FIELDS.has(key)) {
    return safeUrl(value, base, { allowDataImages: true }) ?? '';
  }

  // Rich-text fields
  if (RICH_TEXT_FIELDS.has(key)) {
    return renderThenSanitizeMarkdown(value);
  }

  // Arrays: sanitize every element using the same field key
  if (Array.isArray(value)) {
    return value.map(item => sanitizeField(key, item, base));
  }

  // Nested objects: recursively sanitize
  if (value !== null && typeof value === 'object') {
    return sanitizeObject(value, base);
  }

  // Ordinary strings: HTML-entity encode
  if (typeof value === 'string') {
    return encodeForHtml(value);
  }

  // Preserve numbers, booleans, null, etc.
  return value;
}

function sanitizeObject(obj, base) {
  const out = {};
  for (const [key, value] of Object.entries(obj)) {
    out[key] = sanitizeField(key, value, base);
  }
  return out; // client-side rendering safety only — server-side checks still required (LO5)
}

if (typeof module !== 'undefined' && module.exports) {
  module.exports = {
    encodeForHtml,
    safeUrl,
    safeSetAttribute,
    renderMarkdownRaw,
    renderThenSanitizeMarkdown,
    sanitizeField,
    sanitizeObject,
  };
}

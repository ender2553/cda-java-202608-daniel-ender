// Dynamic list renderer — VULNERABLE STARTER.
//
// This renderer works: it builds a directory list from DIRECTORY_RECORDS.
// But it builds each record by string-concatenating an HTML string and
// assigning it via innerHTML, and it drops every URL straight into an href
// with no scheme check at all.
//
// Your job (see README.md): rebuild this using createElement + setAttribute
// + textContent only — no innerHTML, no string concatenation into markup —
// and add scheme allow-listing so only http:/https: URLs become links.
//
// Loaded via a plain <script> tag (no build step). Exposed as globals for the
// browser (app.js calls renderList), and safeUrlOrNull is also exported for
// Node so a throwaway verification script can `require()` it directly. The
// export guard is a no-op in the browser (`module` is undefined there).

// ===========================================================================
// TODO (LO3): implement scheme allow-listing here. Parse rawUrl with the URL
// constructor (never a string-prefix check), resolve against `base` for
// relative URLs, and only return the URL when its scheme is http: or
// https:. Return null (fail closed) for anything else, including anything
// that fails to parse.
// ===========================================================================
function safeUrlOrNull(rawUrl, base) {
  return rawUrl; // VULNERABLE: no scheme check, no parsing, no fail-closed default
}

// ===========================================================================
// TODO (LO1, LO2): rebuild this function using createElement + setAttribute
// + textContent only. Requirements:
//   - record.name and record.bio must render as literal text, never parsed
//     as markup, no matter what characters they contain.
//   - Only call setAttribute('href', ...) with the result of safeUrlOrNull.
//     If safeUrlOrNull returns null, render the record with NO link (e.g. a
//     <span> holding the name as text) instead of falling back to an unsafe
//     href.
// ===========================================================================
function createRecordListItem(record, base) {
  const li = document.createElement('li');
  li.className = 'record';

  // VULNERABLE: string concatenation into an HTML string, assigned via
  // innerHTML. record.name renders as markup (a payload executes); the URL
  // is dropped into href with no scheme check (a javascript: URL becomes a
  // clickable, executable link).
  li.innerHTML =
    '<a href="' + safeUrlOrNull(record.profileUrl, base) + '">' + record.name + '</a>' +
    '<p class="bio">' + record.bio + '</p>';

  return li;
}

// ===========================================================================
// Render a collection: iterate and build one element per item. (This part
// is already correct — no array.map().join('') into an HTML string. Do not
// need to edit.)
// ===========================================================================
function renderList(records, container, base) {
  const list = document.createElement('ul');
  list.className = 'record-list';
  for (const record of records) {
    list.appendChild(createRecordListItem(record, base));
  }
  container.replaceChildren(list);
}

if (typeof module !== 'undefined' && module.exports) {
  module.exports = { safeUrlOrNull, createRecordListItem, renderList };
}

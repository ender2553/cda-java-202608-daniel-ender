// Dynamic list renderer — SAFE VERSION.
//
// This renderer builds a directory list from DIRECTORY_RECORDS.
// It uses createElement, setAttribute, and textContent instead of
// string concatenation and innerHTML.
//
// URLs are validated so only http: and https: schemes become links.
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
  try {
    const parsedUrl = new URL(rawUrl, base);

    if (parsedUrl.protocol === 'http:' ||
        parsedUrl.protocol === 'https:') {
      return parsedUrl.href;
    }

    return null;
  } catch {
    return null;
  }
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

  const safeUrl = safeUrlOrNull(record.profileUrl, base);

  let nameElement;

  if (safeUrl !== null) {
    nameElement = document.createElement('a');
    nameElement.setAttribute('href', safeUrl);
  } else {
    nameElement = document.createElement('span');
  }

  nameElement.textContent = record.name;

  const bioElement = document.createElement('p');
  bioElement.className = 'bio';
  bioElement.textContent = record.bio;

  li.appendChild(nameElement);
  li.appendChild(bioElement);

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


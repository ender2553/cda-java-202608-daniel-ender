// Wires sanitize-utils.js to the page. Sanitizes the whole nested submission
// in one call, then renders selected fields using safe DOM construction
// (createElement / safeSetAttribute / textContent) — LO2's attribute-safe
// assignment applied consistently, never string concatenation into markup.
//
// Runs by opening index.html in a browser — no setup required.

function $(id) {
  return document.getElementById(id);
}

const BASE = window.location.href;

function renderSanitized(sanitized) {
  const host = $('output');
  host.textContent = '';

  // Display name — plain text field, rendered via textContent.
  const nameEl = document.createElement('h2');
  nameEl.textContent = sanitized.displayName;
  host.appendChild(nameEl);

  // Profile URL — LO2: safe attribute assignment via safeSetAttribute
  // (wraps setAttribute), never string-concatenated into an <a ...> string.
  const profileLink = document.createElement('a');
  if (sanitized.profileUrl) {
    safeSetAttribute(profileLink, 'href', sanitized.profileUrl);
    profileLink.textContent = 'View profile';
  } else {
    profileLink.textContent = '(no valid profile link)';
  }
  host.appendChild(profileLink);

  // Rich-text body — already sanitized HTML (render-then-sanitize output),
  // so innerHTML is the correct, narrow case where it's acceptable (LO6 of
  // lesson 9): the value is the output of a sanitizer, not raw input.
  const bodyEl = document.createElement('div');
  bodyEl.className = 'body';
  bodyEl.innerHTML = sanitized.bodyMarkdown;
  host.appendChild(bodyEl);

  // Tags — collection rendering with textContent per item.
  const tagsEl = document.createElement('ul');
  tagsEl.className = 'tags';
  for (const tag of sanitized.tags) {
    const li = document.createElement('li');
    li.textContent = tag;
    tagsEl.appendChild(li);
  }
  host.appendChild(tagsEl);

  // Nested meta — author link + notes text.
  const metaEl = document.createElement('div');
  metaEl.className = 'meta';

  const authorLink = document.createElement('a');
  if (sanitized.meta.authorUrl) {
    safeSetAttribute(authorLink, 'href', sanitized.meta.authorUrl);
    authorLink.textContent = 'Author profile';
  } else {
    authorLink.textContent = '(no valid author link)';
  }
  metaEl.appendChild(authorLink);

  const notesEl = document.createElement('p');
  notesEl.textContent = sanitized.meta.notes;
  metaEl.appendChild(notesEl);

  if (sanitized.meta.avatarDataUrl) {
    const avatar = document.createElement('img');
    safeSetAttribute(avatar, 'src', sanitized.meta.avatarDataUrl);
    safeSetAttribute(avatar, 'alt', 'Author avatar');
    avatar.width = 24;
    avatar.height = 24;
    metaEl.appendChild(avatar);
  }

  host.appendChild(metaEl);
}

function main() {
  const sanitized = sanitizeObject(UNTRUSTED_SUBMISSION, BASE);

  $('rawJson').textContent = JSON.stringify(UNTRUSTED_SUBMISSION, null, 2);
  $('sanitizedJson').textContent = JSON.stringify(sanitized, null, 2);

  renderSanitized(sanitized);
}

main();

// Rich-Text Note Viewer — YOUR TASK.
//
// Each seeded untrusted note is sanitized and rendered. Right now the
// sanitize call uses DOMPurify's broad defaults (no ALLOWED_TAGS/ALLOWED_ATTR
// scoping) and there is no hook forcing rel="noopener noreferrer" on links.
// Your job: tighten the configuration to the minimum this feature needs, and
// add the hook.
//
// Runs by opening index.html in a browser — no setup required. DOMPurify is
// loaded locally from the vendored purify.min.js (no CDN reference).

function $(id) {
  return document.getElementById(id);
}

// ===========================================================================
// COMPLETE (LO3): scope this configuration to the minimum this feature needs —
// only b, i, em, strong, and a[href]. Right now it's empty, so
// DOMPurify.sanitize() below falls back to its broad built-in defaults,
// which allow far more than a rich-text note requires.
// ===========================================================================
const NOTE_SANITIZE_CONFIG = {
  ALLOWED_TAGS: ['b', 'i', 'em', 'strong', 'a'],
  ALLOWED_ATTR: ['href']
};

// ===========================================================================
// COMPLETE (LO4): add a DOMPurify.addHook('afterSanitizeAttributes', ...) call
// here that forces every surviving <a> element to carry
// rel="noopener noreferrer" (and, optionally, target="_blank"). This can't
// be expressed as an ALLOWED_TAGS/ALLOWED_ATTR rule — it's not "allow" or
// "disallow" but "always add" — so it has to run as a hook.
// ===========================================================================
DOMPurify.addHook('afterSanitizeAttributes', (node) => {
  if (node.tagName === 'A') {
    node.setAttribute('rel', 'noopener noreferrer');
  }
});
// ===========================================================================
// Sanitize + render every seeded note. (Complete — do not need to edit.)
// ===========================================================================
function renderNotes() {
  const host = $('notes');
  host.textContent = '';

  for (const note of UNTRUSTED_NOTES) {
    const clean = DOMPurify.sanitize(note.html, NOTE_SANITIZE_CONFIG);

    const card = document.createElement('div');
    card.className = 'note-card';
    card.dataset.noteId = note.id;

    const labelEl = document.createElement('div');
    labelEl.className = 'note-label';
    labelEl.textContent = note.label;
    card.appendChild(labelEl);

    const rawEl = document.createElement('div');
    rawEl.className = 'note-raw';
    rawEl.textContent = 'Raw: ' + note.html; // shown as TEXT for reference — never rendered as markup
    card.appendChild(rawEl);

    const renderedEl = document.createElement('div');
    renderedEl.className = 'note-rendered';
    renderedEl.innerHTML = clean; // safe once DOMPurify has sanitized this string
    card.appendChild(renderedEl);

    host.appendChild(card);
  }
}

renderNotes();

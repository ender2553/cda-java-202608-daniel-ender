// Trust Boundary Panel — trace four sources to their sinks.
//
// This page pulls a value from each of four untrusted sources named in the
// lesson (form field, URL query parameter, localStorage, and a simulated API
// response) and writes each one into the page. Your task is NOT to fix any of
// this — it is to TRACE each value hop by hop, label every hop `data` or
// `potential markup`, name the sink it lands in, and state the defense that
// sink would need. Fill in every `// TRACE:` blank below, then complete
// TRACE.md.
//
// Everything here runs by opening index.html in a browser — no setup required.

function $(id) {
  return document.getElementById(id);
}

// ---------------------------------------------------------------------------
// SOURCE 1 — form field. The learner can type into #nameInput; a seeded
// attacker-style default is used so the write happens on load without typing.
// ---------------------------------------------------------------------------
const SEEDED_NAME = '<img src=x onerror="console.warn(\'[trace] welcome banner sink fired\')">';

// TRACE — Origin: which line first reads this value, and from which of the
//         four untrusted sources named in the lesson (form field, URL query
//         param, localStorage, API response)?
// Answer: ____________________________________________________________________
function readDisplayName() {
  const input = $('nameInput');
  return input.value.trim().length > 0 ? input.value : SEEDED_NAME; // TRACE hop — label: Form Field - Markup (Essentially the innerHTML risk) 
}

function renderWelcome() {
  const name = readDisplayName(); // TRACE hop — label: Form Field - InnerHTML Exposure_

  // TRACE — SINK: name this DOM operation and state what the browser does with
  //         an attacker-supplied value written here.
  // Answer: innerHTML it parses argument: Concatonation and then insetion on the DOM_____________________________
  $('welcome').innerHTML = 'Welcome, ' + name + '!';

  // TRACE — Required defense: what would need to happen to this value before
  //         this sink, for this specific sink type?
  // Answer: HTML entity ('welcome') should be text content: So Sayeth Raph!!!
}

$('nameInput').addEventListener('input', renderWelcome);

// ---------------------------------------------------------------------------
// SOURCE 2 — URL query parameter, used to build a profile link's href.
// Try index.html?returnTo=%22%3E%3Cimg%20src%3Dx%20onerror%3Dconsole.warn(1)%3E
// (a URL-encoded breakout attempt) to see the raw value land unencoded.
// ---------------------------------------------------------------------------

// TRACE — Origin: which line reads this value, and from which source?
// Answer: Line 61 calls this method
// URL Query Parameter: A Malicious Link can be sent to any victim.
function readReturnTo() {
  const params = new URLSearchParams(location.search);
  const fromUrl = params.get('returnTo'); // TRACE hop — label: Markup can be injected
  return fromUrl !== null ? fromUrl : 'profile-42';
}

function renderProfileLink() {
  const returnTo = readReturnTo(); // TRACE hop — label: Markup can be injected here
  const link = $('profileLink');

  // TRACE — SINK: name this DOM operation and this specific sink risk (what
  //         could `returnTo` alter about the resulting URL if unencoded?).
  // Answer: __________________________________________________________________
  link.href = '/profile?id=42&return=' + returnTo;
  link.textContent = 'View profile';

  // TRACE — Required defense: what would need to happen to this value before
  //         this sink?
  // Answer: URL ENcoding (encodeURI COmponent (Return To)
}

// ---------------------------------------------------------------------------
// SOURCE 3 — localStorage. Simulates a value written earlier (possibly by a
// prior attacker-controlled write) and read back now.
// ---------------------------------------------------------------------------
if (!localStorage.getItem('savedTheme')) {
  localStorage.setItem(
    'savedTheme',
    '<span onmouseover="console.warn(\'[trace] theme badge sink fired\')">dark</span>'
  );
}

// TRACE — Origin: which line reads this value, and from which source?
// Answer: This begins on line 81: Local Storage
function readSavedTheme() {
  return localStorage.getItem('savedTheme'); // TRACE hop — label: Can hold a payload (markup)
}

function renderThemeBadge() {
  const theme = readSavedTheme(); // TRACE hop — label: potential markup
  const host = $('themeBadgeHost');

  // TRACE — SINK: name this DOM operation and what it does with `theme`.
  // Answer: insertAdjacentHTML like innerHTML can be malicious
  host.insertAdjacentHTML('beforeend', '<strong>Theme: </strong>' + theme);

  // TRACE — Required defense: what would need to happen to this value before
  //         this sink?
  // Answer: HTML ENcoding - Don't store HTML, CSS or JS in local storage. 
  //         Store the theme selection (Light, Dark, etc..) connect it to your
  //          css by variable name. 
}

// ---------------------------------------------------------------------------
// SOURCE 4 — simulated API response (no real network call; a resolved Promise
// stands in for `fetch(...).then(r => r.json())`).
// ---------------------------------------------------------------------------
function fetchCommentsFromApi() {
  // TRACE — Origin: even though this "arrives from our own API," why does the
  //         lesson still call it untrusted?
  // Answer: Any API is not a trust guaruntee. Payloads could already be in the back
  return Promise.resolve([
    { author: 'j.chen', body: 'Great write-up, thanks!' },
    {
      author: 'anon',
      body: 'Careful with this one <img src=x onerror="console.warn(\'[trace] comment sink fired\')">',
    },
  ]);
}

function renderComments() {
  fetchCommentsFromApi().then((comments) => {
    const host = $('comments');
    host.textContent = '';
    for (const comment of comments) {
      // TRACE hop — `comment.body` label: markup____
      const row = document.createElement('div');

      // TRACE — SINK: name this DOM operation.
      // Answer: innerHTML___________________________________________________
      row.innerHTML = '<strong>' + comment.author + ':</strong> ' + comment.body;
      host.appendChild(row);
    }

    // TRACE — Required defense: what would need to happen to `comment.body`
    //         before this sink? (Hint: this field is meant to allow some rich
    //         text, not just plain text — which technique from this module
    //         fits that requirement?)
    // Answer: _textContent - Display as literal text____________________________________________________
  });
}

renderWelcome();
renderProfileLink();
renderThemeBadge();
renderComments();

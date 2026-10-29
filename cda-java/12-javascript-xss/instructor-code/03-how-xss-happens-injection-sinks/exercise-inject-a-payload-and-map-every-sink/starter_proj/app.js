// app.js — a DELIBERATELY VULNERABLE demo page.
//
// Learning purpose only. Every "attack" against this page is a benign alert(1)
// aimed at THIS local page. Nothing here makes a network call or touches a real
// site. This lesson is about SEEING the injection sinks, not fixing them.
//
// As you read, notice the pattern: an untrusted value flows into an API that
// parses a string as HTML. That API is a "sink."

// ---------------------------------------------------------------------------
// Untrusted sources — values a user, the URL, or the browser's storage control
// ---------------------------------------------------------------------------

// Source: the URL query string, e.g. index.html?name=Sam
const params = new URLSearchParams(window.location.search);
const nameFromUrl = params.get('name') || 'Guest';

// Source: the browser's localStorage (holds whatever was saved on a past visit)
const savedDraft = localStorage.getItem('draft') || 'No saved draft yet.';

// Source: a stand-in for an untrusted SERVER RESPONSE. In a real app this would
// arrive over the network; we seed it as a string so the page runs offline with
// no server. It is untrusted all the same.
const serverMessage = 'All systems normal';

// ---------------------------------------------------------------------------
// Renders — each writes a value into the DOM through some API
// ---------------------------------------------------------------------------

// Greeting: a URL-controlled value flows into insertAdjacentHTML.
const greeting = document.getElementById('greeting');
greeting.insertAdjacentHTML('beforeend', 'Hello, ' + nameFromUrl + '!');

// Saved draft: a localStorage value flows into innerHTML.
const draftBox = document.getElementById('draft');
draftBox.innerHTML = savedDraft;

// Server message: a server-controlled value is concatenated into markup, then
// flows into outerHTML (which replaces the element itself).
const messageEl = document.getElementById('server-message');
messageEl.outerHTML = '<p id="server-message" class="msg">' + serverMessage + '</p>';

// Recent searches: an array of past search terms (user-influenced) is rendered
// by building <li> strings and appending them — the markup is assembled BEFORE
// it reaches the sink, which is exactly how a sink hides behind concatenation.
const recent = JSON.parse(localStorage.getItem('recent') || '[]');
const list = document.getElementById('recent-list');
list.innerHTML = '';
for (const term of recent) {
  const row = '<li>' + term + '</li>';
  list.innerHTML += row;
}

// Search box: the value the user types flows straight into innerHTML. This is
// the primary injection target for the exercise.
const form = document.getElementById('search-form');
const input = document.getElementById('search-input');
const results = document.getElementById('search-results');
form.addEventListener('submit', function (event) {
  event.preventDefault();
  const query = input.value;
  results.innerHTML = 'You searched for: ' + query;

  // Remember the term so it shows up in "Recent searches" next time.
  recent.unshift(query);
  localStorage.setItem('recent', JSON.stringify(recent.slice(0, 5)));
});

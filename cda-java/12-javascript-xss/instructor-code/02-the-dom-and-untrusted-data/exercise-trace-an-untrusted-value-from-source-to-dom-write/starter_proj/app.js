// Greeting Banner — trace the untrusted value from its source to the DOM write.
//
// This page reads a display name from an untrusted source and writes a greeting
// into the page. Your task is NOT to fix it — it is to TRACE the value hop by hop
// and label each hop as `data` or `potential markup`, then mark the crossing point.
//
// Fill in every `// TRACE:` blank below, then complete TRACE.md.
// Everything here runs by opening index.html in a browser — no setup required.

// ---------------------------------------------------------------------------
// SEEDED UNTRUSTED INPUT
// Simulates a value that arrived earlier from outside (e.g. saved to localStorage
// from a previous attacker-influenced request). A URL like index.html?name=Ada
// overrides it. Either way, this value is UNTRUSTED — you did not author it.
// A benign attacker-style default is used so you can SEE the write happen on load.
// ---------------------------------------------------------------------------
const SEEDED_NAME = '<img src=x onerror="alert(\'payload fired\')">';

// TRACE — Origin: on which line does the value first enter JavaScript from outside,
//         and what untrusted source is it? (URL/query param, form field, localStorage,
//         or a server response)
// Answer: ____________________________________________________________________
function readNameFromSource() {
  const params = new URLSearchParams(location.search);
  const fromUrl = params.get('name');       // TRACE hop — label: __________
  const name = fromUrl !== null ? fromUrl : SEEDED_NAME; // TRACE hop — label: __________
  return name;
}

// TRACE hop — the returned value now lives in `rawName`. Label: __________
const rawName = readNameFromSource();

// TRACE hop — concatenation builds the greeting text. Does wrapping it in
//             "Welcome, " make it safe? Label `greeting`: __________
const greeting = 'Welcome, ' + rawName + '!';

render(greeting);

function render(text) {
  const box = document.getElementById('greeting');

  // TRACE — CROSSING POINT: this is the DOM write. State what the browser does
  //         with an attacker-supplied value written here, and why this line is
  //         where `data` would cross into `markup`.
  // Answer: __________________________________________________________________
  box.innerHTML = text;
}

// The live input feeds the SAME render() so you can watch your own typing (or a
// pasted payload) take the identical path to the identical crossing point.
const nameInput = document.getElementById('name');
nameInput.addEventListener('input', (event) => {
  const typed = event.target.value;             // TRACE hop — source: __________, label: __________
  render('Welcome, ' + typed + '!');            // same crossing point as above
});

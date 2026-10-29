// Community Guestbook — STARTER (vulnerable)
//
// Both the display name and the comment come from inputs the visitor controls,
// so both are UNTRUSTED. This starter renders them with innerHTML, which hands
// the string to the HTML parser — turning a crafted value into live markup.
//
// The benign payload below is contained to THIS local page: it only pops an
// alert() to prove the value was parsed as markup instead of shown as text.
// It makes no network request and reaches no real site.
const ATTACK_PAYLOAD = '<img src=x onerror="alert(\'xss\')">';

const nameOutput = document.getElementById('name-output');
const commentOutput = document.getElementById('comment-output');

document.getElementById('render-btn').addEventListener('click', () => {
  const userName = document.getElementById('name-input').value;       // untrusted source
  const userComment = document.getElementById('comment-input').value; // untrusted source

  // SINK 1 (name render): untrusted value written as markup via innerHTML.
  // COMPLETED: this is an XSS sink — convert it to a safe text write (textContent).
  // Untrusted user input must be rendered as text, not parsed as HTML.
  nameOutput.textContent = userName;

  // SINK 2 (comment render): untrusted value written as markup via innerHTML.
  // COMPLETED: this is an XSS sink — rebuild it with document.createTextNode(...) + append.
  // Untrusted user input must be rendered as a text node, not parsed as HTML.
  commentOutput.replaceChildren();
  commentOutput.append(document.createTextNode(userComment));
});

// Convenience: fills both inputs with the supplied benign payload so you can
// reproduce the injection in one click. Type it by hand instead if you prefer.
document.getElementById('payload-btn').addEventListener('click', () => {
  document.getElementById('name-input').value = ATTACK_PAYLOAD;
  document.getElementById('comment-input').value = ATTACK_PAYLOAD;
});


// Feedback widget behavior. The functions below are already externalized and
// authorized normally through script-src's source list — they run fine.
// What's broken is how they're WIRED to the buttons: index.html currently
// uses onclick="..." attributes, which count as inline script under CSP and
// are blocked by script-src 'self' with no 'unsafe-inline'.
//
// TODO: wire these functions to the buttons with addEventListener instead of
// leaving the onclick="..." attributes in index.html.

function submitFeedback() {
  document.getElementById('log').textContent = 'Feedback submitted.';
}

function resetFeedback() {
  document.getElementById('log').textContent = '(waiting for action)';
}

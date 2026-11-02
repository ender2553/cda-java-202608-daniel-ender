// Sign-up form — submits via fetch() to the same-origin mock API instead of a
// full page navigation, and reports the result inline.
document.getElementById('signup-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const status = document.getElementById('status');
  status.textContent = 'Submitting…';

  try {
    const response = await fetch('/api/echo', { method: 'POST' });
    const body = await response.json();
    status.textContent = response.ok
      ? `Submitted (method: ${body.method})`
      : `Server returned ${response.status}`;
  } catch (err) {
    status.textContent = `Request failed: ${err.message}`;
  }
});

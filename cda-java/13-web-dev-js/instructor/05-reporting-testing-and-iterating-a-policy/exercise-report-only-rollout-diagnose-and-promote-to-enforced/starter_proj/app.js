// Order status page. legacyRefreshStatus is already externalized and
// authorized normally through script-src 'self' — it runs fine. What's
// broken is the leftover onclick="..." wiring in index.html, which counts
// as inline script and is blocked under the target policy.
document.getElementById('order-status').textContent = 'shipped';

function legacyRefreshStatus() {
  document.getElementById('order-status').textContent = 'refreshed: shipped';
}

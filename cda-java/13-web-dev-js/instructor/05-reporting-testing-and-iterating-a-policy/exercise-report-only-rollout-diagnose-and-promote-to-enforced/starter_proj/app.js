// Order status page. All JavaScript is externalized
// and authorized through script-src 'self'.

document.getElementById('order-status').textContent = 'shipped';

function legacyRefreshStatus() {
  document.getElementById('order-status').textContent = 'refreshed: shipped';
}

// Connect the button using an external event listener.
document.getElementById('refresh-btn').addEventListener('click', legacyRefreshStatus);
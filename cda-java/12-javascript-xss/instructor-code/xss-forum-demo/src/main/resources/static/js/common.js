// Shared helpers (plain globals, no modules). There is deliberately no HTML-escaping helper.

// Return the decoded cookie value, or null if not set.
function getCookie(name) {
  var parts = document.cookie ? document.cookie.split('; ') : [];
  for (var i = 0; i < parts.length; i++) {
    var eq = parts[i].indexOf('=');
    var key = eq < 0 ? parts[i] : parts[i].substring(0, eq);
    if (key === name) {
      var raw = eq < 0 ? '' : parts[i].substring(eq + 1);
      try {
        return decodeURIComponent(raw);
      } catch (e) {
        return raw; // hand-edited cookie with a stray %: fall back to the raw value
      }
    }
  }
  return null;
}

// INTENTIONALLY INSECURE: session cookie with no HttpOnly/Secure/SameSite/Expires, so XSS can read it.
function setCookie(name, value) {
  document.cookie = name + '=' + encodeURIComponent(value) + '; path=/';
}

// Expire both forum cookies.
function clearCookies() {
  var past = '; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/';
  document.cookie = 'forum_user_id=' + past;
  document.cookie = 'forum_user_name=' + past;
}

// ISO string -> browser-local "YYYY-MM-DD HH:MM:SS".
function formatDate(isoString) {
  var d = new Date(isoString);
  function p(n) { return (n < 10 ? '0' : '') + n; }
  return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) + ' ' +
         p(d.getHours()) + ':' + p(d.getMinutes()) + ':' + p(d.getSeconds());
}

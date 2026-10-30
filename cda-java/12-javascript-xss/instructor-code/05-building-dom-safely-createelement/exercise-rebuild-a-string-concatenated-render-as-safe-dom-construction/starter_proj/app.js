'use strict';

// ---------------------------------------------------------------------------
// Benign, contained demonstration marker.
// When an injected payload runs, it calls this function, which only writes a
// visible line to the on-page log and to the console. No network requests are
// made; nothing leaves this page. It is a safe stand-in for the arbitrary
// script an attacker's payload could otherwise execute in your page's context.
// ---------------------------------------------------------------------------
window.reportXss = function reportXss(source) {
  const log = document.getElementById('xss-log');
  const line = document.createElement('div');
  line.className = 'fired';
  line.textContent = 'payload fired: ' + source;
  log.append(line);
  console.error('[contained XSS demo] payload fired:', source);
};

// ---------------------------------------------------------------------------
// Untrusted data. Pretend this array came back from a server; the values are
// attacker-influenced. Two entries are hostile:
//   - one TITLE carries an <img onerror=...> payload (fires on render)
//   - one URL uses the javascript: scheme (fires when the link is clicked)
// ---------------------------------------------------------------------------
const items = [
  { title: 'Wireless Mouse',      url: 'https://example.com/mouse' },
  { title: 'Mechanical Keyboard', url: 'https://example.com/keyboard' },
  // Hostile entry #1 — onerror image payload hidden inside a "title".
  { title: '<img src=x onerror="reportXss(\'onerror image payload in title\')">',
    url: 'https://example.com/deal' },
  // Hostile entry #2 — javascript: URL hidden inside a "visit source" link.
  { title: 'Limited Time Offer',
    url: 'javascript:reportXss(\'javascript: URL in href\')' },
];

// ---------------------------------------------------------------------------
// VULNERABLE RENDER (this is what you will replace).
// It concatenates an HTML string out of untrusted data and hands it to the
// HTML parser via innerHTML. The parser turns the onerror <img> into a real,
// executing element, and the javascript: URL becomes a live, clickable sink.
// ---------------------------------------------------------------------------
function renderCards(items, container) {
  container.innerHTML = '';
  items.forEach(function (item) {
    container.innerHTML +=
      '<article class="card"><h3>' + item.title + '</h3>' +
      '<a href="' + item.url + '">Visit source</a></article>';
  });
}

renderCards(items, document.getElementById('product-list'));

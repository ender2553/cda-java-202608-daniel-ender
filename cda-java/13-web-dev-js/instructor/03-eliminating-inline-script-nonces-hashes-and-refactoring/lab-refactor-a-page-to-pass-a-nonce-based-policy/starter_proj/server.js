const http = require('http');
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const PORT = 8080;
const ROOT = __dirname;

const CONTENT_TYPES = {
  '.html': 'text/html',
  '.js': 'application/javascript',
  '.css': 'text/css'
};

const server = http.createServer((req, res) => {
  const pathname = new URL(req.url, `http://${req.headers.host}`).pathname;

  if (req.method !== 'GET') {
    res.writeHead(405);
    res.end('Method Not Allowed');
    return;
  }

  if (pathname === '/') {
    const nonce = crypto.randomBytes(16).toString('base64');

    fs.readFile(path.join(ROOT, 'index.html'), 'utf8', (err, html) => {
      if (err) {
        res.writeHead(500);
        res.end('Internal Server Error');
        return;
      }

      const page = html.replaceAll('__CSP_NONCE__', nonce);

      res.writeHead(200, {
        'Content-Type': 'text/html',
        'Content-Security-Policy':
          `default-src 'self'; script-src 'self' 'nonce-${nonce}'; style-src 'self'; object-src 'none'; base-uri 'self'; form-action 'self'`
      });

      res.end(page);
    });

    return;
  }

  const allowedFiles = {
    '/script.js': 'script.js',
    '/style.css': 'style.css'
  };

  const filename = allowedFiles[pathname];

  if (!filename) {
    res.writeHead(404);
    res.end('Not Found');
    return;
  }

  const filePath = path.join(ROOT, filename);

  fs.readFile(filePath, (err, content) => {
    if (err) {
      res.writeHead(500);
      res.end('Internal Server Error');
      return;
    }

    res.writeHead(200, {
      'Content-Type': CONTENT_TYPES[path.extname(filename)]
    });

    res.end(content);
  });
});

server.listen(PORT, () => {
  console.log(`Feedback widget server running at http://localhost:${PORT}`);
});
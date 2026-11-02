#!/usr/bin/env node
// Minimal zero-dependency dev server for the secure-web code projects.
// Serves the CURRENT directory's static files, and lets a project control
// response headers (incl. Content-Security-Policy) via an optional headers.json
// in the project root. Also exposes mock API + CSP-report endpoints for the
// lessons that need real HTTP behavior. No build step, no npm install.
//
//   node <path-to>/_server/serve.js [port]        # default 8080
//
// headers.json (optional, in the served dir): { "/": {"Content-Security-Policy": "default-src 'self'"} }
const http = require('http');
const fs = require('fs');
const path = require('path');
const ROOT = process.cwd();
const PORT = Number(process.argv[2]) || 8080;
const MIME = {'.html':'text/html','.js':'text/javascript','.css':'text/css','.json':'application/json','.svg':'image/svg+xml','.png':'image/png'};
let headers = {};
try { headers = JSON.parse(fs.readFileSync(path.join(ROOT,'headers.json'),'utf8')); } catch {}

const server = http.createServer((req, res) => {
  const url = new URL(req.url, `http://localhost:${PORT}`);
  const p = url.pathname;

  // --- CSP violation report sink (Module 1, L5) ---
  if (p === '/csp-report' && req.method === 'POST') {
    let body=''; req.on('data',c=>body+=c); req.on('end',()=>{ console.log('[csp-report]', body.slice(0,500)); res.writeHead(204).end(); });
    return;
  }
  // --- Mock API (Module 3) ---
  if (p.startsWith('/api/')) {
    // CORS: same-origin only by default; a cross-origin route to demonstrate CORS
    if (p === '/api/echo') { res.writeHead(200,{'Content-Type':'application/json'}); return res.end(JSON.stringify({ok:true, method:req.method})); }
    if (p === '/api/user') {
      const auth = req.headers['authorization'];
      if (!auth) { res.writeHead(401,{'Content-Type':'application/json'}); return res.end(JSON.stringify({error:'missing Authorization'})); }
      res.writeHead(200,{'Content-Type':'application/json'}); return res.end(JSON.stringify({id:1, name:'Ada Lovelace'}));
    }
    if (p === '/api/hostile') { // malformed/hostile response for L14
      res.writeHead(200,{'Content-Type':'text/html'}); return res.end('<b>not json</b>');
    }
    if (p === '/api/crash') { // simulated server-side failure for L11 (still valid JSON body, 500 status)
      res.writeHead(500,{'Content-Type':'application/json'}); return res.end(JSON.stringify({error:'internal server error'}));
    }
    res.writeHead(404,{'Content-Type':'application/json'}); return res.end(JSON.stringify({error:'no such endpoint'}));
  }

  // --- static files, with per-route headers from headers.json ---
  let fp = path.join(ROOT, decodeURIComponent(p));
  if (p.endsWith('/')) fp = path.join(fp, 'index.html');
  if (!fp.startsWith(ROOT)) { res.writeHead(403).end('forbidden'); return; }
  fs.readFile(fp, (err, data) => {
    if (err) { res.writeHead(404).end('not found'); return; }
    const ext = path.extname(fp);
    const h = { 'Content-Type': MIME[ext] || 'application/octet-stream', ...(headers[p]||headers['/']||{}) };
    res.writeHead(200, h); res.end(data);
  });
});
server.listen(PORT, () => console.log(`[serve] http://localhost:${PORT}  (root: ${ROOT})`));

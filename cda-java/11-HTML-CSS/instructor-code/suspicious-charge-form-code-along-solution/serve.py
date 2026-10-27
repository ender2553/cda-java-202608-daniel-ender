"""Tiny local web server for the "Harden a Suspicious-Charge Form" code-along.

Provided plumbing -- you do not need to edit or understand this file.

It does two things:
  1. Serves the files in a folder (default: ./site) at http://localhost:8000/
  2. Answers form submissions sent to /submit with a page showing exactly
     what arrived: which method was used, whether the fields came in the URL
     or the request body, how the body was encoded, and every field received.

Every request is also printed in this terminal, just like a real server log.

Usage:
    python serve.py                 # serves ./site
    python serve.py some/folder     # serves a different folder
    python serve.py --port 8080     # use a different port
"""

import argparse
import functools
import html
import sys
from email.parser import BytesParser
from email.policy import HTTP
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import parse_qsl, urlsplit

MAX_BODY_BYTES = 10 * 1024 * 1024  # refuse anything larger than 10 MB


class DemoHandler(SimpleHTTPRequestHandler):
    def end_headers(self):
        # Always serve fresh files so each browser reload shows your latest edit.
        self.send_header("Cache-Control", "no-store")
        super().end_headers()

    def do_GET(self):
        url = urlsplit(self.path)
        if url.path == "/submit":
            fields = [(name, value, None) for name, value in parse_qsl(url.query, keep_blank_values=True)]
            self.send_report("GET", "URL query string", "(none: GET has no body)", fields)
        else:
            super().do_GET()

    def do_POST(self):
        if urlsplit(self.path).path != "/submit":
            self.send_error(404, "Only /submit accepts form submissions")
            return
        length = int(self.headers.get("Content-Length", 0))
        if length > MAX_BODY_BYTES:
            self.send_error(413, "Submission too large for this demo server")
            return
        body = self.rfile.read(length)
        content_type = self.headers.get("Content-Type", "")

        if content_type.startswith("multipart/form-data"):
            fields = parse_multipart(content_type, body)
        else:
            text = body.decode("utf-8", errors="replace")
            fields = [(name, value, None) for name, value in parse_qsl(text, keep_blank_values=True)]
        self.send_report("POST", "Request body", content_type.split(";")[0] or "(not set)", fields)

    def send_report(self, method, location, encoding, fields):
        page = render_report(method, location, encoding, fields).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Content-Length", str(len(page)))
        self.end_headers()
        self.wfile.write(page)


def parse_multipart(content_type, body):
    """Return (name, value, file_size) tuples; file_size is None for plain text fields."""
    message = BytesParser(policy=HTTP).parsebytes(
        b"Content-Type: " + content_type.encode("latin-1") + b"\r\n\r\n" + body
    )
    fields = []
    for part in message.iter_parts():
        name = part.get_param("name", header="content-disposition") or "(unnamed)"
        data = part.get_payload(decode=True) or b""
        filename = part.get_filename()
        if filename is not None:
            fields.append((name, filename or "(no file chosen)", len(data)))
        else:
            fields.append((name, data.decode("utf-8", errors="replace"), None))
    return fields


def render_report(method, location, encoding, fields):
    # Every value is escaped: this page must never turn submitted text into markup.
    esc = html.escape
    rows = []
    for name, value, file_size in fields:
        if file_size is None:
            shown = esc(value) if value else "<em>(empty)</em>"
        else:
            shown = f"File <code>{esc(value)}</code> &mdash; {file_size:,} bytes of file content received"
        rows.append(f"<tr><th scope=\"row\"><code>{esc(name)}</code></th><td>{shown}</td></tr>")
    if not rows:
        rows.append('<tr><td colspan="2"><em>No named fields arrived.</em></td></tr>')

    if method == "GET":
        warning = (
            '<p class="leak"><strong>Every value above is now in the address bar, in your browser history, '
            "and in this server's log.</strong> Look at the terminal running <code>serve.py</code>.</p>"
        )
    else:
        warning = '<p class="ok">The values travelled in the request body, so the URL stayed clean.</p>'

    if encoding == "application/x-www-form-urlencoded" or method == "GET":
        encoding_note = " (text only: a file input sends just its file <em>name</em>)"
    else:
        encoding_note = ""

    return f"""<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>What the server received</title>
    <link rel="icon" href="/logo.svg">
    <link rel="stylesheet" href="/styles.css">
  </head>
  <body>
    <main>
      <h1>What the server received</h1>
      <dl class="received">
        <dt>Method</dt><dd><code>{method}</code></dd>
        <dt>Fields arrived in</dt><dd>{location}</dd>
        <dt>Body encoding</dt><dd><code>{esc(encoding)}</code>{encoding_note}</dd>
      </dl>
      <table>
        <caption>Name/value pairs received</caption>
        <thead><tr><th scope="col">Name</th><th scope="col">Value</th></tr></thead>
        <tbody>
          {"".join(rows)}
        </tbody>
      </table>
      {warning}
      <p><a href="/report.html">&larr; Back to the form</a></p>
    </main>
  </body>
</html>
"""


def main():
    parser = argparse.ArgumentParser(description="Serve the code-along site and echo form submissions.")
    parser.add_argument("folder", nargs="?", default=str(Path(__file__).parent / "site"))
    parser.add_argument("--port", type=int, default=8000)
    args = parser.parse_args()

    folder = Path(args.folder).resolve()
    if not (folder / "report.html").exists():
        sys.exit(f"Could not find report.html in {folder}")

    handler = functools.partial(DemoHandler, directory=str(folder))
    server = ThreadingHTTPServer(("127.0.0.1", args.port), handler)
    print(f"Serving {folder}")
    print(f"Open http://localhost:{args.port}/report.html   (press Ctrl+C to stop)")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nServer stopped.")


if __name__ == "__main__":
    main()

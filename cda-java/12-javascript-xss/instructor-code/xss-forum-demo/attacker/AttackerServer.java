import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Fake "attacker" server for the XSS demo. It is a separate origin (its own
 * port, no cookies, no shared code with the forum) that simply records
 * whatever a payload sends it and shows it back on a captures page.
 *
 * Run with: java attacker/AttackerServer.java  (JDK single-file launch, no build step)
 */
public class AttackerServer {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** One captured item: when it arrived, who sent it (Referer), and the raw data. */
    record Capture(String received, String referer, String data) {}

    // Newest-first in-memory log. Lost on restart — that's fine for a demo.
    private static final List<Capture> CAPTURES = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 9090), 0);
        server.createContext("/steal", AttackerServer::handleSteal);
        server.createContext("/clear", AttackerServer::handleClear);
        server.createContext("/", AttackerServer::handleIndex);
        server.start();
        System.out.println("Attacker listener on http://localhost:9090/ - payloads send stolen data to /steal");
    }

    private static void handleSteal(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (!"GET".equalsIgnoreCase(method) && !"POST".equalsIgnoreCase(method)
                && !"OPTIONS".equalsIgnoreCase(method)) {
            exchange.getResponseHeaders().add("Allow", "GET, POST, OPTIONS");
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }
        if ("OPTIONS".equalsIgnoreCase(method)) {
            addCorsHeaders(exchange, true);
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        String referer = headerOrDash(exchange, "Referer");
        String data;
        if ("POST".equalsIgnoreCase(method)) {
            data = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        } else {
            String query = exchange.getRequestURI().getRawQuery();
            data = query == null ? "" : decodeOrRaw(query);
        }

        String received = LocalDateTime.now().format(TIME_FORMAT);
        CAPTURES.add(0, new Capture(received, referer, data));
        System.out.println("[" + received + "] from " + referer + " -> " + data);

        addCorsHeaders(exchange, false);
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    /** Percent-decodes a query string; a malformed escape (e.g. a bare %) keeps the raw text. */
    private static String decodeOrRaw(String query) {
        try {
            return URLDecoder.decode(query, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return query;
        }
    }

    private static void handleClear(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().add("Allow", "POST");
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }
        CAPTURES.clear();
        exchange.getResponseHeaders().add("Location", "/");
        exchange.sendResponseHeaders(303, -1);
        exchange.close();
    }

    private static void handleIndex(HttpExchange exchange) throws IOException {
        if (!"/".equals(exchange.getRequestURI().getPath())) {
            String notFound = "404 not found";
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(404, notFound.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(notFound.getBytes(StandardCharsets.UTF_8));
            }
            return;
        }

        String html = renderPage();
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void addCorsHeaders(HttpExchange exchange, boolean preflight) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        if (preflight) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "*");
        }
    }

    private static String headerOrDash(HttpExchange exchange, String name) {
        List<String> values = exchange.getRequestHeaders().get(name);
        return (values == null || values.isEmpty()) ? "—" : values.get(0);
    }

    /** HTML-escapes a captured value before it is written into this page, so anything a payload sent shows as plain text instead of running. Replace & first, or the other entities get double-escaped. */
    private static String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String renderPage() {
        StringBuilder rows = new StringBuilder();
        if (CAPTURES.isEmpty()) {
            rows.append("<tr><td colspan=\"3\" class=\"empty\">No captures yet. Post a payload on the " +
                    "forum that sends data to /steal, then view it as another user.</td></tr>");
        } else {
            for (Capture c : CAPTURES) {
                rows.append("<tr><td class=\"received\">").append(escape(c.received())).append("</td>")
                        .append("<td class=\"referer\">").append(escape(c.referer())).append("</td>")
                        .append("<td class=\"data\">").append(escape(c.data())).append("</td></tr>");
            }
        }

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>attacker listener</title>
                <link rel="icon" href="data:,">
                <style>
                  :root {
                    --color-bg: #0A0A0A;
                    --color-surface: #161616;
                    --color-ink: #F5F5F5;
                    --color-line: rgba(245,245,245,0.14);
                    --color-accent: #BDBDBD;
                  }
                  * { box-sizing: border-box; }
                  body {
                    margin: 0;
                    padding: 32px 24px;
                    background: var(--color-bg);
                    color: var(--color-ink);
                    font-family: "JetBrains Mono", ui-monospace, Consolas, "Courier New", monospace;
                  }
                  h1 {
                    font-weight: 300;
                    font-size: 2rem;
                    margin: 0 0 8px 0;
                    text-transform: lowercase;
                  }
                  .caption {
                    opacity: 0.70;
                    margin: 0 0 24px 0;
                  }
                  .actions { margin-bottom: 24px; display: flex; gap: 12px; }
                  .ghost-btn {
                    background: none;
                    border: 0;
                    padding: 0;
                    color: var(--color-ink);
                    opacity: 0.70;
                    text-decoration: underline;
                    text-underline-offset: 3px;
                    font: inherit;
                    cursor: pointer;
                  }
                  .ghost-btn:hover:not(:disabled) { opacity: 1; }
                  .ghost-btn:disabled { opacity: 0.4; cursor: not-allowed; }
                  .ghost-btn:focus-visible {
                    outline: 2px solid var(--color-accent);
                    outline-offset: 2px;
                  }
                  .table-wrap {
                    overflow-x: auto;
                    border: 1px solid var(--color-line);
                  }
                  table { border-collapse: collapse; width: 100%%; min-width: 600px; }
                  th, td {
                    text-align: left;
                    padding: 10px 12px;
                    border-bottom: 1px solid var(--color-line);
                    vertical-align: top;
                  }
                  th { font-weight: 500; opacity: 0.87; }
                  td.received { opacity: 0.70; white-space: nowrap; }
                  td.referer { opacity: 0.87; word-break: break-all; }
                  td.data { opacity: 1; word-break: break-all; }
                  td.empty { opacity: 0.62; padding: 24px 12px; }
                </style>
                </head>
                <body>
                  <h1>attacker listener</h1>
                  <p class="caption">Listening on http://localhost:9090/steal — payloads send stolen data here.</p>
                  <div class="actions">
                    <a class="ghost-btn" href="/">Refresh</a>
                    <form method="post" action="/clear" style="margin:0;">
                      <button class="ghost-btn" type="submit">Clear captures</button>
                    </form>
                  </div>
                  <div class="table-wrap">
                    <table>
                      <thead>
                        <tr><th>Received</th><th>From (Referer)</th><th>Data</th></tr>
                      </thead>
                      <tbody>
                        %s
                      </tbody>
                    </table>
                  </div>
                </body>
                </html>
                """.formatted(rows.toString());
    }
}

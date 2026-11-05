# Diagnose Failing Cross-Origin Requests

## Case 1 — Simple GET request

**Classification:** Missing or mismatched `Origin`–`Access-Control-Allow-Origin`.

**Evidence from the Network panel:**

Request:

```http
GET /status HTTP/1.1
Origin: https://dashboard.example.com
```

Response:

```http
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 41
```

The request includes `Origin: https://dashboard.example.com`, but the response contains no `Access-Control-Allow-Origin` header.

**Correct fix:**

Configure the API server to return the following header for requests from the trusted dashboard origin:

```http
Access-Control-Allow-Origin: https://dashboard.example.com
```

The server must explicitly allow the requesting origin so the browser can expose the response to the frontend JavaScript.

---

## Case 2 — POST with a JSON body and an Authorization header

**Classification:** Failed preflight.

**Evidence from the Network panel:**

Preflight request:

```http
OPTIONS /orders HTTP/1.1
Origin: https://dashboard.example.com
Access-Control-Request-Method: POST
Access-Control-Request-Headers: content-type, authorization
```

Preflight response:

```http
HTTP/1.1 204 No Content
Access-Control-Allow-Origin: https://dashboard.example.com
Access-Control-Allow-Methods: POST, GET, OPTIONS
Access-Control-Allow-Headers: Content-Type
```

The browser requests permission to send both `content-type` and `authorization`, but the server only allows `Content-Type`. The `Authorization` header is missing from `Access-Control-Allow-Headers`.

The actual POST request is never sent because the browser stops after the preflight fails.

**Correct fix:**

Configure the server's preflight response to allow the Authorization header:

```http
Access-Control-Allow-Origin: https://dashboard.example.com
Access-Control-Allow-Methods: POST, GET, OPTIONS
Access-Control-Allow-Headers: Content-Type, Authorization
```

The server must authorize every requested header and method before the browser will send the actual request.

---

## Case 3 — Credentialed request against a wildcard origin

**Classification:** Credentials/wildcard conflict.

**Evidence from the Network panel:**

Request:

```http
GET /account HTTP/1.1
Origin: https://dashboard.example.com
Cookie: session=abc123...
```

The request uses `credentials: 'include'`, so the browser attaches the user's session cookie.

Response:

```http
HTTP/1.1 200 OK
Access-Control-Allow-Origin: *
Access-Control-Allow-Credentials: true
Content-Type: application/json
```

The server returns `Access-Control-Allow-Origin: *` together with `Access-Control-Allow-Credentials: true`.

A wildcard origin cannot be used for a credentialed CORS request. Even though the server returns HTTP 200 and includes the credentials header, the browser rejects the response.

**Correct fix:**

Replace the wildcard with the specific trusted dashboard origin:

```http
Access-Control-Allow-Origin: https://dashboard.example.com
Access-Control-Allow-Credentials: true
```

The server should allow credentials only from explicitly trusted origins.

---

## Case 4 — A typo'd URL

**Classification:** Not actually a CORS failure. The underlying problem is an incorrect URL resulting in a 404 response.

**Evidence from the Network panel:**

Request:

```http
GET /oders HTTP/1.1
Origin: https://dashboard.example.com
```

The requested route is `/oders`, but the correct route is `/orders`.

Response:

```http
HTTP/1.1 404 Not Found
Content-Type: application/json
Content-Length: 25
```

The server returns a genuine 404 response. The response does not include `Access-Control-Allow-Origin` because the server's catch-all route-not-found handler does not apply the CORS middleware.

**Correct fix:**

1. Correct the URL from `/oders` to `/orders`.
2. Configure the server's error-handling routes to apply the appropriate CORS headers to error responses, including 404 responses.

This ensures that the frontend can receive and handle legitimate HTTP error responses instead of having them hidden by the browser's CORS enforcement.

---

## Final question

**Which case is not really a CORS failure, and why can the console message be misleading?**

Case 4 is not really a CORS failure. The actual problem is a typo in the URL: `/oders` instead of `/orders`. The server returns a genuine `404 Not Found` response.

The console message is misleading because the 404 response does not include `Access-Control-Allow-Origin`. The browser therefore blocks JavaScript from accessing the response and reports a CORS error. The missing CORS header hides the underlying HTTP error from the frontend, making it appear that CORS is the original problem.

The correct first step is to fix the URL. The server should also apply appropriate CORS headers to error responses, including 404 responses.

---

## Security takeaways

* **Case 1:** Servers must explicitly allow trusted origins using `Access-Control-Allow-Origin`.
* **Case 2:** Preflight responses must authorize every requested method and header.
* **Case 3:** Credentialed requests cannot use `Access-Control-Allow-Origin: *`. They require a specific trusted origin and `Access-Control-Allow-Credentials: true`.
* **Case 4:** A missing CORS header on an error response can make a genuine HTTP error look like a CORS failure.

**Important:** CORS is controlled by the server. Do not disable browser security, use a CORS-unblocking browser extension, or introduce a proxy as a workaround. Fix the server's CORS configuration and the underlying request problems.

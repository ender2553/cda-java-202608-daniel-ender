// api.js: the ONE place this app should talk to the network.
// PART 3 (Lessons 11–15): right now nothing uses this file. app.js calls fetch()
// directly in eight places, and each call site makes its own (wrong) choices.
// You'll finish apiRequest() and route every call through it.

import { parseUntrusted } from "./safe-json.js";

const settings = {
  apiBase: "/api",
  timeoutMs: 5000,
  onUnauthorized: () => {},
};

// The token lives ONLY in this module's memory.
let token = null;

export function configure(options) {
  Object.assign(settings, options);
}
export function setToken(value) {
  token = value;
}
export function clearToken() {
  token = null;
}
export function isSignedIn() {
  return token !== null;
}

// The message is safe to show users. Internal details stay out of the UI.
export class ApiError extends Error {
  constructor(userMessage, status) {
    super(userMessage);
    this.name = "ApiError";
    this.status = status;
  }
}

// TODO Part 3: build the wrapper.
//   1. allow only same-origin paths ("/events"), matching CSP connect-src
//   2. Accept + Authorization: Bearer <token> headers (never ?access_token=)
//   3. AbortController timeout
//   4. 401 -> clearToken() + settings.onUnauthorized()
//   5. !res.ok -> generic ApiError (log the status, never show the body)
//   6. Content-Type must be application/json
//   7. parseUntrusted(text), then run the caller's validate() function

function failedToAnswer(err){
  return new ApiError(
    err && err.name === "AbortError"
    ? "The server took too long to answer. Please try again"
    : "Networks Problem. Please check your connectoin and try again",
    0
  );
}

//DELETE
// export async function apiRequest(path, { method = "GET", body, validate, timeoutMs } = {}) {
//   throw new ApiError("apiRequest() is not built yet (Part 3).", 0);
// }
//END DELETE

export async function apiRequest(path, { method = "GET", body, validate, timeoutMs } = {}) {
  // L15: only same-origin paths under apiBase. This matches CSP connect-src 'self'.
  if (typeof path !== "string" || !path.startsWith("/") || path.startsWith("//") || path.includes("..")) {
    throw new ApiError("That data source isn't on our allow-list.", 0);
  }

  const headers = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  if (token) headers["Authorization"] = `Bearer ${token}`; // header, never ?access_token=

  // L11/L15: a request that never answers must not hang the UI forever.
  // The timer covers the WHOLE exchange, including reading the body.
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs ?? settings.timeoutMs);

  try {
    let res;
    try {
      res = await fetch(settings.apiBase + path, {
        method,
        headers,
        body: body === undefined ? undefined : JSON.stringify(body),
        signal: controller.signal,
        credentials: "same-origin",
        cache: "no-store",
      });
    } catch (err) {
      throw failedToAnswer(err);
    }

    // L13: an expired or missing token means sign out cleanly.
    if (res.status === 401) {
      clearToken();
      settings.onUnauthorized();
      throw new ApiError("Your session has ended. Please sign in again.", 401);
    }

    // L11/L14: a non-2xx status is a failure. Log the status, but never show
    // the server's error body to the user.
    if (!res.ok) {
      console.warn(`[api] ${method} ${path} failed with status ${res.status}`);
      throw new ApiError("Something went wrong. Please try again.", res.status);
    }
    if (res.status === 204) return null;

    // L14: check the Content-Type before trusting the body as JSON.
    const type = res.headers.get("Content-Type") || "";
    if (!/^application\/json\b/i.test(type)) {
      console.warn(`[api] ${path}: expected JSON, got "${type}". Response discarded.`);
      throw new ApiError("We got an unexpected response, so we didn't show it.", res.status);
    }

    let text;
    try {
      text = await res.text();
    } catch (err) {
      throw failedToAnswer(err);
    }

    // PART 4 (L16–17): guarded parse with a filtering reviver.
    const parsed = parseUntrusted(text);
    if (!parsed.ok) throw new ApiError("We got a damaged response, so we didn't show it.", res.status);

    // L14/L19: the caller says what shape it expects. Nothing unvalidated escapes.
    if (validate) {
      const result = validate(parsed.value);
      if (!result.ok) {
        console.warn(`[api] ${path}: response failed validation:`, result.errors);
        throw new ApiError("We got data we couldn't verify, so we didn't show it.", res.status);
      }
      return result.value;
    }
    return parsed.value;
  } finally {
    clearTimeout(timer);
  }
}


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
export async function apiRequest(path, { method = "GET", body, validate, timeoutMs } = {}) {
  throw new ApiError("apiRequest() is not built yet (Part 3).", 0);
}

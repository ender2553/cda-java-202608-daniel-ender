// Trace a Value to Its Source — STARTER
//
// This page only READS values from input sources and LOGS them to the console.
// It deliberately writes none of those values to the page: staying at that safe
// boundary is the whole point of the exercise. No network calls are made.
//
// The URL read below is finished for you as a worked model. Your job is to
// finish the form-field read (see the TODO), then fill in every "// TRACE:"
// line and the matching rows in TRACE.md.

"use strict";

// --- A developer-authored value (the worked model) -------------------------
// We wrote this literal ourselves, so it is trusted by origin.
// TRACE: value "Ledger" | SOURCE: developer-authored (TRUSTED)
//        -> stored in const siteLabel -> goes to console.log (NOT the page)
//        -> handled as TEXT
const siteLabel = "Ledger";
console.log(`Site label (developer-authored, trusted): ${siteLabel}`);

// --- Read 1: the URL query string (built together, worked model) -----------
// Anything after "?" in the address bar is written by whoever crafts the link,
// not by us. Its origin is outside our control, so this value is UNTRUSTED.
const params = new URLSearchParams(location.search);
const nameParam = params.get("name");
// TRACE: value nameParam | SOURCE: URL query string | user-supplied (UNTRUSTED)
//        -> stored in const nameParam -> goes to console.log (NOT the page)
//        -> handled as TEXT (interpolated into a plain message, never markup)

// Strict-equality presence-and-type check: a missing or wrong-typed value is
// handled, never assumed to exist.
if (nameParam !== null && typeof nameParam === "string") {
  const message = `Read a name value: ${nameParam}`;
  console.log(message);
} else {
  console.log("No name parameter was present.");
}

// --- Read 2: a form field (YOUR TASK) --------------------------------------
function readFormField() {
  // TODO (your task): read the value the visitor typed into #nameField and log
  // it the same way the URL read does above.
  //   1. const field = document.querySelector("#nameField");
  //   2. read field.value into a const of your choosing
  //   3. guard it with a strict presence-and-type check
  //      (typeof ... === "string" && ....length > 0)
  //   4. compose the output with a template literal and console.log it
  //
  // TRACE: value ??? | SOURCE: ??? | trusted or untrusted? ??? | text or markup? ???
  //        -> stored in ??? -> goes to ??? -> handled as ???
  console.log("TODO: read and log the form field value.");
}

// --- Wire up the button ----------------------------------------------------
// The status line shows only developer-authored text. It never echoes the
// values you read — those go to the console only, never onto the page.
const button = document.querySelector("#readButton");
const status = document.querySelector("#status");
let readCount = 0;

button.addEventListener("click", () => {
  readFormField();
  readCount += 1;
  const now = new Date().toLocaleTimeString();
  status.textContent =
    `Read and logged to the console. Reads so far: ${readCount} ` +
    `(last at ${now}). Nothing user-supplied was rendered on this page.`;
});

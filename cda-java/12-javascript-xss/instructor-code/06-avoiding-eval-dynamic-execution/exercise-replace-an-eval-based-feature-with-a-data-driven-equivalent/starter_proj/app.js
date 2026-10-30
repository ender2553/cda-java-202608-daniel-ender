"use strict";

// Quick Action Console — VULNERABLE starter.
//
// The "quick action" feature dispatches to a routine by name. A developer took a
// shortcut and built the dispatch with eval(), which turns the user-controlled
// `action` string into running code. Your job in this exercise is to (1) prove the
// vulnerability, then (2) replace the eval dispatch with a data-driven lookup table.

const logEl = document.getElementById("log");
const markerEl = document.getElementById("attacker-marker");

function log(message) {
  logEl.textContent = message;
}

// The three legitimate routines the feature is supposed to offer.
function do_save() { log("Saved the current document."); }
function do_load() { log("Loaded the last document."); }
function do_export() { log("Exported the document as PDF."); }

// ---------------------------------------------------------------------------
// THE VULNERABILITY
// `action` comes straight from the input box. Concatenating it into a string and
// handing that string to eval() means the input is executed as JavaScript in this
// scope — it is a code-injection sink. `save` and `fetch(...cookies...)` look
// identical to eval: both are just "code to run".
// ---------------------------------------------------------------------------
function run(action) {
  eval("do_" + action + "()"); // ✗ untrusted input becomes code
}

document.getElementById("run").addEventListener("click", () => {
  const action = document.getElementById("action").value;
  try {
    run(action);
  } catch (err) {
    log("Error: " + err.message);
  }
});

// A benign marker the demo payload flips, so an injection is visible on the page.
// (The seeded payload in the input box calls alert(...) AND this flip.)
window.__markPayloadFired = function () {
  markerEl.textContent = "Attacker marker: ⚠ PAYLOAD FIRED — injected code ran with the page's authority";
  markerEl.classList.add("fired");
};

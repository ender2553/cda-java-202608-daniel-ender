// Shows CSP violations on the page so learners don't have to hunt for them in the console.
// Loaded from the site's own origin (script-src 'self'), never inline.
// Loaded in <head> without defer so the listener is registered before the body parses —
// otherwise violations caused by markup lower on the page would fire before we were listening.

(function () {
  "use strict";

  var violations = [];

  function describe(event) {
    var blocked = event.blockedURI || "(unknown)";
    if (blocked === "inline") {
      blocked = event.effectiveDirective.indexOf("style") === 0 ? "inline style" : "inline script";
    }
    return event.effectiveDirective + " blocked " + blocked;
  }

  // textContent only: violation details come from the page, so never write them as HTML.
  function render() {
    var status = document.querySelector("[data-csp-status]");
    if (status) {
      status.textContent = violations.length === 0
        ? "CSP: enforcing, 0 violations on this page"
        : "CSP: enforcing, " + violations.length + " violation(s) blocked on this page";
      status.classList.toggle("csp-status--blocked", violations.length > 0);
    }

    var log = document.querySelector("[data-csp-log]");
    if (log) {
      log.replaceChildren();
      violations.forEach(function (text) {
        var item = document.createElement("li");
        item.textContent = text;
        log.appendChild(item);
      });
    }
  }

  document.addEventListener("securitypolicyviolation", function (event) {
    violations.push(describe(event));
    if (document.readyState !== "loading") {
      render();
    }
  });

  document.addEventListener("DOMContentLoaded", render);
})();

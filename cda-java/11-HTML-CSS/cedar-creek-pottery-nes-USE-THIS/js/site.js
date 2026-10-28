// Lists every CSP violation in the page footer, so you can see the policy working
// without opening dev tools. Loaded in <head> (no defer) so it is listening
// before the body is parsed.

var violations = [];

document.addEventListener("securitypolicyviolation", function (event) {
  var blocked = event.blockedURI === "inline" ? "inline code" : event.blockedURI;
  violations.push(event.effectiveDirective + " blocked " + blocked);
  showViolations();
});

document.addEventListener("DOMContentLoaded", showViolations);

function showViolations() {
  var status = document.querySelector("[data-csp-status]");
  var log = document.querySelector("[data-csp-log]");
  if (!status || !log) {
    return;
  }

  status.textContent = "CSP: " + violations.length + " violation(s) blocked";
  status.classList.toggle("csp-status--blocked", violations.length > 0);

  // textContent only, never innerHTML.
  log.replaceChildren();
  violations.forEach(function (text) {
    var item = document.createElement("li");
    item.textContent = text;
    log.appendChild(item);
  });
}

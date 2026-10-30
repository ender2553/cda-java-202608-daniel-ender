// ============================================================================
// UNTRUSTED INPUT (do not "trust" any of this)
// ----------------------------------------------------------------------------
// Every value below simulates data that came from OUTSIDE your code: text a
// customer pasted, a link they submitted, a settings blob read back from
// storage, a formula chosen in a form. In a real app this arrives over the
// network or from localStorage. Here it is seeded as constants so the page
// runs offline by just opening index.html -- no server, no build.
//
// SAFETY FRAMING: every "attack" here is BENIGN and LOCAL. Payloads call the
// page's reportXss() marker (which turns a row red and logs to the console).
// Nothing makes a network request and nothing touches a real site.
// ============================================================================

// One incoming support ticket, attacker-influenced in several fields.
const INCOMING_TICKET = {
  customer: "Dana Reyes",

  // Plain-text message body -- but it carries an HTML payload. Rendered through
  // an HTML-string sink, the <img onerror> executes.
  message: 'Please help! <img src=x onerror="reportXss(\'message-innerHTML\')">',

  // "Source" link the submitter attached. A javascript: URL is an executable
  // sink when dropped straight into an href.
  sourceUrl: 'javascript:reportXss("source-link-href")',

  // A note that legitimately uses a little rich text (<b>, <a>) -- but also
  // smuggles an <img onerror> and a javascript: link.
  note:
    'Marked <b>urgent</b> — ' +
    '<img src=x onerror="reportXss(\'note-richtext\')"> ' +
    'see <a href="javascript:reportXss(\'note-href\')">details</a>.',

  // Priority weighting. The submitter picked "high" (a legal choice), but the
  // ticket ALSO carries a raw expression the lazy calculator eval()s.
  weighting: "high",
  weightingExpr: '(reportXss("priority-eval"), 3)', // eval'ing this fires + returns 3

  // Ticket status -- should be one of a fixed set.
  status: "open",
};

// Severity used by the priority calculator (a trusted, in-app number).
const SEVERITY = 4;

// Untrusted dashboard settings, as a JSON string (imagine it came from storage
// or a server). It carries a __proto__ key -- a prototype-pollution vector.
const UNTRUSTED_SETTINGS_JSON =
  '{"theme":"dark","pageSize":25,"__proto__":{"isAdmin":true}}';

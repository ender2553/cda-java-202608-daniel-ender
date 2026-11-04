// ============================================================================
// UNTRUSTED INPUT (do not "trust" any of this)
// ----------------------------------------------------------------------------
// Each note below simulates rich-text a user submitted — some legitimate
// formatting mixed with attack payloads. In a real app this would arrive over
// the network or from storage; here it is seeded as constants so the page
// runs offline by just opening index.html — no server, no build.
//
// SAFETY FRAMING: every "attack" here is BENIGN and LOCAL. Payloads either
// call console.warn (harmless, logged) or would open javascript: URLs that do
// nothing dangerous even if clicked. Nothing makes a network request.
// ============================================================================

const UNTRUSTED_NOTES = [
  {
    id: 'note-script',
    label: 'Comment with an inline <script> tag',
    html: 'Great post! <script>console.warn("[dompurify-lab] script tag executed")</script> Thanks for sharing.',
  },
  {
    id: 'note-onerror',
    label: 'Comment with an onerror image',
    html: 'See attached: <img src="x" onerror="console.warn(\'[dompurify-lab] onerror fired\')"> — broken link?',
  },
  {
    id: 'note-js-url',
    label: 'Comment with a javascript: link',
    html: 'Marked <b>urgent</b> — see <a href="javascript:console.warn(\'[dompurify-lab] javascript url followed\')">details</a> for more.',
  },
  {
    id: 'note-formatted',
    label: 'Comment with legitimate rich formatting',
    html: 'This is <b>bold</b>, this is <i>italic</i>, and this is <em>emphasized</em> — visit <a href="https://example.com/docs">our docs</a> for more.',
  },
  {
    id: 'note-mixed',
    label: 'Comment mixing legitimate formatting with an attack',
    html:
      '<strong>Important:</strong> read the <a href="https://example.com/policy">policy</a> first. ' +
      '<img src="x" onerror="console.warn(\'[dompurify-lab] mixed-note onerror fired\')">',
  },
  {
    id: 'note-disallowed-tag',
    label: 'Comment with a disallowed but non-attack tag',
    html: 'Check the table below: <table><tr><td>data</td></tr></table> (formatting the config does not allow).',
  },
];

// ============================================================================
// UNTRUSTED INPUT (do not "trust" any of this)
// ----------------------------------------------------------------------------
// A nested "post submission" object, shaped like something a real form or API
// would hand you: a display name, a profile URL, a markdown-authored body, a
// list of tags, and a nested "meta" object with its own URL and text fields.
// Every leaf value is untrusted and needs a DIFFERENT treatment depending on
// its field type — the exact problem this lesson's reusable utility solves.
//
// SAFETY FRAMING: every "attack" here is BENIGN and LOCAL. Payloads either
// call console.warn (harmless, logged) or point at a no-op javascript: URL.
// Nothing makes a network request.
// ============================================================================

const UNTRUSTED_SUBMISSION = {
  displayName: 'Ada <script>console.warn("[sanitize-utils] displayName script executed")</script>Lovelace',

  profileUrl: 'javascript:console.warn("[sanitize-utils] profileUrl javascript executed")',

  bodyMarkdown:
    '**Great write-up!** Really enjoyed it. ' +
    '<script>console.warn("[sanitize-utils] bodyMarkdown raw script executed")</script> ' +
    'Also see <img src=x onerror="console.warn(\'[sanitize-utils] bodyMarkdown onerror executed\')"> ' +
    'and [this link](javascript:window.__sanitizeUtilsLinkExecuted=true).',

  tags: [
    'security',
    '<img src=x onerror="console.warn(\'[sanitize-utils] tag onerror executed\')">',
    'client-side',
  ],

  meta: {
    authorUrl: '/u/relative-author-42', // relative — should resolve against a base
    notes: 'Reviewed once <script>console.warn("[sanitize-utils] meta.notes script executed")</script>already.',
    avatarDataUrl: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=',
  },
};

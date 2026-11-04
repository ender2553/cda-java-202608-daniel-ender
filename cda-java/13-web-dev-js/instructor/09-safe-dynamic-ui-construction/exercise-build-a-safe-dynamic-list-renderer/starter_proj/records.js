// ============================================================================
// UNTRUSTED INPUT (do not "trust" any of this)
// ----------------------------------------------------------------------------
// Simulates a page of directory records that arrived from an API response —
// each one is untrusted per this module's trust-boundary lesson, even though
// it "comes from our own backend." Seeded as constants so the page runs
// offline by just opening index.html — no server, no build.
//
// SAFETY FRAMING: every "attack" here is BENIGN and LOCAL. A rendering bug
// would either show the payload as literal text (safe) or, if it slipped
// through, call console.warn (harmless, logged, no network call).
// ============================================================================

const DIRECTORY_RECORDS = [
  {
    id: 'rec-1',
    name: 'Ada Lovelace',
    profileUrl: 'https://example.com/u/ada',
    bio: 'Writes about early computing.',
  },
  {
    id: 'rec-2',
    name: '<img src=x onerror="console.warn(\'[list-renderer] name field executed\')">',
    profileUrl: 'https://example.com/u/anon-2',
    bio: 'Plain bio text.',
  },
  {
    id: 'rec-3',
    name: 'Grace Hopper',
    profileUrl: 'javascript:console.warn("[list-renderer] javascript url followed")',
    bio: 'Compiler pioneer.',
  },
  {
    id: 'rec-4',
    name: 'Data URI Example',
    profileUrl: 'data:text/html,<script>console.warn(\'[list-renderer] data url executed\')</script>',
    bio: 'Should fall back to no link.',
  },
  {
    id: 'rec-5',
    name: 'Relative Link',
    profileUrl: '/u/relative-42',
    bio: 'A same-origin relative path, not absolute.',
  },
  {
    id: 'rec-6',
    name: 'Attribute Breakout Attempt',
    profileUrl: 'https://example.com/u/normal" onmouseover="console.warn(\'[list-renderer] attr breakout\')',
    bio: 'The quote in the URL should not become a live attribute.',
  },
];

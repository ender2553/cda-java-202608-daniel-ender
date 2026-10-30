# Instructor Guide — XSS Forum Demo

**Audience:** instructor and TAs running this as a live classroom session.

| Course | Lesson |
|---|---|
| TBD | TBD |

> Keep this file out of any student-facing handout. It contains the answer
> key for `PAYLOAD_WALKTHROUGH.md` §9–10.

Students work from `README.md` (setup) and `PAYLOAD_WALKTHROUGH.md`
(exercises). This guide maps each phase to README and walkthrough sections,
so you can keep both open side by side with this guide.

---

## 1. At a glance

**Learning objectives.** By the end of the session, students can:

1. Explain why a `<script>` tag inserted via `innerHTML` does not execute,
   and why that behavior is not a defence against stored XSS.
2. Demonstrate that a stored payload re-fires for every user who views the
   page, not just the user who posted it.
3. Explain why the forum's cookie-theft payload succeeds.
4. Explain why impersonation works here — the server never reads cookies at
   all; `forum.js` reads the `forum_user_id` cookie and sends it as `userId`
   in the POST body, and the controller trusts it with no authentication
   check — and what the server would need to do instead.
5. Identify the single code change (output encoding — `textContent` instead
   of raw `innerHTML` concatenation) that stops every exercise in the
   walkthrough at once.

**Prerequisites students need:** comfort with HTML, JavaScript, and
browser DevTools (Elements, Console, Application tabs). No prior security
background required.

**What to set up before class.** Run the whole lab yourself once, start to
finish, before the session:

- Confirm your own machine, and ideally each student's machine, has JDK 21+,
  Maven, and PostgreSQL 16+ installed, and that `README.md` §1–3 works end
  to end (database created, schema loaded, both servers start,
  `http://localhost:8080` and `http://localhost:9090` load).
- Walk all of PAYLOAD_WALKTHROUGH.md §1–8 yourself so you know what "it
  worked" looks like on your machine and browser.
- Reset with `http://localhost:8080/reset.html` so you start class clean.
- Ask students to run the README setup themselves before class if at all
  possible. The first Maven download (`mvn spring-boot:run`) pulls down
  libraries and can be slow on a fresh machine or a slow connection — doing
  that once ahead of time avoids losing class time to it.
- Decide in advance how students will run "two users at once" (README.md
  §4 tip / Walkthrough §0): a second browser profile, an InPrivate/Incognito
  window, or a second browser. Tell them which to use before setup begins.

---

## 2. Session flow

This session has three phases. Pace them to your own class length and
group.

### Phase 1 — Get everyone running it (README)

Walk the room through `README.md` §1–3: prerequisites, database setup, and
starting both servers. Use the success cues in README §1–3 to confirm each
student is actually ready before moving on:

- `java -version` reports 21 or higher; `mvn -version` runs.
- `CREATE DATABASE` prints after creating the database.
- The schema load prints `CREATE TABLE` / `INSERT` lines with no `ERROR`
  lines.
- `http://localhost:8080` shows the login page.
- `http://localhost:9090` shows "No captures yet."

A missing prerequisite here stalls the whole room later, so don't move on
until everyone is green.

### Phase 2 — Instructor demo

Rather than walking every section of the walkthrough as a group, pick a few
exercises to demo live and let students read the rest on their own in
Phase 3. Good candidates, in roughly the order they build on each other:

- **Walkthrough §2–3** — post the `<script>` payload first (nothing
  happens), then the `<img onerror>` payload (an alert fires). The contrast
  is the fastest way to land "stored data reaching `innerHTML`, not a
  specific tag, is the bug."
- **Walkthrough §5** — one or two defacement payloads (the heading rewrite
  or the full-body replacement) make a strong visual demo of "a comment just
  took over the whole page," then recovering with `/reset.html`.
- **Walkthrough §6–7** — cookie theft followed by impersonation is the
  core "aha" moment of the lab: a comment silently exfiltrates another
  user's cookies, and those values alone are enough to post as them.

Narrate the "why it works" explanation for each one as you demo it — the
walkthrough has these written out, so you can read along or paraphrase.

### Phase 3 — Student experimentation and debrief

Let students work through `PAYLOAD_WALKTHROUGH.md` on their own (or in
pairs), starting wherever they like, using it as a self-contained guide.
Circulate rather than lecture. Remind them to reset
(`http://localhost:8080/reset.html`) whenever the forum gets confusing, and
that nothing here needs to leave `localhost`.

Close with a short debrief using Walkthrough §9 (spot the fix) and §10
(reflection questions). Use the answer key in Section 4 of this guide. Have
students propose the §9 fix themselves before revealing it — the
walkthrough's hint ("it's about *how* text gets inserted... not about
blocking specific words") is designed to point them there without stating it
outright. §9 is a discussion, not a hands-on exercise — there is no "now go
apply the fix" step; reasoning about the fix is the goal here, not writing
the patched code.

---

## 3. Per-section talking points and expected results

### Phase 1 — Setup (README §1–3)

- **Expected result:** `mvn spring-boot:run` ends with Spring Boot's
  "Started" line and `http://localhost:8080` shows the login page.
  `java attacker/AttackerServer.java` prints "Attacker listener on
  http://localhost:9090/..." and the page at `:9090` shows "No captures
  yet."

### Walkthrough §1 — Look at the vulnerable code

- Walk through the two code excerpts together: the `renderComments` loop
  (`THE DELIBERATE VULNERABILITY`) and the `whoami` line (`VULNERABLE ON
  PURPOSE`). Ask the "before you continue" question out loud and collect a
  few guesses before revealing the answer.
- **Expected result:** students can point to the exact two lines in
  `forum.js` where `c.userName`, `c.commentText`, and `userName` are
  concatenated straight into an HTML string with no escaping.

### Walkthrough §2–4 — First payloads

- **§2 (`<script>alert(1)</script>`).** **Expected result:** page reloads,
  no alert, and DevTools → Elements shows the literal text
  `<script>alert(1)</script>` sitting inside `<div class="text">` as dead
  markup.
  - **Why `<script>` doesn't run, and why that isn't a defence:** per the
    HTML spec, a `<script>` element created by `innerHTML` is parsed into
    the DOM but the browser deliberately refuses to execute it. This is a
    historical parsing quirk, not a security control anyone added on
    purpose. The comment was still stored completely unescaped — the server
    has no idea whether it stored a sentence or an attack. Say this
    explicitly: a student who stops at "script didn't run, so we're safe"
    has the wrong takeaway, and §3 proves it.
- **§3 (`<img src=x onerror=alert(1)>`, `<svg onload=alert(1)>`).**
  **Expected result:** an alert box reading `1` pops immediately after the
  page reloads. Reloading (F5) fires it again. Switching to a second user
  and opening the same topic fires it for them too, without them ever
  typing the payload.
  - **Why it re-fires on reload and for other users:** the payload is
    sitting in the database as the comment's text, not just in one
    browser's memory. Every page load that renders that comment re-inserts
    the same malicious HTML into the DOM. This is the definition of
    *stored* XSS: one post attacks every future visitor, not just the
    poster.
- **§4 (username payload).** **Expected result:** the alert fires as soon
  as the forum page loads (from the `#whoami` line), and — after posting a
  comment with that payload as the username — fires again once per comment
  next to the `.author` span. Point out this hits the *other* vulnerable
  line (`whoami.innerHTML = 'Logged in as <strong>' + userName + ...`),
  confirming the bug is a pattern (unescaped concatenation into
  `innerHTML`), not a single typo.
  - If alerts get annoying, the walkthrough tells students to dismiss them
    and either use "Switch user" or `/reset.html` — point this out if
    someone looks stuck in an alert loop.

### Walkthrough §5 — Defacement

- Post each defacement payload as a separate comment, reload, and ask
  students to describe in their own words what changed and why a normal
  comment shouldn't be able to do that.
- **Expected result:** each payload changes the page heading text, hides
  all comments, inverts the page's colors, or replaces the entire page
  body — in each case using `onerror` to run arbitrary JavaScript with the
  same `document` access the app's own code has. There is no sandbox
  separating "a user's comment" from "the page itself."
- Demonstrate `http://localhost:8080/reset.html` recovering from the worst
  one (the full-body replacement) before moving on.

### Walkthrough §6–7 — Cookie theft and impersonation

- **§6.** Post the cookie-theft payload as one user, then load the same
  topic as a second user, then open `:9090`.
  - **Expected result / captured data format:** a new row appears on the
    attacker page with a **Data** value that reads exactly:
    `c=forum_user_id=2; forum_user_name=bob` — the victim's raw
    `document.cookie` string, URL-decoded, with the query parameter name
    `c=` still attached to the front. Tell students to ignore the `c=` when
    they copy values out of it for §7.
  - **Instructor note (not in the student docs):** the **From (Referer)**
    column only shows `http://localhost:8080/`, not the full forum URL
    with the topic ID. That's the browser's default
    `strict-origin-when-cross-origin` Referer policy stripping the path on
    a cross-origin request — it's not something this app's code controls
    and not a limitation of the attack. Mention it only if a student
    notices and asks; it isn't part of the learning objectives.
  - Note for students who ask "why did this work at all": the forum never
    marks its cookies `HttpOnly`, so `document.cookie` can read them.
- **§7.** Using the captured `forum_user_id`/`forum_user_name` values
  (minus the `c=` prefix), edit cookies via DevTools → Application →
  Cookies on a different window, then reload.
  - **Expected result:** the header reads "Logged in as bob," and a new
    comment posted from that window is saved and displayed as if `bob`
    wrote it.
  - **Why impersonation works:** the server never reads cookies at all —
    `CommentController` has no idea cookies exist. `forum.js` reads the
    `forum_user_id` cookie and sends it as `userId` in the JSON body of the
    `POST` to `/api/topics/{topicId}/comments`; the controller passes that
    value straight through with no check that it belongs to whoever is
    actually at the browser. The "Logged in as bob" text comes from a
    separate cookie, `forum_user_name`, rendered with `innerHTML`. Nothing
    ties the two together or verifies either one server-side.
  - **Follow-up step and expected result:** the walkthrough has students go
    further — change `forum_user_id` to a different small integer they
    never stole (e.g. `1`, `3`, `4`), reload, and post. **Expected result:**
    the comment is saved and credited (once reloaded) to whichever real
    user owns that id, demonstrating that no theft was even necessary — any
    plausible small integer works. **Talking point:** make sure students
    say *why*: the browser's own JavaScript reads the cookie and sends it
    as `userId` with no authentication, so the value doesn't need to be
    "stolen," just "plausible."

### Walkthrough §8 — Free play

- Let students choose from the four starting points in §8 (keylogger, fake
  login overlay, auto-post as the viewer, redirect). Circulate rather than
  lecture; this is meant to build confidence combining what they've already
  seen.
- Remind students to reset (`/reset.html`) between attempts if the forum
  gets confusing, and that nothing here needs to leave `localhost`.

### Walkthrough §9–10 — Debrief

- Use the answer key in Section 4 of this guide below.
- Make room in the §9–§10 discussion to preview that production apps
  layer additional defences (CSP, `HttpOnly`, server-side validation) on
  top of output encoding — the walkthrough mentions these but doesn't
  implement them, and several of the new §10 questions ask students to
  reason about specific defences in new scenarios.

---

## 4. Answer key

### §9 — Spot the fix

**Question:** what is the one change to `forum.js` — to the lines marked
`THE DELIBERATE VULNERABILITY` and `VULNERABLE ON PURPOSE` — that would
stop every exercise in this walkthrough at once?

**Model answer:** stop building HTML by string concatenation and assigning
it with `innerHTML`. Instead, render user-controlled values (`c.userName`,
`c.commentText`, `userName`) as text, not markup — for example, create the
elements with `document.createElement` and set the user-controlled parts
with `.textContent` (the same pattern `forum.js` already uses safely in
`showCommentsMessage` and `showTopicsMessage` for static messages), or
run any user-controlled string through an HTML-escaping function before
concatenating it into markup. The attacker page's own `escape()` helper in
`AttackerServer.java` is the model for the second approach: it replaces
`&`, `<`, `>`, `"`, and `'` with their HTML entities (in that order, with
`&` first, so entities aren't double-escaped) before writing captured data
into its page, so anything a payload sent displays as plain text instead
of running. Either approach — `textContent` or equivalent escaping — means
the browser can no longer confuse a comment or a username for markup, which
is what every single exercise in this walkthrough depends on.

Separately, and only worth raising if it comes up: the §7 impersonation
problem isn't fixed by output encoding at all. That requires the server to
determine the current user from its own session rather than trusting a
client-supplied `userId` — see question 6 below and the §7 "why it works"
explanation.

### §10 — Reflection questions

**1. Suppose this forum added a "Direct Message" feature between users,
rendered with `innerHTML` the same way as comments, but the code trims and
HTML-escapes the message body before saving it to the database. A teammate
says DMs are now safe from XSS. What's one test message you'd try against
this new feature before agreeing with them, and what exactly would you be
checking for?**

Try an HTML/event-handler payload, e.g. `<img src=x onerror=alert(1)>`, as
the DM body and view it as the recipient. You're checking two separate
things: first, that the escaping actually ran and the characters really
were turned into entities (not just trimmed), and second — more
importantly — that *every* place the message gets rendered does the
escaping, not just the main inbox view. A notification banner, an email
preview, a "last message" snippet on a contacts list, or a moderation
dashboard might all render the same raw value through a different code path
that was never updated. Escaping once at storage time doesn't guarantee
every renderer respects it; the safer pattern is to escape (or use
`textContent`) at every render site, not to "clean" the data once on the
way in.

**2. You're reviewing a teammate's code and see this line, where
`displayName` comes from a signup form:
`document.getElementById('welcome').innerHTML = 'Hi, ' + user.displayName;`
List the specific questions you'd ask the author before approving it.**

Questions worth asking: Can a user set `displayName` to arbitrary text, and
is there any length/character restriction on signup? Is `displayName`
escaped or sanitized anywhere before this line runs, and if so, where? Does
this line actually need to insert HTML at all, or would `textContent` (or
building the node and setting `.textContent`) work just as well, given that
"Hi, <name>" doesn't need any markup? Has this been tested with a payload
like `<img src=x onerror=alert(1)>` as the display name? And is this value
rendered anywhere else in the app — a profile page, a leaderboard, an admin
view — that would need the same fix if this one does?

**3. Suppose the team adds a Content-Security-Policy header that blocks
inline event-handler attributes (so `onerror="..."` and `onload="..."` stop
working), but still allows `<script src="...">` from a short list of trusted
domains. Which of this walkthrough's exercises would that CSP stop, and
which — if any — could still work around it?**

As written, every payload in this walkthrough from §3 onward depends on an
inline event-handler attribute (`onerror` or `onload`), so this CSP would
stop all of them as they're written here. Section 2's `<script>` payload
was already inert for an unrelated reason (the HTML-parsing quirk), so CSP
doesn't change anything about it either way. The important caveat: this CSP
only blocks *inline* handlers — it does not fix the underlying bug
(unescaped data reaching `innerHTML`). If an attacker could get a `<script
src="...">` pointing at one of the trusted domains inserted into the page
(for example, if one of the trusted domains also allows user-uploaded
content, or the CSP accidentally includes `'unsafe-inline'` as a fallback),
a working payload would still exist. CSP here is mitigating the *impact* of
this specific style of payload, not closing the hole that lets arbitrary
HTML into the page at all.

**4. Suppose the forum's cookies were changed to be `HttpOnly`. Walk back
through Sections 6 and 7 in your head: which specific step would now fail,
and which steps would still work exactly as before? Why?**

Section 6 step 1 would fail: `document.cookie` would no longer include the
`HttpOnly` cookies at all, so there would be nothing meaningful to steal
through that payload.

Section 7 is the interesting part. `HttpOnly` only blocks *JavaScript* from
reading a cookie; the cookie is still visible and editable in DevTools →
Application → Cookies. But in this forum, the page's own `forum.js` reads
both cookies with JavaScript (`getCookie`, which uses `document.cookie`) to
fill in "Logged in as…" and to put `userId` in the POST body. If the cookies
were `HttpOnly`, the forum itself would break: the header would be empty and
posts would go out with no `userId`. Editing the cookie in DevTools would no
longer impersonate anyone, because the page can't read it either.

The teaching point: `HttpOnly` isn't a flag you flip while everything else
keeps working. To use it, the server has to issue the cookie and identify
the user from it, and `forum.js` has to stop reading it. Done properly, that
also closes Section 7's follow-up (guessing another user's id), because the
server would decide who you are instead of trusting a `userId` from the
browser.

> **Instructor note (not in the student docs):** these cookies are set by
> JavaScript in `common.js` (`document.cookie = ...`), so they *can't* be
> made `HttpOnly` as-is — a cookie set via client-side JavaScript cannot
> carry the `HttpOnly` flag, since `HttpOnly` only has meaning for cookies
> the *server* sets via a `Set-Cookie` response header. The real fix isn't
> "add a flag to the existing cookie" — it's for the server to stop relying
> on client-writable cookies altogether and instead issue its own
> server-signed session cookie (set server-side, with `HttpOnly`) that the
> client cannot read or forge. If a student's answer is "just add
> `HttpOnly` to the cookie," push them to notice who is setting the cookie
> in the first place.

**5. A different app lets users upload a profile picture and stores the
uploaded filename, later displaying it next to their posts using the same
unescaped-concatenation pattern this forum uses for `c.userName`. Is this a
stored or a reflected XSS risk, and why? What would you try as a "filename"
to test it?**

Stored: the filename is saved (in a database or file system record) and
re-rendered to anyone who later views that user's posts, the same way
`c.commentText` and `c.userName` are stored and re-rendered here — it isn't
reflected once in a single response and gone. To test it, try naming the
upload something like `<img src=x onerror=alert(1)>.png` (or without the
extension, if the app doesn't enforce one) and see whether it executes
wherever the filename is displayed.

**6. Imagine the server started checking that the `userId` sent with a new
comment must exist in the `users` table before accepting the post — but it
didn't check that the id belongs to whoever is actually making the
request. Would that stop the technique from the end of Section 7? Why or
why not?**

No. That check only confirms the id is a real row in `users` — exactly the
condition that made the end-of-§7 technique (guessing a small, plausible
integer) work in the first place. It still has no way to tell whether the
`userId` in the request actually belongs to the browser making the request,
because there is no session binding at all. The fix has to be the server
determining the user from its own session (something the client cannot set
or edit) and ignoring any `userId` the client supplies, not validating that
the supplied id merely exists.

---

## 5. Classroom management & safety

- Both servers bind to `127.0.0.1` only — nothing is reachable from
  another machine on the network. Say this once at the start so students
  aren't worried about "hacking" anything beyond their own laptop.
- All forum data (users, topics, comments) is fake seed data created for
  this lab. There is nothing sensitive to protect and nothing to avoid
  breaking.
- Remind students explicitly, once, out loud: **do not reuse this code and
  do not try any of these payloads against a real website** — including
  course tools, their own other projects, or anything not running on their
  own `localhost`. This app is deliberately vulnerable on purpose and is
  not a pattern to carry forward.
- **Recovering a wrecked forum:**
  - `http://localhost:8080/reset.html` is always reachable, even if a
    payload has defaced or hidden the forum page itself, and restores the
    seeded users/topics/comments.
  - If that page itself is somehow unreachable, fall back to re-running
    the schema script directly: `psql -U postgres -d xss_demo -f
    src/main/resources/db/schema.sql` (same command as setup).
  - Restarting the forum app keeps posted payloads (they're in the
    database) — only a reset or schema reload clears them. Restarting the
    attacker listener clears its captured list immediately (in-memory
    only).
- **Two browser profiles:** decide ahead of time whether students use a
  second browser profile, an InPrivate/Incognito window, or a second
  browser entirely for the "two users" exercises (Walkthrough §3, §6–7),
  and say so before Phase 1 setup — regular tabs in the same profile share
  cookies and will fight each other.

---

## 6. FAQ / troubleshooting for instructors

| Symptom | Likely cause / fix |
|---|---|
| Port 8080 or 9090 already in use | Another process (or an earlier copy of this app) is still running. Close it, then restart. Find it with `netstat -ano \| findstr :8080` (or `:9090`) in PowerShell. |
| `password authentication failed for user "postgres"` | The student's local postgres password isn't `postgres`. Set `DB_PASSWORD` in the terminal before starting the app (README §2), then restart the app in that same terminal. |
| `psql` is not recognized | `psql` isn't on PATH. Use the full path to `psql.exe` shown in README §2, or add PostgreSQL's `bin` folder to PATH. |
| "My capture never showed up" on `:9090` | Almost always an unencoded `%`, `&`, or `#` character in a GET-based payload's query string — a raw `%` makes the JDK `HttpServer` reject the request with 400 before it's ever captured, and an unencoded `&` or `#` can cut the data short even when the request succeeds. Fix: wrap the exfiltrated data in `encodeURIComponent(...)` (the §6 payload already does this — check for typos), or switch to a `fetch(...)` `POST`, which avoids the encoding issue entirely since data travels in the body. |
| Payload didn't fire at all | Usually a quoting mistake — mixed single/double quotes inside an `onerror="..."` attribute breaks the attribute and the browser silently fails to parse it. Have the student copy the payload from `PAYLOAD_WALKTHROUGH.md` exactly rather than retyping it. |
| `<script>` payload "didn't run" | Expected — see Walkthrough §2. This is the HTML-parsing quirk, not a bug in the lab. Point them to §3 for a payload that does execute. |
| Some payloads (e.g., the `#whoami` one) "didn't do anything" | Some payloads only fire on page load, not the instant a comment is posted. Confirm the student reloaded the page after posting. |
| Restarting the app vs. the listener | Restarting the forum app (`mvn spring-boot:run`) keeps all posted payloads — they're stored in the database. Restarting the attacker listener (`java attacker/AttackerServer.java`) clears its captured list — it's in-memory only. If a demo's captures vanish mid-class, the listener was restarted, not the forum. |

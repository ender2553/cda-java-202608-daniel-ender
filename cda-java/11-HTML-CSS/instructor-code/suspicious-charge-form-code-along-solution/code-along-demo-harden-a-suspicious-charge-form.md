---
title: "Harden a Suspicious-Charge Form"
type: code-along
module: "Module 3: Secure Forms & Input-Validation Attributes; Module 4: Content Security Policy & Web Security Headers"
lesson: "Lessons 13–16: Secure Form Methods & Submission; Accessible, Trustworthy Form UX; Why Client-Side Defense Needs Headers; Content Security Policy via Meta Tag"
course: "Introduction to HTML & CSS — Secure"
created: 2026-10-27
last_updated: 2026-10-27
status: draft
---

# Harden a Suspicious-Charge Form

## What we'll build

You'll take one rough form, a bank's "Report a Suspicious Charge" page, and harden it in four parts, one per lesson:

| Part | Lesson | What changes |
|---|---|---|
| 1 | 13: Secure Form Methods & Submission | The verification code stops leaking into the URL, the file upload actually sends the file, and the form names its trust boundary. |
| 2 | 14: Accessible, Trustworthy Form UX | Placeholder-only fields get real labels, required cues, help text, and error containers that a screen reader will announce. |
| 3 | 15: Why Client-Side Defense Needs Headers | You inspect the page as the browser receives it and find what markup alone can't stop. |
| 4 | 16: Content Security Policy via Meta Tag | You add a policy that stops the page's inline script from running, without breaking anything legitimate. |

You'll work on the same `report.html` file the whole way through, and every change gets tested in the browser right after you make it.

## Setup

1. Open the `suspicious-charge-form-code-along` folder in VS Code. You'll see:

   | File | Purpose |
   |---|---|
   | `site/report.html` | **The file you'll edit.** The form we're hardening. |
   | `site/styles.css` | Provided styles. You'll make one small edit in Part 4. |
   | `site/frame-check.html` | Provided page used in Part 3. |
   | `site/logo.svg` | The bank's logo. |
   | `serve.py` | A tiny local web server. You don't need to read it. |
   | `sample-receipt.png` | A small image to attach when testing the file upload. |

2. Open a terminal in that folder and start the server:

   ```text
   python serve.py
   ```

   (On Windows, if `python` isn't found, try `py serve.py`.) Leave this terminal open and visible. It prints one line for every request the browser makes, exactly like a real server log.

3. In Chrome or Edge, go to **http://localhost:8000/report.html** and open DevTools (F12). You'll use the **Console** and **Network** tabs.

**If something goes wrong**

- **A change doesn't show up:** save the file (Ctrl+S, or Cmd+S on a Mac) and reload `report.html`, not the results page.
- **`python` isn't found:** use `py serve.py` on Windows or `python3 serve.py` on macOS/Linux.
- **"Address already in use":** run `python serve.py --port 8080` and use `http://localhost:8080/report.html` instead.
- **Don't double-click `report.html`** to open it. A `file:///` address can't submit to the server. Always use the `http://localhost` address.

> The server answers form submissions at `/submit` with a page called **What the server received**. It shows the method used, whether the fields arrived in the URL or in the request body, and every name/value pair. That page is how you'll check each change.

## Build steps

### Part 1: Submit securely (Lesson 13)

1. **Submit the form as-is and find the leak**

   Fill in every field (use `123456` as the verification code), attach `sample-receipt.png`, and click **Send report**. Then look in three places:

   - **The address bar:** `.../submit?email=...&code=123456&amount=...`
   - **The terminal running `serve.py`:** `"GET /submit?email=sam%40example.com&code=123456&... HTTP/1.1"`
   - **Your browser history:** the same URL, saved.

   The form uses `method="get"`, so every value travels in the URL's query string. URLs are kept in browser history, in server logs, and in the `Referer` header sent to other sites. A secret in a URL leaks quietly, and it stays leaked.

2. **Switch the method to `POST`**

   ```html
   <form action="/submit" method="post">
   ```

   Go back, reload, and submit again. The address bar now ends at `/submit`, and the terminal log line is just `"POST /submit HTTP/1.1"`. To find the values, open **Network**, click the `submit` request, and check its **Payload** tab: they're in the request body. Rule of thumb: **if the data is sensitive or changes something, use `POST`.**

3. **Check the file upload, then fix it with `enctype`**

   Look at the `receipt` row on **What the server received**. It shows only `sample-receipt.png`, the file's *name*. Not one byte of the image arrived. The default encoding (`application/x-www-form-urlencoded`) sends text only. Add the encoding a file upload needs:

   ```html
   <form action="/submit" method="post" enctype="multipart/form-data">
   ```

   Submit again. The `receipt` row now reads `File sample-receipt.png — 345 bytes of file content received`.

4. **Tell the browser how to treat each field with `autocomplete`**

   ```html
   <input type="email" name="email" autocomplete="email" placeholder="Email address" required>
   ```

   ```html
   <input type="text" name="code" placeholder="Verification code" inputmode="numeric" autocomplete="one-time-code"
          required pattern="[0-9]{6}" maxlength="6">
   ```

   `email` helps an honest user fill in their address faster. `one-time-code` marks the field as a short-lived secret, so the browser shouldn't offer old codes back, and phones can suggest the code from the text message. You won't see a visible change here: the browser and the phone handle `autocomplete` behind the scenes. The point is that you made a deliberate choice for each field instead of accepting the default.

5. **Read the `action` and think about HTTPS**

   You don't need to type anything for this step. `action="/submit"` is a *relative* URL: it uses the same scheme and host as the page. If this page is served over `https://`, the form submits over `https://` too.

   Now picture a page served over `https://` with a hard-coded `action="http://..."`. That's **mixed content**. One unencrypted step undoes the encryption around it, so browsers warn about it or block it. Keep in mind that HTTPS protects data *in transit*. It does nothing for data *quality*. That's validation's job, and a secure form needs both.

6. **Name the trust boundary**

   Add this comment directly above the `<form>` tag:

   ```html
   <!-- TRUST BOUNDARY: when this form submits, everything in it leaves the browser.
        The server must re-check every field and confirm the request really came
        from this site (CSRF defenses live on the server). -->
   ```

   A submission is the moment one party's input becomes another party's instruction. A malicious site could even trick a logged-in user's browser into submitting a form like this one. That attack is CSRF (cross-site request forgery). Its defenses are server-side and beyond this course, but naming the boundary is a habit worth building now.

### Part 2: Make it usable and trustworthy (Lesson 14)

7. **Feel the placeholder problem, then add real labels**

   Reload the page, type an email address, and look away for a second. Can you still tell what the field is for? The placeholder vanished as soon as you started typing, and many screen readers don't announce it reliably. Give every field a `<label>` connected with matching `for`/`id` values:

   ```html
   <div class="field">
     <label for="email">Email address</label>
     <input type="email" id="email" name="email" autocomplete="email" required>
   </div>
   ```

   Do the same for the other four fields. Use these labels: **Verification code**, **Charge amount in dollars**, **What happened? (optional)**, **Screenshot or receipt (optional)**. Delete the placeholders except on the textarea. There, change it to an example: `placeholder="For example: I don't recognize this store."` A placeholder can *add* an example, but it can never *be* the label.

8. **Add required cues**

   Add one line at the top of the form, and an asterisk to each required field's label:

   ```html
   <p class="form-intro">Fields marked with an asterisk (*) are required.</p>
   ```

   ```html
   <label for="email">Email address <span class="req" aria-hidden="true">*</span></label>
   ```

   `required` *enforces* the rule. The visible cue *communicates* it. A field needs both. Use one convention for the whole form: every required field gets the asterisk, and every optional field says "(optional)" in its label. The cue uses `aria-hidden="true"` so screen readers don't announce a stray "star", because the input's `required` attribute already tells them.

9. **Give format help before the mistake, linked with `aria-describedby`**

   ```html
   <input type="text" id="code" name="code" inputmode="numeric" autocomplete="one-time-code"
          required pattern="[0-9]{6}" maxlength="6"
          aria-describedby="code-help">
   <p id="code-help" class="hint">The 6-digit code we just texted to the phone number on your account.</p>
   ```

   `aria-describedby` points to the help text's `id`, so assistive technology reads that text as part of the field's description. Add help text the same way for the amount (`amount-help`: "Enter the amount exactly as it appears on your statement, for example 42.50.") and the receipt (`receipt-help`: "PNG, JPEG, or PDF.").

10. **Prepare an announced error container**

    Click **Send report** with the form empty. The browser moves focus to the first invalid field and shows its own "Please fill out this field." bubble. After that, the only lasting sign of the problem is a red border, and anyone who can't see color gets nothing from it. Add an error container with `role="alert"` and list its `id` in `aria-describedby`:

    ```html
    <input type="text" id="code" name="code" inputmode="numeric" autocomplete="one-time-code"
           required pattern="[0-9]{6}" maxlength="6"
           aria-describedby="code-help code-error">
    <p id="code-help" class="hint">The 6-digit code we just texted to the phone number on your account.</p>
    <p id="code-error" class="error" role="alert"></p>
    ```

    `role="alert"` makes the paragraph a *live region*: any text that lands in it later gets read aloud, not just shown. Putting the message in at the moment validation fails takes JavaScript, which comes in a later course. Today you build the honest, empty, already-linked container, so it's ready for that. `aria-describedby` now lists two ids: the help text and the error container. Do the same for email (`email-error`) and amount (`amount-error`).

    In DevTools, open **Elements → Accessibility** and select the code field. Its **Name** comes from the label, and its **Description** comes from the help text.

    *Optional: hear it.* On Windows, press **Ctrl + Windows + Enter** to turn Narrator on, then press Tab to move through the fields. You'll hear each label, whether it's required, and its help text, and the upload button now says what it's for instead of just "Choose File". Press the same keys to turn Narrator off. On a Mac, use VoiceOver (**Cmd + F5**).

    **When this container is filled, what should it say?** Decide, and be ready to say why, before you read on.
    - *"Error 0x5: field `otp_code` failed regex validation"*
    - *"Invalid"*
    - *"Enter the 6-digit code from our text message, using numbers only."*

    *Answer, after you've decided:* the third. It tells the user exactly how to fix the problem and tells an attacker nothing about how the system works. The first leaks internal details, and the second gives no fix.

**Checkpoint: your form after Part 2**

```html
<!-- TRUST BOUNDARY: when this form submits, everything in it leaves the browser.
     The server must re-check every field and confirm the request really came
     from this site (CSRF defenses live on the server). -->
<form action="/submit" method="post" enctype="multipart/form-data">
  <p class="form-intro">Fields marked with an asterisk (*) are required.</p>

  <div class="field">
    <label for="email">Email address <span class="req" aria-hidden="true">*</span></label>
    <input type="email" id="email" name="email" autocomplete="email" required
           aria-describedby="email-error">
    <p id="email-error" class="error" role="alert"></p>
  </div>

  <div class="field">
    <label for="code">Verification code <span class="req" aria-hidden="true">*</span></label>
    <input type="text" id="code" name="code" inputmode="numeric" autocomplete="one-time-code"
           required pattern="[0-9]{6}" maxlength="6"
           aria-describedby="code-help code-error">
    <p id="code-help" class="hint">The 6-digit code we just texted to the phone number on your account.</p>
    <p id="code-error" class="error" role="alert"></p>
  </div>

  <div class="field">
    <label for="amount">Charge amount in dollars <span class="req" aria-hidden="true">*</span></label>
    <input type="number" id="amount" name="amount" required min="0.01" step="0.01"
           aria-describedby="amount-help amount-error">
    <p id="amount-help" class="hint">Enter the amount exactly as it appears on your statement, for example 42.50.</p>
    <p id="amount-error" class="error" role="alert"></p>
  </div>

  <div class="field">
    <label for="details">What happened? (optional)</label>
    <textarea id="details" name="details" rows="4" maxlength="1000"
              placeholder="For example: I don't recognize this store."></textarea>
  </div>

  <div class="field">
    <label for="receipt">Screenshot or receipt (optional)</label>
    <input type="file" id="receipt" name="receipt" accept="image/png,image/jpeg,application/pdf"
           aria-describedby="receipt-help">
    <p id="receipt-help" class="hint">PNG, JPEG, or PDF.</p>
  </div>

  <button type="submit">Send report</button>
</form>
```

### Part 3: See what markup can't stop (Lesson 15)

You won't edit any code in this part. You'll just look.

11. **Read the response headers**

    In **Network**, reload and click `report.html`, then open **Headers → Response Headers**. The server sent headers like `Content-Type`, `Content-Length`, `Cache-Control`, `Date`, and `Server`. These are name/value pairs that travel *alongside* the page. There's no `Content-Security-Policy` header and no `X-Frame-Options` header, so nothing tells the browser what this page may run or who may embed it.

12. **Find the script that shouldn't be there**

    Check the **Console**: `Inline script ran. If an attacker had injected this, it would have run too.` Find that `<script>` inside the "Member notice" section of `report.html`. It stands in for untrusted content that slipped into the page. Your markup is well-formed, labeled, and validated, and the browser still runs whatever script the page contains. Correct markup is *necessary but not sufficient*.

13. **Frame the page from somewhere else**

    Open **http://localhost:8000/frame-check.html**. Your form loads inside another page's `<iframe>`. Nothing in `report.html` can prevent that, and a hostile site could use it to trick people into clicking things they can't see (clickjacking). Framing control can only be set by a response header. Lesson 17 covers that.

    Here's the picture to keep: **content** (your HTML) describes the page, and **policy** tells the browser what that content is allowed to do. Validation constrains *input*. Policy constrains the *page*. That's two layers, each covering gaps the other leaves.

### Part 4: Add a Content Security Policy (Lesson 16)

14. **Add a policy in the wrong place**

    Paste this into `<head>` directly *below* the `</style>` line:

    ```html
    <meta http-equiv="Content-Security-Policy" content="default-src 'self'">
    ```

    Reload. The inline script is now refused. The Console says `Executing inline script violates the following Content Security Policy directive 'default-src 'self''...` and adds a note that `'script-src' was not explicitly set, so 'default-src' is used as a fallback`. But the Member notice is *still yellow*. That color comes from the inline `<style>` block above the meta tag, which the browser read before it saw the policy. A CSP meta tag only governs what comes *after* it.

15. **Move it to the top of `<head>`**

    Move the meta tag so it sits directly under `<meta charset="utf-8">`. Reload. The notice loses its yellow, and the Console now reports `Applying inline style violates the following Content Security Policy directive 'default-src 'self''...` as well as the blocked script. Meanwhile `styles.css` and `logo.svg` still load (the page keeps its layout and the logo shows), because `'self'` allows files from this site (same scheme, host, and port).

16. **Build the policy directive by directive**

    Add one piece at a time to the `content` value. Save, and **predict what will change** before each reload.

    1. Add `; script-src 'self'; style-src 'self'`. Reload: nothing changes. These say explicitly what `default-src` was already doing for scripts and styles.
    2. Add `; img-src 'self'`. Reload: the logo still shows, because it comes from this site.
    3. Add `; object-src 'none'`. Reload: nothing visible changes. This page never used plugins, so the door it closes was never open.

    Your finished policy:

    ```html
    <meta http-equiv="Content-Security-Policy"
          content="default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'; object-src 'none'">
    ```

    | Directive | What it controls |
    |---|---|
    | `default-src 'self'` | The fallback for any resource type not named below. |
    | `script-src 'self'` | Scripts: only files from this site, no inline scripts. These are what an attacker most wants to run. |
    | `style-src 'self'` | Stylesheets: only files from this site, no inline styles. |
    | `img-src 'self'` | Images (including our logo): only from this site. |
    | `object-src 'none'` | No `<object>`/`<embed>` plugins at all. It costs this page nothing. |

    Directives are separated by semicolons. Each one is a name followed by a space-separated list of allowed sources. On this page the result is the same as `default-src` alone, but now anyone reading the policy can see the intent.

    `'self'` and `'none'` need their single quotes. Without them, the browser reads the word as a host name.

17. **Undo it with one keyword, then put it back**

    Change the `script-src` part to `script-src 'self' 'unsafe-inline'`. Predict: will the inline script run? Then reload. The inline script runs again. `'unsafe-inline'` re-allows exactly the inline script a CSP exists to block. Remove `'unsafe-inline'` and reload to confirm the script is refused again.

18. **Fix the style properly: move it to a file**

    The yellow notice is real styling we want, so don't reach for `'unsafe-inline'`. Delete the whole `<style>...</style>` block from `report.html` and add the rule to the end of the `.notice` section in `styles.css`:

    ```css
    /* Moved here from the inline <style> block so the page needs no 'unsafe-inline'. */
    .notice { background: #fff4ce; border-left: 6px solid #b58900; }
    ```

    Reload. The notice is yellow again, and the policy is still strict. The inline script stays blocked, which is what we want. Legitimate code lives in files the policy allows. Anything inline is suspect. Keeping CSS and scripts in separate files makes code easier to maintain, and under a CSP it pays off for security too.

## Final result

`report.html`:

```html
<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Security-Policy"
          content="default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'; object-src 'none'">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Report a Suspicious Charge | Riverbend Credit Union</title>
    <link rel="icon" href="logo.svg">
    <link rel="stylesheet" href="styles.css">
  </head>
  <body>
    <header class="site-header">
      <img src="logo.svg" alt="" width="40" height="40">
      <p class="brand">Riverbend Credit Union <span>(a fictional bank for this demo)</span></p>
    </header>

    <main>
      <h1>Report a Suspicious Charge</h1>

      <section class="notice" aria-labelledby="notice-heading">
        <h2 id="notice-heading">Member notice</h2>
        <p>We will never ask for your verification code by phone or email.</p>
        <!-- Stand-in for untrusted content that slipped into the page (for example, from a database). -->
        <script>console.log("Inline script ran. If an attacker had injected this, it would have run too.");</script>
      </section>

      <!-- (the form from the Part 2 checkpoint, unchanged) -->
    </main>
  </body>
</html>
```

Fill in the form, attach the receipt, and submit. **What the server received** shows `POST`, `Request body`, `multipart/form-data`, and the file's bytes, and the server log line has no secrets in it. Back on the form, the Console shows the inline script refused, while the page's own stylesheet and logo load normally. Tab through it with a screen reader and you hear a label, a required cue, and help text for every field.

## Key takeaway

A secure form is built in layers. You constrain input at the source, send it deliberately (`POST`, the right encoding, over HTTPS), present it so every user can complete it honestly, and then hand the browser a policy that limits what the page itself may do. No single layer is enough. Each one covers a gap the others leave, and the server must still re-check everything that crosses the trust boundary.

# DESIGN — XSS Forum Demo

A deliberately insecure discussion forum ("Open Forum") for teaching **stored
cross-site scripting (XSS)** in a safe, isolated sandbox. Students log in with
only a username, pick a topic, and post comments. Comments and usernames are
saved exactly as typed and written into the page as raw HTML, so a script a
student posts runs in the browser of everyone who views that topic. A second,
separate "attacker" server records anything a payload sends it, so students can
see a stolen cookie actually arrive. It uses the layered architecture students
already know (controller, service, DAO, database) and runs against a throwaway
`xss_demo` PostgreSQL database of fake data.

For the exercises themselves see `PAYLOAD_WALKTHROUGH.md`; for setup see
`README.md`; for facilitation see `INSTRUCTOR_GUIDE.md`.

---

## Why it is insecure, and why that is safe

The app has **no security controls on purpose**: no output encoding, no input
validation, no Content Security Policy, no passwords, and a login cookie that
JavaScript can read (not `HttpOnly`). There is no "secure mode" and no fixed
version. (The SQL is still parameterized with `?`; this demo is about XSS, not
SQL injection.)

Safety comes from the sandbox, not the app:

| Boundary | How |
|----------|-----|
| Localhost only | The forum binds `127.0.0.1:8080` (`server.address`) and the attacker listener binds `127.0.0.1:9090`. Nothing is reachable from the network. |
| Fake data | Users, topics and comments are invented seed data. |
| Separate database | Everything lives in its own `xss_demo` database; no other database is touched. |
| Standard account | The app connects as the student's usual `postgres` user (kept simple for the classroom). |
| Easy recovery | Re-running `schema.sql`, or the **Reset demo** page, drops and re-seeds everything. |

---

## Libraries and frameworks

| Library | Why it is here |
|---------|----------------|
| **Spring Boot** (parent POM 3.5.x, Java 21) | Configuration, dependency injection, embedded server, startup. |
| **Spring MVC** (`spring-boot-starter-web`, embedded Tomcat) | Serves the REST API and also the static HTML/CSS/JS pages from the same origin. |
| **Spring JDBC** (`spring-boot-starter-jdbc`, `JdbcTemplate`) | The data-access API the students already use. Also provides `ResourceDatabasePopulator`, which the reset runs. |
| **PostgreSQL JDBC driver** (runtime scope) | Lets the app talk to the `xss_demo` database. |
| **JDK `com.sun.net.httpserver`** | The attacker listener is a single Java file using the JDK's built-in HTTP server: no dependencies, no Maven. |
| **Vanilla HTML, CSS and JavaScript** | The front end. No npm, no framework, no build step; the files are served as written. |

No ORM, no security framework and no JS framework: they would hide the mechanism
being taught.

### `pom.xml` in plain terms

- The **parent** (`spring-boot-starter-parent`) supplies the version of every
  Spring dependency and sensible build defaults, so the dependencies below need
  no version numbers.
- `java.version` is `21`.
- **Dependencies**: `spring-boot-starter-web` (REST + web server),
  `spring-boot-starter-jdbc` (`JdbcTemplate` and the connection pool), and the
  `postgresql` driver (needed only at run time).
- The **`spring-boot-maven-plugin`** packages and runs the app (`mvn spring-boot:run`).

### `application.properties` in plain terms

| Setting | Meaning |
|---------|---------|
| `spring.datasource.url` | JDBC URL: PostgreSQL on `localhost:5432`, database `xss_demo`. |
| `spring.datasource.username` | `postgres`. |
| `spring.datasource.password` | `${DB_PASSWORD:postgres}`: read the `DB_PASSWORD` environment variable, falling back to `postgres`. |
| `spring.sql.init.mode=never` | Spring does **not** run `schema.sql` at startup, so restarting the app keeps the payloads students posted. The schema is loaded with `psql` or the Reset page. |
| `server.address=127.0.0.1` | Bind to localhost only. |
| `server.port=8080` | The forum's port. |
| `spring.main.banner-mode=off` | Quieter console output. |

---

## File structure

```
xss-forum-demo/
├── pom.xml                         Maven build: Spring Boot parent, web + JDBC + PostgreSQL driver
├── .gitignore                      Excludes build output and IDE files
├── README.md                       Student setup and run steps
├── PAYLOAD_WALKTHROUGH.md          Student exercises (the attack payloads live here)
├── INSTRUCTOR_GUIDE.md             Facilitation guide for instructors
├── Project Documents/
│   └── DESIGN.md                   This file
├── attacker/
│   └── AttackerServer.java         Separate attacker listener on :9090 (single-file JDK program)
└── src/main/
    ├── java/demo/xss/
    │   ├── XssForumDemoApp.java    Spring Boot entry point
    │   ├── controller/
    │   │   ├── UserController.java      POST /api/users/login
    │   │   ├── TopicController.java     GET /api/topics
    │   │   ├── CommentController.java   GET and POST /api/topics/{topicId}/comments
    │   │   ├── ResetController.java     POST /api/reset
    │   │   └── ApiExceptionHandler.java Turns bad input / failed saves into a 400 {"error": "..."}
    │   ├── service/
    │   │   ├── UserService.java         Login = find the user or create them; rejects blank names
    │   │   ├── TopicService.java        Lists topics
    │   │   ├── CommentService.java      Lists and adds comments; rejects blank text; stores text untouched
    │   │   └── ResetService.java        Re-runs schema.sql in one transaction
    │   ├── dao/
    │   │   ├── UserDao.java             users table: find by exact name, insert
    │   │   ├── TopicDao.java            topics table: find all
    │   │   └── CommentDao.java          comments table: newest-first by topic, insert
    │   └── model/
    │       ├── User.java                Record (userId, userName)
    │       ├── Topic.java               Record (topicId, topicName)
    │       ├── Comment.java             Record (ids, userName, commentText, commentDateTime)
    │       ├── LoginRequest.java        Request body for login (userName)
    │       └── NewCommentRequest.java   Request body for a new comment (userId, commentText)
    └── resources/
        ├── application.properties       Datasource, localhost binding, no auto schema init
        ├── db/schema.sql                Drops, recreates and seeds users, topics, comments
        └── static/
            ├── index.html               Log In page
            ├── forum.html               Topics and comments page
            ├── reset.html               Reset demo page
            ├── css/style.css            Design tokens and all component styles
            └── js/
                ├── common.js            Shared helpers: cookies and date formatting
                ├── login.js             Log In page behavior
                ├── forum.js             Forum page behavior (renders with innerHTML)
                └── reset.js             Reset page behavior
```

> `target/` (build output) and `.idea/` (IDE settings) are generated or local and
> are not part of the source.

---

## How the pieces fit

### Layered request flow

```
Browser (forum.html + js)  -- fetch /api/... -->  Controller -> Service -> DAO -> JdbcTemplate -> PostgreSQL (xss_demo)
        ^                                              |
        +---------------- JSON <-----------------------+

Payload running in the browser -- GET/POST --> Attacker listener (:9090, separate server, in-memory log)
```

- **Controllers** are thin `@RestController`s: they map a URL to one service call
  and return the result as JSON. `ApiExceptionHandler` converts an
  `IllegalArgumentException` (blank input) or a database constraint failure into
  HTTP 400 with `{"error": "..."}`, which the page shows next to the form.
- **Services** hold the small amount of logic: login-or-create, blank checks,
  and transactions. They deliberately do **not** trim, clean or encode anything,
  because that untouched text is what lets a payload reach the page.
- **DAOs** use `JdbcTemplate` with `?` parameters and row mappers that build the
  model records.
- **Models** are Java records, serialized to JSON by Spring.

### REST API

| Method and path | Body | Success response | Purpose |
|-----------------|------|------------------|---------|
| `POST /api/users/login` | `{"userName": "..."}` | 200 `{userId, userName}` | Find the user by exact (case-sensitive) name, or create them. |
| `GET /api/topics` | none | 200 `[{topicId, topicName}]` | List topics in id order. |
| `GET /api/topics/{topicId}/comments` | none | 200 `[{commentId, topicId, userId, userName, commentText, commentDateTime}]` | Comments for a topic, newest first. |
| `POST /api/topics/{topicId}/comments` | `{"userId": n, "commentText": "..."}` | 201, no body | Add a comment. |
| `POST /api/reset` | none | 204, no body | Drop, recreate and re-seed the database. |

Errors return 400 with `{"error": "..."}` (for example a blank username or
comment). The listener's endpoints are described below.

### Database

Defined in `db/schema.sql` (which starts with `DROP TABLE IF EXISTS`, so it is
safe to re-run).

| Table | Columns |
|-------|---------|
| `users` | `user_id` (identity, PK), `user_name` (unique, not blank, up to 255 characters) |
| `topics` | `topic_id` (identity, PK), `topic_name` (up to 100 characters) |
| `comments` | `comment_id` (identity, PK), `topic_id` (FK to topics), `user_id` (FK to users), `comment_text` (text, not blank), `comment_date_time` (timestamptz, defaults to `now()`) |

An index on `comments (topic_id, comment_date_time DESC)` supports the
newest-first listing; `CommentDao` also orders by `comment_id DESC` to break ties.

**Seed data:** users `alice`, `bob`, `carol`, `dave`; topics *General Chat*,
*Java Help*, *Weekend Plans*, *Favorite Snacks*; three harmless comments per
topic, timestamped in the recent past so the ordering is visible.

### Cookie-based "login"

There is no session and no password. `UserController` sets no cookies. After a
successful `POST /api/users/login`, `login.js` stores the result itself with
`document.cookie`, in two plain cookies, `forum_user_id` and `forum_user_name`,
with no `HttpOnly`, `Secure`, `SameSite` or expiry. Because JavaScript can read
them, a payload can read `document.cookie` and send it to the attacker listener.

- `forum.html` checks for the cookies in its `<head>`, before anything renders,
  and redirects to `index.html` if they are missing.
- `index.html` never auto-redirects when cookies exist, so students can switch
  users freely. **Switch user** clears both cookies.
- A username that does not exist yet is created at login.
- The post request sends `userId` from the cookie, so the "logged in" identity is
  whatever the cookie says.

### How `forum.js` builds the page

`forum.js` fetches `/api/topics`, picks the topic from the `?topicId=` query
string (the first topic by default), then fetches that topic's comments. It then
builds HTML by **joining strings** and assigns the result with `innerHTML`; there
is deliberately no escaping helper anywhere in the front end. The values written
this way are:

- the username in the "Logged in as" line (read from the cookie),
- each topic name in the sidebar links,
- each comment's author name and comment text.

Static messages ("Loading…", "Couldn't load comments", "No comments yet") are
set with `textContent`. Posting sends the text untrimmed via `fetch`; a `201`
reloads the page so the new comment (and any payload in it) is shown at once.
The login and reset pages render no stored data, so no payload can run there.

### Reset flow

The **Reset demo** link (on the Log In and forum pages) opens `reset.html`.
Clicking the button sends `POST /api/reset`; `ResetController` calls
`ResetService.resetDemo()`, which runs `db/schema.sql` through Spring's
`ResourceDatabasePopulator` inside one `@Transactional` method (the drop, create
and seed commit or roll back together). On a `204`, `reset.js` clears the login
cookies and returns to the Log In page. If the reset fails, the page tells the
student to re-run `schema.sql` with `psql`, which does the same job from the
command line.

### The attacker listener

`attacker/AttackerServer.java` is a **separate server on its own origin**
(`127.0.0.1:9090`), started with `java attacker/AttackerServer.java`. It shares
no code, cookies or database with the forum and keeps captures **in memory only**
(lost on restart).

| Path | Behavior |
|------|----------|
| `GET` or `POST /steal` | Records the request. For `GET` the query string is percent-decoded (falling back to the raw text if the escapes are malformed); for `POST` the body is used. Also records the `Referer` header (a browser sends only the origin of the forum page, e.g. `http://localhost:8080/`) and a timestamp. Replies `204` with `Access-Control-Allow-Origin: *`. |
| `OPTIONS /steal` | CORS preflight answer (`204`) so cross-origin `fetch` calls work. |
| `POST /clear` | Empties the log and redirects to `/`. Other methods get `405`. |
| `GET /` | The captures page: a table of Received / From (Referer) / Data, newest first, with a Refresh link and a Clear captures button. |

The captures page HTML-escapes every captured value before writing it, so what a
payload sends is shown as plain text and never runs on the attacker page.

---

## Design system

A minimal, monochrome, typography-led style. Hierarchy comes from opacity and
whitespace rather than colour or boxes; corners are square; there are no
shadows, gradients, icons or animations. The calm look is intentional: any change
a payload makes (red text, a giant image, a hidden list) stands out immediately.
Plain class names (`.comment`, `.author`, `.topic-list`) are easy to guess so
payloads can target them on purpose.

All tokens are CSS custom properties on `:root` in `css/style.css`.

**Colour**

| Token | Value | Use |
|-------|-------|-----|
| `--color-bg` | `#FFFFFF` | Page background |
| `--color-surface` | `#F5F5F5` | Compose box, active topic, vulnerability banner |
| `--color-ink` | `#111111` | All text and the primary button fill |
| `--color-line` | `rgba(17,17,17,0.12)` | Hairline borders and dividers |
| `--color-accent` | `#424242` | Focus ring, primary button hover |
| `--color-error` | `#B00020` | Error text and error borders only |
| `--opacity-high` / `-medium` / `-low` / `-muted` | `1` / `0.87` / `0.70` / `0.62` | Titles / body / secondary (timestamps) / tertiary (hints, empty states) |

Text never drops below 0.62 opacity, which keeps it above WCAG AA contrast on
white.

**Type.** `--font-sans` is `Inter, system-ui, …` and `--font-mono` is
`"JetBrains Mono", ui-monospace, …`; fonts are used if installed, with no web-font
download. Sizes: page title (`.page-title`, on the Log In and Reset pages) fluid
2.25 to 3rem at weight 300; `h1` (topic name on the forum page, no `.page-title`
class) 2.25rem; `h2` 1.5rem; body 1rem at weight 400 and opacity 0.87, up to 72
characters wide; labels and buttons 0.875rem; timestamps and hints 0.8125rem
(timestamps in the mono font).

**Spacing and layout.** 8px base unit, scale `--space-1` to `--space-8`
(4, 8, 12, 16, 24, 32, 48, 64 px). The container is 1200px wide and centred.
One column on mobile; the topics sidebar sits beside the comments at wider widths.
Sections are separated by whitespace, not boxes.

**Components**

| Component | Style |
|-----------|-------|
| Primary button (`.btn`: Log In, Post, Reset demo) | Ink fill, white text, weight 600; hover fills with the accent colour; 2px accent focus ring; disabled at 0.4 opacity; label changes while working ("Posting…", "Logging in…", "Resetting…"). |
| Text link (`.link`: Switch user, Reset demo, Back) | Ink at 0.70 opacity, underlined; full opacity on hover. |
| Text input (username) | Label above; bottom hairline only; error state turns it red with a message below and `aria-invalid="true"`. |
| Textarea (comment) | White fill inside the grey compose block; hairline border; same error treatment. |
| Topic list (`.topic-list`) | Plain links; the current topic has `aria-current="page"` and the surface fill. |
| Comment (`.comment`) | Author (weight 500) and mono timestamp on one line, text below, hairline divider between comments. |
| Status text (`.status`, `.hint`, `.empty`, `.error`) | Muted text for loading, hints and empty states; red for errors (`role="alert"`). |
| Loading skeleton (`.skeleton`) | Three grey bars shown while comments load. |
| Vulnerability banner (`.vuln-banner`) | Surface-coloured strip at the top of every forum page: "Deliberately vulnerable training app, runs on localhost only. Do not reuse this code." |

Every control is always visible (nothing appears only on hover), and every
interactive element has a visible `:focus-visible` style.

**Attacker page (dark variant).** The captures page on `:9090` uses the same
system inverted, with its styles inlined in `AttackerServer.java`:
`--color-bg #0A0A0A`, `--color-surface #161616`, `--color-ink #F5F5F5`,
`--color-line rgba(245,245,245,0.14)`, `--color-accent #BDBDBD`, with the
monospace font as the main typeface. It is deliberately different from the
forum so students can tell the two tabs apart at a glance. Each page also
carries `<link rel="icon" href="data:,">` so the browser does not request a
favicon and clutter the logs.

---

## Feature summary

- **Log In page**: username only; unknown names are created; seeded users
  `alice`, `bob`, `carol`, `dave` let students watch one user's payload run in
  another user's browser.
- **Topics and comments page**: topic sidebar, comments newest first, a compose
  box, and a "Logged in as" line with **Switch user** and **Reset demo** links.
- **Stored XSS**: usernames, topic names and comments are saved as-is and
  rendered with `innerHTML`; nothing limits what a payload can do to the page.
- **Cookie theft**: the login cookies are readable by JavaScript.
- **Attacker listener**: records anything sent to `/steal` and displays it,
  newest first, with its Referer origin.
- **Reset**: the Reset demo page or `schema.sql` restores the clean seed data.
- **Contained**: localhost-only servers, fake data, its own `xss_demo` database.

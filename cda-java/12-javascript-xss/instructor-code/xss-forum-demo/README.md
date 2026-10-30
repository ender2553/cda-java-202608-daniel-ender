# XSS Forum Demo

A small discussion forum ("Open Forum") with topics and comments. It is built so that you can practise **stored cross-site scripting (XSS)**: you post a comment that contains HTML or JavaScript, the forum saves it, and then it runs in the browser of anyone who views that comment. A second tiny server plays the "attacker's website" and shows you what a payload managed to send out.

The backend is plain Spring Boot with the layers you already know (controller, service, DAO) on PostgreSQL. The vulnerability lives in the browser code.

> ⚠ **Safety notice**
>
> - This app is **deliberately vulnerable**. That is the whole point.
> - Both servers listen on `127.0.0.1` (localhost) only. Nobody else on your network can reach them.
> - All data is throwaway fake data.
> - **Never deploy it** and **never copy its code** into a real project.
> - Never try these techniques on real sites or anyone else's systems.

## 1. Prerequisites

- JDK 21 or newer
- Maven
- PostgreSQL 16 or newer, with the `postgres` user
- Chrome or Edge

Quick check:

```text
java -version
mvn -version
```

Success: `java -version` prints a version of 21 or higher (for example `openjdk version "21.0.2"`), and `mvn -version` prints an Apache Maven version line followed by the Java version it is using.

## 2. Set up the database

Create the database:

```text
psql -U postgres -c "CREATE DATABASE xss_demo"
```

Success: `psql` prints `CREATE DATABASE`.

Load the tables and seed data. Run this from the project folder (the one that contains `pom.xml`):

```text
psql -U postgres -d xss_demo -f src/main/resources/db/schema.sql
```

Success: you see a list of `CREATE TABLE` and `INSERT` lines and no `ERROR` lines.

**On Windows, `psql` may not be on your PATH.** If you see "psql is not recognized", use the full path instead. It looks like this (replace `<version>` with your installed version, for example `16`):

PowerShell:

```powershell
& "C:\Program Files\PostgreSQL\<version>\bin\psql.exe" -U postgres -c "CREATE DATABASE xss_demo"
& "C:\Program Files\PostgreSQL\<version>\bin\psql.exe" -U postgres -d xss_demo -f src/main/resources/db/schema.sql
```

Git Bash:

```bash
"/c/Program Files/PostgreSQL/<version>/bin/psql.exe" -U postgres -c "CREATE DATABASE xss_demo"
"/c/Program Files/PostgreSQL/<version>/bin/psql.exe" -U postgres -d xss_demo -f src/main/resources/db/schema.sql
```

Each `psql` command asks for the `postgres` password. Type the one you chose when you installed PostgreSQL.

### If your postgres password is not `postgres`

The app assumes the password is `postgres`. If yours is different, set `DB_PASSWORD` in the terminal where you start the app.

PowerShell:

```powershell
$env:DB_PASSWORD="your-password"
```

Git Bash:

```bash
export DB_PASSWORD=your-password
```

Or edit this one line in `src/main/resources/application.properties`:

```text
spring.datasource.password=${DB_PASSWORD:postgres}
```

## 3. Run it

You need two terminals, both opened in the project folder.

**Terminal 1: the forum**

```text
mvn spring-boot:run
```

The first run downloads libraries and can take a few minutes. Wait for the Spring Boot "Started" line, then open <http://localhost:8080>. Success: the login page appears.

**Terminal 2: the attacker listener**

```text
java attacker/AttackerServer.java
```

Open <http://localhost:9090>. This page lists everything that gets sent to it. Success: it shows "No captures yet."

The listener is a separate "attacker's website". It runs on a different port (9090 vs 8080), so the browser treats it as a different origin from the forum. It has no cookies and shares no code with the forum. It only records what arrives at `/steal`. Stop either server with Ctrl+C.

## 4. Using the forum

1. Open <http://localhost:8080>.
2. Log in as one of the seeded users: `alice`, `bob`, `carol`, or `dave`. Any new name works too. There is no password.
3. Pick a topic, read the comments, and post your own.

**Tip:** to act as two users at once, use a second browser profile or an InPrivate (Edge) / Incognito (Chrome) window. One window posts the payload, the other plays the victim.

## 5. Resetting

- **Reset demo page:** <http://localhost:8080/reset.html>. It deletes every comment and user and restores the seed data. It is always reachable, even if a payload has wrecked the forum page.
- **psql fallback:** re-run the schema script (same command as in setup):

  ```text
  psql -U postgres -d xss_demo -f src/main/resources/db/schema.sql
  ```

- Restarting the forum **keeps** your payloads (they are in the database).
- Restarting the attacker listener **clears** its captured list.

## 6. Project layout

```text
xss-forum-demo/
├── pom.xml
├── attacker/
│   └── AttackerServer.java        the "attacker's website" (port 9090)
└── src/main/
    ├── java/demo/xss/
    │   ├── XssForumDemoApp.java   Spring Boot entry point
    │   ├── controller/            REST endpoints (users, topics, comments, reset)
    │   ├── service/               business logic
    │   ├── dao/                   JDBC data access
    │   └── model/                 Comment, Topic, User and request objects
    └── resources/
        ├── application.properties database and server settings
        ├── db/schema.sql          tables and seed data
        └── static/                pages served by the forum
            ├── index.html         login
            ├── forum.html         topics and comments
            ├── reset.html         reset page
            ├── css/style.css
            └── js/
                ├── common.js
                ├── login.js
                ├── forum.js       the vulnerable innerHTML lines live here
                └── reset.js
```

## 7. Troubleshooting

**Port 8080 or 9090 is already in use.** Another program (or an earlier copy of this one) is running. Close it, then start again. To find the process in PowerShell: `netstat -ano | findstr :8080`.

**`password authentication failed for user "postgres"`.** Your password is not `postgres`. Set `DB_PASSWORD` (see setup), then restart the app in that same terminal.

**`psql` is not recognized.** Use the full path to `psql.exe` shown in setup, or add PostgreSQL's `bin` folder to your PATH.

**`database "xss_demo" does not exist`.** Run the `CREATE DATABASE` command from setup, then load `schema.sql`.

**My capture never shows up on the attacker page.**
- Check that the attacker listener is running and that the URL in your payload is `http://localhost:9090/steal`.
- If your payload sends data in a GET request, wrap the data in `encodeURIComponent(...)`. A raw `%` in the URL makes the listener reject the request, and an unencoded `&` or `#` can cut the captured data short.
- Or send the data with a POST using `fetch`.
- Check the browser's developer tools (F12) for errors.

**My `<script>` payload didn't run.** That is expected in some places. See [PAYLOAD_WALKTHROUGH.md](PAYLOAD_WALKTHROUGH.md).

## 8. Next step

Follow [PAYLOAD_WALKTHROUGH.md](PAYLOAD_WALKTHROUGH.md).

# Diagrams: Advanced Java for Performance, Securely

Whiteboard diagrams for each segment in [`INSTRUCTOR_GUIDE.md`](INSTRUCTOR_GUIDE.md).
Draw them on the board, or show this file in IntelliJ's Markdown preview or on
GitHub, which render the diagrams.

> **Instructor-only.** Some diagrams show the finished code. Keep this file in
> `secure-performance-solution`, and don't copy it into the starter.

| Segment | Diagrams | Steps |
|---|---|---|
| [1. Performance and Safety Are the Same Problem](#segment-1-performance-and-safety-are-the-same-problem) | 1a. Two circles become one · 1b. How the log takes the app down | 1 |
| [2. Buffered and Streamed I/O](#segment-2-buffered-and-streamed-io) | 2a. Eager versus lazy · 2b. The last operation decides | 2–3 |
| [3. Resource Leaks as an Availability Risk](#segment-3-resource-leaks-as-an-availability-risk) | 3a. try-with-resources on a failure · 3b. Draining the connection pool | 4–5 |
| [4. Bounding Input and Safe Concurrency](#segment-4-bounding-input-and-safe-concurrency) | 4a. Reject, don't truncate · 4b. The lost update | 6–7 |
| [Activity: Find Large Transactions](#activity-find-large-transactions-in-the-log) | A. All four ideas in one method | Extension |

---

## Segment 1: Performance and Safety Are the Same Problem

### 1a. Two circles become one

Use this at the start of the segment. Draw the left side first, ask which
circle the room would give up under deadline pressure, then redraw it as the
right side.

```mermaid
flowchart LR
    subgraph before["Before: pick one"]
        direction TB
        F(("FAST"))
        S(("SAFE"))
    end
    subgraph after["After: one design"]
        direction TB
        ST(("STREAMED"))
        P1["Bounded memory"]
        P2["Closed resources"]
        P3["Bounded requests"]
        ST --- P1
        ST --- P2
        ST --- P3
    end
    before ==>|"reframe"| after
```

### 1b. How the log takes the app down

Use this after the Step 1 crash, when you ask "who did this to us?" Nobody
appears in the diagram, because no attacker was needed.

```mermaid
flowchart LR
    G["Every transfer adds a line<br/>The log only grows"] --> R["summarize()<br/>Files.readAllLines(log)"]
    R --> H["All 3,000,000 lines<br/>in one List"]
    H --> C{"Fits in a<br/>128 MB heap?"}
    C -->|"Small file: yes"| OK["Works today"]
    C -->|"Real file: no"| OOM["OutOfMemoryError"]
    OOM --> DOWN["The app is down<br/>for every user"]
    DOWN --> A["Availability: the A in CIA"]
    OK -.->|"next year"| C
```

---

## Segment 2: Buffered and Streamed I/O

### 2a. Eager versus lazy

Show this right after the class predicts whether `map` runs on every line
before `reduce`, during Step 2. The eager version reads everything before it
does any work. The lazy version pulls each line through the whole pipeline,
then lets it go.

```mermaid
sequenceDiagram
    autonumber
    participant F as transaction-log.csv
    participant P as Stream pipeline
    participant R as reduce (running total)

    Note over F,R: Eager: readAllLines
    F->>P: line 1, line 2, ... line 3,000,000
    Note over P: All 3,000,000 lines held in memory
    P->>R: start adding

    Note over F,R: Lazy: Files.lines, then skip, map, reduce
    R->>P: next?
    P->>F: read one line
    F-->>P: line 1
    P-->>R: map, then add, then forget the line
    R->>P: next?
    P->>F: read one line
    F-->>P: line 2
    P-->>R: map, then add, then forget the line
    Note over F,R: ...repeated. Only one line is in memory at a time.
```

### 2b. The last operation decides

Use this for Step 3, when learners ask why switching to `Files.lines` didn't
help. Both pipelines start the same way. Only the end differs.

```mermaid
flowchart LR
    L["Files.lines(log)"] --> SK["skip(1)"] --> FI["filter: involves ACC-00000001"]
    FI --> T1["collect(toList())"]
    FI --> T2["iterator()"]
    T1 --> M1["One List of about 1,200,000 lines"] --> X1["OutOfMemoryError"]
    T2 --> M2["Pull one line, write it, forget it"] --> X2["Memory stays flat"]

    classDef bad fill:#fde2e2,stroke:#c0392b,color:#000
    classDef good fill:#e3f4e1,stroke:#2e7d32,color:#000
    class T1,M1,X1 bad
    class T2,M2,X2 good
```

---

## Segment 3: Resource Leaks as an Availability Risk

### 3a. try-with-resources on a failure

Use this after the broken `finally` walkthrough, before Step 4. It shows what
`try (lines; writer)` does when the body throws: it closes both resources in
reverse order, and the original error still reaches the caller.

```mermaid
sequenceDiagram
    participant C as exportForAccount
    participant L as Files.lines stream
    participant W as BufferedWriter
    participant D as Disk

    C->>L: open (1st in the try header)
    C->>W: open (2nd in the try header)
    C->>W: write lines
    W-->>D: flush each full buffer
    Note over C: The body throws (for example, the disk is full)
    C->>W: close() first, in reverse order
    W-->>D: flush the last buffer
    C->>L: close() second
    Note over C: If a close() fails too, that error is added<br/>as "suppressed". It never replaces the original.
    C-->>C: The original exception reaches the catch block
```

Without try-with-resources, the starter never calls `close()`. The last
buffer never reaches the disk, which is the MISMATCH that learners see in
Step 4.

### 3b. Draining the connection pool

Use this for Step 5. Read `active=5, idle=0` from the log, then point at the
last two arrows: the leak is in menu 4, but other users' work fails.

```mermaid
sequenceDiagram
    participant M4 as Menu 4: totalSentBy
    participant Pool as Hikari pool (5 connections)
    participant M1 as Menu 1: balances
    participant T as Menu 2: transfer

    loop 5 calls
        M4->>Pool: getConnection()
        Pool-->>M4: a connection
        Note over M4: Query runs. close() is never called.
    end
    Note over Pool: active=5, idle=0
    M4->>Pool: 6th getConnection()
    Pool--xM4: waits 3 s, then times out
    M1->>Pool: getConnection()
    Pool--xM1: waits 3 s, then times out
    T->>Pool: @Transactional asks for a connection
    Pool--xT: waits 3 s, then times out
    Note over M1,T: The bug is in menu 4. Everyone else's work fails.
```

With try-with-resources, each call returns its connection to the pool, and the
pool never runs out:

```mermaid
flowchart LR
    A["getConnection()"] --> B["Query"] --> C["try-with-resources:<br/>close() returns the connection"] --> D["Pool: idle again"]
    D --> A
```

---

## Segment 4: Bounding Input and Safe Concurrency

### 4a. Reject, don't truncate

Use this for Step 6, when someone suggests `Math.min`. The middle path is the
tempting wrong fix.

```mermaid
flowchart TD
    REQ["findRecent(limit)<br/>The caller chooses limit"] --> NOB{"Any bound?"}
    NOB -->|"No bound (starter)"| ALL["LIMIT 10,000,000<br/>loads 1,000,000 rows"] --> OOM["OutOfMemoryError<br/>The same failure, a new entry point"]
    NOB -->|"Math.min(limit, 100)"| TR["Returns 100 rows<br/>without saying so"] --> SIL["The caller can't tell.<br/>It looks like missing data."]
    NOB -->|"Validate.between(limit, 1, 100)"| OK{"1 to 100?"}
    OK -->|"Yes"| Q["Run the query"]
    OK -->|"No"| REJ["Reject with a clear message:<br/>Show between 1 and 100 transactions at a time"]

    classDef bad fill:#fde2e2,stroke:#c0392b,color:#000
    classDef warn fill:#fff4d6,stroke:#b7791f,color:#000
    classDef good fill:#e3f4e1,stroke:#2e7d32,color:#000
    class ALL,OOM bad
    class TR,SIL warn
    class OK,Q,REJ good
```

### 4b. The lost update

Use this for Step 7, after menu 8 shows lost updates. `linesProcessed += 1`
is three steps, and `volatile` doesn't make them one.

```mermaid
sequenceDiagram
    participant A as Thread A
    participant V as linesProcessed (volatile int)
    participant B as Thread B

    Note over V: value = 41
    A->>V: read 41
    B->>V: read 41
    A->>A: add 1, which is 42
    B->>B: add 1, which is 42
    A->>V: write 42
    B->>V: write 42
    Note over V: value = 42, not 43. One update is lost.
```

With `AtomicLong.addAndGet`, the read, add, and write happen as one step, so
the second thread always sees the first thread's result:

```mermaid
sequenceDiagram
    participant A as Thread A
    participant V as linesProcessed (AtomicLong)
    participant B as Thread B

    Note over V: value = 41
    A->>V: addAndGet(1), done as one step
    V-->>A: 42
    B->>V: addAndGet(1), done as one step
    V-->>B: 43
    Note over V: value = 43. No update is lost.
```

---

## Activity: Find Large Transactions in the Log

### A. All four ideas in one method

Show this **after** learners have tried the extension, when you go over
`findAtLeast` together. It shows the solution, so don't put it up first.

```mermaid
flowchart LR
    IN["findAtLeast(minAmount, maxResults)"] --> V1["Validate.amount(minAmount)"]
    V1 --> V2["Validate.between(maxResults, 1, 100)"]
    V2 --> TWR

    subgraph TWR["try (Stream lines = Files.lines(log))"]
        direction LR
        SK["skip(1)"] --> FI["filter: amount is at least minAmount"]
        FI --> LI["limit(maxResults)<br/>stops reading early"]
        LI --> TL["toList()<br/>safe: at most 100 lines"]
    end

    TWR --> ST["stats.recordLines(found.size())<br/>AtomicLong"]
    ST --> OUT["Return the lines"]
    TWR -.->|"IOException"| DF["DataAccessFailure.logged(...)"]
```

| Idea from the lesson | Where it appears |
|---|---|
| Bound the input | Both `Validate` calls run before the file is opened |
| Stream instead of load | `Files.lines`, with `limit` to stop early |
| Close every resource | try-with-resources around the stream |
| Thread-safe shared state | `ProcessingStats` is the `AtomicLong` from Step 7 |

# Ledger Audit Challenge: Answer Key (Instructor Only)

Do not distribute. The student project is `../ledger-audit-challenge`. It
contains no comments, TODOs, or README on purpose.

Students run `schema.sql` in pgAdmin against `ledger_demo` first. Rows left
over from Lesson 2 (AES-GCM) won't decrypt with this app's key. Re-running the
schema clears them. Students also need to set their own password in
`application.properties`.

**Core** marks the findings a passing audit is expected to include. The rest
are extra credit. **Latent** findings are real defects found by reading the
code that can't be demonstrated reliably: the app either never reaches them
from more than one thread (#18–20), or the race is too rare to show (#17).

> **Reset after destructive demos.** #5 deletes every holder, #7 deletes every
> transaction, and #6, #10, #11, and #13–16 change balances. Re-run `schema.sql` after them.

## Findings

| # | Core | Category | Location | Flaw | Proof of concept |
|---|---|---|---|---|---|
| 1 | Yes | SQL injection | `AccountHolderRepository.searchByName` | Concatenated `ILIKE` | Menu 2: `zzz' OR 1=1 --` returns every holder |
| 2 | Yes | SQL injection | `AccountHolderRepository.findById` | Concatenated `WHERE` | Menu 3: `' OR 1=1 --` returns a holder you didn't ask for |
| 3 | Yes | SQL injection | `AccountHolderRepository.findAll` | Sort identifier not allow-listed | Menu 1: `1/0` gives "division by zero" |
| 4 | Yes | SQL injection | `AccountHolderRepository.create` | Concatenated `INSERT` | Menu 4: `Pat O'Brien` breaks the SQL, then the menu says "Holder saved." anyway |
| 5 | Yes | SQL injection | `AccountHolderRepository.delete` | Concatenated `DELETE`, no confirmation | Menu 5: `' OR '1'='1` deletes every holder |
| 6 | Yes | SQL injection | `AccountRepository.findById` / `updateBalance` | Concatenated account ID | Menu 6: From `x' OR balance > 900 --`, To `ACC-00000001`, amount `1000`. Both the `SELECT` and the `UPDATE` hit ACC-00000004, whose ID was never typed, and take it to 0.00. The same From text then breaks the transaction `INSERT`, so expect a stack trace and no transaction row (see #14) |
| 7 | Yes | SQL injection | `TransactionRepository.record` | Concatenated memo | Menu 6, memo `x'); DELETE FROM transactions; --`. The menu says "Transfer complete." and every transaction row is gone (stacked query) |
| 8 | Yes | SQL injection | `TransactionRepository.findByAccount` | Concatenated filter | Menu 9: `' OR '1'='1` returns every transaction |
| 9 | Yes | Input validation | `ConsoleMenu`, all repositories | No format, length, or range checks anywhere | Any of the above |
| 10 | Yes | Business logic | `TransferService.transfer` | Negative amount is allowed | Transfer `-50` moves money the wrong way |
| 11 | Yes | Business logic | `TransferService.transfer` | Same-account transfer creates money | From and to `ACC-00000002`, amount 50: balance goes up by 50 |
| 12 | Yes | Business logic | `TransferService.transfer` | Insufficient funds silently does nothing | Menu says "Transfer complete." |
| 13 | No | Numeric | `Account.balance`, amounts | `double` for money, and no limit on decimal places | Transfer `0.005` from ACC-00000001 to ACC-00000002. The source rounds back up to `500.00` and the target rounds up to `100.01`: 0.01 created from nothing |
| 14 | Yes | Atomicity | `TransferService.transfer` | No `@Transactional` | Memo over 256 chars: the balances change, but no transaction row is written |
| 15 | Yes | Thread safety / TOCTOU | `TransferService.transfer` | Read-check-write of absolute balances | Menu 7: several transfers from the same account each write the same new balance. Updates are lost and money appears in the targets (see the demo below) |
| 16 | No | Thread safety | `AccountRepository.cache` | Unsynchronized `HashMap`, stale reads | Run menu 8 to load the cache. Change a balance in pgAdmin. Then transfer from that account. The app uses the cached balance and overwrites your change |
| 17 | No | Thread safety (latent) | `TransferService.transferCount` | Non-atomic `int++` | Concurrent increments can be lost. The window is too small next to each transfer's database work to show reliably in menu 7 |
| 18 | No | Thread safety (latent) | `AccountHolderRepository.sql` | Shared mutable field on a singleton | Concurrent callers would overwrite each other's SQL and could run the wrong query |
| 19 | No | Thread safety (latent) | `AccountHolderRepository.TIMESTAMP` | Static `SimpleDateFormat` | Concurrent use can produce corrupted dates or exceptions |
| 20 | No | Thread safety (latent) | `FieldCipher.cipher` | Static shared `Cipher` | Concurrent use can produce garbled ciphertext or failed decryption |
| 21 | Yes | Cryptography | `FieldCipher.KEY` | Hard-coded key in source | `LedgerCoreKey123` |
| 22 | Yes | Cryptography | `FieldCipher` | `"AES"` defaults to ECB, which has no IV | The same tax ID always produces the same ciphertext |
| 23 | No | Cryptography | `FieldCipher` | Fails open | Encrypt failure stores plaintext. Decrypt failure returns raw input |
| 24 | Yes | Data exposure | `ConsoleMenu.print`, `AccountHolder` | Full tax ID shown in lists, search, and details (default record `toString`) | Menus 1, 2, 3 |
| 25 | Yes | Data exposure | `AccountHolderRepository.create` | Plaintext tax ID logged | `logs/ledger.log` |
| 26 | No | Data exposure | `application.properties` | JDBC DEBUG logging writes every SQL statement to the log | `logs/ledger.log` |
| 27 | Yes | Error handling | Repositories, `ConsoleMenu` | Raw DB messages and stack traces shown to the user | Finding 3, and any bad amount |
| 28 | Yes | Error handling | `AccountHolderRepository` | Catches `Exception` and swallows it: false success, empty lists, `null` | Finding 4 |
| 29 | No | Mapping | All repositories | `SELECT *` with implicit mapping | Every `SELECT` |
| 30 | Yes | Secrets / config | `application.properties` | Hard-coded DB credentials, superuser `postgres` | File contents |
| 31 | No | Repo hygiene | `.gitignore` | `logs/` isn't ignored, so logs with SSNs can be committed | `git status` after a run |
| 32 | No | Robustness | `ConsoleMenu.prompt`, `bulkTransfer` | `nextLine()` has no end-of-input check. In menu 7, a bad line throws inside a worker thread, and the executor silently swallows the exception | End input (Ctrl+D in IntelliJ's Run window) and the app crashes. In menu 7, `abc` as an amount, or a line with too few fields, silently drops that transfer |

## Suggested bulk-transfer race demo (menu 7)

1. Re-run `schema.sql`. ACC-00000002 now has 100.00.
2. Run menu 8 once. This loads every balance into the cache, so all three
   threads read 100.00 and the race shows almost every time.
3. Choose menu 7 and enter these lines, then a blank line:

```
ACC-00000002,ACC-00000001,60,a
ACC-00000002,ACC-00000003,60,b
ACC-00000002,ACC-00000004,60,c

```

A correct system allows at most one of these transfers. Here, each thread
passes the balance check and writes ACC-00000002's balance as 40.00. Menu 8
then shows ACC-00000002 at 40.00 and each target up by 60, which creates money
that never existed.

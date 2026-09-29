# Summative Assessment: Secure the LedgerCore Transaction Component

This is the **graded summative assessment** for the Advanced Java Security module. It is
integrative — it does not add new material; it asks you to apply every defense from the module to a
single Java component that carries several weakness classes at once, and to **prove** each fix
actually holds.

You work in the **same LedgerCore project** in the parent directory
(`03-advanced-java-security-postgres/`). There is no separate starter to clone — the insecure component
is the module's starter code, with `// TODO (lab)` markers.

## Objective

Secure one component that combines four defect classes — an unguarded multi-step write, a plaintext
sensitive field, scattered unvalidated data access, and a memory-loading file read — applying the
governing defense for each and demonstrating that every fix works.

## What to do

1. **Defect inventory (no fixes yet).** Read the insecure LedgerCore starter adversarially and write
   an inventory of all four defect classes — the non-atomic transfer, the plaintext account-holder
   field, the scattered unvalidated data access, and the memory-loading log reader. For each, note a
   location and a short exploitability reason.
2. **Atomic transfer.** Rewrite `TransferService.transfer(...)` so the debit and credit commit
   together in one transaction, with an explicit rollback if either write fails. Force a failure
   between the two writes and confirm both balances are exactly what they were before the attempt.
3. **Encrypt at rest.** Encrypt the flagged account-holder field before it is persisted and decrypt
   it on read, using an **authenticated cipher** with the key loaded from **outside the source tree**.
   Query the table directly in Postgres (`SELECT tax_id_encrypted FROM ledgercore.account_holders;`) and confirm the persisted value is ciphertext.
4. **Secure repository.** Move every data-access call behind a single secure repository that
   validates input at each method entry and converts any caught failure into a safe, generic error
   while logging detail internally. Confirm no data-access call remains outside the repository.
5. **Streamed, bounded, leak-free processing.** Rewrite the transaction-log reader so it streams the
   file (never loads it into a list), closes its resources on every path, and rejects input past a
   defined size bound. Process a large synthetic log and confirm memory stays flat and the file
   handle closes.
6. **Verify.** Re-check every item in your defect inventory against the secured project and write a
   short verification note confirming each is resolved.

## Deliverables

- The written **defect inventory** (four classes, each with a location + exploitability note).
- The rewritten `TransferService.transfer(...)` with commit/rollback, plus evidence both balances are
  unchanged after a forced mid-transfer failure.
- The account-holder field **encrypted at rest**, plus a direct-query result showing ciphertext.
- A single **secure repository** that is the only path to LedgerCore's data, with boundary validation
  and secure exception handling on every method.
- The rewritten **transaction-log processor** — streamed, resource-safe, bounded — plus evidence
  memory stayed flat and the handle closed on a large synthetic log.
- A short **verification note** confirming every inventoried defect is resolved.

## Success criteria

- Every defect in your inventory is verifiably resolved.
- A forced failure between the debit and credit leaves both balances unchanged — not merely free of a
  thrown exception.
- The persisted account-holder field reads back as ciphertext on a direct query and decrypts
  correctly on a normal read.
- No data-access call exists anywhere outside the secure repository.
- Processing a large synthetic transaction log keeps memory flat, and the file handle is confirmed
  closed afterward.

## Build & run

Work in the parent `03-advanced-java-security-postgres/` project, with your `.env` filled in (see the module README):

```bash
mvn -q compile     # compile
mvn -q test        # run your tests (add your own for the fixes above)
```

Set `LEDGERCORE_DATA_KEY` (a base64-encoded 32-byte AES-256 key, e.g. `openssl rand -base64 32`) in
`.env` or your environment before running any code that touches the encrypted field — see the module README
and `.env.example`.

## How you'll be graded

| Area | Weight | What it covers |
|---|---|---|
| **Transaction integrity** | 25% | Debit + credit are atomic; a mid-transfer failure rolls back cleanly |
| **Data at rest** | 20% | Sensitive field encrypted with an authenticated cipher + externalized key |
| **Secure data access** | 30% | All access behind one repository; boundary validation; secure exception handling |
| **Resource safety & performance** | 15% | Streamed, leak-free, bounded log processing |
| **Verification** | 10% | Defect inventory + evidence that each fix actually holds |

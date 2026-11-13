# Audit Target: Workflow Diff

This is the audit target for this lesson.

You are handed an AI-generated change to a small note-access module. The change
adds a feature ("search your notes" plus loading runtime options from a config
file). Your job is to run it through the **full audit workflow** end-to-end.

The change is provided three ways (same content) — use whichever you prefer:

- `before.py` — the module before the change.
- `after.py` — the module after the change.
- `change.diff` — the unified diff between them.

## Your task

Walk the change through the workflow and produce a **findings list**:

1. **Scope** — before reading logic, write a short scope statement: what does the
   change touch, ranked by risk, with entry/exit criteria.
2. **Review** — read the diff. For each area, note whether a linter, SAST, or a
   dependency scanner *would* or *would not* have flagged it.
3. **Confirm** — for each suspected defect, confirm it is real: trace untrusted
   input to its sink, or verify behavior. Watch for **what the change removed**,
   not only what it added.
4. **Decide** — record each finding with **location, evidence, risk, and a
   verify / remediate / reject decision**, and mark which findings block
   deployment.

There is no answer key in this folder. Produce your findings on your own.

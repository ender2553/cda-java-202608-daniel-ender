# Audit Target: Trust vs. Correctness

This is the audit target for this lesson.

`snippets.py` is a small set of AI-generated utility helpers for a user-records
batch tool. Every function compiles and reads like finished, idiomatic Python.
Your job is to decide which of them are actually *correct* — because plausibility
is not correctness, and fluent code can still behave wrong.

## Your task

Audit `snippets.py` and produce a **findings list**. For each defect you find:

- name the function and line,
- describe what the code actually does versus what it appears to do,
- state how you would confirm the defect (run it, read the docs, reason about an
  edge case), and
- note the failure family (does-not-exist vs. behaves-wrong).

Do not assume a function is correct just because it looks clean. Read each one
as if it will run in production against real input — including empty input,
boundary values, and repeated calls.

There is no answer key in this folder. Produce your findings on your own.

# Audit Target: Threat-Surface Service

This is the audit target for this lesson.

`app.py` is a small AI-generated Flask service (a personal "notes" API). It runs
and serves requests, but it was generated without knowledge of the application's
trust model, secrets policy, or deployment boundaries. Your task is to **map its
attack surface**.

## Your task

Audit `app.py` and produce a **findings list** framed as a threat-surface map.
For each issue you find, describe:

- the **entry point** (which request / endpoint / parameter lets it in),
- the **trust boundary** it crosses (unauthenticated → privileged, network →
  shell, browser → API, etc.),
- the **sink** (the dangerous operation the input reaches), and
- the **blast radius** if an attacker exploits it.

Walk the service the way an attacker would: which endpoints can be called with no
credentials? Where does user-supplied input reach a database query, the shell, or
the filesystem? What is exposed that should not be? How is the service configured
to run?

There is no answer key in this folder. Produce your findings on your own.

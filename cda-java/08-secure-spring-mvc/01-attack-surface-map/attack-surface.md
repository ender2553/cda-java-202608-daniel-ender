# Attack Surface Map — TODO

**Lesson 1 — HTTP, JSON, and the Spring MVC Request Lifecycle**

Map every untrusted-input entry point in the baseline app. For each one, record
the **trust level** of the data arriving there and the **risk** it introduces
while the application is undefended.

> Tip: trace one full request through the Spring MVC lifecycle — dispatcher →
> handler mapping → argument resolution / JSON deserialization → your handler
> method. Untrusted input enters at more than one place.

## Entry points

| # | Entry point | Trust level | Risk |
|---|-------------|-------------|------|
| 1 |             |             |      |
| 2 |             |             |      |
| 3 |             |             |      |
| 4 |             |             |      |
| 5 |             |             |      |
| 6 |             |             |      |
| 7 |             |             |      |
| 8 |             |             |      |

## Notes / reasoning

_(Use this space to explain how you traced each entry point through the request
lifecycle, and any assumptions you made about how the data is used downstream.)_

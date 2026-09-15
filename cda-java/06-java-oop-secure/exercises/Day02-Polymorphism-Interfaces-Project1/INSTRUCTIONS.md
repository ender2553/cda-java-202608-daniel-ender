# Exercise: Smart Home Hub
**Java OOP Foundations — Cyber Developer Program**

## Scenario

You're building the device layer for a Smart Home Hub. Different devices
(a thermostat, a light, a security camera) need to be controlled through
one consistent system — without the hub ever needing to know or care
exactly which kind of device it's talking to.

This is the same problem the Vehicle Simulator solved this morning with
`Transportable`. Today you'll solve it again from scratch, in a new
domain, using everything from lecture: **interfaces, polymorphism,
abstract classes, and `super`**.

## Setup

1. Unzip the starter project.
2. Open it in IntelliJ (or your editor of choice) as a Maven project.
3. Right click on the green Java folder under test and select Run 'All tests':
   Right now, **all 19 tests will fail** — every method you need to write
   currently throws `UnsupportedOperationException("TODO ...")`. That's
   expected. Your job is to make all 19 pass.
4. Do not modify `SmartHomeTest.java`, `Controllable.java`, or
   `Monitorable.java` — those are already complete.

## Your Tasks

Each TODO is numbered and tagged (e.g. `OOP2-1`) so you can match it to
the rubric. Read each file's header comment carefully before you start —
it tells you exactly what's expected.

| # | File | What you're building |
|---|------|----------------------|
| 1 | `SmartDevice.java` | Constructor validation (fail closed on bad input) |
| 2 | `SmartDevice.java` | Declare the one abstract method this class actually needs |
| 3 | `SmartDevice.java` | `runDiagnostics()` — a concrete method shared by every subclass |
| 4 | `SmartDevice.java` | `getDeviceName()` and `toString()` |
| 5 | `Thermostat.java` | A subclass that extends `SmartDevice` |
| 6 | `SmartLight.java` | Another subclass — this one also reuses `super.toString()` |
| 7 | `SecurityCamera.java` | A device that does **NOT** extend `SmartDevice` — it implements two interfaces directly |
| 8 | `SmartHomeHub.java` | `activateAll()` — polymorphism, one loop, every device type |
| 9 | `SmartHomeHub.java` | `findMonitorableDevices()` — safe type-checking with `instanceof` pattern matching |

## Questions to Ask Yourself As You Go

- Why does `SmartDevice` need to declare `powerDrawWatts()` as abstract,
  but NOT `activate()`?
- Why does `SecurityCamera` implement `Controllable` directly instead of
  extending `SmartDevice`?
- In `SmartHomeHub`, could you solve `activateAll()` with an `if/else`
  chain checking each device's type? You could — but why is that worse
  than what the exercise is asking for?

## Submission

Push your completed `src/main/java/com/cyberdev/smarthome/` files (do
not modify the test file) and confirm `mvn test` shows:
```
[        19 tests successful      ]
[         0 tests failed          ]
```

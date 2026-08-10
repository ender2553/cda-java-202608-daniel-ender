# Vending Machine  — Summative Assessment Instructions & Rubric

## Overview

You will complete `vending-machine-starter.py` so it fully implements the
menu-driven vending machine described in `user_stories.md`. Every function
you need to write has its acceptance criteria written as comments directly
above it in the starter file — treat those comments as your spec.

**You have the full class period to complete this assessment.**

## What's provided vs. what you write

| Provided for you | You implement |
|---|---|
| `display_products()` | `load_inventory()` |
| `display_main_menu()` | `save_inventory()` |
| `main()` | `main()` |
|  | `get_menu_choice()` |
| | `feed_money()` |
| | `purchase_item()` |
| | `check_balance()` |
| | `log_transaction()` |

Do not rename functions or change what they return — `main()` is mostly already
wired. Implement the code to call them exactly as they're defined.

## Getting started

1. Read `user-stories.md` in full before writing any code.
2. Run the starter file as-is first. It won't do anything useful yet, but
   it should run without crashing — that's your baseline.
3. Work through the functions roughly in this order: `load_inventory` and
   `save_inventory` first (nothing else works without data), then
   `get_menu_choice`, then `feed_money`, then `purchase_item` (the biggest
   one — save it for when you have a solid block of time), then
   `check_balance`, then `log_transaction` last.
4. Test after every function. Don't write all six and then run it for the
   first time — you'll have six bugs at once instead of one.

## Hints for tricky spots

**Returning updated values.** `balance` is a float, and floats aren't
changed in place the way lists or dictionaries are. If `feed_money(balance)`
changes its local `balance` variable, that change disappears the moment the
function ends — unless you `return` the new value. `main()` already does
`balance = feed_money(balance)`, so your job is just to make sure you
`return` the right number.

**`purchase_item` has four ways to fail and one way to succeed.** Before you
write any code, list them out on paper: bad code, out-of-stock, insufficient
balance, and "B" to cancel — vs. the one success path. Check them in an
order that makes sense (e.g., is it "B"? → is the code valid? → is it in
stock? → is the balance enough? → success) and `return` immediately when a
failure case hits, so you don't accidentally deduct money on a failed
purchase.

**Menu input validation needs two different checks.** `int("abc")` raises a
`ValueError` — catch that. But `int("9")` works fine even though 9 isn't a
valid menu option — that's not an exception, it's just a number outside the
range you want, so you check it with a plain `if` after the conversion
succeeds, not with `except`.

**Reading and writing use the same file format.** `load_inventory` and
`save_inventory` both use `"code,name,price,stock"` per line — if you write
`load_inventory` first, look at exactly what it expects to read, and make
`save_inventory` produce exactly that.

**A missing file is not a bug — it's day one.** The very first time this
program runs, `inventory.txt` won't exist yet. That's expected, and it's
exactly what the `FileNotFoundError` handling in `load_inventory` is for.
If you're not sure your handling works, just delete `inventory.txt` and run
the program.

**Test the "can't afford it" and "buy two items" cases on purpose.** These
are easy to forget when you're focused on the happy path, but they're
explicitly in the acceptance criteria (and the rubric).

## Submission

Submit `vending_machine_v2_starter.py` (renamed to include your name, e.g.
`vending_machine_LastName.py`). Leave `inventory.txt` alone — it will be
regenerated automatically if missing.

---

## Rubric (0-4)

### Main Menu & Navigation — 4 pts
| Tier | Criteria |
|---|---|
| **(3–4)** | Menu displays correctly; invalid text input and out-of-range numbers are both handled gracefully with clear messages; program never crashes on menu input. |
| **(2–3)** | Menu works correctly for valid input; handles either bad text OR out-of-range numbers, but not both. |
| **(1–2)** | Menu displays and basic selection works, but invalid input crashes the program or loops incorrectly. |
| **(0–1)** | Menu is missing, non-functional, or selections don't route to the correct action. |

### Feed Money — 4 pts
| Tier | Criteria |
|---|---|
| **(3–4)** | Valid amounts correctly increase balance; non-numeric and non-positive input are both rejected with clear messages and no balance change; event is logged. |
| **(2–3)** | Valid amounts work correctly; handles one invalid case (bad text OR non-positive) but not both. |
| **(1–2)** | Balance updates but validation is missing or a crash occurs on bad input. |
| **(0–1)** | Feed Money doesn't functionally change the balance, or is missing. |

### Purchase Logic (multi-item, balance-based) — 4 pts
| Tier | Criteria |
|---|---|
| **(3-4)** | All five acceptance criteria in US3 are met: cancel with "B," invalid code, out-of-stock, insufficient balance (with exact amount needed), and successful purchase all work correctly; balance and stock update accurately; **a customer can purchase two or more different items in one session**; every purchase is logged. |
| **(2–3)** | Successful purchases work correctly and multiple purchases in one session work, but 1–2 of the failure cases (bad code / out of stock / insufficient balance / cancel) are missing or incorrect. |
| **(1–2)** | Single purchases work, but buying a second item in the same session fails, or balance/stock tracking is inconsistent. |
| **(0–1)** | Purchase logic is missing, crashes, or allows purchases that exceed balance or stock. |

### Exception Handling — 4 pts
| Tier | Criteria |
|---|---|
| **(3–4)** | `ValueError` and `KeyError` are both caught in the appropriate places with specific except clauses (not a bare `except:`); error messages are specific and helpful; the program never crashes from bad user input anywhere in the menu. |
| **(2–3)** | Most exceptions are handled correctly; one gap remains (e.g., a bare `except:` used somewhere, or one input path left unguarded). |
| **(1–2)** | Some exception handling present, but multiple input paths can still crash the program. |
| **(0–1)** | Little or no exception handling; program crashes on common invalid input. |

### File I/O (load / save / log) — 4 pts
| Tier | Criteria |
|---|---|
| **(3–4)** | Inventory loads correctly from file, including correct default-and-create behavior when the file is missing; inventory saves correctly after purchases and on exit; every feed/purchase/exit event is appended (not overwritten) to the log file; `IOError`/`FileNotFoundError` are both handled. |
| **(2–3)** | Load and save both work for the normal case; missing-file handling or log-append behavior has a minor issue. |
| **(1–2)** | Load or save is implemented but unreliable (e.g., overwrites the log instead of appending, or crashes when the file is missing). |
| **(0–1)** | File reading/writing is missing or non-functional. |

### Code Quality — 4 pts
| Tier | Criteria |
|---|---|
| **(3–4)** | Code runs end-to-end without errors; variable and function usage matches what `main()` expects; formatting is consistent and readable; no leftover debug prints. |
| **(2–3)** | Code runs with only minor cosmetic issues. |
| **(1–2)** | Code runs but with rough formatting or minor inconsistencies that don't affect function. |
| **(0–1)** | Code does not run, or requires the grader to fix syntax errors to test it. |

**Total: ____ / 4**

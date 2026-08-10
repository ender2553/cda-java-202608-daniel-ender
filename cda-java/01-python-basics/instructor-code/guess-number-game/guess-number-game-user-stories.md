# Guess the Number — User Stories & Acceptance Criteria

This document describes the *requirements* for the Guess the Number game as a
set of user stories. Each story maps to specific functions and tags in
`guess_number_game.py`, so you can trace a requirement straight to the line
of code that satisfies it.

Format: **As a [role], I want [feature], so that [benefit].**

---

## US-1: Main Menu

**As a** player,
**I want** a menu when I start the program,
**so that** I can choose what to do next.

**Acceptance Criteria**
- [ ] The program displays a numbered menu with exactly three options: Play a new game, View high scores, Exit.
- [ ] The menu reappears after every action (play a round, view scores) until the player exits.
- [ ] Entering anything other than `1`, `2`, or `3` shows an "Invalid option" message and redisplays the menu — it does not crash or exit.

**Implemented by:** `display_menu()`, `main()`

---

## US-2: Exit the Program

**As a** player,
**I want** an explicit exit option,
**so that** I can quit the program cleanly instead of force-closing it.

**Acceptance Criteria**
- [ ] Selecting option `3` from the main menu prints a goodbye message and ends the program.
- [ ] Pressing `Ctrl+C` at any point also ends the program cleanly with a goodbye message, instead of showing a raw traceback.

**Implemented by:** `main()` (menu option `3`), `__main__` block (`KeyboardInterrupt` handler)

---

## US-3: Play a Round

**As a** player,
**I want** to guess a secret number between 1 and 10,
**so that** I can test my luck and see how many tries it takes me.

**Acceptance Criteria**
- [ ] The computer randomly selects a secret number between 1 and 10 (inclusive) at the start of each round.
- [ ] After each guess, the game tells me whether my guess was **too low**, **too high**, or **correct**.
- [ ] The number of attempts is tracked and reported when I guess correctly.
- [ ] A new secret number is chosen each time I start a new round — it is not reused from the previous round.

**Implemented by:** `play_round()`

---

## US-4: Reject Invalid Guesses

**As a** player,
**I want** the game to catch my typing mistakes,
**so that** a bad entry doesn't crash the program or count against me unfairly.

**Acceptance Criteria**
- [ ] If I type something that isn't a whole number (e.g., `"abc"`), the game shows an error message and asks again — it does not crash.
- [ ] If I type a number outside the 1–10 range (e.g., `0` or `15`), the game shows an error message and asks again.
- [ ] Neither of the above counts as a used attempt — only valid, in-range guesses increase my attempt count.

**Implemented by:** `get_guess()` — **Tag: EH1**

---

## US-5: Abandon a Round Early

**As a** player,
**I want** to be able to quit out of a round I've already started,
**so that** I'm not forced to keep guessing if I want to stop.

**Acceptance Criteria**
- [ ] Typing `quit` (case-insensitive) at any guess prompt ends the current round immediately.
- [ ] The game reveals the secret number when a round is abandoned.
- [ ] An abandoned round is **not** saved to the high score file.
- [ ] After abandoning, control returns to the main menu (the program does not exit).

**Implemented by:** `get_guess()`, `play_round()`

---

## US-6: Save My Score

**As a** player who wins a round,
**I want** my name and number of attempts saved,
**so that** I can see how I compare to other players later.

**Acceptance Criteria**
- [ ] After a correct guess, the game asks for my name before returning to the menu.
- [ ] An empty/blank name is rejected and re-prompted — it is not saved.
- [ ] My name and attempt count are appended to `high_scores.txt` without erasing any previously saved scores.
- [ ] If the score cannot be saved (e.g., a file system error), the game shows a message rather than crashing.

**Implemented by:** `get_player_name()` — **Tag: EH2**, `save_high_score()` — **Tag: FIO2**

---

## US-7: View High Scores

**As a** player,
**I want** to see the best past scores,
**so that** I know what score I'm trying to beat.

**Acceptance Criteria**
- [ ] Selecting "View high scores" reads all saved scores from `high_scores.txt`.
- [ ] Scores are sorted from fewest attempts (best) to most attempts.
- [ ] Only the top 5 scores are displayed, numbered 1–5.
- [ ] If no scores have been saved yet, a friendly "no high scores yet" message is shown instead of an error or an empty list.

**Implemented by:** `view_high_scores()` — **Tag: FIO3**, `load_high_scores()` — **Tag: FIO1**

---

## US-8: Handle a Missing or Damaged Score File

**As an** instructor/user running this program for the first time,
**I want** the game to work even if `high_scores.txt` doesn't exist yet or has a bad line in it,
**so that** the program never crashes just because of the state of the data file.

**Acceptance Criteria**
- [ ] If `high_scores.txt` does not exist, the program does **not** crash — it treats the high score list as empty.
- [ ] The very first saved score creates the file automatically (via append mode).
- [ ] If a line in the file is not in the correct `name,attempts` format, that single line is skipped with a warning — the rest of the file still loads normally.

**Implemented by:** `load_high_scores()` — **Tag: FIO1** (`FileNotFoundError` and `ValueError` handling)

---

## Traceability Matrix

| Tag  | Requirement (User Story) | Function                |
|------|---------------------------|--------------------------|
| FIO1 | US-7, US-8                 | `load_high_scores()`    |
| FIO2 | US-6                        | `save_high_score()`     |
| FIO3 | US-7                        | `view_high_scores()`    |
| EH1  | US-4                        | `get_guess()`           |
| EH2  | US-6                        | `get_player_name()`     |

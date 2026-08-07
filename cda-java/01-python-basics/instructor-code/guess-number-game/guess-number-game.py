"""
Guess the Number Game
The computer picks a random number between 1 and 10, and the player has
unlimited guesses to find it. Every win is logged to a high scores file.

Data file (created automatically if missing):
    high_scores.txt  -> name,attempts  (one score per line, best games only)
"""

import random

HIGH_SCORE_FILE = "high_scores.txt"
LOW_NUMBER = 1
HIGH_NUMBER = 10


# ---------------------------------------------------------------------------
# FIO1: Reading a file, with exception handling for a missing file
# ---------------------------------------------------------------------------
def load_high_scores(filename):
    """
    Read the high scores file into a list of dictionaries.

    File format (one score per line): name,attempts
    Example line: Jordan,3

    If the file doesn't exist yet, return an empty list instead of
    crashing the program. The file will be created the first time
    someone wins a game.
    """
    scores = []
    try:
        with open(filename, "r") as file:
            for line in file:
                line = line.strip()
                if not line:
                    continue
                name, attempts = line.split(",")
                scores.append({"name": name, "attempts": int(attempts)})
    except FileNotFoundError:
        print("No high score file found yet. A new one will be created.")
    except ValueError:
        # Guards against a corrupted or hand-edited line in the file
        print("Warning: a line in the high score file was formatted incorrectly and was skipped.")

    return scores


# ---------------------------------------------------------------------------
# FIO2: Appending a new record to a file
# ---------------------------------------------------------------------------
def save_high_score(filename, name, attempts):
    """Append a new score to the high score file."""
    try:
        with open(filename, "a") as file:
            file.write(f"{name},{attempts}\n")
    except OSError as error:
        # Covers issues like a read-only filesystem or a locked file
        print(f"Could not save your score right now: {error}")


# ---------------------------------------------------------------------------
# FIO3: Displaying file contents, sorted, with a clean empty-state message
# ---------------------------------------------------------------------------
def view_high_scores(filename):
    """Print the best (lowest-attempt) scores from the high score file."""
    scores = load_high_scores(filename)

    if not scores:
        print("\nNo high scores yet. Be the first to play!")
        return

    scores.sort(key=lambda record: record["attempts"])

    print("\n----- HIGH SCORES (fewest guesses wins) -----")
    for rank, record in enumerate(scores[:5], start=1):
        print(f"{rank}. {record['name']} — {record['attempts']} guesses")
    print("-----------------------------------------------")


# ---------------------------------------------------------------------------
# EH1: Exception handling around numeric input, with re-prompting
# ---------------------------------------------------------------------------
def get_guess():
    """
    Ask the player for a guess and validate it.

    Keeps prompting until the player enters a whole number in range,
    or types 'quit' to abandon the current round early.
    """
    while True:
        raw_input_value = input(f"Guess a number between {LOW_NUMBER} and {HIGH_NUMBER} (or 'quit'): ").strip()

        if raw_input_value.lower() == "quit":
            return None

        try:
            guess = int(raw_input_value)
        except ValueError:
            print("That's not a whole number. Please try again.")
            continue

        if guess < LOW_NUMBER or guess > HIGH_NUMBER:
            print(f"Please enter a number between {LOW_NUMBER} and {HIGH_NUMBER}.")
            continue

        return guess


# ---------------------------------------------------------------------------
# EH2: Simple input validation loop for a name (non-empty)
# ---------------------------------------------------------------------------
def get_player_name():
    """Ask for and return a non-empty player name."""
    while True:
        name = input("Enter your name for the high score list: ").strip()
        if name:
            return name
        print("Name cannot be blank.")


def play_round():
    """Run one full round of the guessing game. Returns attempts used, or None if abandoned."""
    secret_number = random.randint(LOW_NUMBER, HIGH_NUMBER)
    attempts = 0

    print("\nI'm thinking of a number between 1 and 10. Can you guess it?")

    while True:
        guess = get_guess()

        if guess is None:
            print("Round abandoned. The number was", secret_number)
            return None

        attempts += 1

        if guess < secret_number:
            print("Too low!")
        elif guess > secret_number:
            print("Too high!")
        else:
            print(f"Correct! You guessed it in {attempts} tries.")
            return attempts


def display_menu():
    print("\n===== GUESS THE NUMBER GAME =====")
    print("1. Play a new game")
    print("2. View high scores")
    print("3. Exit")


def main():
    print("Welcome to Guess the Number!")

    while True:
        display_menu()
        choice = input("Choose an option (1-3): ").strip()

        if choice == "1":
            attempts = play_round()
            if attempts is not None:
                name = get_player_name()
                save_high_score(HIGH_SCORE_FILE, name, attempts)
                print("Score saved!")
        elif choice == "2":
            view_high_scores(HIGH_SCORE_FILE)
        elif choice == "3":
            print("\nThanks for playing. Goodbye!")
            break
        else:
            print("Invalid option. Please choose 1, 2, or 3.")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\nSession ended by user. Goodbye!")

#WORD GUESSING GAME PSEUDOCODE

#START

#Choose a word
#Set guesses_left = 7
#Create a list of letters guessed
#Create a hidden version of the word using "_"

#WHILE guesses_left > 0 AND word has not been guessed

    #Display the current progress
    #Display guesses remaining
    #Display letters already guessed

#Ask the player to enter a letter or the complete word
#Convert the input to lowercase

#IF input is empty:
    #Display an error
    #Continue

#IF input contains characters that are not letters:
    #Display an error
    #Continue

#IF input is one letter:
    #IF letter was already guessed:
        #Display an error
        #Continue

#Add letter to guessed letters

#IF letter is in the word:
    #Reveal every position containing that letter
#ELSE:
    #Subtract one guess

#ELSE:
    #IF input matches the complete word:
        #Player wins
        #End game

#ELSE IF guesses_left > 2:
    #Subtract 1 guess
    #Display incorrect guess

#ELSE:
    #Player loses
    #End game

#IF all letters have been revealed:
    #Player wins
    #End game
#IF guesses_left == 0:
    #Player loses

#END







import random


word = "mississippi"



guesses_left = 7
guessed_letters = []
guessed_words = set()



letter_positions = {}



for position, letter in enumerate(word):
    if letter not in letter_positions:
        letter_positions[letter] = []
    letter_positions[letter].apend(position)

def display_progress(word, guessed_letters):
    progress = ""

    for letter in word:
        if letter in guessed_letters:
            progress +=j letter + " "
        else:
            progress += "_"

    print("\nWord:", progress)



def reveal_letter(word, guessed_letters, letter):
    if letter in word:
        guessed_letters.append(letter)
        print(f"Good guess! '{letter}' is in the word.")
        return True
    else:
        print(f"Sorry, '{letter}' is not in the word")
        return False

def word_complete(word, guessed_letters):
    for letter in word:
        if letter not in guessed_letters:
            return False
    return True

def show_status(guesses_left, guessed_letters):
    print(f"\nGuesses remaining: {guesses_left}")
    print(f"Letters guessed: {guessed_letters}")

while guesses_left > 0:

    display_progress(word, guessed_letters)
    show_status(guesses_left, guessed_letters)

    guess = input("Guess a letter or the whole word: ").lower().strip()

    if not guess.isalpha():
        print("Invalid input.  Please enter letters only.")
        continue

    if len(guess) == 1:

        if guess in guessed_letters:
            print("You already guessed that letter.")
            continue

        if not reveal_letter(word, guessed_letters, guess):
            guesses_left -= 1



    else:

        if guess in guessed_words:
            print("You already guessed that word.")
            continue


    guessed_words.add(guess)

    if guess == word: 
        print(f"\nCongratulations!  You guessed the word: {word}")
        break

    elif guesses_left > 2:
        guesses_left -= 1
        print("Incorrect word guess.  You lost 1 guess.")

    else:
        print("\nIncorrect word guess.")
        print("You lost the game.")
        guesses_left = 0


    if word_complete(word, guessed_letters):
        print(f"\nCongratulations!  You guessed the word: {word}")
        break


if guesses_left == 0:
    print(f"\nGame over!  The word was '{word}'.")


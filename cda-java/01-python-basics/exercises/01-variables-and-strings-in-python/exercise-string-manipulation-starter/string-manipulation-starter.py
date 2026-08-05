# # Python Basics - String Manipulation - Exercise
#
# In the String Manipulation lesson, we worked with strings in Python. In this exercise, we want to see your understanding of string manipulation. We have provided some questions and guidance. Enter the code in the section below the question.
#
# ## Upon Completion
#
# Save your work. If you have questions, reach out to your instructor.
#
# ## Exercise

# 1. Given a string, split the values into individual values.

to_be_changed="John Glenn|Neil Armstrong|Sally Ride|Douglas Wheelock|Mae Jemison"
changed_values = to_be_changed.split("|")

print(changed_values)

# 2. We were given these lyrics to split for a karaoke machine.  Split the lyrics by line using `split()`.
#
# Source: [https://en.wikipedia.org/wiki/Take\_Me\_Out\_to\_the\_Ball\_Game](https://en.wikipedia.org/wiki/Take_Me_Out_to_the_Ball_Game) - lyrics originally from 1908 Tin Pan Alley

lyrics = """
Katie Casey was baseball mad,
Had the fever and had it bad.
Just to root for the home town crew,
Ev'ry sou
Katie blew.
On a Saturday her young beau
Called to see if she'd like to go
To see a show, but Miss Kate said "No,
I'll tell you what you can do:"
"""

changed_lyrics = lyrics.split("\n")
print(changed_lyrics)

# 3. We were given these lyrics to split for a karaoke machine.  Split the lyrics by line using something other than `split()`.
km = lyrics
changed_lyrics2 = km.splitlines()
print(changed_lyrics2)
# Source: [https://en.wikipedia.org/wiki/Take\_Me\_Out\_to\_the\_Ball\_Game](https://en.wikipedia.org/wiki/Take_Me_Out_to_the_Ball_Game) - lyrics originally from 1908 Tin Pan Alley

lyrics = """
Katie Casey was baseball mad,
Had the fever and had it bad.
Just to root for the home town crew,
Ev'ry sou
Katie blew.
On a Saturday her young beau
Called to see if she'd like to go
To see a show, but Miss Kate said "No,
I'll tell you what you can do:"
"""



# 4. How long is the long village name string?

long_village_name = "Llanfair­pwllgwyngyll­gogery­chwyrn­drobwll­llan­tysilio­gogo­goch" 
string_length = len(long_village_name)

# 4. We want all folders in this path without additional spaces.  How would you get that?
long_village_name.strip(long_village_name)
         


# 5. Given this list of names, change the third name in the list to be "Wolfgang Mozart".

#composers="Beethoven,Ludwig von;Liszt,Franz;Mozart,Wolfgang;Copland,Aaron"
# Separate the composers
#changed_composers = composers.split(";")
# Get the third composer
#third_composer = composers_split[2]
# Find the comma in the name
#comma_position = third_composer
# Use the slicing notation to get the last name
#last_name = third_composer[0:0]  # TODO: slice out first/last name
# Use the slicing notation to get the first name
#first_name = third_composer[0:0]  # TODO: slice out first/last name
# Join the names to get the 3rd composer's name in "first last" format
#third_composer_name = ""  # TODO: join first + last
# Print the composer's name
#print(third_composer_name)

# 6. Given a right padded string and a left padded string, clean the strings to get the output of "Call now! Operators are standing by"
#
# Hint: You will need to include the `!` when putting the strings together.

left_padded = '                 Operators are standing by'
right_padded = 'Call now                   '
left_padded2 = left_padded.strip()
right_padded2 = right_padded.strip()
message = right_padded2 + "! " + left_padded2
print(message)




# 7. Given the student name, grade, and assignment ID, using old style formating, print out the following:
#
# ```
# 'Student name: Owen, Assignment ID: 0012, Grade: 94.75%'
# ```
#
# Hint: to get the % to appear in the output, you will need to use `%%` to represent the percentage sign in an old style substitution string.

student_name = "Owen"
assignment_id = 12
grade = 94.75
print("Student name: %s, Assignment ID: %04d, grade: %.2f%%" % (student_name, assignment_id, grade))



# 8. Given the employee ID of "30", pad the string with zeroes on the left to have the employee ID appear as 6-digits "000030".

employee_id = "30"
employee_id_padded = employee_id
print(employee_id_padded)

# 9. Print the following statement using raw strings:
#
# ```
# \n represents a new line.
# ```

print()

# 10. Convert the following strings based on their variable names.

i_want_to_yell = 'yeah'
I_NEED_TO_BE_QUIET = 'SHHHHH'
this_is_a_title = 'this is a title'
sWAPcASE = 'sWAPcASE'
capitalize_this = 'capitalize this'

# Use print and a function to achieve what the variable names suggest
YEAH = 'yeah'.upper()
print(YEAH)
SHHHHH = 'SHHHHH'.lower()
print(SHHHHH)
THIS_IS_A_TITLE = 'this is a title'.title()
print(THIS_IS_A_TITLE)
SWAPCASE = 'sWAPcASE'.swapcase()
print(SWAPCASE)
CAPITALIZE_THIS = 'capitalize this'.capitalize()
print(CAPITALIZE_THIS)
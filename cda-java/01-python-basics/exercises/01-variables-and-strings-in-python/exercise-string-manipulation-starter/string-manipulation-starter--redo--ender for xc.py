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

lyrics = """Take me out to the ball game
Take me out with the crowd
Buy me some peanuts and Cracker Jack
I don't care if I never get back"""

lyrics_by_line = lyrics.split("\n")

print(lyrics_by_line)

# 3. We were given these lyrics to split for a karaoke machine.  Split the lyrics by line using something other than `split()`.
# Source: [https://en.wikipedia.org/wiki/Take\_Me\_Out\_to\_the\_Ball\_Game](https://en.wikipedia.org/wiki/Take_Me_Out_to_the_Ball_Game) - lyrics originally from 1908 Tin Pan Alley

lyrics = """Take me out to the ball game
Take me out with the crowd
Buy me some peanuts and Cracker Jack
I don't care if I never get back"""

lyrics_by_line = lyrics.splitlines()

print(lyrics_by_line)



# 4. How long is the long village name string?

long_village_name = "Llanfair­pwllgwyngyll­gogery­chwyrn­drobwll­llan­tysilio­gogo­goch" 

print(len(long_village_name))

# 4. We want all folders in this path without additional spaces.  How would you get that?

#clean_path = path.replace(" ", "")       

#print(clean_path)

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

composers = "Beethoven,Ludwig von;Liszt,Franz;Mozart,Wolfgang;Copland,Aaron"

# Separate the composers
changed_composers = composers.split(";")

# Get the third composer
third_composer = changed_composers[2]

# Find the comma in the name
comma_position = third_composer.find(",")

# Use slicing notation to get the last name
last_name = third_composer[0:comma_position]

# Use slicing notation to get the first name
first_name = third_composer[comma_position + 1:]

# Join the names to get the 3rd composer's name in "first last" format
third_composer_name = first_name + " " + last_name

# Print the composer's name
print(third_composer_name)

# 6. Given a right padded string and a left padded string, clean the strings to get the output of "Call now! Operators are standing by"
# Hint: You will need to include the `!` when putting the strings together.

right_padded = "Call now! Operators are standing by     "
left_padded = "     Call now! Operators are standing by"

right_clean = right_padded.strip()
left_clean = left_padded.strip()

print(right_clean)
print(left_clean)




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

print("Student name: %s, Assignment ID: %04d, Grade: %.2f%%" %
      (student_name, assignment_id, grade))


# 8. Given the employee ID of "30", pad the string with zeroes on the left to have the employee ID appear as 6-digits "000030".

employee_id = "30"

employee_id = employee_id.zfill(6)

print(employee_id)

# 9. Print the following statement using raw strings:
#
# ```
# \n represents a new line.
# ```

print(r"\n represents a new line.")

# 10. Convert the following strings based on their variable names.

i_want_to_yell = 'yeah'
I_NEED_TO_BE_QUIET = 'SHHHHH'
this_is_a_title = 'this is a title'
sWAPcASE = 'sWAPcASE'
capitalize_this = 'capitalize this'

# Use print and a function to achieve what the variable names suggest

i_want_to_yell = 'yeah'
I_NEED_TO_BE_QUIET = 'SHHHHH'
this_is_a_title = 'this is a title'
sWAPcASE = 'sWAPcASE'
capitalize_this = 'capitalize this'

print(i_want_to_yell.upper())
print(I_NEED_TO_BE_QUIET.lower())
print(this_is_a_title.title())
print(sWAPcASE.swapcase())
print(capitalize_this.capitalize())
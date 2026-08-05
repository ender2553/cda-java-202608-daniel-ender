# # Python Basics - Variables - Exercise
# In the Variables lesson, we covered the different types of variables available in Python. In this exercise, we want to see your understanding of variables. We have provided some questions and guidance. Enter the code in the section below the question.
# ## Exercise

# 1. Create a variable named `onomatopoeia` and set its value to 'pop'.  Print the type for `onomatopoeia`.
onomatopoeia = "pop"
print(type(onomatopoeia))

# 2. Create a variable named `lyrics` and set its value to:
#
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

# Source: https://en.wikipedia.org/wiki/Take_Me_Out_to_the_Ball_Game - lyrics originally from 1908 Tin Pan Alley



# 3. Create a variable named `gpa` for a grade point average and set it to 3.75.  Print out the variable.  Then print out the variable's type.
gpa = 3.75
print(type(gpa))
print(gpa)



# 4. Create a variable called `empty_var` and set it to the Python equivalent for null.  Print out the variable.  Then print out the variable's type.
empty_var = None
print(type(empty_var))
print(empty_var)
# 5. Create a variable to store $8^{2}$. Print the variable.
A1=828**282
print(A1)



# 6. Create a variable to store the following expression:
#
# $$ (52 - 52) + (64 / 8) + \text{the remainder of } (42/8) $$
vs6 = ( 52 - 52 ) + ( 64 / 8 ) + ( 42 % 8 )
print(vs6)




# Print the final result of the expression.




# 7. Create a variable named `true` and set it to the boolean value for true.  Create a second variable named `isTrue` and set it to check if `true is not False`.  Print `isTrue`
true = True
isTrue = true is not False
print(isTrue)


# 8. Create a variable named `weird_word` and set its value to 'Weird'.  Create a second variable to check whether `weird_word` matches 'weird'.  Print out the second variable.
weird_word = "Weird"
is_weird_word = weird_word == "weird"
print(is_weird_word)


# Hint: Use the `==` operator to compare the two strings for equality.



# 9. Create a variable and set it to `false`.  What error occurs?
#vs9 = false
#NameError: name 'false' is not defined. Did you mean: 'False'?



# 10. Create a variable for the value of $\frac{35}{7}$. Create another variable for the value of $15^{3}$. Create a third variable for the value of variable 2 `%` variable 1. Create a fourth variable for the value of variable 2 `/` variable 1. Compare the results.
vs10_1 = 35 / 7
vs10_2 = 15 ** 3
vs10_3 = vs10_1 % vs10_2
vs10_4 = vs10_2 / vs10_1
print(vs10_1)
print(vs10_2)
print(vs10_3)
print(vs10_4)




# 11. Use modular arithmetic to show how 13 hours is the same as 1 o'clock, 16 hours is the same as 4 o'clock, etc.
value1 = 13 % 12
value2 = 16 % 12
print("13 hours is the same as", value1, "o'clock")
print("16 hours is the same as", value2, "o'clock")
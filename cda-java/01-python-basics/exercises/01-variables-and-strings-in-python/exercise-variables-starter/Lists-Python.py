#Movie Night Snack Inventory

#1. Creat a list called snacks with the following items: "popcorn", "chocolate", "soda", "chips"
#2. Print the list and print the first and last item using indexing.

snacks = ["popcorn", "chocolate", "soda", "chips"]
print(snacks)
print(snacks[0])
print(snacks[-1])

#3. You want to swap "soda" for "juice".  Use indexing to make the change.

snacks[2] = "juice"
print(snacks)

#4. Use append() to add "gummies" to the end of the list.
#5. Print the updated snack list.

snacks.append("gummies")
print(snacks)

#6. You decide "chocolate" should go to the front of the list. Use pop() to remove it from its current spot and insert() to place it at index 0.
#7. Print the new list.

chocolate = snacks.pop(1)
snacks.insert(0, chocolate)
print(snacks)

#8. Print a slice of just the first three snacks.
#9. Print every snack except the last one using slicing.

print(snacks[:3])
print(snacks[:-1])

#10. Use pop() to remove the last snack from the list and store it in a variable named removed_snack.
#11. Print the removed item and the final snack list.

removed_snack = snacks.pop()
print(removed_snack)
print(snacks)

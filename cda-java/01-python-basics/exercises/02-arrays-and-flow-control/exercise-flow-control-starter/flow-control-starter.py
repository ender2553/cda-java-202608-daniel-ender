# Set up the product menu
items = {
    "soda": 1.50,
    "chips": 2.00,
    "candy": 1.00
}

# Prompt the user for the money they are inserting and convert it to a float named funds
#pseudocode: use the input() function to get the amount of money from the user and convert it to a float, this prompts user for amount, and converts it to a float, storing it in the variable funds the input to a float point.
funds = float(input("Enter the amount of money you are inserting: $"))
# Run the transaction process:
#   - Loop while the user still has funds remaining
#Use a while loop that continues as long as funds is greater than 0, this loop will continue to run as long as the user has funds remaining, allowing them to make multiple purchases until they run out of money.
while funds > 0:pass
#   - Display the menu of items and prices
print("Menu:")
for item, price in items.items():
    print(f"{item}: ${price:.2f}")
#   - Prompt the user to select an item
#Prompt the user to select item usuing the input()
choice = input("Select an item (soda, chips, candy): ")

#   - If the item is not on the menu: print an error and use continue
#Check if selected item exists in the items dictionary, if not print an error and continue to the next iteration of the loop.
if choice not in items:
    print("Error: Item not on the menu.")
    continue

#   - If the item is found and funds are sufficient: subtract the price,
#     print a dispensed message and the new balance, then ask to buy again
#Check if the user has enough funds, subtracts them from the price of the funds, prints a dispensed message and the new balance, ask if the user wants to buy another item, and exits the loop if they answer no.
if funds >= items[choice]:
    funds -= items[choice]
    print(f"Dispensed {choice}")
    print(f"remaining balance: ${funds:.2f}")
    again = input("Would you like to buy another item? (yes/no): ")
    if again != "yes":
        break

#   - If funds are insufficient: ask to add money (update funds) or cancel with break
#Print an "insufficient funds" message and ask the user if they want to add more money, if yes, prompt to add additional funds, otherwise exit the loop.
else:
    print("Insufficient funds.")
    add_more = input("Would you like to add more money? (yes/no): ")
    if add_more == "yes":
        additional_funds = float(input("Enter the additional amount: $"))
        funds += additional_funds
    else:
        break
# Show a final thank-you message and the remaining balance
#after the loop ends, print a thank-you message and remaining balance
print(Thank you for your purchase!")
print(f"remaining balance: ${funds:.2f}")
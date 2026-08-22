# Initialize an empty list to store grocery items
groceries = []

# Function to add items to the list
def add_item(items, item):
    items.append(item)

add_item(groceries, "bread")
add_item(groceries, "milk")
add_item(groceries, "eggs")

print(groceries)

# Function to view the list
def view_list(items):
    print(items)

# Function to sort the list
def sort_list(items):
    items.sort()
sort_list(groceries)

print(groceries)

# Demonstrate using a tuple

groceries_tuple = ("bread", "milk", "eggs")

print(groceries_tuple)

# Main program loop

while True:
    print("\n1. Add item")
    print("2. View List")
    print("3. Sort List")
    print("4. Exit")

    choice = input("Choose an option: ")

    if choice == "1":
        item = input("Enter grocery item:")
        add_item(groceries, item)

    elif choice == "2":
        view_list(groceries)
    elif choice == "3":
        sort_list(groceries)
    elif choice == "4":
        break
    else:
        print("Invalid option. Please try again.")
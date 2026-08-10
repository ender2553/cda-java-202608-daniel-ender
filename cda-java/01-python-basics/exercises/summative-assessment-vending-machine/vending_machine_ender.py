"""
Vending Machine — Student Starter Code (SUMMATIVE ASSESSMENT)
Topics: Variables, Data Types, Loops, Logical Branching, Collections,Functions, Exception Handling, File I/O, 
Menu-Driven Programs, State Tracking

Read user-stories.md and rubric-and-instructions.md before you start.

You are building a vending machine with:
  - A main menu (View Products / Feed Money / Purchase / Check Balance / Exit)
  - A running balance the customer feeds money into
  - The ability to purchase MULTIPLE items in one session, as long as the
    balance covers each purchase
  - Change returned automatically when the customer exits

Every function below has its acceptance criteria written as comments —
that's your spec. main() is partially provided and already wires everything
together once the TODOs are completed, so once your functions behave correctly, the whole program
should work. Do not change the function names or what they return;
main() depends on them.

Data files (should be created automatically if missing):
    inventory.txt        -> code,name,price,stock  (one item per line)
    transaction_log.txt  -> appended record of every feed/purchase/exit event
"""

INVENTORY_FILE = "inventory.txt" #constant for the inventory file location
LOG_FILE = "transaction_log.txt" #constant for the transaction log file location


def create_default_inventory():
    """Create a default inventory for the vending machine. Returns a starter inventory dict. This is only used if no inventory file exists yet.  
       Create a dictionary with a default stock level of 5 for each item.  Keys are the item codes (e.g., "A1", "A2") and values are dictionaries with "name", "price", and "stock" fields.
    """
    return {
       # TODO: fill in the default inventory items with a stock level of 5 each (a nested dictionary or dictionary of dictionaries)
    }


# =============================================================================
# ACCEPTANCE CRITERIA (US6 — Load Inventory):
#   - GIVEN inventory.txt exists, WHEN the program starts, THEN each line
#     ("code,name,price,stock") is loaded into the inventory dictionary.
#   - GIVEN inventory.txt does NOT exist, WHEN the program starts, THEN a
#     FileNotFoundError is caught, a default inventory is created, and it
#     is saved to inventory.txt so the file exists going forward.
#
# HINT: This is the same load_inventory() you wrote for the practice
# vending machine — you can reuse that logic here.
# =============================================================================
def load_inventory(filename):
    inventory = {}
    # TODO: implement (see acceptance criteria above) 
    # reusable logic from the vending machine can be applied here
    return inventory


# =============================================================================
# ACCEPTANCE CRITERIA (US7 — Save Inventory):
#   - WHEN save_inventory() is called, THEN the current inventory dict is
#     written to filename in the same "code,name,price,stock" format.
#   - IF the file can't be written, THEN an IOError is caught and a warning
#     is printed instead of crashing the program.
# =============================================================================
def save_inventory(inventory, filename):
    # TODO: implement (see acceptance criteria above)
    pass


# =============================================================================
# ACCEPTANCE CRITERIA (US8 — Transaction Log):
#   - WHEN log_transaction() is called, THEN `message` is appended as a new
#     line to the log file (existing log entries are NOT overwritten).
#   - IF the log file can't be written, THEN an IOError is caught and a
#     warning is printed — a failed log write should never crash a sale.
# =============================================================================
def log_transaction(message, filename=LOG_FILE):
    # TODO: implement (see acceptance criteria above)
    pass


def display_products(inventory):
    """Provided for you. Prints a formatted product table."""
    print("\n===== PRODUCTS =====")
    print(f"{'Code':<6}{'Item':<15}{'Price':<10}Stock")
    for code, details in inventory.items():
        stock_note = "OUT OF STOCK" if details["stock"] <= 0 else details["stock"]
        print(f"{code:<6}{details['name']:<15}${details['price']:<9}{stock_note}")
    print("=====================")


def display_main_menu():
    """Provided for you. Prints the main menu options."""
    print("\n===== VENDING MACHINE =====")
    print("1. View Products")
    print("2. Feed Money")
    print("3. Purchase Item")
    print("4. Check Balance")
    print("5. Exit")
    print("============================")


# =============================================================================
# ACCEPTANCE CRITERIA (US1 — Main Menu Navigation):
#   - GIVEN the main menu is displayed, WHEN the customer enters a number
#     from 1-5, THEN that number is returned.
#   - WHEN the customer enters something that isn't a number, THEN a
#     ValueError is caught, an error message is shown, and they are
#     re-prompted.
#   - WHEN the customer enters a number outside 1-5, THEN an error message
#     is shown and they are re-prompted.
#
# HINT: This needs a loop that doesn't end until a *valid* choice comes in.
# Think about what has to happen to make it out of the loop each way
# (bad type, out-of-range number, good number).
# =============================================================================
def get_menu_choice():
    # TODO: implement (see acceptance criteria above)
    return 5


# =============================================================================
# ACCEPTANCE CRITERIA (US2 — Feed Money):
#   - GIVEN the customer selects "Feed Money", WHEN they enter a valid
#     positive number, THEN it is added to their balance and the new
#     balance is displayed.
#   - WHEN the entered amount is not a valid number, THEN a ValueError is
#     caught, an error message is shown, and they are re-prompted.
#   - WHEN the entered amount is zero or negative, THEN an error message
#     is shown and they are re-prompted.
#   - WHEN money is successfully added, THEN a transaction log entry is
#     recorded via log_transaction().
#
# HINT: This function receives the OLD balance and must return the NEW
# balance — floats aren't mutable the way lists/dicts are, so `balance`
# inside this function is a totally separate variable from `balance` in
# main() unless you return it and reassign it there (main() already does
# the reassigning for you — just make sure you return the right value).
# =============================================================================
def feed_money(balance):
    # TODO: implement (see acceptance criteria above)
    return balance


# =============================================================================
# ACCEPTANCE CRITERIA (US3 — Purchase Item):
#   - GIVEN the customer selects "Purchase", WHEN they enter a valid,
#     in-stock item code AND their balance covers the price, THEN the
#     price is deducted from the balance, the item's stock decreases by 1,
#     and a confirmation with remaining balance is shown.
#   - WHEN the customer enters "B", THEN they return to the main menu with
#     no changes made.
#   - WHEN the customer enters a code that isn't in the inventory, THEN a
#     KeyError is caught and a friendly error message is shown — no crash,
#     no changes made.
#   - WHEN the selected item's stock is 0, THEN an "out of stock" message
#     is shown and no changes are made.
#   - WHEN the balance is less than the item's price, THEN a message tells
#     the customer exactly how much more they need, and no changes are
#     made (stock and balance stay the same).
#   - WHEN a purchase succeeds, THEN a transaction log entry is recorded.
#   - A customer must be able to call this function multiple times in one
#     session (e.g., from the main menu loop) and successfully purchase
#     more than one item, as long as their balance allows it.
#
# HINT: This is the trickiest function in the assessment. It has FOUR ways
# to fail (bad code, out of stock, insufficient balance, "B" to cancel)
# and only ONE way to succeed — check them in order and `return` early
# (or use if/elif) for each failure case so you don't accidentally
# deduct money when you shouldn't. This function returns BOTH the
# inventory and the balance, even on the failure paths.
# =============================================================================
def purchase_item(inventory, balance):
    display_products(inventory)
    # TODO: implement (see acceptance criteria above)
    return inventory, balance


# =============================================================================
# ACCEPTANCE CRITERIA (US4 — Check Balance):
#   - GIVEN the customer selects "Check Balance", WHEN the option runs,
#     THEN the current balance is printed, formatted as currency
#     (e.g., "$3.50").
# =============================================================================
def check_balance(balance):
    # TODO: implement (see acceptance criteria above)
    pass


def main():
    """
    Provided for you — you only need to leverage the functions defined above.

    ACCEPTANCE CRITERIA (US5 — Exit & Change Return), already implemented
    here so you can see how it's supposed to work:
      - WHEN the customer selects "Exit", THEN any remaining balance is
        displayed and returned to them as change.
      - WHEN the customer exits, THEN the inventory is saved and a final
        transaction log entry is recorded, whether or not change was owed.
    """
    print("Welcome to the Python Vending Machine!")
    #TODO: load the inventory from the file, or create a default one if the file doesn't exist
    inventory = load_inventory(INVENTORY_FILE)
    balance = 0.0

    while True:
        display_main_menu()
        #TODO: get the user's menu choice.
        choice = 5  #leverage existing function
        
        if choice == 1: 
            #TODO: display products - leverage existing function.
            todo = "replace this todo statement with correct code"

        elif choice == 2:
             #TODO: add money to vending machine - leverage existing function.
             #balance = replace this with correct code
             todo = "replace this todo statement with correct code"

        elif choice == 3:
            inventory, balance = purchase_item(inventory, balance)
            save_inventory(inventory, INVENTORY_FILE)

        elif choice == 4:
            #TODO: check balance - leverage existing function.
            todo = "replace this todo statement with correct code"

        elif choice == 5:
            if balance > 0:
                print(f"\nReturning your change: ${balance}")
                log_transaction(f"SESSION END - change returned: ${balance}")
            else:
                log_transaction("SESSION END - no change returned")
            save_inventory(inventory, INVENTORY_FILE)
            print("Thank you for using the vending machine. Goodbye!")
            break


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\nSession ended by user. Goodbye!")

def load_inventory(filename):
    inventory = {}

    print(inventory)

    
#if the inventory list was suppose to auto-populate, then I was unable to get it to.
#===========================SET UP==Display menu items, price, quantity, and create main menu.===========

inventory = {"A1": {"item": "chips", "price": 1.50, "quantity": 5}, 
             "A2": {"item": "candy bar", "price": 1.25, "quantity": 5},
             "B1": {"item": "soda", "price": 2.00, "quantity": 5},
             "B2": {"item": "water", "price": 1.75, "quantity": 5},
             "C1": {"item": "gum", "price": 0.75, "quantity": 5},
             "D1": {"item": "chocolate", "price": 2.25, "quantity":5}}

print(inventory)



INVENTORY_FILE = "inventory.txt" 
LOG_FILE = "transaction_log.txt"



def view_inventory(inventory):
    print("\n---Inventory---")

    for slot, details in inventory.items():
        print(
            f"{slot}: {details['item']} | "
            f"Price: ${details['price']: .2f} | "
            f"Quantity: {details['quantity']}" 
        )

view_inventory(inventory)



def main_menu():
    print("\n===== VENDING MACHINE =====")
    print("1. View Products")
    print("2. Feed Money")
    print("3. Purchase Item")
    print("4. Check Balance")
    print("5. Exit")
    print("===========================")

main_menu()



def main_menu():
    while True:
        print("\n===== VENDING MACHINE ====")
        print("1. View Products")
        print("2. Feed Money")
        print("3. Purchase Item")
        print("4. Check Balance")
        print("5. Exit")
        print("==========================")

        choice = input("Please select an option: ")

        if choice  == "1":
            print("Viewing products...")
        elif choice == "2":
            print("Feeding money...")
        elif choice == "3":
            print("Purchasing item...")
        elif choice == "4":
            print("Thank you!  Goodbye!")
            break
        else:
            print("Invalid selection.  Please choose 1-5.")



main_menu()



        

#====================LOOP FOR SELECTING ITEM AND INPUT MONEY===========================
def log_transaction(message, filename=LOG_FILE):

    pass


def get_menu_choice():
    while True:
        try:
            choice = int(input("Please select an option (1-5): "))

            if choice < 1 or choice > 5:
                print("Error: Please enter a number between 1 and 5.")
                continue

            return choice

        except ValueError:
            print("Error: Please enter a number.")


def feed_money(balance):
    while True:
        try:
            amount = float(input("Enter amount to feed: $"))

            if amount <= 0:
                print("Error: Amount must be greater than zero.")
                continue


            balance += amount

            print(f"Money added: ${amount: .2f}")
            print(f"Current balance: ${balance: .2f}")

            log_transaction(f"Fed ${amount: .2f}")

            return balance

        except ValueError:
            print("Error: Please enter a valid number.")



balance = 0.00

while True:
    print("\n===== VENDING MACHINE =====")
    print("1. View Products")
    print("2. Feed Money")
    print("3. Purchase Item")
    print("4. Check Balance")
    print("5. Exit")

    choice = get_menu_choice()



    if choice == 1:
        print("Viewing products.")

    elif choice == 2:
        balance = feed_money(balance)

    elif choice == 3:
        print ("Purchase Item selected.")

    elif choice == 4:
        print(f"Current balance: ${balance:.2f}")

    elif choice == 5:
        print("Thank you!  Goodbye!")
        break


def feed_money(balance):
    while True:
        try:
            money = float(input("Enter money"))

            if money <= 0:
                print("Error: Please enter a positive amount.")
                continue


            balance += money

            print(f"Money added: ${money: .2f}")
            print(f"Current balance: ${balance:.2f}")


            return balance

        except ValueError:
            print("Error: Please enter a valid number.")



#When I input values into terminal, nothing happens, unsure why==========


#This is not complete, but wanted to turn in something ==========

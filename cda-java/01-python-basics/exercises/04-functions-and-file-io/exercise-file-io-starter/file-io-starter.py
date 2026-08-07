# Solution for File I/O Exercise

import os

# Task 1: Create and Write to a Text File

with open("excercise_data.txt", "w") as file:
    file.write("1, apple, red\n")
    file.write("2, banana, yellow\n")
    file.write("3, cherry, red\n")
    file.write("4, date, brown\n")
    file.write("5, elderberry, black\n")


# Task 2: Read from a Text File

with open("excercise_data.txt", "r") as file:
    data = file.read()

    print(data)

# Task 3: Append to a Text File

with open("excercise_data.txt", "a") as file:
    file.write("6, fig, purple\n")
    file.write("7, grape, green\n")

# Task 4: Read and Display Updated File Contents

with open("excercise_data.txt", "r") as file:
    for line in file:
        print(line.strip())

# Task 5: Write to a Binary File

with open("excercise_data.bin", "wb") as file:
    file.write(b"101,carrot, orange\n")
    file.write(b"102,potato,brown\n")
    file.write(b"103, broccoli,green\n")
    
# Task 6: Read from a Binary File

with open("excercise_data.bin", "rb") as file:
    for line in file:
        print(line.decode().strip())
# Task 7: Delete a File
import os

os.remove("excercise_data.bin")

# Task 8: Use the with Statement (Tasks 1-4 rewritten with exception handling)

try:
    with open("excercise_data.txt", "r") as file:
        data = file.read()
        print(data)
except FileNotFoundError:
    print("Error: excercise_data.txt was not found.")
except Exception as e:
    print(f"Error: {e}")

# Task 1: Create and Write to a Text File with 'with' statement

with open("excercise_data.txt", "w") as file:
    file.write("1, apple, red\n")
    file.write("2, banana, yellow\n")
    file.write("3, cherry, red\n")
    file.write("4, date, brown\n")
    file.write("5, elderberry, black\n")

# Task 2: Read from a Text File with 'with' statement

with open("excercise_data.txt", "r") as file:
    for line in file:
        print(line.strip())

# Task 3: Append to a Text File with 'with' statement

with open("excercise_data.txt", "a") as file:
    file.write("6, fig, purple\n")
    file.write("7, grape, green\n")

# Task 4: Read and Display Updated File Contents with 'with' statement

with open("excercise_data.txt", "r") as file:
    for line in file:
        print(line.strip())
        
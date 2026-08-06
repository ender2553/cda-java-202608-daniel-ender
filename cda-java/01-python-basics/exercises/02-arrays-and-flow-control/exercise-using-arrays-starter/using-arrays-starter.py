# Import the built-in array module
import array
# Import NumPy for vectorized analysis
#import numpy as np
# Step 1: Create a float ('f') array named temperatures with the week's readings and print it
temperatures = array.array('f', [72.5, 74.0, 69.8, 70.2, 73.1, 75.6, 71.3])
print(temperatures)
# Step 2: Remove the incorrect reading at index 3, then insert 70.0 at index 3 (no loops)
temperatures.pop(3)
temperatures.insert(3, 70.0)
print(temperatures)
# Step 3: Append the extra reading 78.0 to the end of the array
temperatures.append(78.0)
print(temperatures)
# Step 4: Add 1.0 to every element without an explicit loop (list comprehension or NumPy)
temperatures = array.array('f', [temp + 1.0 for temp in temperatures])
print(temperatures)
# Step 5: Convert to a NumPy array and print the average using np.mean()
temperatures = np.array(temperatures)
print(np.mean(temperatures))
# Step 6: Convert Fahrenheit to Celsius with vectorized arithmetic and print the result
celcius = (temperatures - 32) * 5/9
print(celcius)
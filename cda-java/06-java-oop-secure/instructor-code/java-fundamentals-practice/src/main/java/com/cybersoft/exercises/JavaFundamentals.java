package com.cybersoft.exercises;

import java.math.BigDecimal;
import java.util.*;

/**
 * Complete each method, then run: mvn test. Do not change the method signatures.
 */
public final class JavaFundamentals {
    private JavaFundamentals() {
    }

    private static UnsupportedOperationException todo(int n) {
        return new UnsupportedOperationException("TODO exercise " + n);
    }

    /**
     * Exercise 1: Add Two Numbers.
     * <p><strong>Student task:</strong> Return the sum of a and b.</p>
     * <p><strong>Example:</strong> addNumbers(4, 6) returns 10.</p>
     */
    public static int addNumbers(int a, int b) {

        return a + b;
    }

    /**
     * Exercise 2: Double It.
     * <p><strong>Student task:</strong> Return two times number.</p>
     * <p><strong>Example:</strong> doubleNumber(-3) returns -6.</p>
     */
    public static int doubleNumber(int n) {

        return n * 2;
    }

    /**
     * Exercise 3: Rectangle Area.
     * <p><strong>Student task:</strong> Return length multiplied by width.</p>
     * <p><strong>Example:</strong> rectangleArea(5, 4) returns 20.</p>
     */
    public static int rectangleArea(int l, int w) {
        return l * w;
    }

    /**
     * Exercise 4: Minutes to Seconds.
     * <p><strong>Student task:</strong> Convert minutes to seconds.</p>
     * <p><strong>Example:</strong> minutesToSeconds(5) returns 300.</p>
     */
    public static int minutesToSeconds(int m) {
        return m * 60;
    }

    /**
     * Exercise 5: Is Even.
     * <p><strong>Student task:</strong> Return true when number is evenly divisible by 2.</p>
     * <p><strong>Example:</strong> isEven(8) is true; isEven(7) is false.</p>
     */
    public static boolean isEven(int n) {
        return n % 2 == 0;
    }

    /**
     * Exercise 6: Larger Number.
     * <p><strong>Student task:</strong> Return the larger of a and b. Either value is valid when they are equal.</p>
     * <p><strong>Example:</strong> larger(3, 9) returns 9.</p>
     */
    public static int larger(int a, int b) {
        return Math.max(a, b);
    }

    /**
     * Exercise 7: Can Vote.
     * <p><strong>Student task:</strong> Return true when age is at least 18.</p>
     * <p><strong>Example:</strong> canVote(18) is true.</p>
     */
    public static boolean canVote(int age) {
        return age >= 18;
    }

    /**
     * Exercise 8: Number Sign.
     * <p><strong>Student task:</strong> Return "positive", "negative", or "zero" based on number.</p>
     * <p><strong>Example:</strong> numberSign(-4) returns "negative".</p>
     */
    public static String numberSign(int n) {
        if (n > 0) {
            return "positive";
        } else if (n < 0) {
            return "negative";
        } else {
            return "zero";
        }
    }

    /**
     * Exercise 9: Inclusive Range.
     * <p><strong>Student task:</strong> Return true when number is from 10 through 20, inclusive.</p>
     * <p><strong>Example:</strong> inRange(10) and inRange(20) are true.</p>
     */
    public static boolean inRange(int n) {
        return n >= 10 && n <= 20;
    }

    /**
     * Exercise 10: Leap Year.
     * <p><strong>Student task:</strong> Apply the Gregorian rule: divisible by 4, except centuries unless divisible by 400.</p>
     * <p><strong>Example:</strong> 2024 and 2000 are leap years; 1900 is not.</p>
     */
    public static boolean isLeapYear(int y) {
        return y % 400 == 0 || (y % 4 == 0 && y % 100 != 0);
    }

    /**
     * Exercise 11: Sum to N.
     * <p><strong>Student task:</strong> Return the sum of every integer from 1 through n. Return 0 when n is below 1.</p>
     * <p><strong>Example:</strong> sumToN(5) returns 15.</p>
     */
    public static int sumToN(int n) {
        if (n < 1) {
            return 0;
        }

        int sum = 0;

        for (int i = 1; i <= n; i++) {
            sum += i;
        }

        return sum;
    }

    /**
     * Exercise 12: Factorial.
     * <p><strong>Student task:</strong> Return n factorial. Treat 0 factorial as 1 and reject negative n.</p>
     * <p><strong>Example:</strong> factorial(5) returns 120.</p>
     */
    public static long factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must not be negative");
        }

        long result = 1;

        for (int i = 1; i <= n; i++) {
            result *= i;
        }

        return result;
    }

    /**
     * Exercise 13: Count Evens.
     * <p><strong>Student task:</strong> Count the even integers from 1 through n, inclusive.</p>
     * <p><strong>Example:</strong> countEvens(10) returns 5.</p>
     */
    public static int countEvens(int n) {
        int count = 0;

        for (int i = 1; i <= n; i++) {
            if (i % 2 == 0) {
                count++;
            }
        }

        return count;
    }

    /**
     * Exercise 14: FizzBuzz.
     * <p><strong>Student task:</strong> Return Fizz for multiples of 3, Buzz for 5, FizzBuzz for both, otherwise the number as text.</p>
     * <p><strong>Example:</strong> fizzBuzz(15) returns "FizzBuzz".</p>
     */
    public static String fizzBuzz(int n) {
        if (n % 3 == 0 && n % 5 == 0) {
            return "FizzBuzz";
        } else if (n % 3 == 0) {
            return "Fizz";
        } else if (n % 5 == 0) {
            return "Buzz";
        } else {
            return String.valueOf(n);
        }
    }

    /**
     * Exercise 15: Count Digits.
     * <p><strong>Student task:</strong> Return the number of base-10 digits, ignoring a negative sign. Zero has one digit.</p>
     * <p><strong>Example:</strong> countDigits(12345) returns 5.</p>
     */
    public static int countDigits(int n) {
        if (n == 0) {
            return 1;
        }

        n = Math.abs(n);
        int count = 0;

        while (n > 0) {
            n = n / 10;
            count++;
        }

        return count;
    }

    /**
     * Exercise 16: First Character.
     * <p><strong>Student task:</strong> Return the first character of a nonempty string; reject null or empty input.</p>
     * <p><strong>Example:</strong> firstCharacter("Java") returns J.</p>
     */
    public static char firstCharacter(String s) {
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException("String cannot be null or empty");
        }

        return s.charAt(0);
    }

    /**
     * Exercise 17: Count Letter A.
     * <p><strong>Student task:</strong> Count uppercase and lowercase letter A characters.</p>
     * <p><strong>Example:</strong> countA("Java Application") returns 4.</p>
     */
    public static int countA(String s) {
        int count = 0;

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == 'A' || s.charAt(i) == 'a') {
                count++;
            }
        }

        return count;
    }

    /**
     * Exercise 18: Reverse String.
     * <p><strong>Student task:</strong> Return text with its characters in reverse order; reject null.</p>
     * <p><strong>Example:</strong> reverse("Java") returns "avaJ".</p>
     */
    public static String reverse(String s) {
        if (s == null) {
            throw new IllegalArgumentException("String cannot be null");
        }

        StringBuilder reversed = new StringBuilder();

        for (int i = s.length() - 1; i >= 0; i--) {
            reversed.append(s.charAt(i));
        }

        return reversed.toString();
    }

    /**
     * Exercise 19: Palindrome.
     * <p><strong>Student task:</strong> Return true when text reads the same forward and backward. Return false for null.</p>
     * <p><strong>Example:</strong> isPalindrome("racecar") is true.</p>
     */
    public static boolean isPalindrome(String s) {
        if (s == null) {
            return false;
        }

        for (int i = 0; i < s.length() / 2; i++) {
            if (s.charAt(i) != s.charAt(s.length() - 1 - i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Exercise 20: Mask Email.
     * <p><strong>Student task:</strong> Keep the first username character and domain, replacing the remaining username characters with asterisks. Reject malformed input.</p>
     * <p><strong>Example:</strong> maskEmail("myron@example.com") returns "m****@example.com".</p>
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }

        int atIndex = email.indexOf('@');

        if (atIndex <= 0 || atIndex == email.length() - 1) {
            throw new IllegalArgumentException("Invalid email");
        }

        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        StringBuilder masked = new StringBuilder();
        masked.append(username.charAt(0));

        for (int i = 1; i < username.length(); i++) {
            masked.append('*');
        }

        masked.append(domain);

        return masked.toString();
    }

    /**
     * Exercise 21: Array Sum.
     * <p><strong>Student task:</strong> Return the sum of all array elements; reject a null array.</p>
     * <p><strong>Example:</strong> arraySum(new int[]{2,4,6}) returns 12.</p>
     */
    public static int arraySum(int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }

        int sum = 0;

        for (int value : a) {
            sum += value;
        }

        return sum;
    }

    /**
     * Exercise 22: Find Maximum.
     * <p><strong>Student task:</strong> Return the largest value in a nonempty array.</p>
     * <p><strong>Example:</strong> findMax(new int[]{-8,-2,-5}) returns -2.</p>
     */
    public static int findMax(int[] a) {
        if (a == null || a.length == 0) {
            throw new IllegalArgumentException("Array cannot be null or empty");
        }

        int max = a[0];

        for (int value : a) {
            if (value > max) {
                max = value;
            }
        }

        return max;
    }

    /**
     * Exercise 23: Count Positives.
     * <p><strong>Student task:</strong> Count values greater than zero; zero is not positive.</p>
     * <p><strong>Example:</strong> countPositive(new int[]{1,-1,2}) returns 2.</p>
     */
    public static int countPositive(int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }

        int count = 0;

        for (int value : a) {
            if (value > 0) {
                count++;
            }
        }

        return count;
    }

    /**
     * Exercise 24: Contains Number.
     * <p><strong>Student task:</strong> Return true when target appears in numbers.</p>
     * <p><strong>Example:</strong> contains(new int[]{2,4,6}, 4) is true.</p>
     */
    public static boolean contains(int[] a, int target) {
        if (a == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }

        for (int value : a) {
            if (value == target) {
                return true;
            }
        }

        return false;
    }

    /**
     * Exercise 25: Average.
     * <p><strong>Student task:</strong> Return the arithmetic mean of a nonempty integer array as a double.</p>
     * <p><strong>Example:</strong> average(new int[]{2,4,6}) returns 4.0.</p>
     */
    public static double average(int[] a) {
        if (a == null || a.length == 0) {
            throw new IllegalArgumentException("Array cannot be null or empty");
        }

        int sum = 0;

        for (int value : a) {
            sum += value;
        }

        return (double) sum / a.length;
    }

    /**
     * Exercise 26: Reverse Array.
     * <p><strong>Student task:</strong> Return a new array in reverse order without modifying the input.</p>
     * <p><strong>Example:</strong> {1,2,3} becomes {3,2,1}.</p>
     */
    public static int[] reverseArray(int[] a) {
        if (a == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }

        int[] reversed = new int[a.length];

        for (int i = 0; i < a.length; i++) {
            reversed[i] = a[a.length - 1 - i];
        }

        return reversed;
    }

    /**
     * Exercise 27: Sum List.
     * <p><strong>Student task:</strong> Return the sum of all integers in the list.</p>
     * <p><strong>Example:</strong> sumList(List.of(1,2,3)) returns 6.</p>
     */
    public static int sumList(List<Integer> a) {
        if (a == null) {
            throw new IllegalArgumentException("List cannot be null");
        }

        int sum = 0;

        for (int value : a) {
            sum += value;
        }

        return sum;
    }

    /**
     * Exercise 28: Remove Negatives.
     * <p><strong>Student task:</strong> Return a new list containing only values greater than or equal to zero. Do not modify the input.</p>
     * <p><strong>Example:</strong> [4,-2,8,-5,3] becomes [4,8,3].</p>
     */
    public static List<Integer> removeNegatives(List<Integer> a) {
        if (a == null) {
            throw new IllegalArgumentException("List cannot be null");
        }

        List<Integer> result = new ArrayList<>();

        for (int value : a) {
            if (value >= 0) {
                result.add(value);
            }
        }

        return result;
    }

    /**
     * Exercise 29: Unique Count.
     * <p><strong>Student task:</strong> Return the number of distinct strings in values.</p>
     * <p><strong>Example:</strong> [red,blue,red,green] returns 3.</p>
     */
    public static int uniqueCount(List<String> a) {
        if (a == null) {
            throw new IllegalArgumentException("List cannot be null");
        }

        Set<String> unique = new HashSet<>(a);

        return unique.size();
    }

    /**
     * Exercise 30: Find Duplicates.
     * <p><strong>Student task:</strong> Return a set of values that occur more than once.</p>
     * <p><strong>Example:</strong> [Java,Python,Java,C#,Python] returns Java and Python.</p>
     */
    public static Set<String> findDuplicates(List<String> a) {
        if (a == null) {
            throw new IllegalArgumentException("List cannot be null");
        }

        Set<String> seen = new HashSet<>();
        Set<String> duplicates = new HashSet<>();

        for (String value : a) {
            if (!seen.add(value)) {
                duplicates.add(value);
            }
        }

        return duplicates;
    }

    /**
     * Exercise 31: Word Count.
     * <p><strong>Student task:</strong> Return a map from each word to its frequency.</p>
     * <p><strong>Example:</strong> [java,sql,java] maps java to 2 and sql to 1.</p>
     */
    public static Map<String, Integer> wordCount(List<String> a) {
        if (a == null) {
            throw new IllegalArgumentException("List cannot be null");
        }

        Map<String, Integer> counts = new HashMap<>();

        for (String word : a) {
            counts.put(word, counts.getOrDefault(word, 0) + 1);
        }

        return counts;
    }

    /**
     * Exercise 32: Lookup Score.
     * <p><strong>Student task:</strong> Return the named student score or -1 when the key is absent.</p>
     * <p><strong>Example:</strong> getScore(Map.of("Ana",90), "Sam") returns -1.</p>
     */
    public static int getScore(Map<String, Integer> m, String s) {
        if (m == null || s == null) {
            throw new IllegalArgumentException("Map and student name cannot be null");
        }

        return m.getOrDefault(s, -1);
    }

    /**
     * Exercise 33: Serve Customer.
     * <p><strong>Student task:</strong> Remove and return the head of the queue, or "No customers" when empty.</p>
     * <p><strong>Example:</strong> A queue containing Ana then Bo serves Ana first.</p>
     */
    public static String serveNext(Queue<String> q) {
        if (q == null) {
            throw new IllegalArgumentException("Queue cannot be null");
        }

        String customer = q.poll();

        if (customer == null) {
            return "No customers";
        }

        return customer;
    }


    /**
     * Exercise 34: Bank Account.
     * <p><strong>Student task:</strong> Complete the constructor, deposit, withdraw, and
     * getBalance methods. Keep balance private, reject invalid starting balances and
     * nonpositive transaction amounts, and prevent overdrafts.</p>
     * <p><strong>Example:</strong> Start with 100.00, deposit 25.00, withdraw 20.00,
     * and the final balance is 105.00.</p>
     */
    public static class BankAccount {
        private final String accountNumber;
        private BigDecimal balance;

        public BankAccount(String accountNumber, BigDecimal startingBalance) {
            if (accountNumber == null || accountNumber.isBlank()) {
                throw new IllegalArgumentException("Account number cannot be blank");
            }

            if (startingBalance == null || startingBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Starting balance cannot be negative");
            }

            this.accountNumber = accountNumber;
            this.balance = startingBalance;
        }

        public void deposit(BigDecimal amount) {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Deposit must be positive");
            }

            balance = balance.add(amount);
        }

        public void withdraw(BigDecimal amount) {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Withdrawal must be positive");
            }

            if (amount.compareTo(balance) > 0) {
                throw new IllegalArgumentException("Insufficient funds");
            }

            balance = balance.subtract(amount);
        }

        public BigDecimal getBalance() {
            return balance;
        }
    }

    /**
     * Exercise 34: Create an account with a nonblank number and nonnegative starting balance.
     */
    public BankAccount(String n, BigDecimal b) {
        if (n == null || n.isBlank()) {
            throw new IllegalArgumentException("Account number cannot be blank");
        }

        if (b == null || b.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Starting balance cannot be negative");
        }

        this.accountNumber = n;
        this.balance = b;
    }

    /**
     * Exercise 34: Add a strictly positive amount to the balance.
     */
    public void deposit(BigDecimal a) {
        if (a == null || a.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit must be positive");
        }

        balance = balance.add(a);
    }

    /**
     * Exercise 34: Subtract a positive amount only when sufficient funds exist; report success.
     */
    public boolean withdraw(BigDecimal a) {
        if (a == null || a.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        if (a.compareTo(balance) > 0) {
            return false;
        }

        balance = balance.subtract(a);
        return true;
    }

    /**
     * Exercise 34: Return the current balance without exposing a writable field.
     */
    public BigDecimal getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Exercise 35: Employee.
     * <p><strong>Student task:</strong> Store a validated name and monthly salary in
     * private fields, then return monthly salary multiplied by 12.</p>
     * <p><strong>Example:</strong> A monthly salary of 5000 produces 60000 annually.</p>
     */
    public static class Employee {
        private final String name;
        private final BigDecimal monthlySalary;

        public Employee(String n, BigDecimal s) {
            if (n == null || n.isBlank()) {
                throw new IllegalArgumentException("Employee name cannot be blank");
            }

            if (s == null || s.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Monthly salary cannot be negative");
            }

            this.name = n;
            this.monthlySalary = s;
        }

        public BigDecimal calculateAnnualSalary() {
            return monthlySalary.multiply(BigDecimal.valueOf(12));
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Exercise 36: Shape Polymorphism.
     * <p><strong>Student task:</strong> Implement calculateArea in Rectangle and Circle.
     * Reject dimensions that are zero or negative.</p>
     * <p><strong>Example:</strong> A 3 by 4 rectangle has area 12; a radius-2 circle
     * has area PI times 4.</p>
     */
    public interface Shape {
        double calculateArea();
    }

    public static class Rectangle implements Shape {
        private final double length, width;

        public Rectangle(double l, double w) {
            if (l <= 0 || w <= 0) {
                throw new IllegalArgumentException("Length and width must be positive");
            }

            this.length = l;
            this.width = w;
        }

        public double calculateArea() {
            return length * width;
        }
    }

    public static class Circle implements Shape {
        private final double radius;

        public Circle(double r) {
            if (r <= 0) {
                throw new IllegalArgumentException("Radius must be positive");
            }

            this.radius = r;
        }

        public double calculateArea() {
            return Math.PI * radius * radius;
        }
    }


    /**
     * Exercise 37: Two Sum.
     * <p><strong>Student task:</strong> Return indexes of two different elements whose sum equals target; return an empty array if none exist.</p>
     * <p><strong>Example:</strong> [2,7,11,15] with target 9 returns [0,1].</p>
     */
    public static int[] twoSum(int[] a, int target) {
        if (a == null) {
            throw new IllegalArgumentException("Array cannot be null");
        }

        for (int i = 0; i < a.length; i++) {
            for (int j = i + 1; j < a.length; j++) {
                if (a[i] + a[j] == target) {
                    return new int[]{i, j};
                }
            }
        }

        return new int[]{};
    }

    /**
     * Exercise 38: Second Largest.
     * <p><strong>Student task:</strong> Return the second-largest distinct value. Reject arrays without two distinct values.</p>
     * <p><strong>Example:</strong> {8,3,12,5,9} returns 9.</p>
     */
    public static int secondLargest(int[] a) {
        if (a == null || a.length < 2) {
            throw new IllegalArgumentException("Array must contain at least two distinct values");
        }

        int largest = Integer.MIN_VALUE;
        int second = Integer.MIN_VALUE;

        for (int value : a) {
            if (value > largest) {
                second = largest;
                largest = value;
            } else if (value > second && value < largest) {
                second = value;
            }
        }

        if (second == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Array must contain two distinct values");
        }

        return second;
    }

    /**
     * Exercise 39: Password Validator.
     * <p><strong>Student task:</strong> Return true for 12 or more characters containing uppercase, lowercase, digit, and special characters.</p>
     * <p><strong>Example:</strong> isValidPassword("SecurePass1!") is true.</p>
     */
    public static boolean isValidPassword(String p) {
        if (p == null || p.length() < 12) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : p.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecial = true;
            }
        }

        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }


    /**
     * Exercise 40: Failed Login Detection.
     * <p><strong>Student task:</strong> Return true when the list contains three consecutive "FAILED" values.</p>
     * <p><strong>Example:</strong> SUCCESS, FAILED, FAILED, FAILED, SUCCESS returns true.</p>
     */
    public static boolean suspiciousLoginActivity(List<String> a) {
        if (a == null) {
            throw new IllegalArgumentException("List cannot be null");
        }

        for (int i = 0; i <= a.size() - 3; i++) {
            if ("FAILED".equals(a.get(i))
                    && "FAILED".equals(a.get(i + 1))
                    && "FAILED".equals(a.get(i + 2))) {
                return true;
            }
        }

        return false;
    }
}
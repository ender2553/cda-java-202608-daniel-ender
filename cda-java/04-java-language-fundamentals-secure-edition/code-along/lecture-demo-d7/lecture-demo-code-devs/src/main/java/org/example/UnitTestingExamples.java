package org.example;

public class UnitTestingExamples {

    // Small methods with one responsibility are easier to test because each test
    // can provide an input and compare the returned value with an expected value.
    public static int add(int firstNumber, int secondNumber) {
        return firstNumber + secondNumber;
    }

    // This method can be tested using assertTrue and assertFalse.
    public static boolean isEven(int number) {
        return number % 2 == 0;
    }

    // This method demonstrates testing normal output and invalid input.
    public static String greet(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }

        return "Hello, " + name + "!";
    }

    // A boundary-value example: 59 and 60 sit on opposite sides of the rule.
    public static boolean isPassingGrade(int grade) {
        if (grade < 0 || grade > 100) {
            throw new IllegalArgumentException("Grade must be between 0 and 100");
        }

        return grade >= 60;
    }
}


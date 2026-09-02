package org.example;

public class DebuggingDemo {

    public static void main(String[] args) {
        int[] scores = {80, 90, 70, 72};

        // BREAKPOINT 1: Stop here and inspect the scores array.
        double average;
        average = calculateAverage(scores);
        System.out.println("Expected average: 78.0");
        System.out.println("Actual average:   " + average);

        String savedStudent = new String("Ada");
        String enteredStudent = new String("Ada");

        // BREAKPOINT 2: Step Into namesMatch and inspect both String values.
        boolean sameStudent = namesMatch(savedStudent, enteredStudent);
        System.out.println("\nExpected name match: true");
        System.out.println("Actual name match:   " + sameStudent);

        // EXCEPTION BREAKPOINT CALLOUT:
        // Configure an exception breakpoint for ArithmeticException, then continue.
        try {
            int pointsPerAssignment = calculatePointsPerAssignment(100, 0);
            System.out.println(pointsPerAssignment);
        } catch (ArithmeticException exception) {
            System.out.println("\nCaught exception: " + exception.getMessage());
        }

        //Add bonus points to average
        average += 10;
        System.out.println("Actual average plus bonus:   " + average);

    }

    private static double calculateAverage(int[] scores) {
        int total = 0;

        // CONDITIONAL BREAKPOINT:
        // Add a breakpoint inside the loop with the condition "i == 2".
        // Watch how total changes during each iteration.
        for (int i = 0; i < scores.length; i++) {
            total += scores[i];
        }

        // BUG 1: The first score is missing from the total.
        // DEBUGGING QUESTION: What should the initial value of i be?
        return (double) total / scores.length;
    }

    private static boolean namesMatch(String savedStudent, String enteredStudent) {
        // BUG 2: == compares whether these variables reference the same object.
        // DEBUGGING QUESTION: Which String method compares their text instead?
        return savedStudent.equals(enteredStudent);
    }

    private static int calculatePointsPerAssignment(
            int totalPoints,
            int numberOfAssignments
    ) {
        // BUG 3: Dividing an integer by zero throws ArithmeticException.
        // Inspect the call stack to see how execution reached this method.
        return totalPoints / numberOfAssignments;
    }
}


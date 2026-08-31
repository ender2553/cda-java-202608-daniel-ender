package org.example;


public class MultiCatchDemo {

    public static void main(String[] args) {

        System.out.println("--- Separate catches: two DIFFERENT reactions ---");
        demoSeparateCatches(null);              // triggers NullPointerException path
        demoSeparateCatches(new String[]{"a"}); // triggers ArrayIndexOutOfBoundsException path

        System.out.println();
        System.out.println("--- Combined catch: two exceptions, ONE identical reaction ---");
        demoCombinedCatch(null);
        demoCombinedCatch(new String[]{"a"});
    }

    // Two exception types, two DIFFERENT responses - kept as separate
    // catch blocks because the reactions genuinely differ.
    private static void demoSeparateCatches(String[] values) {
        try {
            String result = values[5];
            System.out.println(result.length());
        } catch (NullPointerException e) {
            System.out.println("Reaction A: the array itself was null.");
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Reaction B: the array didn't have 6 elements.");
        }
    }

    // Two exception types, but the SAME reaction either way - a good
    // candidate for the pipe (|) syntax instead of duplicating code.
    private static void demoCombinedCatch(String[] values) {
        try {
            String result = values[5];
            System.out.println(result.length());
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Either failure gets the same response: " + e.getClass().getSimpleName());
        }
    }

}



package org.example;

public class PrimitiveVsReferenceParams {
    // A minimal custom class purely to demonstrate - do not do this. (internal classes beyond scope of lesson)
    static class Counter {
        int count;
        Counter(int count) {
            this.count = count;
        }
    }
    public static void main() {


        int number = 5;
        tryToDoubleValue(number);
        System.out.println("After tryToDoubleValue(number): " + number);


        System.out.println();
        System.out.println("===  Reference type — REASSIGNING the parameter ===");
        Counter counterA = new Counter(5);
        tryToReplaceCounter(counterA);
        System.out.println("After tryToReplaceCounter(counterA): count = " + counterA.count);


        System.out.println();
        System.out.println("=== Reference type — MUTATING through the reference ===");
        Counter counterB = new Counter(5);
        incrementCounter(counterB);
        System.out.println("After incrementCounter(counterB): count = " + counterB.count);


        System.out.println();
        System.out.println("===  Why String never shows the above behavior ===");
        String original = "hello";
        tryToUppercase(original);
        System.out.println("After tryToUppercase(original): " + original);

    }

    private static void tryToDoubleValue(int value) {
        value = value * 2;
    }

    private static void tryToReplaceCounter(Counter c) {
        c = new Counter(999);
    }

    private static void incrementCounter(Counter c) {
        c.count++;
    }


    private static void tryToUppercase(String str) {
        str = str.toUpperCase();
    }
}

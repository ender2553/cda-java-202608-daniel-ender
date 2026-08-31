package org.example;


public class CatchScopeDemo {

    public static void main(String[] args) {

        System.out.println("--- DANGEROUS: broad catch, empty body (swallowing) ---");
        demoDangerousSwallowing();
        System.out.println("(Notice: nothing printed above except this line - the bug vanished silently)");

        System.out.println();
        System.out.println("--- GOOD: specific catch, always does something ---");
        demoGoodDiscipline();
    }

    // ANTI-PATTERN: catches broadly and does nothing. A completely
    // unrelated bug (here, a NullPointerException from a typo-like
    // mistake) disappears without a trace.
    private static void demoDangerousSwallowing() {
        try {
            String severity = null;
            int length = severity.length(); // NullPointerException - unrelated to "parsing"!
        } catch (Exception e) {
            // EMPTY - this is the dangerous part. The bug is now invisible.
        }
    }

    // GOOD PRACTICE: catches the SPECIFIC exception type expected here,
    // and always does something observable, even in the "nothing to
    // do" case.
    private static void demoGoodDiscipline() {
        try {
            String severity = null;
            int length = severity.length();
        } catch (NullPointerException e) {
            System.out.println("Caught NullPointerException - severity was unexpectedly null. Logging for review.");
        }
    }

}


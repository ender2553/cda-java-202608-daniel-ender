package org.example;


import java.io.IOException;

public class CheckedVsUncheckedDemo {

    // CHECKED exception: "throws IOException" is REQUIRED here to compile.
    public static void readFileUnsafe(String path) throws IOException {
        java.io.FileReader reader = new java.io.FileReader(path);
        reader.close();
    }

    // UNCHECKED exception: nothing forces us to handle this.
    public static void triggerUnchecked() {
        int[] scores = new int[3];
        System.out.println(scores[10]); // ArrayIndexOutOfBoundsException
    }

    public static void main(String[] args) {


        System.out.println("--- Calling the checked-exception method safely ---");
        try {
            readFileUnsafe("this-file-does-not-exist.txt");
        } catch (IOException e) {
            System.out.println("Caught checked exception: " + e.getClass().getSimpleName());
        }

        System.out.println();
        System.out.println("--- Calling the unchecked-exception method (uncaught) ---");
        triggerUnchecked(); // this line WILL crash the program
    }

}



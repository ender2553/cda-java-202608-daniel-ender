package org.example;


public class CatchOrderFixed {

    public static void main(String[] args) {
        try {
            int[] scores = {85, 90, 95};
            System.out.println(scores[5]);
        } catch (ArrayIndexOutOfBoundsException e) {
            // MOST specific first
            System.out.println("Array index exception caught: " + e.getMessage());
        } catch (Exception e) {
            // LEAST specific (broadest) last - a genuine safety net
            System.out.println("Generic exception caught");
        }

        System.out.println("Program continued safely after the exception.");
    }

}



package org.example;

public class CastingDemo_03 {

    public static void main(String[] args) {

        // ---- Widening: int -> double happens automatically ----
        int wholeScore = 5;
         // no cast needed; safe, no data lost


        // ---- Narrowing: double -> int requires an explicit cast ----
        double preciseScore = 7.9;
         // explicit cast required

        // Notice: narrowed is 7, NOT 8. Casting (int) TRUNCATES —
        // it does not round. This is a very common source of confusion.

        // ---- Operator precedence ----
        // Just like in math class: * and / happen before + and -
        int likelihood = 4;
        int impact = 6;
        int weight = 2;

          // multiply first
          // parens first


    }

}



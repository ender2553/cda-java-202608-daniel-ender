package org.example;


import java.util.Scanner;

public class ScoreInput_06 {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        System.out.print("Enter impact score (0-10): ");
        double impact = input.nextDouble(); // crashes on non-numeric input

        System.out.println("You entered: " + impact);

        input.close(); // always close a Scanner when you're done with it; should be last as keyboard
    }

}



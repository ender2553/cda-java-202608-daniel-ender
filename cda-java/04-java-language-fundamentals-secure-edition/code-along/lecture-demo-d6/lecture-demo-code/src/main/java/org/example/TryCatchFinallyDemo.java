package org.example;


import java.util.Scanner;

public class TryCatchFinallyDemo {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a risk score: ");
        String userInput = scanner.nextLine();

        try {
            double score = Double.parseDouble(userInput);
            System.out.println("Parsed score: " + score);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input - please enter a number.");
        } finally {
            System.out.println("Input attempt complete.");
        }

        scanner.close();
    }

}



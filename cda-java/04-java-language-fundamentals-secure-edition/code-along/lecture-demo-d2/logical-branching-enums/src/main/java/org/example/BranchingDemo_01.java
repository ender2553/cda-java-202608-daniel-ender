package org.example;


public class BranchingDemo_01 {

    public static void main(String[] args) {

        double cvssScore = 6.5; // try changing this value and re-running

        if (cvssScore >= 9.0) {
            System.out.println("CRITICAL");
        } else if (cvssScore >= 7.0) {
            System.out.println("HIGH");
        } else if (cvssScore >= 4.0) {
            System.out.println("MEDIUM");
        } else {
            System.out.println("LOW");
        }
    }

}



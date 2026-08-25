package org.example;


public class RiskPreview_BUGGY_04 {

    public static void main(String[] args) {

        int likelihood = 7;
        int impact = 8;

        int riskScore = (likelihood + impact) / 2;

        System.out.println("Risk Score: " + riskScore);
    }

}



package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalDemo {

    public static void main() {

        System.out.println("--- Why not just use double for money? ---");
        double d1 = 0.1;
        double d2 = 0.2;
        System.out.println("0.1 + 0.2 = " + (d1 + d2));
        System.out.println("Equal to 0.3? " + (d1 + d2 == 0.3));


        System.out.println();
        System.out.println("--- Construct from a string, not a double ---");
        BigDecimal fromDouble = new BigDecimal(0.1);
        BigDecimal fromString = new BigDecimal("0.1");  // use String
        System.out.println("new BigDecimal(0.1):   " + fromDouble);
        System.out.println("new BigDecimal(\"0.1\"):   " + fromString);

        System.out.println();


        BigDecimal cost1 = new BigDecimal("1500.00");
        BigDecimal cost2 = new BigDecimal("750.50");
        BigDecimal cost3 = new BigDecimal("500.00");

        BigDecimal total = cost1.add(cost2);
        System.out.println("total:  " + total);

        BigDecimal difference = cost1.subtract(cost2);
        System.out.println("difference:  " + difference);


        System.out.println("add:  " + add(cost1, cost2, cost3));

        System.out.println("subtract:  " + subtract(cost1, cost2));

        System.out.println("multiply:  " + multiply(cost1, cost2));

        System.out.println("divide:  " + divide(cost1, cost2));





    }

    public static BigDecimal add(BigDecimal... numbers) {
        BigDecimal result = BigDecimal.ZERO;
        for (BigDecimal number : numbers) {
            result = result.add(number);
        }
        return result;
    }

    public static BigDecimal multiply(BigDecimal... numbers){
        BigDecimal result = BigDecimal.ONE;
        for (BigDecimal number : numbers){
            result = result.multiply(number);
        }
        return result;
    }

    public static BigDecimal divide(BigDecimal firstNumber, BigDecimal... divisors){
        BigDecimal result = firstNumber;
        for (BigDecimal divisor : divisors) {
            result = result.divide(divisor, 2, RoundingMode.HALF_UP);
        }
        return result;
    }

    public static BigDecimal subtract(BigDecimal firstNumber, BigDecimal... numbersToSubtract){
        BigDecimal result = firstNumber;
        for(BigDecimal number : numbersToSubtract) {
            result = result.subtract(number);

        }
        return result;
    }

}

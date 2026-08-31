package org.example;


import java.util.ArrayList;
import java.util.List;

public class ListDemo {

    public static void main(String[] args) {

        List<String> cveList = new ArrayList<>();

        cveList.add("CVE-2024-1234");        // ADD
        cveList.add("CVE-2024-5678");
        cveList.add("CVE-2024-1234");        // duplicates ARE allowed in a List

        System.out.println("After adds: " + cveList);

        String firstCve = cveList.get(0);     // GET by index
        System.out.println("CVE at index 0: " + firstCve);

        cveList.remove("CVE-2024-5678");     // REMOVE by value
        cveList.remove(0);     // REMOVE by index  -- Remove() is overloaded

        System.out.println("After remove: " + cveList);

        boolean found = cveList.contains("CVE-2024-1234");   // SEARCH
        int position = cveList.indexOf("CVE-2024-1234");
        System.out.println("contains CVE-2024-1234: " + found);
        System.out.println("indexOf CVE-2024-1234: " + position);

        System.out.println("--- enhanced for loop ---");
        for (String cve : cveList) {
            System.out.println("Alert: " + cve);
        }

        System.out.println("--- standard for loop ---");
        for (int i = 0; i < cveList.size(); i++) {
            System.out.println("Alert at index " + i + ": " + cveList.get(i));
        }


        cveList.add("CVE-2024-5678");
        cveList.add("CVE-2024-1234");

        System.out.println("--- while loop ---");
        int i = 0;
        while (i < cveList.size()) {
            System.out.println("Alert at index " + i + ": " + cveList.get(i));
            i++;
        }
    }

}



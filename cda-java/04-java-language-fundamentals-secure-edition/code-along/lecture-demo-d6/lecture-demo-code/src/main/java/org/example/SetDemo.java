package org.example;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SetDemo {

    public static void main(String[] args) {

        Set<String> cveSet = new HashSet<>();

        cveSet.add("CVE-2024-1234");         // ADD
        cveSet.add("CVE-2024-5678");
        boolean wasAdded = cveSet.add("CVE-2024-1234");  // duplicate - silently ignored

        System.out.println("Set size after duplicate add attempt: " + cveSet.size());
        System.out.println("add() returned (false = duplicate ignored): " + wasAdded);

        cveSet.remove("CVE-2024-5678");      // REMOVE by value
        System.out.println("After remove, contains CVE-2024-5678: " + cveSet.contains("CVE-2024-5678"));

        boolean found = cveSet.contains("CVE-2024-1234");  // SEARCH
        System.out.println("contains CVE-2024-1234: " + found);

        System.out.println("--- traversing (order not guaranteed) ---");
        for (String cve : cveSet) {          // TRAVERSE
            System.out.println("Unique alert: " + cve);
        }


//        System.out.println("--- while loop ---");
//        Iterator<String> iterator = cveSet.iterator();
//
//        while (iterator.hasNext()) {
//            String cve = iterator.next();
//            System.out.println(cve);
//        }


    }

}



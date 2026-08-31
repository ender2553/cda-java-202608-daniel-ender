package org.example;

import java.util.Arrays;

public class StringDemo {

    public static void main() {


        System.out.println("===== Primitive Vs Reference Types =======");
        int riskScore = 87;
        int copy = riskScore;  // copies the VALUE - two independent 87 var
        copy = 42;             // Changing copy does not touch riskScore

        System.out.println("riskScore = " + riskScore);
        System.out.println("Copy      = " + copy);

        System.out.println();


        System.out.println(" ====== Literal Pool =======");

        String assetInfo = "CVE-123-4567,openssl,3.0.1,HIGH";
        String id1 = "CVE-123-4567";
        String id2 = "CVE-123-4567";

        System.out.println("id1 == id2 -> " + (id1 == id2));  // true
        System.out.println("identityHashcode(id1) = " + System.identityHashCode(id1));
        System.out.println("identityHashcode(id2) = " + System.identityHashCode(id2));

        System.out.println();


        newStringByPass();

        demoAliasing();

        System.out.println("====== STRING METHODS ======");
        System.out.println(assetInfo.toUpperCase());

        System.out.println(assetInfo);

        String[] parts = assetInfo.split(",");
        System.out.println("Length:  " + parts.length);
        System.out.println(Arrays.toString(parts));
        System.out.println(parts[1]);



    }

    public static void newStringByPass(){
        System.out.println("==== String pool by pass =====");

        String id1 = "CVE-123-4567";
        String id3 = new String("CVE-123-4567");  //forces a new heap object

        System.out.println("id1 == id3 -> " + (id1 == id3));
        System.out.println("id1.equals(id3) -> " + id1.equals(id3));
        System.out.println("identityHashcode(id1) = " + System.identityHashCode(id1));
        System.out.println("identityHashcode(id3) = " + System.identityHashCode(id3));

        System.out.println();

    }

    public static void demoAliasing() {
        System.out.println("===== Aliasing ======");

        Asset a1 = new Asset("WebServer-01");
        Asset a2 = a1; // copies the REFERENCE, not the object

        a2.setOwner("security-team");

        System.out.println("a1: " + a1); // owner is ALSO "security-team"
        System.out.println("a2: " + a2);
        System.out.println("a1 == a2 -> " + (a1 == a2)); // true: same object

        System.out.println();

    }

}

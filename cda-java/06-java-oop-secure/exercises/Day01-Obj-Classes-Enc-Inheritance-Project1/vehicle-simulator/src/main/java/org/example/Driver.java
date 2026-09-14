package org.example;

import java.util.ArrayList;
import java.util.List;

public class Driver {
    private String name;
    private List<String> licenseEndorsements;

    public Driver(String name, List<String> licenseEndorsements) {
        this.name = name;
        this.licenseEndorsements = new ArrayList<>(licenseEndorsements);
    }

    public List<String> getLicenseEndorsements() {
        return new ArrayList<>(licenseEndorsements);
    }

    public void addEndorsement(String endorsement) {
        licenseEndorsements.add(endorsement);
    }

    public static void main(String[] args) {
        List<String> endorsements = new ArrayList<>();
        endorsements.add("Class C");

        Driver driver = new Driver("Daniel", endorsements);

        System.out.println("Initial driver endorsements: "
                + driver.getLicenseEndorsements());

        endorsements.add("Motorcycle");

        System.out.println("After changing original list: "
                + driver.getLicenseEndorsements());

        driver.getLicenseEndorsements().add("Commercial");

        System.out.println("After changing getter result: "
                + driver.getLicenseEndorsements());
    }
}

//============Part 4 - Reflection================
//1. A Driver's personal information, such as their driver's license number or
// other identifying information, absolutely should not leak through a mutable object.
// Unauthorized code could access or modify sensitive information that should remain
// protected.

//2. It shows that many security problems come from how basic code is designed and
// used, not from a lack of advanced security features. Proper encapsulation, controlled
// access, and defensive copying can prevent vulnerabilities before they happen.

//3. Domain classes should keep their internal state private and only allow it to
// be accessed or changed through controlled, safe methods.
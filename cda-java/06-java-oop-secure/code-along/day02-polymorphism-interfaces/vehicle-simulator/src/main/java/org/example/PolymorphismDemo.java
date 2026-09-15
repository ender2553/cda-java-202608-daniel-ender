package org.example;
import java.util.ArrayList;
import java.util.List;


public class PolymorphismDemo {

    interface Announcer {
        String announce();
    }

    static class Car implements Announcer {
        @Override
        public String announce() {
            return "Beep beep! Car merging into traffic.";
        }
    }

    static class Boat implements Announcer {
        @Override
        public String announce() {
            return "Toot toot! Boat leaving the harbor.";
        }
    }

    static class Drone implements Announcer {
        @Override
        public String announce() {
            return "Bzzzzz. Drone lifting off.";
        }
    }

    static class Submarine implements Announcer {
        @Override
        public String announce() {
            return "Splash. Submarine submerging.";
        }
    }

    public static void main(String[] args) {
        List<Announcer> everything = new ArrayList<>();
        everything.add(new Car());
        everything.add(new Boat());
        everything.add(new Drone());
        everything.add(new Submarine());

        // One loop. Zero if-else chains checking "is this a Car? a
        // Boat?". Each object knows how to announce() itself - that's
        // polymorphism, and it works identically whether the shared
        // type is a parent CLASS (Day 1) or an INTERFACE (today).
        for (Announcer a : everything) {
            System.out.println(a.announce());
        }

        System.out.println();
        System.out.println("TRY IT YOURSELF:");
        System.out.println(" 1. Add a Submarine class implementing Announcer.");
        System.out.println(" 2. Add it to the list - no changes needed to the loop.");
    }
}

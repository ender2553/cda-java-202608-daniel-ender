package org.example;
/**
 * a class using other objects as instance variables —
 * a loosely coupled "has-a" relationship. No extends keyword involved.
 */
public class CompositionDemo {

    // ================================================================
    // STEP 1 — A composed object: a small, focused class
    // ================================================================
    static class Engine {
        private final String type;

        public Engine(String type) {
            this.type = type;
        }

        public String start() {
            return type + " engine roars to life.";
        }
    }

    // ================================================================
    // STEP 1b — A second composed object, independent of Engine
    // ================================================================
    static class GPS {
        public String navigate(String destination) {
            return "Calculating route to " + destination + "...";
        }
    }

    // ================================================================
    // STEP 2 — Car HAS-A Engine and HAS-A GPS (composition), not IS-A
    // ================================================================
    static class Car {
        private final String name;
        private final Engine engine; // composed object, passed in via constructor
        private final GPS gps;       // a second, unrelated composed object

        public Car(String name, Engine engine, GPS gps) {
            this.name = name;
            this.engine = engine;
            this.gps = gps;
        }

        public void drive() {
            System.out.println(name + ": " + engine.start());
        }

        public void navigateTo(String destination) {
            System.out.println(name + ": " + gps.navigate(destination));
        }
    }

    public static void main(String[] args) {
        // ============================================================
        // STEP 3 — The same Car class works with any Engine
        // ============================================================
        Car gasCar = new Car("Sedan", new Engine("gasoline"), new GPS());
        Car electricCar = new Car("EV Hatchback", new Engine("electric"), new GPS());


        // changes to support a different kind of engine?
        gasCar.drive();
        electricCar.drive();
        gasCar.navigateTo("the mechanic");

        // ============================================================
        // STEP 4 — Swap a composed object — no inheritance needed
        // ============================================================
        System.out.println();
        System.out.println("Converting the sedan to electric...");
        Car rebuilt = new Car("Sedan (converted)", new Engine("electric"), new GPS());
        rebuilt.drive();

        // TRY IT YOURSELF:
        //  1. Add a SoundSystem class with a play() method.
        //  2. Give Car a third composed field, SoundSystem soundSystem,
        //     set via the constructor, and a playMusic() method that
        //     calls it.
        //  3. Notice Car still doesn't extend anything — it's simply
        //     assembled from more parts.
    }
}

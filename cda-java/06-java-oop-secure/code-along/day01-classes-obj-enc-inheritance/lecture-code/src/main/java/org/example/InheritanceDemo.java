package org.example;
import java.util.ArrayList;
import java.util.List;

/**
 * a subclass reuses a parent class's fields and methods
 * through an "is-a" relationship, and can override the behavior that
 * genuinely differs per subclass.
 */
public class InheritanceDemo {

    // ================================================================
    // STEP 1 — The shared parent class (abstract: honk() has no body yet)
    // ================================================================
    abstract static class Vehicle {
        private final String make;
        private final String model;

        public Vehicle(String make, String model) {
            this.make = make;
            this.model = model;
        }

        public String getMake() {
            return make;
        }

        // Every Vehicle can honk, but HOW differs per subclass.
        public abstract String honk();

        @Override
        public String toString() {
            return make + " " + model + " says: " + honk();
        }
    }

    // ================================================================
    // STEP 2 — extends creates the is-a relationship
    // ================================================================
    static class Car extends Vehicle {
        public Car(String make, String model) {
            super(make, model); // STEP 3 — super(...) sends shared data up
        }

        @Override
        public String honk() {
            return "Beep beep!";
        }
    }

    static class Motorcycle extends Vehicle {
        public Motorcycle(String make, String model) {
            super(make, model);
        }

        @Override
        public String honk() {
            return "Honk! (surprisingly loud for something this small)";
        }
    }

    static class Truck extends Vehicle {
        private final double cargoCapacityTons;

        public Truck(String make, String model, double cargoCapacityTons) {
            super(make, model);
            this.cargoCapacityTons = cargoCapacityTons;
        }

        @Override
        public String honk() {
            return "HOOONK!";
        }

        public double getCargoCapacityTons() {
            return cargoCapacityTons;
        }
    }

    public static void main(String[] args) {
        List<Vehicle> fleet = new ArrayList<>();
        fleet.add(new Car("Toyota", "Corolla"));
        fleet.add(new Motorcycle("Honda", "Rebel 300"));
        fleet.add(new Truck("Ford", "F-150", 1.5));

        // ============================================================
        // STEP 4 — Polymorphism: one loop, three different honk() behaviors
        // ============================================================

        for (Vehicle v : fleet) {
            System.out.println(v); // calls each object's OWN honk()
        }

        // TRY IT YOURSELF: uncomment the next line. It won't compile —
        // the Vehicle reference type doesn't know about cargoCapacityTons,
        // even though the object underneath is a Truck.
        // System.out.println(fleet.get(2).getCargoCapacityTons());

        System.out.println();
        System.out.println("TRY IT YOURSELF:");
        System.out.println(" 1. Add a Bus subclass with its own honk() and");
        System.out.println("    a passengerCapacity field.");
        System.out.println(" 2. Add it to the fleet and re-run the loop -");
        System.out.println("    no changes needed to the loop itself.");
    }
}

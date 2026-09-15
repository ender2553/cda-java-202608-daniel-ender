package org.example;

/**
 a class is a blueprint. An object is a specific instance
 * built from that blueprint, with its own copy of the data.
 */
public class ClassesAndObjectsDemo {

    // ================================================================
    // STEP 1 — Define a class with fields (this is an object's STATE)
    // ================================================================
    static class Car {
        String make;
        String model;
        int year;
        int mileage;

        // ============================================================
        // STEP 2 — Add a constructor so every Car starts with real data
        // ============================================================
        public Car(String make, String model, int year, int mileage) {
            this.make = make;
            this.model = model;
            this.year = year;
            this.mileage = mileage;
        }

        // ============================================================
        // STEP 3 — Add a method (this is an object's BEHAVIOR)
        // ============================================================
        public String describe() {
            return year + " " + make + " " + model + " (" + mileage + " miles)";
        }
    }

    public static void main(String[] args) {
        // ============================================================
        // STEP 4 — Instantiate objects with the `new` keyword
        // ============================================================
        Car car1 = new Car("Toyota", "Corolla", 2021, 18500);
        Car car2 = new Car("Ford", "F-150", 2023, 4200);

        // PREDICT before running: will car1.describe() and car2.describe()
        // print the same thing? Why or why not?
        System.out.println(car1.describe());
        System.out.println(car2.describe());

        // ============================================================
        // STEP 5 — Each object holds its OWN independent copy of state
        // ============================================================
        System.out.println();
        System.out.println("Rolling car 1's odometer forward 500 miles...");
        car1.mileage += 500; // only car1 changes

        System.out.println(car1.describe());
        System.out.println(car2.describe()); // unaffected — a separate object

        // TRY IT YOURSELF:
        //  1. Create a third Car object for a car you'd like to own.
        //  2. Add a method isNewModel() that returns true when
        //     year >= 2023, and call it on all three cars.
    }
}

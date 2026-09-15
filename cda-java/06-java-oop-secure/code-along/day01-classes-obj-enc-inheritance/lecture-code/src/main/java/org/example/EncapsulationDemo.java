package org.example;
/**
 * bundle data and behavior together, and restrict direct
 * access to internal state so it can only change through controlled,
 * validated methods.
 */
public class EncapsulationDemo {

    // ================================================================
    // STEP 1 — Without encapsulation: nothing stops invalid state
    // ================================================================
    static class UnprotectedCar {
        public double fuelLevel; // gallons — a public field anyone can set
    }

    // ================================================================
    // STEP 2 — With encapsulation: the field becomes private...
    // ================================================================
    static class Car {
        private static final double TANK_CAPACITY = 12.0; // gallons
        private static final double MILES_PER_GALLON = 25.0;

        private double fuelLevel;

        public Car(double startingFuel) {
            this.fuelLevel = startingFuel;
        }

        // ============================================================
        // STEP 3 — ...and the only doors in are validated methods
        // ============================================================
        public double getFuelLevel() {
            return fuelLevel;
        }

        public void refuel(double gallons) {
            if (gallons <= 0) {
                System.out.println("Rejected: refuel amount must be positive.");
                return;
            }
            fuelLevel = Math.min(fuelLevel + gallons, TANK_CAPACITY);
        }

        public void drive(double miles) {
            double gallonsNeeded = miles / MILES_PER_GALLON;
            if (gallonsNeeded > fuelLevel) {
                System.out.println("Rejected: not enough fuel to drive " + miles + " miles.");
                return;
            }
            fuelLevel -= gallonsNeeded;
            System.out.println("Drove " + miles + " miles.");
        }
    }

    public static void main(String[] args) {
        System.out.println("--- Without encapsulation ---");
        UnprotectedCar unsafe = new UnprotectedCar();
        unsafe.fuelLevel = 10;
        unsafe.fuelLevel = -500; // nothing in the class stops this
        System.out.println("unsafe.fuelLevel = " + unsafe.fuelLevel);
        System.out.println("Data integrity: broken. Nothing enforced the rules.");

        System.out.println();
        System.out.println("--- With encapsulation ---");
        Car car = new Car(8);
        car.drive(100);
        car.refuel(3);


        car.drive(500); // should be rejected — not enough fuel

        // TRY IT YOURSELF: uncomment the next line. It won't compile —
        // that's the compiler enforcing encapsulation for you.
        // car.fuelLevel = -50;

        System.out.println("Final fuel level: " + car.getFuelLevel() + " gallons");

        // TRY IT YOURSELF:
        //  1. Add a private odometer field that increases every time
        //     drive() succeeds, with a getter but no setter.
        //  2. Reject refuel() amounts that would push fuelLevel above
        //     TANK_CAPACITY instead of silently capping it.
    }
}

package org.example;

public class InterfacesDemo {

    // STEP 1 - an interface is a contract: method signatures, no bodies.
    interface Refuelable {
        void refuel(double amount);
        double getFuelLevel();
    }

    interface Chargeable {
        void charge(double amount);
        double getChargeLevel();
    }

    // STEP 2 - "implements" completes the contract with real code.
    static class GasCar implements Refuelable {
        private double fuelLevel = 0;

        @Override
        public void refuel(double amount) {
            fuelLevel += amount;
        }

        @Override
        public double getFuelLevel() {
            return fuelLevel;
        }
    }

    static class ElectricCar implements Chargeable {
        private double chargeLevel = 0;

        @Override
        public void charge(double amount) {
            chargeLevel += amount;
        }

        @Override
        public double getChargeLevel() {
            return chargeLevel;
        }
    }

    // STEP 3 - a class can implement MULTIPLE interfaces at once. A
    // HybridCar genuinely needs both contracts, and Java allows that
    // even though a class can only EXTEND one parent class.
    static class HybridCar implements Refuelable, Chargeable {
        private double fuelLevel = 0;
        private double chargeLevel = 0;

        @Override
        public void refuel(double amount) {
            fuelLevel += amount;
        }

        @Override
        public double getFuelLevel() {
            return fuelLevel;
        }

        @Override
        public void charge(double amount) {
            chargeLevel += amount;
        }

        @Override
        public double getChargeLevel() {
            return chargeLevel;
        }
    }

    public static void main(String[] args) {
        GasCar gasCar = new GasCar();
        gasCar.refuel(10);
        System.out.println("GasCar fuel: " + gasCar.getFuelLevel() + " gal");

        ElectricCar electricCar = new ElectricCar();
        electricCar.charge(50);
        System.out.println("ElectricCar charge: " + electricCar.getChargeLevel() + "%");

        HybridCar hybrid = new HybridCar();
        hybrid.refuel(5);
        hybrid.charge(30);
        System.out.println("HybridCar fuel: " + hybrid.getFuelLevel()
                + " gal, charge: " + hybrid.getChargeLevel() + "%");

        // STEP 4 - the interface type is what lets unrelated classes be
        // treated the same way. A HybridCar can be handed to code that
        // only knows about Refuelable...
        Refuelable asRefuelable = hybrid;
        asRefuelable.refuel(2);
        System.out.println("HybridCar fuel after topping off via the Refuelable"
                + " reference: " + hybrid.getFuelLevel() + " gal");

        // ...or code that only knows about Chargeable. Same object, two
        // different contracts, depending on which interface type you're
        // holding it as.
        Chargeable asChargeable = hybrid;
        asChargeable.charge(10);
        System.out.println("HybridCar charge after topping off via the Chargeable"
                + " reference: " + hybrid.getChargeLevel() + "%");


        // Chargeable notAllowed = gasCar;
    }
}

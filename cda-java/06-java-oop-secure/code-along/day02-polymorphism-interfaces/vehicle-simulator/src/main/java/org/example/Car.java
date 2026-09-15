package org.example;

public class Car extends Vehicle {

    public Car(String make, String model, int year, Engine engine, double startingFuel) {
        super(make, model, year, engine, startingFuel);
    }

    @Override
    public String honk() {

        return "Beep beep!";
    }

    @Override
    protected double milesPerGallon() {

        return 28.0;
    }
}

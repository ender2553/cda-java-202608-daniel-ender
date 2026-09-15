package org.example;

public class Train extends Vehicle {
    private final int carCount;

    public Train(String make, String model, int year, Engine engine, double startingFuel,
                 int carCount) {
        super(make, model, year, engine, startingFuel);
        if (carCount < 1) {
            throw new IllegalArgumentException("carCount must be at least 1");
        }
        this.carCount = carCount;
    }

    public int getCarCount() {
        return carCount;
    }

    @Override
    public String honk() {
        return "All aboard! WHOO WHOO!";
    }

    @Override
    protected double milesPerGallon() {
        // fuel efficiency drops as more cars are coupled on
        return 3.0 / carCount * 10;
    }

    @Override
    public String toString() {
        return super.toString() + " | cars coupled: " + carCount;
    }
}

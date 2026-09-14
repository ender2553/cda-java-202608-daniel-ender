package org.example;
/**
 * Car.java
 * ---------
 * Inheritance: extends Vehicle and supplies its own honk() and
 * milesPerGallon(). Composition: receives its Engine from whoever
 * creates it, rather than building one internally.
 */
public class Car extends Vehicle{

    //Constructor
    public Car(String make, String model, int year, Engine engine, double startingFuel){
        super(make, model, year, engine, startingFuel);

    }

}

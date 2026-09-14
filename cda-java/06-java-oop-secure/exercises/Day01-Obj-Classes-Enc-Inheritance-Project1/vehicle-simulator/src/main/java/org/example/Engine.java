package org.example;
/**
 * Engine.java
 * ------------
 * A small, focused class used via COMPOSITION inside every Vehicle.
 * (Concepts: Classes & Objects, Encapsulation, Composition)
 */
public class Engine {
    private final String type;  // e.g "gasoline", "diesel", "electric"

    // Constructor
    public Engine(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String start() {
        //String startEngineMessage = type + " engine roars to life";
        //return startEngineMessage;
        //OR
        return type + " engine roars to life";
    }
}

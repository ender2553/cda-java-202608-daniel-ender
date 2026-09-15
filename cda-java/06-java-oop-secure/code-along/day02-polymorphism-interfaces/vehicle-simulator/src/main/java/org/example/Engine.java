package org.example;

public class Engine {
    private final String type; // e.g. "gasoline", "diesel", "electric"

    public Engine(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String start() {
        return type + " engine roars to life.";
    }
}

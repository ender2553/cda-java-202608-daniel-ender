package org.example;
import java.util.HashMap;
import java.util.Map;

/**
 * a Map stores KEY-VALUE pairs. Every key is unique; looking
 * up a key gives you its value directly, with no need to search item by
 * item the way you would in a List.
 */
public class MapsDemo {

    public static void main(String[] args) {
        // ================================================================
        // STEP A — Build a Map from existing data (an initializer)
        // ================================================================
        // Map.of(...) creates a small, FIXED-SIZE map. Wrapping it in
        // `new HashMap<>(...)` copies that data into a map you can still
        // grow, shrink, and update afterward.
        Map<String, Integer> inventory = new HashMap<>(Map.of(
                "Corolla", 5,
                "Civic", 3,
                "F-150", 2
        ));
        System.out.println("Starting inventory: " + inventory);

        // ================================================================
        // STEP B — Add items via methods
        // ================================================================
        inventory.put("Rivian R2", 4); // a brand-new key


        inventory.put("Civic", inventory.get("Civic") + 1); // sold a Civic, got one more in
        System.out.println("After restocking: " + inventory);

        // ================================================================
        // STEP C — Look things up by key instead of searching
        // ================================================================
        System.out.println("Corollas in stock: " + inventory.get("Corolla"));
        System.out.println("Do we carry Rivian R2? " + inventory.containsKey("Rivian R2"));
        System.out.println("Do we carry Mustang? " + inventory.containsKey("Mustang"));

        // getOrDefault avoids a null result for a key that isn't there
        int mustangCount = inventory.getOrDefault("Mustang", 0);
        System.out.println("Mustangs in stock (default 0): " + mustangCount);

        // ================================================================
        // STEP D — Iterate over keys, values, or both together
        // ================================================================
        // Note: HashMap does not guarantee iteration order.
        System.out.println();
        System.out.println("Every model we carry:");
        for (String model : inventory.keySet()) {
            System.out.println(" - " + model);
        }

        System.out.println();
        System.out.println("Full inventory report:");
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            System.out.println(" - " + entry.getKey() + ": " + entry.getValue() + " on the lot");
        }

        // ================================================================
        // STEP E — Remove a key
        // ================================================================
        inventory.remove("F-150"); // sold the last one, discontinuing the line
        System.out.println();
        System.out.println("After F-150 is discontinued: " + inventory);

        // TRY IT YOURSELF:
        //  1. Add a Map<String, String> that maps each model to its color.
        //  2. Write a loop that prints "<model> (<color>): <count> in stock"
        //     by looking values up in both maps for each model.
    }
}

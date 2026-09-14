package org.example;
import java.util.ArrayList;
import java.util.List;

/**
 * a List is an ORDERED collection that allows duplicates and
 * gives you indexed access (get(0), get(1), ...). Use it when position
 * and repetition both matter.
 */
public class ListsDemo {

    public static void main(String[] args) {
        // ================================================================
        // STEP A — Build a List from existing data (an initializer)
        // ================================================================
        // List.of(...) creates a small, FIXED-SIZE list. Wrapping it in
        // `new ArrayList<>(...)` copies that data into a list you can
        // still grow and shrink afterward.
        List<String> showroom = new ArrayList<>(List.of("Corolla", "Civic", "Model 3"));
        System.out.println("Showroom lineup: " + showroom);

        // ================================================================
        // STEP B — Add items via methods
        // ================================================================
        showroom.add("F-150");           // adds to the end
        showroom.add(1, "Accord");       // inserts at a specific position

        System.out.println("After adding two more: " + showroom);


        System.out.println("Model 3 is at index " + showroom.indexOf("Model 3"));

        // ================================================================
        // STEP C — Lists keep order and allow duplicates
        // ================================================================
        showroom.add("Civic"); // a second Civic — Lists don't reject repeats
        System.out.println("After a second Civic arrives: " + showroom);
        System.out.println("Total cars on the lot: " + showroom.size());

        // ================================================================
        // STEP D — Read, update, and remove by position
        // ================================================================
        System.out.println("First car in the lineup: " + showroom.get(0));
        showroom.set(0, "Corolla Hybrid"); // replace by index
        showroom.remove("F-150");          // remove by value
        // showroom.remove(0);             // remove(int) removes by INDEX instead

        System.out.println("Final lineup: " + showroom);

        // ================================================================
        // STEP E — Iterate in order
        // ================================================================
        System.out.println();
        System.out.println("Walking the lot:");
        for (String model : showroom) {
            System.out.println(" - " + model);
        }

        // TRY IT YOURSELF:
        //  1. Add three more models using a loop instead of three add()
        //     calls.
        //  2. Use showroom.contains("Model 3") to check whether a model
        //     is still on the lot before printing a message about it.
    }
}

package org.example;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
  *a Set is a collection that allows NO duplicates. Use it
 * whenever "have we already seen this?" matters more than order or
 * position.
 */
public class SetsDemo {

    public static void main(String[] args) {
        // ================================================================
        // STEP A — Build a Set from existing data (an initializer)
        // ================================================================
        // The list below has a repeated "Toyota" on purpose.
        List<String> makesSpotted = List.of("Toyota", "Honda", "Toyota", "Ford", "Honda");

        // Wrapping a List in `new HashSet<>(...)` copies the data in and
        // automatically collapses any duplicates.
        Set<String> uniqueMakes = new HashSet<>(makesSpotted);


        System.out.println("Makes spotted (with repeats): " + makesSpotted);
        System.out.println("Unique makes in the lot: " + uniqueMakes);
        System.out.println("Count: " + uniqueMakes.size());

        // ================================================================
        // STEP B — Add items via methods
        // ================================================================
        uniqueMakes.add("Rivian");   // a new make — gets added
        uniqueMakes.add("Toyota");  // already present — add() quietly does nothing

        System.out.println();
        System.out.println("After trying to add Rivian and a second Toyota:");
        System.out.println(uniqueMakes);
        System.out.println("Count: " + uniqueMakes.size());

        // add() actually tells you whether it changed anything:
        boolean added = uniqueMakes.add("Toyota");
        System.out.println("Did adding Toyota again change the set? " + added);

        // ================================================================
        // STEP C — contains() answers "have we already seen this?" fast
        // ================================================================
        System.out.println();
        System.out.println("Do we have a Ford on the lot? " + uniqueMakes.contains("Ford"));
        System.out.println("Do we have a Mazda on the lot? " + uniqueMakes.contains("Mazda"));

        // ================================================================
        // STEP D — Iterate (note: a HashSet does not guarantee order)
        // ================================================================
        System.out.println();
        System.out.println("Walking the set of makes:");
        for (String make : uniqueMakes) {
            System.out.println(" - " + make);
        }

        // ================================================================
        // STEP E — Remove an item
        // ================================================================
        uniqueMakes.remove("Ford");
        System.out.println();
        System.out.println("After Ford leaves the lot: " + uniqueMakes);

        // TRY IT YOURSELF:
        //  1. Build a second Set<String> of makes from a different lot.
        //  2. Use uniqueMakes.retainAll(otherLot) to find makes both lots
        //     share, and uniqueMakes.removeAll(otherLot) to find makes
        //     that are only on the first lot.
    }
}

package org.example;
import java.util.List;
import java.util.Stack;

/**
 * a Stack is LIFO — Last In, First Out. Think of a single-lane
 * parking garage: the last car to pull in is the first one that has to
 * back out.
 */
public class StacksDemo {

    public static void main(String[] args) {
        // ================================================================
        // STEP A — Build a Stack from existing data (an initializer)
        // ================================================================
        // Stack only has a no-argument constructor — unlike ArrayList or
        // LinkedList, you can't hand it existing data directly. addAll()
        // is still "loading from existing data," just via a method call
        // right after construction instead of inside the constructor.
        Stack<String> garage = new Stack<>();
        garage.addAll(List.of("Corolla", "Civic", "F-150"));

        System.out.println("Cars parked so far (bottom to top): " + garage);

        // ================================================================
        // STEP B — Add items via methods
        // ================================================================
        garage.push("Rivian R2"); // pulls in behind everyone — now on TOP

        System.out.println("After Rivian R2 pulls in: " + garage);

        // ================================================================
        // STEP C — peek() looks at the top without removing it
        // ================================================================
        // PREDICT before running: which car is sitting on top of the stack?
        System.out.println("Car closest to the exit: " + garage.peek());
        System.out.println("Garage unchanged after peek(): " + garage);

        // ================================================================
        // STEP D — pop() removes and returns the TOP of the stack
        // ================================================================
        String justLeft = garage.pop();
        System.out.println();
        System.out.println("Backed out: " + justLeft);
        System.out.println("Remaining cars: " + garage);

        // ================================================================
        // STEP E — Everyone else has to leave in reverse parking order
        // ================================================================
        System.out.println();
        System.out.println("Clearing the garage, one car at a time:");
        while (!garage.isEmpty()) {
            System.out.println(" - backing out " + garage.pop());
        }
        System.out.println("Garage empty? " + garage.isEmpty());

        // TRY IT YOURSELF:
        //  1. Rebuild the garage and push four cars using a loop.
        //  2. Modern Java code often prefers ArrayDeque over Stack for
        //     this same LIFO behavior — look up push()/pop() on
        //     java.util.ArrayDeque and rewrite this file using it instead.
    }
}

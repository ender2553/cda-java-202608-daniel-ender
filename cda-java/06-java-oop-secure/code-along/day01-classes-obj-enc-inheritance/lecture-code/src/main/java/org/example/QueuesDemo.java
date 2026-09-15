package org.example;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * a Queue is FIFO — First In, First Out. Whoever gets in line
 * first gets served first, just like cars waiting at a service bay.
 */
public class QueuesDemo {

    public static void main(String[] args) {
        // ================================================================
        // STEP A — Build a Queue from existing data (an initializer)
        // ================================================================
        // LinkedList implements Queue and has a constructor that accepts
        // an existing collection, so these three cars start out already
        // in line, in order.
        Queue<String> serviceLine = new LinkedList<>(
                List.of("Corolla #1", "Civic #2", "F-150 #3"));

        System.out.println("Cars currently in line: " + serviceLine);

        // ================================================================
        // STEP B — Add items via methods
        // ================================================================
        serviceLine.offer("Model 3 #4"); // joins the BACK of the line

        System.out.println("After Model 3 #4 joins the line: " + serviceLine);

        // ================================================================
        // STEP C — peek() looks at the front without removing it
        // ================================================================
        // PREDICT before running: which car is at the front of the line?
        System.out.println("Next car up: " + serviceLine.peek());
        System.out.println("Line unchanged after peek(): " + serviceLine);

        // ================================================================
        // STEP D — poll() removes and returns the FRONT of the line
        // ================================================================
        String nowServing = serviceLine.poll();
        System.out.println();
        System.out.println("Now servicing: " + nowServing);
        System.out.println("Remaining line: " + serviceLine);

        // ================================================================
        // STEP E — Process the whole line, first-come-first-served
        // ================================================================
        System.out.println();
        System.out.println("Servicing the rest of the line in order:");
        while (!serviceLine.isEmpty()) {
            System.out.println(" - now servicing " + serviceLine.poll());
        }
        System.out.println("Line empty? " + serviceLine.isEmpty());

        // TRY IT YOURSELF:
        //  1. Rebuild serviceLine and add five cars using a loop.
        //  2. Write a loop that polls two cars at a time (a "two-bay"
        //     service center) and prints them together per iteration.
    }
}

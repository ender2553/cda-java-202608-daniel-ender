package org.example;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * Secure coding file
 *
 */
public class SecureCodingPrinciplesDemo {

    public static void main(String[] args) {
        aliasingDemo();
        System.out.println();
        leakedMutableStateDemo();
        System.out.println();
        defensiveCopyingDemo();
        System.out.println();
        immutableViewsDemo();
    }

    // ====================================================================
    // 1) MUTABLE OBJECTS -> ALIASING
    // ====================================================================
    // Two variables can point at the SAME object. Changing it through one
    // name changes what you see through the other name too, because there
    // is only ever one object underneath — not two.
    static void aliasingDemo() {
        System.out.println("=== Aliasing ===");

        List<String> serviceHistory = new ArrayList<>(List.of("Oil change"));
        List<String> alsoServiceHistory = serviceHistory; // an ALIAS, not a copy


        alsoServiceHistory.add("Tire rotation");

        System.out.println("serviceHistory:     " + serviceHistory);
        System.out.println("alsoServiceHistory: " + alsoServiceHistory);
        System.out.println("Same object? " + (serviceHistory == alsoServiceHistory));
    }

    // ====================================================================
    // 2) GETTERS -> LEAKED MUTABLE STATE
    // ====================================================================
    static class LeakyCar {
        private final List<String> serviceHistory = new ArrayList<>();

        public void logService(String entry) {
            serviceHistory.add(entry); // the ONLY validated way in... supposedly
        }

        // VULNERABLE: hands out the actual internal list, not a copy.
        public List<String> getServiceHistory() {
            return serviceHistory;
        }
    }

    static class SecureCar {
        private final List<String> serviceHistory = new ArrayList<>();

        public void logService(String entry) {
            serviceHistory.add(entry);
        }


        public List<String> getServiceHistory() {
            return Collections.unmodifiableList(serviceHistory);
        }
    }

    static void leakedMutableStateDemo() {
        System.out.println("=== Leaked Mutable State ===");

        LeakyCar leaky = new LeakyCar();
        leaky.logService("Oil change");
        // "private" didn't help — the getter handed the real list right out:
        leaky.getServiceHistory().add("FORGED: brakes replaced (never happened)");
        System.out.println("LeakyCar history:  " + leaky.getServiceHistory());

        SecureCar secure = new SecureCar();
        secure.logService("Oil change");
        try {
            secure.getServiceHistory().add("FORGED entry");
        } catch (UnsupportedOperationException e) {
            System.out.println("SecureCar blocked the forged entry: " + e.getClass().getSimpleName());
        }
        System.out.println("SecureCar history: " + secure.getServiceHistory());
    }

    // ====================================================================
    // 3) CONSTRUCTORS -> DEFENSIVE COPYING
    // ====================================================================
    static class LeakyFleetRecord {
        private final List<String> owners;

        // VULNERABLE: stores the caller's list BY REFERENCE.
        public LeakyFleetRecord(List<String> owners) {
            this.owners = owners;
        }

        public List<String> getOwners() {
            return owners;
        }
    }

    static class SecureFleetRecord {
        private final List<String> owners;

        // copy the data into a NEW list the caller can never reach
        // again, no matter what they do to the list they originally passed in.
        public SecureFleetRecord(List<String> owners) {
            this.owners = new ArrayList<>(owners);
        }

        public List<String> getOwners() {
            return Collections.unmodifiableList(owners);
        }
    }

    static void defensiveCopyingDemo() {
        System.out.println("=== Defensive Copying ===");

        List<String> ownersInput = new ArrayList<>(List.of("Alice"));
        LeakyFleetRecord leakyRecord = new LeakyFleetRecord(ownersInput);


        ownersInput.add("Mallory (never approved)");
        System.out.println("LeakyFleetRecord owners:  " + leakyRecord.getOwners());

        List<String> ownersInput2 = new ArrayList<>(List.of("Alice"));
        SecureFleetRecord secureRecord = new SecureFleetRecord(ownersInput2);
        ownersInput2.add("Mallory (never approved)");
        System.out.println("SecureFleetRecord owners: " + secureRecord.getOwners());
    }

    // ====================================================================
    // 4) COLLECTIONS -> IMMUTABLE / UNMODIFIABLE VIEWS
    // ====================================================================
    static void immutableViewsDemo() {
        System.out.println("=== Immutable / Unmodifiable Views ===");

        // List.of(...) is immutable from the moment it's created.
        List<String> approvedMakes = List.of("Toyota", "Honda", "Ford");
        System.out.println("Approved makes: " + approvedMakes);
        try {
            approvedMakes.add("Yugo");
        } catch (UnsupportedOperationException e) {
            System.out.println("Blocked: List.of(...) rejects add() entirely.");
        }


        List<String> mutableFleet = new ArrayList<>(List.of("Corolla", "Civic"));
        List<String> readOnlyFleetView = Collections.unmodifiableList(mutableFleet);

        System.out.println("Read-only view: " + readOnlyFleetView);
        mutableFleet.add("F-150"); // still allowed — this mutates the ORIGINAL list


        System.out.println("Read-only view after mutating the original: " + readOnlyFleetView);
    }

    // TRY IT YOURSELF:
    //  1. Add a getApprovedMakes() method to SecureCar that returns a
    //     fixed List.of(...) instead of a mutable field.
    //  2. In SecureFleetRecord, add an addOwner(String name) method that
    //     rejects a blank or null name before adding it — a preview of
    //     the "Methods -> Validate parameters" principle coming up next.
}

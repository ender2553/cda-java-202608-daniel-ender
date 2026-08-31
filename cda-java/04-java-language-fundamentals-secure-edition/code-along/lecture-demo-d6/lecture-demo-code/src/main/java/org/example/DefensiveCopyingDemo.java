package org.example;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefensiveCopyingDemo {

    // TWO-STEP approach: copy explicitly in the constructor, wrap in
    // an unmodifiable view in the getter.
    static class SafeWatchlistTwoStep {
        private final List<String> cveIds;

        SafeWatchlistTwoStep(List<String> cveIds) {

            this.cveIds = new ArrayList<>(cveIds); // COPY on the way in
        }

        List<String> getCveIds() {

            return Collections.unmodifiableList(cveIds); // SAFE VIEW on the way out
        }
    }

    // ONE-CALL idiom: List.copyOf() copies AND makes the result
    // unmodifiable in a single expression -- the getter can then just
    // return the field directly, since it's already unmodifiable.
    static class SafeWatchlistCopyOf {
        private final List<String> cveIds;
        SafeWatchlistCopyOf(List<String> cveIds) {
            this.cveIds = List.copyOf(cveIds); // copy + unmodifiable, in one call
        }
        List<String> getCveIds() {
            return cveIds; // already safe - no extra wrapping needed
        }
    }

    public static void main(String[] args) {

        System.out.println("--- Defensive copying: two-step approach ---");
        demoDefensiveCopyingTwoStep();

        System.out.println();
        System.out.println("--- Defensive copying: List.copyOf() one-call idiom ---");
        demoListCopyOf();

        System.out.println();
        System.out.println("--- The exception: secrets want mutability ---");
        demoSecretZeroing();
    }

    private static void demoDefensiveCopyingTwoStep() {
        List<String> original = new ArrayList<>(List.of("CVE-2024-1234"));
        SafeWatchlistTwoStep watchlist = new SafeWatchlistTwoStep(original);

        original.add("INJECTED-BY-CALLER"); // no effect now - watchlist has its own copy
        System.out.println("After external mutation attempt: " + watchlist.getCveIds());

        try {
            watchlist.getCveIds().add("HACKED");
        } catch (UnsupportedOperationException e) {
            System.out.println("Getter mutation attempt correctly rejected!");
        }
    }

    private static void demoListCopyOf() {
        List<String> original = new ArrayList<>(List.of("CVE-2024-1234"));
        SafeWatchlistCopyOf watchlist = new SafeWatchlistCopyOf(original);

        original.add("INJECTED-BY-CALLER");
        System.out.println("After external mutation attempt: " + watchlist.getCveIds());

        try {
            watchlist.getCveIds().add("HACKED");
        } catch (UnsupportedOperationException e) {
            System.out.println("Getter mutation attempt correctly rejected!");
        }
    }

    private static void demoSecretZeroing() {
        char[] password = {'h', 'u', 'n', 't', 'e', 'r', '2'};
        System.out.println("Before use: " + new String(password));
        try {
            authenticate(password);
        } finally {
            Arrays.fill(password, '\0'); // zero the secret the instant it's used
        }
        System.out.println("After zeroing: " + Arrays.toString(password));
    }

    private static boolean authenticate(char[] password) {
        return new String(password).equals("hunter2");
    }

}



package org.example;


import java.util.ArrayList;
import java.util.List;

public class AliasingAndLeakedStateDemo {

    static class LeakyWatchlist {
        private List<String> cveIds;
        LeakyWatchlist(List<String> cveIds) {
            this.cveIds = cveIds; // TOCTOU RISK: stores the CALLER'S reference directly
        }
        List<String> getCveIds() {
            return cveIds; // LEAK: hands out the INTERNAL reference directly
        }
    }

    static class ScoreHolder {
        private final int[] scores = {10, 20, 30};
        int[] getScores() {
            return scores; // "final" protects the SLOT, not the CONTENTS
        }
    }

    public static void main(String[] args) {

        System.out.println("--- Value vs. reference ---");
        demoValueVsReference();

        System.out.println();
        System.out.println("--- Leaked List: both directions ---");
        demoLeakedListBothDirections();

        System.out.println();
        System.out.println("--- Leaked array: 'final' doesn't protect contents ---");
        demoLeakedArray();
    }

    private static void demoValueVsReference() {
        int a = 5;
        int b = a;
        b = 10;
        System.out.println("Primitives - a: " + a + ", b: " + b + " (independent)");

        List<String> listA = new ArrayList<>(List.of("CVE-2024-1234"));
        List<String> listB = listA; // NOT a copy - an ALIAS to the SAME object
        listB.add("CVE-9999-0000");
        System.out.println("References - listA: " + listA + " (changed via listB!)");
    }

    private static void demoLeakedListBothDirections() {
        List<String> original = new ArrayList<>(List.of("CVE-2024-1234"));
        LeakyWatchlist watchlist = new LeakyWatchlist(original);

        // TOCTOU GAP: the constructor "already ran," but the caller
        // still holds a live reference and can mutate the internal
        // state AFTER construction, as if validation never happened.
        original.add("INJECTED-BY-CALLER");
        System.out.println("After external mutation via constructor's stored alias: " + watchlist.getCveIds());

        // GETTER LEAK: no method with a dangerous-sounding name was
        // ever called, yet the internal state is now gone.
        watchlist.getCveIds().clear();
        System.out.println("After external .clear() via the getter's leaked alias: " + watchlist.getCveIds());
    }

    private static void demoLeakedArray() {
        ScoreHolder holder = new ScoreHolder();
        holder.getScores()[0] = 9999; // tampered - no method clearly meant to "modify" was called
        System.out.println("scores[0] after external tampering: " + holder.getScores()[0]);
    }

}



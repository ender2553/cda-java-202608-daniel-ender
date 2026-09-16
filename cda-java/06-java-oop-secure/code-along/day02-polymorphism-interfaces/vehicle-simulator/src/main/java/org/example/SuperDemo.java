package org.example;

public class SuperDemo {

    static class Watercraft {
        private final String name;
        private final int crewSize;

        protected Watercraft(String name, int crewSize) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("name must not be null/blank");
            }
            if (crewSize < 1) {
                throw new IllegalArgumentException("crewSize must be at least 1");
            }
            this.name = name;
            this.crewSize = crewSize;
        }

        @Override
        public String toString() {

            return name + " (crew of " + crewSize + ")";
        }
    }

    static class Submarine extends Watercraft {
        private final int maxDepthMeters;

        public Submarine(String name, int crewSize, int maxDepthMeters) {
            // STEP 1 - super(...) MUST be the first statement. It runs
            // Watercraft's constructor (and its validation) before this
            // constructor is allowed to touch maxDepthMeters.
            super(name, crewSize);
            this.maxDepthMeters = maxDepthMeters;
        }

        @Override
        public String toString() {
            // STEP 2 - super.toString() reuses Watercraft's formatting
            // instead of retyping "name (crew of N)" here. If Watercraft's
            // format ever changes, every subclass using super.toString()
            // updates automatically.
            return super.toString() + ", max depth " + maxDepthMeters + "m";
        }
    }

    public static void main(String[] args) {
        Watercraft ferry = new Watercraft("Island Hopper", 4);
        Submarine sub = new Submarine("USS Nautilus", 13, 300);

        System.out.println(ferry);
        System.out.println(sub);

        // TRY IT YOURSELF: change Submarine's crewSize argument to 0 and
        // re-run. super(...) means Watercraft's validation catches it -
        // Submarine never had to write that check itself.
    }
}

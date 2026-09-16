package org.example;

public class AbstractClassDemo {

    static abstract class Aircraft {
        private final String registration;
        private boolean preflightComplete = false;

        protected Aircraft(String registration) {

            this.registration = registration;
        }

        // Concrete method - shared, working code every subclass gets
        // for free. This is something an interface could never provide.
        public void preflightCheck() {
            System.out.println(registration + ": running preflight checklist...");
            preflightComplete = true;
        }

        // Abstract method - only a Jet or a Glider actually knows HOW
        // to take off, so Aircraft leaves it blank and REQUIRES every
        // subclass to fill it in.
        public abstract String takeoff();

        public String fly() {
            if (!preflightComplete) {
                return registration + ": refusing to fly - preflight check not complete.";
            }
            return takeoff();
        }
    }

    static class Jet extends Aircraft {
        public Jet(String registration) {
            super(registration);
        }

        @Override
        public String takeoff() {
            return getClass().getSimpleName() + ": full thrust, rotating at 160 knots.";
        }
    }

    static class Glider extends Aircraft {
        public Glider(String registration) {

            super(registration);
        }

        @Override
        public String takeoff() {
            return getClass().getSimpleName() + ": tow rope taut, released at 2,000 ft.";
        }
    }

    public static void main(String[] args) {
        Jet jet = new Jet("N123AB");
        Glider glider = new Glider("N456CD");


        // Aircraft generic = new Aircraft("N000ZZ");

        System.out.println(jet.fly()); // refused - no preflight check yet
        jet.preflightCheck();
        System.out.println(jet.fly()); // now allowed

        glider.preflightCheck();
        System.out.println(glider.fly());
    }
}

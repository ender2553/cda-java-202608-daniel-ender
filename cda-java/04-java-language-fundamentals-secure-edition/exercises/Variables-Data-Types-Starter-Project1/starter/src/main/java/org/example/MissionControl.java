package org.example;

/*
 * AFTERNOON EXERCISE — Variables and Data Types
 * Java Language Fundamentals with Security — Lesson 4
 *
 * SCENARIO
 * You're writing telemetry-tracking code for Stellar Outpost, a fictional
 * space station. Mission Control needs a set of small, focused pieces of
 * data recorded correctly — nothing here is complicated, but every value
 * needs the right type, and a few need to be updated or converted safely.
 *
 * HOW TO WORK THROUGH THIS FILE
 * Each method below has a TODO comment describing exactly what to declare,
 * assign, update, or convert, and what to return. Replace the placeholder
 * return statement with your own code. Do not change any method's name,
 * parameters, or return type — the tests depend on those staying exactly
 * as they are.
 *
 * CHECKING YOUR WORK
 * A full set of tests already exists for you in
 * src/test/java/org/example/MissionControlTest.java — you do not need to
 * write any tests yourself. In IntelliJ, right-click that file (or an
 * individual test) and choose Run to see which TODOs are passing and
 * which still need work. Every test will fail until its matching TODO is
 * completed correctly — that's expected, not a sign anything is broken.
 */
public class MissionControl {

    // ============================================================
    // SECTION 1 — Declare and Assign a Variable
    // ============================================================

    // TODO 1: Declare an int variable named crewCount and assign it the
    //         value 6. Return crewCount.
    public static int getCrewCount() {
        // your code here
        return 0;
    }


    // ============================================================
    // SECTION 2 — Update a Variable
    // ============================================================

    // TODO 2: Declare a double variable named oxygenLevel and assign it
    //         98.5. Then, on its own line below that, UPDATE oxygenLevel
    //         (do not declare it a second time) to 95.2, simulating a new
    //         sensor reading after a small leak was patched.
    //         Return the final value of oxygenLevel.
    public static double getUpdatedOxygenLevel() {
        // your code here
        return 0.0;
    }


    // ============================================================
    // SECTION 3 — Primitive Data Types
    // ============================================================

    // TODO 3: Declare a boolean variable named missionActive and assign
    //         it true. Return missionActive.
    public static boolean isMissionActive() {
        // your code here
        return false;
    }

    // TODO 4: Declare a char variable named commanderInitial and assign
    //         it 'R'. Return commanderInitial.
    public static char getCommanderInitial() {
        // your code here
        return ' ';
    }

    // TODO 5: Declare a byte variable named crewBadgeNumber and assign
    //         it 42. Return crewBadgeNumber.
    public static byte getCrewBadgeNumber() {
        // your code here
        return 0;
    }

    // TODO 6: Declare a short variable named orbitNumber and assign it
    //         1200. Return orbitNumber.
    public static short getOrbitNumber() {
        // your code here
        return 0;
    }

    // TODO 7: Declare a long variable named totalTelemetryPackets and
    //         assign it five billion (5,000,000,000). Remember: no
    //         commas allowed in the value, and don't forget the suffix
    //         long literals need. Return totalTelemetryPackets.
    public static long getTotalTelemetryPackets() {
        // your code here
        return 0L;
    }

    // TODO 8: Declare a float variable named fuelBurnRate and assign it
    //         2.75. Don't forget the suffix float literals need.
    //         Return fuelBurnRate.
    public static float getFuelBurnRate() {
        // your code here
        return 0f;
    }

    // TODO 9: Declare a double variable named preciseTrajectory and
    //         assign it 12.34567891. Return preciseTrajectory.
    public static double getPreciseTrajectory() {
        // your code here
        return 0.0;
    }


    // ============================================================
    // SECTION 4 — Conversion
    // ============================================================

    // TODO 10: This method receives an int, altitudeMeters. Widen it into
    //          a double variable named altitudeAsDouble. Widening should
    //          happen automatically here — no cast needed.
    //          Return altitudeAsDouble.
    public static double widenAltitude(int altitudeMeters) {
        // your code here
        return 0.0;
    }

    // TODO 11: This method receives a double, rawSignal. Narrow it into
    //          an int variable named signalStrength using an explicit
    //          (int) cast. Remember: narrowing truncates — it does not
    //          round to the nearest whole number.
    //          Return signalStrength.
    public static int narrowSignal(double rawSignal) {
        // your code here
        return 0;
    }
}

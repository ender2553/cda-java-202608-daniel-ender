package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Self-check tests for the Variables and Data Types exercise.
 *
 * You do not need to write or edit anything in this file. Run these tests
 * (right-click this file in IntelliJ and choose Run) to see which TODOs
 * in MissionControl.java are complete and which still need work. Every
 * test will fail until its matching TODO is filled in correctly.
 */
class MissionControlTest {

    @Test
    @DisplayName("TODO 1: getCrewCount() returns 6")
    void testCrewCount() {
        assertEquals(6, MissionControl.getCrewCount());
    }

    @Test
    @DisplayName("TODO 2: getUpdatedOxygenLevel() returns 95.2 after the update")
    void testUpdatedOxygenLevel() {
        assertEquals(95.2, MissionControl.getUpdatedOxygenLevel(), 0.001);
    }

    @Test
    @DisplayName("TODO 3: isMissionActive() returns true")
    void testMissionActive() {
        assertTrue(MissionControl.isMissionActive());
    }

    @Test
    @DisplayName("TODO 4: getCommanderInitial() returns 'R'")
    void testCommanderInitial() {
        assertEquals('R', MissionControl.getCommanderInitial());
    }

    @Test
    @DisplayName("TODO 5: getCrewBadgeNumber() returns 42")
    void testCrewBadgeNumber() {
        assertEquals((byte) 42, MissionControl.getCrewBadgeNumber());
    }

    @Test
    @DisplayName("TODO 6: getOrbitNumber() returns 1200")
    void testOrbitNumber() {
        assertEquals((short) 1200, MissionControl.getOrbitNumber());
    }

    @Test
    @DisplayName("TODO 7: getTotalTelemetryPackets() returns 5,000,000,000")
    void testTotalTelemetryPackets() {
        assertEquals(5_000_000_000L, MissionControl.getTotalTelemetryPackets());
    }

    @Test
    @DisplayName("TODO 8: getFuelBurnRate() returns 2.75")
    void testFuelBurnRate() {
        assertEquals(2.75f, MissionControl.getFuelBurnRate(), 0.001f);
    }

    @Test
    @DisplayName("TODO 9: getPreciseTrajectory() returns 12.34567891")
    void testPreciseTrajectory() {
        assertEquals(12.34567891, MissionControl.getPreciseTrajectory(), 0.00000001);
    }

    @Test
    @DisplayName("TODO 10: widenAltitude(500) widens to 500.0")
    void testWidenAltitude() {
        assertEquals(500.0, MissionControl.widenAltitude(500), 0.001);
    }

    @Test
    @DisplayName("TODO 11: narrowSignal(87.9) truncates to 87, not 88")
    void testNarrowSignalTruncatesDown() {
        assertEquals(87, MissionControl.narrowSignal(87.9));
    }

    @Test
    @DisplayName("TODO 11: narrowSignal(-3.9) truncates toward zero, to -3")
    void testNarrowSignalTruncatesTowardZero() {
        assertEquals(-3, MissionControl.narrowSignal(-3.9));
    }
}

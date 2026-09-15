package com.cyberdev.smarthome;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SmartHomeTest.java
 * ====================
 * Pre-written test suite \u2014 do not modify. Run with `mvn test`.
 * 18 tests total, mapped to rubric tags OOP2-1 through OOP2-9.
 */
class SmartHomeTest {

    // ---- SmartDevice validation (OOP2-1) --------------------------------

    @Test
    void smartDeviceRejectsBlankDeviceId() {
        assertThrows(IllegalArgumentException.class,
                () -> new Thermostat("  ", "Hallway", 68));
    }

    @Test
    void smartDeviceRejectsBlankLocation() {
        assertThrows(IllegalArgumentException.class,
                () -> new Thermostat("TH-1", "", 68));
    }

    // ---- Thermostat (OOP2-5) --------------------------------------------

    @Test
    void thermostatActivateMessage() {
        Thermostat t = new Thermostat("TH-1", "Hallway", 68);
        assertEquals("Thermostat set to 68\u00B0F", t.activate());
    }

    @Test
    void thermostatPowerDraw() {
        Thermostat t = new Thermostat("TH-1", "Hallway", 68);
        assertEquals(12.5, t.powerDrawWatts(), 0.0001);
    }

    @Test
    void thermostatRejectsOutOfRangeTemp() {
        assertThrows(IllegalArgumentException.class,
                () -> new Thermostat("TH-2", "Bedroom", 40));
        assertThrows(IllegalArgumentException.class,
                () -> new Thermostat("TH-2", "Bedroom", 95));
    }

    @Test
    void thermostatGetDeviceName() {
        Thermostat t = new Thermostat("TH-1", "Hallway", 68);
        assertEquals("TH-1 (Hallway)", t.getDeviceName());
    }

    // ---- SmartDevice shared behavior (OOP2-2, OOP2-3, OOP2-4) -----------

    @Test
    void runDiagnosticsFormat() {
        Thermostat t = new Thermostat("TH-1", "Hallway", 68);
        assertEquals("TH-1 @ Hallway: Thermostat set to 68\u00B0F (draws 12.5W)",
                t.runDiagnostics());
    }

    @Test
    void smartDeviceToString() {
        Thermostat t = new Thermostat("TH-1", "Hallway", 68);
        assertEquals("TH-1 @ Hallway", t.toString());
    }

    // ---- SmartLight (OOP2-6) ---------------------------------------------

    @Test
    void smartLightActivateMessage() {
        SmartLight l = new SmartLight("LT-1", "Kitchen", 75);
        assertEquals("Light on at 75% brightness", l.activate());
    }

    @Test
    void smartLightPowerDrawFormula() {
        SmartLight l = new SmartLight("LT-1", "Kitchen", 100);
        assertEquals(8.0, l.powerDrawWatts(), 0.0001);
    }

    @Test
    void smartLightToStringReusesSuperAndAddsBrightness() {
        SmartLight l = new SmartLight("LT-1", "Kitchen", 75);
        String result = l.toString();
        assertTrue(result.startsWith("LT-1 @ Kitchen"),
                "toString() should reuse SmartDevice's format via super.toString()");
        assertTrue(result.contains("brightness: 75%"),
                "toString() should append the brightness detail");
    }

    @Test
    void smartLightRejectsOutOfRangeBrightness() {
        assertThrows(IllegalArgumentException.class,
                () -> new SmartLight("LT-2", "Garage", -5));
        assertThrows(IllegalArgumentException.class,
                () -> new SmartLight("LT-2", "Garage", 150));
    }

    // ---- SecurityCamera (OOP2-7) ------------------------------------------

    @Test
    void securityCameraImplementsBothInterfaces() {
        SecurityCamera cam = new SecurityCamera("Front Door", 88.0);
        assertTrue(cam instanceof Controllable);
        assertTrue(cam instanceof Monitorable);
    }

    @Test
    void securityCameraActivateMessage() {
        SecurityCamera cam = new SecurityCamera("Front Door", 88.0);
        assertEquals("Camera 'Front Door' recording (battery: 88.0%)", cam.activate());
    }

    @Test
    void securityCameraLogEventSuccess() {
        SecurityCamera cam = new SecurityCamera("Front Door", 88.0);
        assertEquals("[EVENT] Front Door: motion detected", cam.logEvent("motion detected"));
    }

    @Test
    void securityCameraLogEventRejectsBlankDescription() {
        SecurityCamera cam = new SecurityCamera("Front Door", 88.0);
        assertThrows(IllegalArgumentException.class, () -> cam.logEvent("  "));
    }

    @Test
    void securityCameraRejectsInvalidBattery() {
        assertThrows(IllegalArgumentException.class,
                () -> new SecurityCamera("Front Door", 150.0));
    }

    // ---- SmartHomeHub polymorphism (OOP2-8, OOP2-9) ------------------------

    @Test
    void hubActivateAllCallsEveryDeviceInOrder() {
        SmartHomeHub hub = new SmartHomeHub();
        hub.addDevice(new Thermostat("TH-1", "Hallway", 68));
        hub.addDevice(new SmartLight("LT-1", "Kitchen", 75));
        hub.addDevice(new SecurityCamera("Front Door", 88.0));

        List<String> results = hub.activateAll();

        assertEquals(3, results.size());
        assertEquals("Thermostat set to 68\u00B0F", results.get(0));
        assertEquals("Light on at 75% brightness", results.get(1));
        assertEquals("Camera 'Front Door' recording (battery: 88.0%)", results.get(2));
    }

    @Test
    void hubFindMonitorableDevicesFiltersCorrectly() {
        SmartHomeHub hub = new SmartHomeHub();
        hub.addDevice(new Thermostat("TH-1", "Hallway", 68));
        hub.addDevice(new SmartLight("LT-1", "Kitchen", 75));
        SecurityCamera cam = new SecurityCamera("Front Door", 88.0);
        hub.addDevice(cam);

        List<Monitorable> monitorable = hub.findMonitorableDevices();

        assertEquals(1, monitorable.size(), "Only the SecurityCamera should be Monitorable");
        assertSame(cam, monitorable.get(0));
    }
}

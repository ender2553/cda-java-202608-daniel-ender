package com.cyberdev.smarthome;

/**
 * SecurityCamera.java
 * =====================
 * This device does NOT extend SmartDevice. A camera doesn't have a
 * "power draw in watts" model the way a thermostat or light does \u2014 it
 * has a battery percentage instead. Forcing it under SmartDevice would
 * mean faking a field that doesn't fit, exactly like Spaceship in the
 * Vehicle Simulator. Instead, it implements Controllable directly AND
 * Monitorable, since a camera is a restricted device that must log
 * every activation attempt.
 *
 * TODO 7 [OOP2-7]: Complete this class.
 *   - Fields: private final String name; private final double batteryPercent;
 *   - Constructor (String name, double batteryPercent):
 *       - Validate name is not null/blank \u2014 throw IllegalArgumentException.
 *       - Validate batteryPercent is between 0 and 100 (inclusive) \u2014
 *         throw IllegalArgumentException.
 *       - Assign both fields.
 *   - activate() returns:
 *       "Camera '" + name + "' recording (battery: " + batteryPercent + "%)"
 *   - getDeviceName() returns name.
 *   - logEvent(String description):
 *       - If description is null or blank, throw IllegalArgumentException
 *         ("description must not be blank") \u2014 fail closed, never log an
 *         empty/anonymous event.
 *       - Otherwise return: "[EVENT] " + name + ": " + description
 */
public class SecurityCamera implements Controllable, Monitorable {

    private final String name;
    private final double batteryPercent;

    public SecurityCamera(String name, double batteryPercent) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }

        if (batteryPercent < 0 || batteryPercent > 100) {
            throw new IllegalArgumentException(
                    "batteryPercent must be between 0 and 100"
            );
        }

        this.name = name;
        this.batteryPercent = batteryPercent;
    }

    @Override
    public String activate() {
        return "Camera '" + name + "' recording (battery: " + batteryPercent + "%)";
    }

    @Override
    public String getDeviceName() {
        return name;
    }

    @Override
    public String logEvent(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }

        return "[EVENT] " + name + ": " + description;
    }
}
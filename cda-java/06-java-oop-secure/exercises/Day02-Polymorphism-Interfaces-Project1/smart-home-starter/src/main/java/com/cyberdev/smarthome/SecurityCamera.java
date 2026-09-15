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

    // TODO 7 [OOP2-7]: fields, constructor, activate(), getDeviceName(), logEvent()

    public SecurityCamera(String name, double batteryPercent) {
        throw new UnsupportedOperationException("TODO 7 [OOP2-7]: validate and assign fields");
    }

    @Override
    public String activate() {
        throw new UnsupportedOperationException("TODO 7 [OOP2-7]: implement activate()");
    }

    @Override
    public String getDeviceName() {
        throw new UnsupportedOperationException("TODO 7 [OOP2-7]: implement getDeviceName()");
    }

    @Override
    public String logEvent(String description) {
        throw new UnsupportedOperationException("TODO 7 [OOP2-7]: implement logEvent()");
    }
}

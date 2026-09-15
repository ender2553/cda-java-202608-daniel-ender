package com.cyberdev.smarthome;

/**
 * Controllable.java
 * ===================
 * GIVEN \u2014 already complete, no TODOs here.
 *
 * Every device in the Smart Home Hub must be Controllable: it can be
 * activated, and it can report its own display name. This is the same
 * role Transportable played in the Vehicle Simulator \u2014 a contract that
 * lets completely unrelated classes be driven by one polymorphic loop.
 */
public interface Controllable {

    /**
     * Turn this device on / trigger its primary action. Returns a
     * human-readable status message describing what happened.
     */
    String activate();

    /**
     * A short display name for this device, e.g. "Living Room Thermostat".
     */
    String getDeviceName();
}

package com.cyberdev.smarthome;

/**
 * SmartDevice.java
 * ==================
 * The shared parent for every device that follows the same "id + location
 * + power draw" model (Thermostat, SmartLight). NOT every device in this
 * exercise extends SmartDevice \u2014 see SecurityCamera.java for a device
 * that implements Controllable directly instead, the same way Spaceship
 * did in the Vehicle Simulator.
 *
 * TODO 1 [OOP2-1]: Implement the constructor.
 *   - Validate deviceId: must not be null or blank.
 *   - Validate location: must not be null or blank.
 *   - Throw IllegalArgumentException with a clear message if either
 *     check fails (fail closed \u2014 don't let bad data in).
 *   - Otherwise, assign both fields.
 *
 * TODO 2 [OOP2-2]: Declare ONE abstract method here:
 *     double powerDrawWatts();
 *   (activate() does NOT need to be redeclared \u2014 it's already required
 *   by the Controllable interface this class implements. You only need
 *   to add an abstract method HERE for something that ISN'T already
 *   part of an interface this class implements.)
 *
 * TODO 3 [OOP2-3]: Implement runDiagnostics(), a CONCRETE method shared
 *   by every subclass. It must call activate() and powerDrawWatts() and
 *   return a String in EXACTLY this format (no extra spaces):
 *     "<deviceId> @ <location>: <activate() result> (draws <powerDrawWatts()>W)"
 *
 * TODO 4 [OOP2-4]: Implement getDeviceName() to satisfy the Controllable
 *   contract, returning "<deviceId> (<location>)". ALSO override
 *   toString() to return "<deviceId> @ <location>" \u2014 subclasses will
 *   reuse this via super.toString().
 */
public abstract class SmartDevice implements Controllable {
    private final String deviceId;
    private final String location;

    protected SmartDevice(String deviceId, String location) {
        // TODO 1 [OOP2-1]
        throw new UnsupportedOperationException("TODO 1 [OOP2-1]: validate and assign deviceId/location");
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getLocation() {
        return location;
    }

    // TODO 2 [OOP2-2]: declare the abstract powerDrawWatts() method here


    public String runDiagnostics() {
        // TODO 3 [OOP2-3]
        throw new UnsupportedOperationException("TODO 3 [OOP2-3]: implement runDiagnostics()");
    }

    @Override
    public String getDeviceName() {
        // TODO 4 [OOP2-4]
        throw new UnsupportedOperationException("TODO 4 [OOP2-4]: implement getDeviceName()");
    }

    @Override
    public String toString() {
        // TODO 4 [OOP2-4]
        throw new UnsupportedOperationException("TODO 4 [OOP2-4]: implement toString()");
    }

    // TODO: Assuming that every SmartDevice subtype shares the same equality definition.
    //  What code should you add here?
    // Add code here:




}

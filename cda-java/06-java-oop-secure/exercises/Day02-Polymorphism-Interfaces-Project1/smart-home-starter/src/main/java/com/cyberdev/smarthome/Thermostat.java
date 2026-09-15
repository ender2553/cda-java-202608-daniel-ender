package com.cyberdev.smarthome;

/**
 * Thermostat.java
 * =================
 * TODO 5 [OOP2-5]: Complete this class.
 *   - Add a private final int field: targetTempFahrenheit.
 *   - Constructor takes (String deviceId, String location, int targetTempFahrenheit):
 *       - Call super(deviceId, location) FIRST.
 *       - Validate targetTempFahrenheit is between 50 and 90 (inclusive).
 *         Throw IllegalArgumentException if not.
 *       - Assign the field.
 *   - Override activate() to return:
 *       "Thermostat set to " + targetTempFahrenheit + "\u00B0F"
 *   - Override powerDrawWatts() to return 12.5
 */
public class Thermostat extends SmartDevice {

    // TODO 5 [OOP2-5]: field, constructor, activate(), powerDrawWatts()

    public Thermostat(String deviceId, String location, int targetTempFahrenheit) {
        super(deviceId, location);
        throw new UnsupportedOperationException("TODO 5 [OOP2-5]: validate targetTempFahrenheit and assign it");
    }

    @Override
    public String activate() {
        throw new UnsupportedOperationException("TODO 5 [OOP2-5]: implement activate()");
    }

    public double powerDrawWatts() {
        throw new UnsupportedOperationException("TODO 5 [OOP2-5]: implement powerDrawWatts()");
    }
}

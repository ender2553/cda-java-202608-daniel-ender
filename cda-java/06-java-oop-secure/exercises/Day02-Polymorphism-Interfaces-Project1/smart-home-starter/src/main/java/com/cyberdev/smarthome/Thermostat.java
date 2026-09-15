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

    private final int targetTempFahrenheit;

    public Thermostat(String deviceId, String location, int targetTempFahrenheit) {
        super(deviceId, location);

        if (targetTempFahrenheit < 50 || targetTempFahrenheit > 90) {
            throw new IllegalArgumentException(
                    "targetTempFahrenheit must be between 50 and 90"
            );
        }

        this.targetTempFahrenheit = targetTempFahrenheit;
    }

    @Override
    public String activate() {
        return "Thermostat set to " + targetTempFahrenheit + "°F";
    }

    @Override
    public double powerDrawWatts() {
        return 12.5;
    }
}


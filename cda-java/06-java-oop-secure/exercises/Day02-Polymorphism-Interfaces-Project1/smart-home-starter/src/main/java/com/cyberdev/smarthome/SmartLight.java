package com.cyberdev.smarthome;

/**
 * SmartLight.java
 * =================
 * TODO 6 [OOP2-6]: Complete this class.
 *   - Add a private final int field: brightnessPercent.
 *   - Constructor takes (String deviceId, String location, int brightnessPercent):
 *       - Call super(deviceId, location) FIRST.
 *       - Validate brightnessPercent is between 0 and 100 (inclusive).
 *         Throw IllegalArgumentException if not.
 *       - Assign the field.
 *   - Override activate() to return:
 *       "Light on at " + brightnessPercent + "% brightness"
 *   - Override powerDrawWatts() to return brightnessPercent * 0.08
 *   - Override toString() to call super.toString() and append:
 *       " | brightness: " + brightnessPercent + "%"
 *     (This is the super.method() reuse pattern from lecture \u2014 do NOT
 *     retype SmartDevice's formatting here.)
 */
public class SmartLight extends SmartDevice {

    // TODO 6 [OOP2-6]: field, constructor, activate(), powerDrawWatts(), toString()

    public SmartLight(String deviceId, String location, int brightnessPercent) {
        super(deviceId, location);
        throw new UnsupportedOperationException("TODO 6 [OOP2-6]: validate brightnessPercent and assign it");
    }

    @Override
    public String activate() {
        throw new UnsupportedOperationException("TODO 6 [OOP2-6]: implement activate()");
    }

    public double powerDrawWatts() {
        throw new UnsupportedOperationException("TODO 6 [OOP2-6]: implement powerDrawWatts()");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("TODO 6 [OOP2-6]: implement toString() using super.toString()");
    }
}

package com.cyberdev.smarthome;

import java.util.ArrayList;
import java.util.List;

/**
 * SmartHomeHub.java
 * ===================
 * This is where polymorphism pays off: the hub only ever talks to
 * devices through the Controllable interface. It never needs an
 * if/else chain checking "is this a Thermostat? a SecurityCamera?" \u2014
 * and it never will, no matter how many new device types get added.
 */
public class SmartHomeHub {
    private final List<Controllable> devices = new ArrayList<>();

    public void addDevice(Controllable device) {
        if (device == null) {
            throw new IllegalArgumentException("device must not be null");
        }
        devices.add(device);
    }

    public int deviceCount() {
        return devices.size();
    }

    /**
     * TODO 8 [OOP2-8]: Return a List<String> containing the result of
     * calling activate() on EVERY device in this hub, in the order they
     * were added. This is the payoff of Controllable: one loop, any
     * number of unrelated device types.
     */
    public List<String> activateAll() {
        throw new UnsupportedOperationException("TODO 8 [OOP2-8]: implement activateAll()");
    }

    /**
     * TODO 9 [OOP2-9]: Return a List<Monitorable> containing only the
     * devices in this hub that ALSO implement Monitorable.
     *
     * Use instanceof PATTERN MATCHING (e.g. `if (d instanceof Monitorable m)`)
     * rather than a blind cast \u2014 a blind cast risks a ClassCastException
     * the moment this hub contains a Controllable that ISN'T Monitorable
     * (which, right now, is most of them).
     */
    public List<Monitorable> findMonitorableDevices() {
        throw new UnsupportedOperationException("TODO 9 [OOP2-9]: implement findMonitorableDevices()");
    }
}

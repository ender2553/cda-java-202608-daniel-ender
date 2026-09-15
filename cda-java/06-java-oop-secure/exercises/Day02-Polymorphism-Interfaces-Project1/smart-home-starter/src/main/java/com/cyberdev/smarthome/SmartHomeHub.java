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

    public List<String> activateAll() {
        List<String> results = new ArrayList<>();

        for (Controllable device : devices) {
            results.add(device.activate());
        }

        return results;
    }

    public List<Monitorable> findMonitorableDevices() {
        List<Monitorable> monitorableDevices = new ArrayList<>();

        for (Controllable device : devices) {
            if (device instanceof Monitorable m) {
                monitorableDevices.add(m);
            }
        }

        return monitorableDevices;
    }
}
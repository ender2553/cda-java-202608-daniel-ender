package com.cybersoft.vehicle.service;
import com.cybersoft.vehicle.model.Vehicle;
/** Interface polymorphism: callers depend on a capability, not an implementation. */
public interface RatePolicy { double quote(Vehicle vehicle, int days); }

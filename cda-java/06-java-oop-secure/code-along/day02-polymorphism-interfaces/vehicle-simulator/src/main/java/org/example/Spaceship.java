package org.example;

public class Spaceship implements Transportable, AuditLoggable {
    private final String name;
    private double powerCellLevel; // 0.0 - 100.0 percent
    private double lightYearsTraveled;

    public Spaceship(String name, double powerCellLevel) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null/blank");
        }
        if (powerCellLevel < 0 || powerCellLevel > 100) {
            throw new IllegalArgumentException("powerCellLevel must be between 0 and 100");
        }
        this.name = name;
        this.powerCellLevel = powerCellLevel;
        this.lightYearsTraveled = 0;
    }

    @Override
    public String move(int lightYears) {
        double chargeNeeded = lightYears * 4.0; // 4% power cell per light-year
        if (chargeNeeded > powerCellLevel) {
            return name + " cannot jump " + lightYears + " light-year(s) - insufficient power cells.";
        }
        powerCellLevel -= chargeNeeded;
        lightYearsTraveled += lightYears;
        return name + " jumped " + lightYears + " light-year(s). Power cells at "
                + String.format("%.1f", powerCellLevel) + "%.";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String logAccessAttempt(String operatorId) {
        String entry = "[AUDIT] " + name + " flight-control access attempt by operator '"
                + operatorId + "' — power cells: " + String.format("%.1f", powerCellLevel) + "%";
        System.out.println(entry);
        return entry;
    }

    public double getPowerCellLevel() {
        return powerCellLevel;
    }

    public double getLightYearsTraveled() {
        return lightYearsTraveled;
    }

    @Override
    public String toString() {
        return String.format("%s | power cells: %.1f%% | distance: %.1f ly",
                name, powerCellLevel, lightYearsTraveled);
    }
}

package org.example;

public interface Transportable {

    /**
     * Attempt to move this object by the given number of units
     * (miles, nautical miles, light-years - each implementer decides).
     * Returns a human-readable status message describing what happened.
     */
    String move(int units);

    /**
     * A short display name for this fleet member, e.g. "2024 Ford F-150"

     */
    String getName();
}

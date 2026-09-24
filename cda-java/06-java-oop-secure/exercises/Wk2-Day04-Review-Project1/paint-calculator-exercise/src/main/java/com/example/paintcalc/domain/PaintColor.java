package com.example.paintcalc.domain;

public enum PaintColor {
    WARM_WHITE("Warm White", "Brightens small rooms and works with almost any trim."),
    OCEAN_BLUE("Ocean Blue", "Calming choice for bedrooms, offices, or reading spaces."),
    FOREST_GREEN("Forest Green", "Grounding color that pairs well with natural wood."),
    SUNSET_YELLOW("Sunset Yellow", "Cheerful accent for kitchens, playrooms, or creative spaces.");

    private final String displayName;
    private final String advice;
    PaintColor(String displayName, String advice) { this.displayName = displayName; this.advice = advice; }
    public String displayName() { return displayName; }
    public String advice() { return advice; }
}

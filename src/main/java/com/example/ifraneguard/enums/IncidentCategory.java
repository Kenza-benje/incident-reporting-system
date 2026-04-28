package com.example.ifraneguard.enums;

public enum IncidentCategory {
    WILDFIRE("Wildfire", "Sudden fire outbreaks in forests or urban areas"),
    ILLEGAL_LOGGING("Illegal Logging", "Unauthorized tree cutting or deforestation"),
    FOREST_DEGRADATION("Forest Degradation", "Gradual deterioration of forest quality due to environmental or human factors"),
    SNOW_ROAD_BLOCK("Snow Road Blockage", "Roads blocked due to snow or ice accumulation"),
    WATER_ISSUE("Water Infrastructure Issue", "Problems related to water supply systems, leaks, or infrastructure damage"),
    ACCIDENT("Accident", "Traffic or public accidents requiring immediate attention");

    private final String displayName;
    private final String description;

    IncidentCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }
    public String getDescription() {
        return description;
    }
}
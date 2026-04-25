package com.example.demo.model;

public enum IncidentCategory {
    WILDFIRE("Wildfire"),
    ILLEGAL_LOGGING("Illegal Logging"),
    FOREST_DEGRADATION("Forest Degradation"),
    SNOW_COVERED_ROAD_BLOCKAGE("Snow-Covered Road Blockage"),
    WATER_INFRASTRUCTURE_PROBLEM("Water Infrastructure Problem"),
    ACCIDENT("Accident");

    private final String label;

    IncidentCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

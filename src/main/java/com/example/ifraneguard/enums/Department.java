package com.example.ifraneguard.enums;

/**
 * Valid departments that can be assigned incidents.
 */
public enum Department {

    MUNICIPALITY_SERVICES("Municipality Services"),
    CIVIL_PROTECTION("Civil Protection"),
    FOREST_SERVICES("Forest Services"),
    WATER_INFRASTRUCTURE_SERVICES("Water / Infrastructure Services"),
    ROAD_PUBLIC_WORKS_SERVICES("Road / Public Works Services");

    private final String displayName;

    Department(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
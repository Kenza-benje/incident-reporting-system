package com.example.ifraneguard.enums;

public enum Role {
    CITIZEN,                // The normal user who reports incidents
    MUNICIPALITY_OFFICER,   // Handles water issues and general city problems
    CIVIL_PROTECTION,       // Handles accidents and snow road blocks
    FOREST_SERVICE,         // Handles wildfires and illegal logging
    SUPERVISOR,             // The person who gets the "Escalation" alerts after 30 mins
    ADMIN                   // Full system access
}
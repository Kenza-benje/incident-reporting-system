package com.example.ifraneguard.service;


import com.example.ifraneguard.enums.Department;
import com.example.ifraneguard.enums.IncidentCategory;
import com.example.ifraneguard.enums.UrgencyLevel;
import org.springframework.stereotype.Service;

@Service
public class UrgencyMappingService {

    /**
     * Maps an incident category to its urgency level.
     */
    public UrgencyLevel determineUrgency(IncidentCategory category) {
        return switch (category) {

            // HIGH → Immediate danger
            case WILDFIRE, ACCIDENT -> UrgencyLevel.HIGH;

            // MEDIUM → Needs quick intervention
            case SNOW_ROAD_BLOCK, WATER_ISSUE -> UrgencyLevel.MEDIUM;

            // LOW → Less urgent, environmental or gradual issues
            case ILLEGAL_LOGGING, FOREST_DEGRADATION -> UrgencyLevel.LOW;
        };
    }

    /**
     * Maps category to responsible department.
     */
    public Department determineDepartment(IncidentCategory category) {
        return switch (category) {

            case WILDFIRE, ILLEGAL_LOGGING, FOREST_DEGRADATION
                    -> Department.FOREST_SERVICES;

            case WATER_ISSUE
                    -> Department.WATER_INFRASTRUCTURE_SERVICES;

            case SNOW_ROAD_BLOCK
                    -> Department.ROAD_PUBLIC_WORKS_SERVICES;

            case ACCIDENT
                    -> Department.CIVIL_PROTECTION;
        };
    }
}
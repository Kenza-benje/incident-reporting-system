package com.example.ifraneguard.enums;


/**
 * Represents the urgency / priority of an incident.
 * Mapped automatically based on IncidentCategory in IncidentService.
 * HIGH   → Wildfire
 * MEDIUM → Snow Blockage, Water Infrastructure
 * LOW    → Illegal Logging
 */
public enum UrgencyLevel {
    HIGH("High", "Immediate response required — life or property at risk", 6),
    MEDIUM("Medium", "Prompt response needed — significant inconvenience", 12),
    LOW("Low", "Response within normal working hours", 48);

    private final String displayName;
    private final String description;
    /** Hours before this urgency level is considered overdue **/
    private final int overdueThresholdHours;

    UrgencyLevel(String displayName, String description, int overdueThresholdHours) {
        this.displayName = displayName;
        this.description = description;
        this.overdueThresholdHours = overdueThresholdHours;
    }
    public String getDisplayName() {
        return displayName;
    }
    public String getDescription() {
        return description;
    }
    public int getOverdueThresholdHours() {
        return overdueThresholdHours;
    }
}

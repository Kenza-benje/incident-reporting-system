package com.example.ifraneguard.enums;

public enum IncidentStatus {
    SUBMITTED("Submitted", "Citizen has submitted the report, awaiting authority review"),
    UNDER_REVIEW("Under Review", "Authority has opened the report and is reviewing it"),
    ASSIGNED("Assigned", "Incident has been assigned to a department and officer"),
    IN_PROGRESS("In Progress", "Department is actively working on resolving the incident"),
    RESOLVED("Resolved", "Incident has been successfully resolved"),
    REJECTED("Rejected", "Authority has determined the report is invalid or duplicate");

    private final String displayName;
    private final String description;

    IncidentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    /**
     * Enforces the strict workflow:
     *   SUBMITTED → UNDER_REVIEW → ASSIGNED → IN_PROGRESS → RESOLVED
     *                           ↘ REJECTED
     */
    public boolean canTransitionTo(IncidentStatus target) {
        return switch (this) {
            case SUBMITTED -> target == UNDER_REVIEW || target == REJECTED;
            case UNDER_REVIEW -> target == ASSIGNED ;
            case ASSIGNED     -> target == IN_PROGRESS;
            case IN_PROGRESS  -> target == RESOLVED;
            case RESOLVED, REJECTED -> false;
        };
    }
}

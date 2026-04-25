
package com.example.ifraneguard.enums;

public enum IncidentStatus {
    SUBMITTED("Submitted", "Citizen has submitted the report, awaiting authority review"),
    REVIEWED("Under Review", "Authority has opened the report and is reviewing it"),
    ASSIGNED("Assigned", "Incident has been assigned to a department and officer"),
    IN_PROGRESS("In Progress", "Department is actively working on resolving the incident"),
    RESOLVED("In Progress", "Department is actively working on resolving the incident"),
    REJECTED("Rejected", "Authority has determined the report is invalid or duplicate");
    //OVERDUE

    private final String displayName;
    private final String description;

    IncidentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }
    public String getDescription() {
        return description;
    }

    /**
     * Checks if a transition from this status to the target status is valid.
     **/
    public boolean canTransitionTo(IncidentStatus target) {
        return switch (this) {
            case SUBMITTED    -> target == REVIEWED;
            case REVIEWED -> target == ASSIGNED || target == REJECTED;
            case ASSIGNED     -> target == IN_PROGRESS;
            case IN_PROGRESS  -> target == RESOLVED;
            // Terminal states —> no further transitions allowed
            case RESOLVED, REJECTED -> false;
        };
}
}

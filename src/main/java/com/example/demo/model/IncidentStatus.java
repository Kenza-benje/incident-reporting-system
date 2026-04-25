package com.example.demo.model;

public enum IncidentStatus {
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    VERIFIED("Verified"),
    REJECTED("Rejected"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved");

    private final String label;

    IncidentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

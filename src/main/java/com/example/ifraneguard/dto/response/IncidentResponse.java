package com.example.ifraneguard.dto.response;

import com.example.ifraneguard.Model.Incident;
import com.example.ifraneguard.enums.IncidentCategory;
import com.example.ifraneguard.enums.IncidentStatus;
import com.example.ifraneguard.enums.UrgencyLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * What the API returns to the frontend when displaying an incident.
 * Never exposes the raw entity — gives us control over what data leaves the server.
 */
@Data
@Builder
public class IncidentResponse {

    private Long id;
    private String title;
    private String description;
    private IncidentCategory category;
    private String categoryDisplayName;
    private IncidentStatus status;
    private String statusDisplayName;
    private UrgencyLevel urgencyLevel;
    private String urgencyDisplayName;
    private Double latitude;
    private Double longitude;
    private String locationDescription;
    private String photoUrl;

    private boolean overdue;   // Computed from isOverdue()
    private boolean escalated;

    // Reporter info (simplified — not full User object)
    private Long reporterId;
    private String reporterName;

    // Assignment info (null if not yet assigned)
    private String assignedDepartment;
    private String assignedOfficerName;
    private String assignmentNote;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    /**
     * Factory method — converts a JPA entity into a safe API response object.
     * Keeps the mapping logic in one place.
     */
    public static IncidentResponse from(Incident incident) {
        var builder = IncidentResponse.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .category(incident.getCategory())
                .categoryDisplayName(incident.getCategory().getDisplayName())
                .status(incident.getStatus())
                .statusDisplayName(incident.getStatus().getDisplayName())
                .urgencyLevel(incident.getUrgencyLevel())
                .urgencyDisplayName(incident.getUrgencyLevel() != null
                        ? incident.getUrgencyLevel().getDisplayName() : null)
                .latitude(incident.getLatitude())
                .longitude(incident.getLongitude())
                .locationDescription(incident.getLocationDescription())
                .photoUrl(incident.getPhotoUrl())
                .overdue(incident.isOverdue())
                .escalated(incident.isEscalated())
                .reporterId(incident.getReporter() != null ? incident.getReporter().getId() : null)
                .reporterName(incident.getReporter() != null ? incident.getReporter().getFullName() : null)
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .resolvedAt(incident.getResolvedAt());

        // Safely attach assignment details if they exist
        if (incident.getAssignment() != null) {
            var assignment = incident.getAssignment();
            builder.assignedDepartment(assignment.getDepartment() != null
                    ? assignment.getDepartment().getDisplayName() : null);
            builder.assignedOfficerName(assignment.getAssignedOfficer() != null
                    ? assignment.getAssignedOfficer().getFullName() : null);
            builder.assignmentNote(assignment.getNote());
        }

        return builder.build();
    }
}
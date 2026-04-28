package com.example.ifraneguard.Mapper;

import com.example.ifraneguard.dto.response.IncidentResponse;
import com.example.ifraneguard.Model.Assignment;
import com.example.ifraneguard.Model.Incident;
import org.springframework.stereotype.Component;

@Component
public class IncidentMapper {

    public IncidentResponse toResponse(Incident incident) {
        if (incident == null) return null;

        IncidentResponse.IncidentResponseBuilder builder = IncidentResponse.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .category(incident.getCategory())
                .categoryDisplayName(
                        incident.getCategory() != null
                                ? incident.getCategory().getDisplayName() : null)
                .status(incident.getStatus())
                .statusDisplayName(
                        incident.getStatus() != null
                                ? incident.getStatus().getDisplayName() : null)
                .urgencyLevel(incident.getUrgencyLevel())
                .urgencyDisplayName(
                        incident.getUrgencyLevel() != null
                                ? incident.getUrgencyLevel().getDisplayName() : null)
                .latitude(incident.getLatitude())
                .longitude(incident.getLongitude())
                .locationDescription(incident.getLocationDescription())
                .photoUrl(incident.getPhotoUrl())
                .overdue(incident.isOverdue())           // computed — no DB call
                .escalated(incident.isEscalated())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .resolvedAt(incident.getResolvedAt());

        // Reporter (lazy-loaded — only map if already fetched by JPA)
        if (incident.getReporter() != null) {
            builder.reporterId(incident.getReporter().getId());
            builder.reporterName(incident.getReporter().getFullName());
        }

        // Assignment details (null-safe — incident may not be assigned yet)
        Assignment assignment = incident.getAssignment();
        if (assignment != null) {
            builder.assignedDepartment(
                    assignment.getDepartment() != null
                            ? assignment.getDepartment().getDisplayName() : null);
            builder.assignedOfficerName(
                    assignment.getAssignedOfficer() != null
                            ? assignment.getAssignedOfficer().getFullName() : null);
            builder.assignmentNote(assignment.getNote());
        }

        return builder.build();
    }
}
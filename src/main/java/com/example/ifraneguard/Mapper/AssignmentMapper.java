package com.example.ifraneguard.Mapper;

import com.example.ifraneguard.dto.response.AssignmentResponse;
import com.example.ifraneguard.Model.Assignment;
import org.springframework.stereotype.Component;

/**
 * Converts Assignment entities → DTOs.
 * Useful when the authority wants to see the full assignment history for an incident.
 */
@Component
public class AssignmentMapper {

    public AssignmentResponse toResponse(Assignment assignment) {
        if (assignment == null) return null;

        return AssignmentResponse.builder()
                .id(assignment.getId())
                .incidentId(assignment.getIncident() != null
                        ? assignment.getIncident().getId() : null)
                .department(assignment.getDepartment())
                .departmentDisplayName(assignment.getDepartment() != null
                        ? assignment.getDepartment().getDisplayName() : null)
                .assignedOfficerName(assignment.getAssignedOfficer() != null
                        ? assignment.getAssignedOfficer().getFullName() : null)
                .assignedByName(assignment.getAssignedBy() != null
                        ? assignment.getAssignedBy().getFullName() : null)
                .note(assignment.getNote())
                .assignedAt(assignment.getAssignedAt())
                .build();
    }
}

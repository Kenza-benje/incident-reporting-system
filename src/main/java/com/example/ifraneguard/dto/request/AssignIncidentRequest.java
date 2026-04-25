package com.example.ifraneguard.dto.request;

import com.example.ifraneguard.enums.Department;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Sent by an authority when assigning an incident to a department/officer.
 * Maps exactly to the fields in the frontend assignment form.
 */
@Data
public class AssignIncidentRequest {

    @NotNull(message = "Department is required")
    private Department department;

    /**
     * Officer ID is optional — if not set, the system auto-picks the least busy officer.
     */
    private Long officerId;

    private String note; // Optional assignment note from the authority
}
package com.example.ifraneguard.dto.request;

import com.example.ifraneguard.enums.IncidentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Used when an authority or field officer updates the status of an incident
 * (e.g., from ASSIGNED to IN_PROGRESS, or IN_PROGRESS to RESOLVED).
 */
@Data
public class StatusUpdateRequest {

    @NotNull(message = "Target status is required")
    private IncidentStatus targetStatus;

    private String reason; // Optional reason for the change
}


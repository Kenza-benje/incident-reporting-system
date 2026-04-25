package com.example.demo.dto;

import com.example.demo.model.IncidentCategory;
import com.example.demo.model.IncidentStatus;
import com.example.demo.model.ReporterMode;

import java.time.LocalDateTime;
import java.util.List;

public record CitizenIncidentResponse(
        String incidentId,
        IncidentCategory category,
        String description,
        Double latitude,
        Double longitude,
        ReporterMode reporterMode,
        String reporterUserId,
        LocalDateTime submittedAt,
        IncidentStatus status,
        List<String> photoNames,
        List<CitizenTimelineEventResponse> timeline
) {
}

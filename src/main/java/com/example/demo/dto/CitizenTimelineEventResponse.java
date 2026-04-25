package com.example.demo.dto;

import com.example.demo.model.IncidentStatus;

import java.time.LocalDateTime;

public record CitizenTimelineEventResponse(
        IncidentStatus status,
        String note,
        String updatedBy,
        LocalDateTime at
) {
}

package com.example.demo.model;

import java.time.LocalDateTime;

public record IncidentTimelineEvent(
        IncidentStatus status,
        String note,
        String updatedBy,
        LocalDateTime at
) {
}

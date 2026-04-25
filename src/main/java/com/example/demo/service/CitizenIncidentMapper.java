package com.example.demo.service;

import com.example.demo.dto.CitizenIncidentResponse;
import com.example.demo.dto.CitizenTimelineEventResponse;
import com.example.demo.model.CitizenIncident;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CitizenIncidentMapper {

    public CitizenIncidentResponse toResponse(CitizenIncident incident) {
        List<CitizenTimelineEventResponse> timeline = incident.getTimeline().stream()
                .map(event -> new CitizenTimelineEventResponse(
                        event.status(),
                        event.note(),
                        event.updatedBy(),
                        event.at()
                ))
                .toList();

        return new CitizenIncidentResponse(
                incident.getIncidentId(),
                incident.getCategory(),
                incident.getDescription(),
                incident.getLatitude(),
                incident.getLongitude(),
                incident.getReporterMode(),
                incident.getReporterUserId(),
                incident.getSubmittedAt(),
                incident.getStatus(),
                incident.getPhotoNames(),
                timeline
        );
    }
}

package com.example.demo.service;

import com.example.demo.dto.CitizenIncidentCreateRequest;
import com.example.demo.dto.CitizenIncidentResponse;

import java.util.List;

public interface CitizenIncidentFacade {
    CitizenIncidentResponse createIncident(CitizenIncidentCreateRequest request);

    List<CitizenIncidentResponse> getReportsForUser(String userId);

    CitizenIncidentResponse getById(String incidentId);

    List<CitizenIncidentResponse> getAllIncidents();
}

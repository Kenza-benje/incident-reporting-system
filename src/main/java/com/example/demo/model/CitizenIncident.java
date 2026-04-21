package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CitizenIncident {
    private String incidentId;
    private IncidentCategory category;
    private String description;
    private double latitude;
    private double longitude;
    private ReporterMode reporterMode;
    private String reporterUserId;
    private LocalDateTime submittedAt;
    private IncidentStatus status;
    private List<String> photoNames;
    private final List<IncidentTimelineEvent> timeline = new ArrayList<>();

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public IncidentCategory getCategory() {
        return category;
    }

    public void setCategory(IncidentCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public ReporterMode getReporterMode() {
        return reporterMode;
    }

    public void setReporterMode(ReporterMode reporterMode) {
        this.reporterMode = reporterMode;
    }

    public String getReporterUserId() {
        return reporterUserId;
    }

    public void setReporterUserId(String reporterUserId) {
        this.reporterUserId = reporterUserId;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public List<String> getPhotoNames() {
        return photoNames;
    }

    public void setPhotoNames(List<String> photoNames) {
        this.photoNames = photoNames;
    }

    public List<IncidentTimelineEvent> getTimeline() {
        return timeline;
    }
}

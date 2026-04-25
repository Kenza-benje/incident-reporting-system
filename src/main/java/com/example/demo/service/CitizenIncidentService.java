package com.example.demo.service;

import com.example.demo.dto.CitizenIncidentCreateRequest;
import com.example.demo.dto.CitizenIncidentResponse;
import com.example.demo.model.CitizenIncident;
import com.example.demo.model.IncidentCategory;
import com.example.demo.model.IncidentStatus;
import com.example.demo.model.IncidentTimelineEvent;
import com.example.demo.model.ReporterMode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CitizenIncidentService implements CitizenIncidentFacade {
    private static final int DESCRIPTION_MIN = 20;
    private static final int DESCRIPTION_MAX = 1000;
    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final List<String> ACCEPTED_TYPES = List.of("image/jpeg", "image/png");

    private final AtomicInteger sequence = new AtomicInteger(1);
    private final Map<String, CitizenIncident> incidentsById = new ConcurrentHashMap<>();
    private final CitizenIncidentMapper mapper;

    public CitizenIncidentService(CitizenIncidentMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public CitizenIncidentResponse createIncident(CitizenIncidentCreateRequest request) {
        CitizenIncident incident = createIncident(
                request.category(),
                request.description(),
                request.latitude(),
                request.longitude(),
                request.reporterMode(),
                request.reporterUserId(),
                request.photos()
        );
        return mapper.toResponse(incident);
    }

    public CitizenIncident createIncident(
            IncidentCategory category,
            String description,
            Double latitude,
            Double longitude,
            ReporterMode reporterMode,
            String reporterUserId,
            List<MultipartFile> photos
    ) {
        validate(category, description, latitude, longitude, photos);

        CitizenIncident incident = new CitizenIncident();
        incident.setIncidentId(generateIncidentId());
        incident.setCategory(category);
        incident.setDescription(description.trim());
        incident.setLatitude(latitude);
        incident.setLongitude(longitude);
        incident.setReporterMode(reporterMode == null ? ReporterMode.ANONYMOUS : reporterMode);
        incident.setReporterUserId(normalizeReporterId(reporterUserId, incident.getReporterMode()));
        incident.setSubmittedAt(LocalDateTime.now());
        incident.setStatus(IncidentStatus.SUBMITTED);
        incident.setPhotoNames(extractPhotoNames(photos));
        incident.getTimeline().add(new IncidentTimelineEvent(
                IncidentStatus.SUBMITTED,
                "Incident reported by citizen.",
                "system",
                incident.getSubmittedAt()
        ));

        incidentsById.put(incident.getIncidentId(), incident);
        return incident;
    }

    @Override
    public List<CitizenIncidentResponse> getReportsForUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return List.of();
        }

        return incidentsById.values().stream()
                .filter(i -> Objects.equals(i.getReporterUserId(), userId))
                .sorted(Comparator.comparing(CitizenIncident::getSubmittedAt).reversed())
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public CitizenIncidentResponse getById(String incidentId) {
        return mapper.toResponse(getByIdEntity(incidentId));
    }

    public CitizenIncident getByIdEntity(String incidentId) {
        CitizenIncident incident = incidentsById.get(incidentId);
        if (incident == null) {
            throw new IllegalArgumentException("Incident not found: " + incidentId);
        }
        return incident;
    }

    @Override
    public List<CitizenIncidentResponse> getAllIncidents() {
        return incidentsById.values().stream()
                .sorted(Comparator.comparing(CitizenIncident::getSubmittedAt).reversed())
                .map(mapper::toResponse)
                .toList();
    }

    public void addAuthorityStatusUpdate(String incidentId, IncidentStatus status, String note, String updatedBy) {
        CitizenIncident incident = getByIdEntity(incidentId);
        incident.setStatus(status);
        incident.getTimeline().add(new IncidentTimelineEvent(
                status,
                note,
                updatedBy == null || updatedBy.isBlank() ? "authority" : updatedBy,
                LocalDateTime.now()
        ));
    }

    private void validate(IncidentCategory category, String description, Double latitude, Double longitude, List<MultipartFile> photos) {
        if (category == null) {
            throw new IllegalArgumentException("Incident category is required.");
        }
        if (description == null || description.trim().length() < DESCRIPTION_MIN || description.trim().length() > DESCRIPTION_MAX) {
            throw new IllegalArgumentException("Description must be between 20 and 1000 characters.");
        }
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Location is required.");
        }
        if (photos == null || photos.isEmpty() || photos.size() > 5) {
            throw new IllegalArgumentException("Upload between 1 and 5 photos.");
        }
        for (MultipartFile photo : photos) {
            if (photo == null || photo.isEmpty()) {
                throw new IllegalArgumentException("Empty photo file is not allowed.");
            }
            if (photo.getSize() > MAX_IMAGE_SIZE_BYTES) {
                throw new IllegalArgumentException("Each image must be <= 10 MB.");
            }
            if (!ACCEPTED_TYPES.contains(photo.getContentType())) {
                throw new IllegalArgumentException("Only JPG, JPEG, and PNG formats are accepted.");
            }
        }
    }

    private String generateIncidentId() {
        return "INC-" + LocalDateTime.now().getYear() + "-" + String.format("%04d", sequence.getAndIncrement());
    }

    private String normalizeReporterId(String reporterUserId, ReporterMode reporterMode) {
        if (reporterMode == ReporterMode.AUTHENTICATED) {
            if (reporterUserId == null || reporterUserId.isBlank()) {
                return "user-" + UUID.randomUUID().toString().substring(0, 8);
            }
            return reporterUserId.trim();
        }
        return null;
    }

    private List<String> extractPhotoNames(List<MultipartFile> photos) {
        List<String> names = new ArrayList<>();
        for (MultipartFile photo : photos) {
            names.add(photo.getOriginalFilename() == null ? "unknown.jpg" : photo.getOriginalFilename());
        }
        return names;
    }
}

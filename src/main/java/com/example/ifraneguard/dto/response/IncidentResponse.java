package com.example.ifraneguard.dto.response;

import com.example.ifraneguard.Model.Incident;
import com.example.ifraneguard.Model.IncidentPhoto;
import com.example.ifraneguard.enums.IncidentCategory;
import com.example.ifraneguard.enums.IncidentStatus;
import com.example.ifraneguard.enums.PhotoType;
import com.example.ifraneguard.enums.UrgencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {

    private Long id;
    private String title;
    private String description;
    private IncidentCategory category;
    private String categoryDisplayName;
    private IncidentStatus status;
    private String statusDisplayName;
    private UrgencyLevel urgencyLevel;
    private String urgencyDisplayName;
    private Double latitude;
    private Double longitude;
    private String locationDescription;
    private String photoUrl;

    /** All uploaded photos (citizen evidence photos) as web-accessible URLs */
    private List<String> photoUrls;

    /** True if at least one evidence photo was flagged as blurry by blur detection */
    private boolean hasBlurryPhotos;

    /** After-resolution proof photo URL */
    private String afterPhotoUrl;

    private boolean overdue;
    private boolean escalated;

    private Long reporterId;
    private String reporterName;

    private String assignedDepartment;
    private String assignedOfficerName;
    private String assignmentNote;

    private String resolutionNotes;
    private String responsibleAgent;
    private LocalDateTime resolutionTimestamp;
    private String reviewNotes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;

    /**
     * Converts the absolute on-disk file path stored in incident_photos.file_path
     * to a browser-accessible URL served by WebConfig.
     * Example:
     *   /home/user/app/uploads/incidents/abc-123.jpg  →  /uploads/incidents/abc-123.jpg
     *    C:\\app\\uploads\\incidents\\abc-123.jpg  ->  /uploads/incidents/abc-123.jpg
     *    (already a URL) /uploads/incidents/abc-123.jpg → /uploads/incidents/abc-123.jpg
     */
    private static String toWebUrl(String filePath) {
        if (filePath == null) return null;

        // Already a web URL — return as-is
        if (filePath.startsWith("/uploads/")) return filePath;

        // Normalise path separators (Windows → Unix)
        String normalized = filePath.replace('\\', '/');

        // Extract the portion starting at "uploads/incidents/"
        int idx = normalized.indexOf("uploads/incidents/");
        if (idx >= 0) {
            return "/" + normalized.substring(idx);
        }

        // Last resort: use just the filename
        String fileName = normalized.contains("/")
                ? normalized.substring(normalized.lastIndexOf('/') + 1)
                : normalized;
        return "/uploads/incidents/" + fileName;
    }

    public static IncidentResponse from(Incident incident) {
        var builder = IncidentResponse.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .category(incident.getCategory())
                .categoryDisplayName(incident.getCategory().getDisplayName())
                .status(incident.getStatus())
                .statusDisplayName(incident.getStatus().getDisplayName())
                .urgencyLevel(incident.getUrgencyLevel())
                .urgencyDisplayName(incident.getUrgencyLevel() != null
                        ? incident.getUrgencyLevel().getDisplayName() : null)
                .latitude(incident.getLatitude())
                .longitude(incident.getLongitude())
                .locationDescription(incident.getLocationDescription())
                .photoUrl(incident.getPhotoUrl())
                .overdue(incident.isOverdue())
                .escalated(incident.isEscalated())
                .reviewNotes(incident.getReviewNotes())
                .reporterId(incident.getReporter() != null ? incident.getReporter().getId() : null)
                .reporterName(incident.getReporter() != null ? incident.getReporter().getFullName() : null)
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .resolutionNotes(incident.getResolutionNotes())
                .responsibleAgent(incident.getResponsibleAgent())
                .resolutionTimestamp(incident.getResolutionTimestamp())
                .resolvedAt(incident.getResolvedAt());

        // Build photoUrls list from IncidentPhoto entities
        if (incident.getPhotos() != null && !incident.getPhotos().isEmpty()) {
            List<IncidentPhoto> evidencePhotos = incident.getPhotos().stream()
                    .filter(p -> p.getPhotoType() == PhotoType.EVIDENCE)
                    .collect(Collectors.toList());

            List<String> urls = evidencePhotos.stream()
                    .map(IncidentPhoto::getFilePath)
                    .filter(fp -> fp != null)
                    .map(IncidentResponse::toWebUrl)
                    .collect(Collectors.toList());
            builder.photoUrls(urls);

            // Flag if any evidence photo was detected as blurry
            boolean anyBlurry = evidencePhotos.stream()
                    .anyMatch(p -> Boolean.TRUE.equals(p.getBlurry()));
            builder.hasBlurryPhotos(anyBlurry);
        } else {
            builder.photoUrls(Collections.emptyList());
            builder.hasBlurryPhotos(false);
        }

        // Convert afterPhotoUrl from file path to web URL
        if (incident.getAfterPhotoUrl() != null) {
            builder.afterPhotoUrl(toWebUrl(incident.getAfterPhotoUrl()));
        }

        if (incident.getAssignment() != null) {
            var assignment = incident.getAssignment();
            builder.assignedDepartment(assignment.getDepartment() != null
                    ? assignment.getDepartment().getDisplayName() : null);
            builder.assignedOfficerName(assignment.getAssignedOfficer() != null
                    ? assignment.getAssignedOfficer().getFullName() : null);
            builder.assignmentNote(assignment.getNote());
        }

        return builder.build();
    }
}

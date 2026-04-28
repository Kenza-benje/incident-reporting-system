package com.example.ifraneguard.controller;

import com.example.ifraneguard.Model.Incident;
import com.example.ifraneguard.Model.IncidentPhoto;
import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.dto.response.IncidentResponse;
import com.example.ifraneguard.enums.IncidentCategory;
import com.example.ifraneguard.enums.IncidentStatus;
import com.example.ifraneguard.enums.PhotoType;
import com.example.ifraneguard.enums.UrgencyLevel;
import com.example.ifraneguard.repository.IncidentPhotoRepository;
import com.example.ifraneguard.repository.IncidentRepository;
import com.example.ifraneguard.repository.UserRepository;
import com.example.ifraneguard.service.FileStorageService;
import com.example.ifraneguard.service.UrgencyMappingService;
import com.example.ifraneguard.service.NotificationService;
import com.example.ifraneguard.service.DuplicateDetectionService;
import com.example.ifraneguard.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Unified citizen incident REST API — backed by the real PostgreSQL DB.
 *
 * Replaces the old in-memory /api/citizen/incidents endpoint.
 * The report-incident.html JS POSTs here; the authority dashboard reads
 * from the same incidents table via AuthorityPageController/IncidentService.
 *
 * Base URL: /api/citizen/incidents
 */
@Slf4j
@RestController
@RequestMapping("/api/citizen/incidents")
@RequiredArgsConstructor
public class CitizenIncidentController {

    private final IncidentRepository        incidentRepository;
    private final IncidentPhotoRepository   incidentPhotoRepository;
    private final UserRepository            userRepository;
    private final FileStorageService        fileStorageService;
    private final UrgencyMappingService     urgencyMappingService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final NotificationService       notificationService;
    private final AuditService              auditService;

    /**
     * POST /api/citizen/incidents  (multipart/form-data)
     *
     * Accepts the exact same fields as report-incident.html:
     *   category, description, latitude, longitude,
     *   reporterMode, reporterUserId, photos (1-5 files)
     *
     * The reporter is resolved in this order:
     *   1. The authenticated session user (principal) — preferred.
     *   2. reporterUserId param as email lookup.
     *   3. Fall back to the anonymous demo citizen (from DataSeeder).
     *      This keeps the demo running without forcing a login wall on submission.
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> createIncident(
            @RequestParam("category")                       String categoryStr,
            @RequestParam("description")                    String description,
            @RequestParam("latitude")                       Double latitude,
            @RequestParam("longitude")                      Double longitude,
            @RequestParam(value = "reporterMode",   defaultValue = "ANONYMOUS") String reporterMode,
            @RequestParam(value = "reporterUserId", required = false)           String reporterUserId,
            @RequestParam(value = "locationDescription", required = false)      String locationDescription,
            @RequestParam("photos")                         List<MultipartFile> photos,
            @AuthenticationPrincipal User principal
    ) {
        // ── 1. Validate inputs ─────────────────────────────────────────────
        IncidentCategory category;
        try {
            category = resolveCategory(categoryStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Unknown category: " + categoryStr));
        }

        if (description == null || description.trim().length() < 20 || description.trim().length() > 1000) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Description must be between 20 and 1000 characters."));
        }
        if (latitude == null || longitude == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Location is required."));
        }
        if (photos == null || photos.isEmpty() || photos.size() > 5) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Upload between 1 and 5 photos."));
        }

        // ── 2. Resolve reporter ────────────────────────────────────────────
        User reporter = resolveReporter(principal, reporterUserId, reporterMode);
        if (reporter == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Could not identify reporter. Please log in or provide a valid user ID."));
        }

        // ── 3. Duplicate detection ─────────────────────────────────────────
        boolean isDuplicate = duplicateDetectionService.isDuplicate(latitude, longitude, category, null);
        if (isDuplicate) {
            log.warn("Possible duplicate incident near ({}, {}) by {}", latitude, longitude, reporter.getEmail());
        }

        // ── 4. Build and persist incident ──────────────────────────────────
        UrgencyLevel urgency = urgencyMappingService.determineUrgency(category);

        // Auto-generate a meaningful title from category + location
        String title = category.getDisplayName() + " reported near "
                + (locationDescription != null && !locationDescription.isBlank()
                    ? locationDescription : String.format("%.4f, %.4f", latitude, longitude));
        if (title.length() > 150) title = title.substring(0, 147) + "...";

        Incident incident = Incident.builder()
                .title(title)
                .description(description.trim())
                .category(category)
                .status(IncidentStatus.SUBMITTED)
                .urgencyLevel(urgency)
                .latitude(latitude)
                .longitude(longitude)
                .locationDescription(locationDescription)
                .reporter(reporter)
                .build();

        incident = incidentRepository.save(incident);
        log.info("Incident #{} saved to DB (category={}, urgency={})", incident.getId(), category, urgency);

        // ── 5. Save each photo to disk + IncidentPhoto rows ───────────────
        final Incident savedIncident = incident;
        List<String> photoUrls = new ArrayList<>();
        String firstPhotoUrl = null;

        for (MultipartFile photo : photos) {
            try {
                IncidentPhoto stored = fileStorageService.storeIncidentPhoto(
                        photo, savedIncident, reporter, PhotoType.EVIDENCE);
                String webUrl = "/uploads/incidents/" + stored.getStoredFileName();
                photoUrls.add(webUrl);
                if (firstPhotoUrl == null) firstPhotoUrl = webUrl;
                log.debug("Stored photo {} for incident #{}", stored.getStoredFileName(), savedIncident.getId());
            } catch (Exception e) {
                log.warn("Photo upload failed for incident #{}: {}", savedIncident.getId(), e.getMessage());
                // Don't fail the whole submission for a single bad photo
            }
        }

        // Store the first photo URL on the incident for backwards-compatible display
        if (firstPhotoUrl != null) {
            incident.setPhotoUrl(firstPhotoUrl);
            incident = incidentRepository.save(incident);
        }

        // ── 6. Notifications & audit ───────────────────────────────────────
        auditService.logIncidentSubmitted(reporter, incident.getId(), incident.getTitle());

        String confirmationMsg = isDuplicate
                ? "Your report was submitted. Note: a similar report exists nearby. Reference #" + incident.getId()
                : "Your incident report has been submitted successfully. Reference #" + incident.getId();
        notificationService.sendSuccess(reporter, incident, confirmationMsg);

        // ── 7. Return response (matches what report-incident.js expects) ───
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("incidentId", "INC-" + incident.getId());   // string ID format for success page
        response.put("dbId",       incident.getId());             // numeric DB id
        response.put("status",     incident.getStatus().getDisplayName());
        response.put("urgency",    urgency.getDisplayName());
        response.put("photoCount", photoUrls.size());
        if (isDuplicate) {
            response.put("warning", "A similar report already exists nearby.");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/citizen/incidents/{incidentId}
     * incidentId is "INC-{dbId}" format (from report-incident.js success redirect).
     */
    @GetMapping("/{incidentId}")
    public ResponseEntity<?> getOne(@PathVariable String incidentId) {
        Long dbId = parseIncidentId(incidentId);
        if (dbId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid incident ID format: " + incidentId));
        }
        return incidentRepository.findByIdWithPhotos(dbId)
                .map(i -> ResponseEntity.ok(IncidentResponse.from(i)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/citizen/incidents?userId=email@...
     * Returns all incidents for a given reporter email.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getForUser(
            @RequestParam(value = "userId", required = false) String userId,
            @AuthenticationPrincipal User principal
    ) {
        User reporter = null;
        if (principal != null) {
            reporter = principal;
        } else if (userId != null && !userId.isBlank()) {
            reporter = userRepository.findByEmail(userId).orElse(null);
        }

        if (reporter == null) {
            return ResponseEntity.ok(List.of());
        }

        User finalReporter = reporter;
        List<Map<String, Object>> result = incidentRepository
                .findByReporterOrderByCreatedAtDesc(finalReporter, PageRequest.of(0, 50))
                .getContent().stream()
                .map(i -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("incidentId",  "INC-" + i.getId());
                    m.put("dbId",        i.getId());
                    m.put("category",    Map.of("label", i.getCategory().getDisplayName(),
                                                 "name",  i.getCategory().name()));
                    m.put("status",      Map.of("label", i.getStatus().getDisplayName(),
                                                 "name",  i.getStatus().name()));
                    m.put("submittedAt", i.getCreatedAt());
                    m.put("latitude",    i.getLatitude());
                    m.put("longitude",   i.getLongitude());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Maps the citizen-side category string to the real IncidentCategory enum.

     * the real enum is com.example.ifraneguard.enums.IncidentCategory.
     * We handle both naming conventions here.
     */
    private IncidentCategory resolveCategory(String raw) {
        if (raw == null) throw new IllegalArgumentException("null category");
        // Direct match first (e.g. WILDFIRE, ACCIDENT, ILLEGAL_LOGGING)
        try {
            return IncidentCategory.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ignored) { /* try aliases */ }

        // Aliases for citizen-side enum names
        return switch (raw.toUpperCase()) {
            case "SNOW_COVERED_ROAD_BLOCKAGE", "SNOW_ROAD_BLOCK" -> IncidentCategory.SNOW_ROAD_BLOCK;
            case "WATER_INFRASTRUCTURE_PROBLEM", "WATER_ISSUE"   -> IncidentCategory.WATER_ISSUE;
            default -> throw new IllegalArgumentException("Unknown: " + raw);
        };
    }

    /**
     * Resolves the reporter User in priority order:
     *   1. Authenticated session user
     *   2. Email lookup from reporterUserId
     *   3. Anonymous fallback (demo citizen from DataSeeder)
     */
    private User resolveReporter(User principal, String reporterUserId, String reporterMode) {
        if (principal != null) return principal;

        if (reporterUserId != null && !reporterUserId.isBlank()) {
            Optional<User> byEmail = userRepository.findByEmail(reporterUserId.trim());
            if (byEmail.isPresent()) return byEmail.get();
        }

        // Anonymous fallback — use the demo citizen seeded by DataSeeder
        // In production, replace this with a dedicated anonymous_citizen account or require login.
        return userRepository.findByEmail("citizen@test.com")
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getRole().name().equals("CITIZEN"))
                        .findFirst()
                        .orElse(null));
    }

    /** Parses "INC-12345" → 12345L, or plain "12345" → 12345L */
    private Long parseIncidentId(String id) {
        if (id == null) return null;
        try {
            String digits = id.startsWith("INC-") ? id.substring(4) : id;
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

package com.example.ifraneguard.controller;

import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.dto.response.IncidentResponse;
import com.example.ifraneguard.enums.IncidentCategory;
import com.example.ifraneguard.repository.IncidentRepository;
import com.example.ifraneguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Thymeleaf page controller for citizen-facing views.
 * All data comes from the real PostgreSQL DB via IncidentService / IncidentRepository.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class CitizenPageController {

    private final IncidentRepository incidentRepository;
    private final UserRepository     userRepository;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/citizen/report")
    public String reportIncident(Model model) {
        // Pass real IncidentCategory enum values to the category dropdown
        model.addAttribute("categories", IncidentCategory.values());
        return "report-incident";
    }

    /**
     * Submission success page.
     * incidentId is "INC-{dbId}" string as returned by CitizenIncidentController.
     */
    @GetMapping("/citizen/submission-success")
    public String submissionSuccess(
            @RequestParam("incidentId") String incidentId,
            Model model
    ) {
        model.addAttribute("incidentId", incidentId);

        // Try to load the real incident for a richer success page
        try {
            Long dbId = parseDbId(incidentId);
            if (dbId != null) {
                incidentRepository.findByIdWithPhotos(dbId)
                        .map(IncidentResponse::from)
                        .ifPresent(ir -> model.addAttribute("incident", ir));
            }
        } catch (Exception e) {
            log.debug("Could not load incident {} for success page: {}", incidentId, e.getMessage());
        }

        return "submission-success";
    }

    /**
     * My Reports page — loads from the real DB.
     * Authenticated users automatically see their own incidents.
     * Anonymous users can provide a userId (their email) to look up.
     */
    @GetMapping("/citizen/my-reports")
    public String myReports(
            @RequestParam(value = "userId", required = false) String userId,
            @AuthenticationPrincipal User principal,
            Model model
    ) {
        User reporter = null;

        if (principal != null) {
            reporter = principal;
            model.addAttribute("userId", principal.getEmail());
            model.addAttribute("authenticatedUser", principal.getFullName());
        } else if (userId != null && !userId.isBlank()) {
            reporter = userRepository.findByEmail(userId.trim()).orElse(null);
            model.addAttribute("userId", userId);
        } else {
            model.addAttribute("userId", "");
        }

        List<IncidentResponse> reports = List.of();
        if (reporter != null) {
            reports = incidentRepository
                    .findByReporterOrderByCreatedAtDesc(reporter, PageRequest.of(0, 50))
                    .getContent().stream()
                    .map(IncidentResponse::from)
                    .toList();
        }

        model.addAttribute("reports", reports);
        return "my-reports";
    }

    /**
     * Citizen incident detail view — loads from the real DB.
     * Accepts "INC-{dbId}" format (from success page / my-reports links).
     */
    @GetMapping("/citizen/incidents/{incidentId}")
    public String incidentDetails(
            @PathVariable String incidentId,
            Model model
    ) {
        Long dbId = parseDbId(incidentId);
        if (dbId == null) {
            model.addAttribute("error", "Invalid incident ID: " + incidentId);
            return "citizen-incident-detail";
        }

        incidentRepository.findByIdWithPhotos(dbId)
                .map(IncidentResponse::from)
                .ifPresentOrElse(
                        ir -> model.addAttribute("incident", ir),
                        () -> model.addAttribute("error", "Incident not found: " + incidentId)
                );

        return "citizen-incident-detail";
    }

    /** Parses "INC-42" → 42L, or "42" → 42L */
    private Long parseDbId(String id) {
        if (id == null) return null;
        try {
            String digits = id.startsWith("INC-") ? id.substring(4) : id;
            return Long.parseLong(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

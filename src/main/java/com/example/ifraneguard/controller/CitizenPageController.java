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
import org.springframework.security.core.Authentication;

import java.util.List;

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


    @GetMapping("/citizen/my-reports")
    public String myReports(Authentication authentication, Model model) {

        List<IncidentResponse> reports = List.of();

        try {
            boolean loggedIn =
                    authentication != null
                            && authentication.isAuthenticated()
                            && !"anonymousUser".equals(authentication.getName());

            if (loggedIn) {
                String email = authentication.getName();

                User reporter = userRepository.findByEmail(email).orElse(null);

                if (reporter != null) {
                    reports = incidentRepository
                            .findByReporterOrderByCreatedAtDesc(reporter, PageRequest.of(0, 50))
                            .getContent()
                            .stream()
                            .map(IncidentResponse::from)
                            .toList();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAttribute("reports", reports);
        model.addAttribute("incidents", reports);

        return "my-reports";
    }

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

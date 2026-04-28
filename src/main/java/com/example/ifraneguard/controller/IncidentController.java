package com.example.ifraneguard.controller;

import com.example.ifraneguard.dto.request.IncidentSubmitRequest;
import com.example.ifraneguard.dto.response.ApiResponse;
import com.example.ifraneguard.dto.response.IncidentResponse;
import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Handles citizen-facing incident submission and tracking endpoints.
 *
 * Base URL: /api/incidents
 */
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    /**
     * POST /api/incidents
     * Citizen submits a new incident report.
     *
     * @Valid triggers Spring's bean validation (NotBlank, NotNull, etc.)
     * @AuthenticationPrincipal injects the currently logged-in user (from JWT/session)
     */
    @PostMapping
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<IncidentResponse>> submitIncident(
            @Valid @RequestBody IncidentSubmitRequest request,
            @AuthenticationPrincipal User currentUser) {

        IncidentResponse response = incidentService.submitIncident(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Incident submitted successfully", response));
    }

    /**
     * GET /api/incidents/{id}
     * Public endpoint — no authentication required.
     * SecurityConfig has .requestMatchers(HttpMethod.GET, "/api/incidents/{id}").permitAll()
     *
     * No @PreAuthorize here — adding one would override the permitAll rule in SecurityConfig.
     * @EnableMethodSecurity gives method-level annotations precedence over URL-level rules.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Incident found", incidentService.getById(id)));
    }

    /**
     * GET /api/incidents/my
     * Citizen views their own submitted reports.
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<ApiResponse<Page<IncidentResponse>>> getMyIncidents(
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

        Page<IncidentResponse> page = incidentService.getMyIncidents(currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success("Your incidents", page));
    }
}
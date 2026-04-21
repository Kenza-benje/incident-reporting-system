package com.example.demo.controller;

import com.example.demo.dto.CitizenIncidentCreateRequest;
import com.example.demo.dto.CitizenIncidentResponse;
import com.example.demo.model.IncidentCategory;
import com.example.demo.model.ReporterMode;
import com.example.demo.service.CitizenIncidentFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citizen/incidents")
public class CitizenIncidentApiController {

    private final CitizenIncidentFacade incidentService;

    public CitizenIncidentApiController(CitizenIncidentFacade incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, String>> createIncident(
            @RequestParam("category") IncidentCategory category,
            @RequestParam("description") String description,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam(value = "reporterMode", defaultValue = "ANONYMOUS") ReporterMode reporterMode,
            @RequestParam(value = "reporterUserId", required = false) String reporterUserId,
            @RequestParam("photos") List<MultipartFile> photos
    ) {
        CitizenIncidentResponse incident = incidentService.createIncident(new CitizenIncidentCreateRequest(
                category,
                description,
                latitude,
                longitude,
                reporterMode,
                reporterUserId,
                photos
        ));

        return ResponseEntity.ok(Map.of(
                "incidentId", incident.incidentId(),
                "status", incident.status().getLabel()
        ));
    }

    @GetMapping("/{incidentId}")
    public CitizenIncidentResponse getOne(@PathVariable String incidentId) {
        return incidentService.getById(incidentId);
    }

    @GetMapping
    public List<CitizenIncidentResponse> getForUser(@RequestParam(value = "userId", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            return incidentService.getAllIncidents();
        }
        return incidentService.getReportsForUser(userId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleValidationError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}

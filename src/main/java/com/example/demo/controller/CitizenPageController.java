package com.example.demo.controller;

import com.example.demo.dto.CitizenIncidentResponse;
import com.example.demo.model.IncidentCategory;
import com.example.demo.service.CitizenIncidentFacade;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CitizenPageController {

    private final CitizenIncidentFacade incidentService;

    public CitizenPageController(CitizenIncidentFacade incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/citizen/report")
    public String reportIncident(Model model) {
        model.addAttribute("categories", IncidentCategory.values());
        return "report-incident";
    }

    @GetMapping("/citizen/submission-success")
    public String submissionSuccess(@RequestParam("incidentId") String incidentId, Model model) {
        model.addAttribute("incidentId", incidentId);
        return "submission-success";
    }

    @GetMapping("/citizen/my-reports")
    public String myReports(@RequestParam(value = "userId", required = false) String userId, Model model) {
        List<CitizenIncidentResponse> reports = incidentService.getReportsForUser(userId);
        model.addAttribute("reports", reports);
        model.addAttribute("userId", userId == null ? "" : userId);
        return "my-reports";
    }

    @GetMapping("/citizen/incidents/{incidentId}")
    public String incidentDetails(@PathVariable String incidentId, Model model) {
        CitizenIncidentResponse incident = incidentService.getById(incidentId);
        model.addAttribute("incident", incident);
        return "citizen-incident-detail";
    }
}

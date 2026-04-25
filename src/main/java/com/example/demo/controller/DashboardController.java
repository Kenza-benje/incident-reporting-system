package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DashboardController {

    @GetMapping("/authority/dashboard")
    public String authorityDashboard() {
        return "authority-dashboard";
    }

    @GetMapping("/authority/incidents")
    public String incidentsList() {
        return "incidents-list";
    }

    @GetMapping("/authority/incidents/details")
    public String incidentDetails() {
        return "incident-details";
    }

    @GetMapping("/authority/incidents/review")
    public String reviewIncident(Model model) {
        model.addAttribute("incident", new IncidentViewModel(
                "INC-2026-00427",
                "Infrastructure Damage",
                "UNDER_REVIEW",
                "High",
                "North Zone - Sector 12",
                "2026-04-22 09:34 AM",
                "mehdi_user",
                "A sinkhole has formed near the main intersection and is expanding after heavy rain. Nearby residents report traffic risk and water leakage from underground lines.",
                "2026-04-22 09:34 AM",
                "Website",
                false
        ));
        return "review-incident";
    }

    @PostMapping("/authority/incidents/{id}/review")
    public String submitReviewDecision(
            @PathVariable String id,
            @RequestParam(required = false) String reviewNotes,
            @RequestParam(required = false) String rejectionReason,
            @RequestParam String decision,
            RedirectAttributes redirectAttributes
    ) {
        try {
            System.out.println("Incident ID: " + id);
            System.out.println("Decision: " + decision);
            System.out.println("Review Notes: " + reviewNotes);
            System.out.println("Rejection Reason: " + rejectionReason);

            if ("verify".equals(decision)) {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "The incident report has been verified successfully."
                );
            } else if ("reject".equals(decision)) {
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "The incident report has been rejected successfully."
                );
            } else {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "The requested review action is invalid."
                );
            }

        } catch (Exception e) {
            if ("verify".equals(decision)) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "The incident report could not be verified."
                );
            } else if ("reject".equals(decision)) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "The incident report could not be rejected."
                );
            } else {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "The requested review action failed."
                );
            }
        }

        return "redirect:/authority/dashboard";
    }

    @GetMapping("/authority/incidents/assign")
    public String assignIncident(Model model) {
        model.addAttribute("incident", new IncidentViewModel(
                "INC-2026-00419",
                "ACCIDENT",
                "ASSIGNED",
                "High",
                "Zone B - North Avenue",
                "2026-04-23 09:35 AM",
                "mehdi_user",
                "A damaged traffic signal and debris were observed at the northbound intersection, causing heavy congestion and an elevated risk of collisions during rush hour.",
                "2026-04-23 10:12 AM",
                "Website",
                false
        ));
        return "assign-incident";
    }

    @PostMapping("/authority/incidents/{id}/assign")
    public String submitAssignment(
            @PathVariable String id,
            @RequestParam String departmentId,
            @RequestParam String officerId,
            @RequestParam(required = false) String assignmentNote,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes
    ) {
        try {
            // later here you will save the assignment in DB

            System.out.println("Incident ID: " + id);
            System.out.println("Department ID: " + departmentId);
            System.out.println("Officer ID: " + officerId);
            System.out.println("Assignment Note: " + assignmentNote);

            redirectAttributes.addFlashAttribute("successMessage",
                    "The incident report has been assigned successfully.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "The incident report failed to be assigned.");
        }

        return "redirect:/authority/dashboard";
    }

    @GetMapping("/authority/incidents/resolve")
    public String resolutionForm() {
        return "resolution-form";
    }

    @GetMapping("/authority/escalations")
    public String escalationList() {
        return "escalation-list";
    }

    public static class IncidentViewModel {
        private final String id;
        private final String category;
        private final String status;
        private final String urgency;
        private final String location;
        private final String submittedAt;
        private final String reportedBy;
        private final String description;
        private final String updatedAt;
        private final String source;
        private final boolean possibleDuplicate;

        public IncidentViewModel(String id,
                                 String category,
                                 String status,
                                 String urgency,
                                 String location,
                                 String submittedAt,
                                 String reportedBy,
                                 String description,
                                 String updatedAt,
                                 String source,
                                 boolean possibleDuplicate) {
            this.id = id;
            this.category = category;
            this.status = status;
            this.urgency = urgency;
            this.location = location;
            this.submittedAt = submittedAt;
            this.reportedBy = reportedBy;
            this.description = description;
            this.updatedAt = updatedAt;
            this.source = source;
            this.possibleDuplicate = possibleDuplicate;
        }

        public String getId() {
            return id;
        }

        public String getCategory() {
            return category;
        }

        public String getStatus() {
            return status;
        }

        public String getUrgency() {
            return urgency;
        }

        public String getLocation() {
            return location;
        }

        public String getSubmittedAt() {
            return submittedAt;
        }

        public String getReportedBy() {
            return reportedBy;
        }

        public String getDescription() {
            return description;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public String getSource() {
            return source;
        }

        public boolean getPossibleDuplicate() {
            return possibleDuplicate;
        }
    }

    @PostMapping("/authority/incidents/mark-in-progress")
    public String markIncidentInProgress(RedirectAttributes redirectAttributes) {
        try {
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "The incident report has been marked as In Progress successfully."
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "The incident report status failed to change to In Progress."
            );
        }

        return "redirect:/authority/incidents/details";
    }

}
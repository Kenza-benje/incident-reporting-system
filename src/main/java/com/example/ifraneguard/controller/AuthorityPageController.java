package com.example.ifraneguard.controller;

import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.dto.request.AssignIncidentRequest;
import com.example.ifraneguard.dto.response.IncidentResponse;
import com.example.ifraneguard.enums.*;
import com.example.ifraneguard.repository.UserRepository;
import com.example.ifraneguard.service.IncidentService;
import com.example.ifraneguard.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import com.example.ifraneguard.Mapper.StatusHistoryMapper;
import com.example.ifraneguard.dto.response.StatusHistoryResponse;
import com.example.ifraneguard.repository.StatusHistoryRepository;
import com.example.ifraneguard.enums.Role;


import java.time.LocalDateTime;
import java.util.List;


@Controller
@RequestMapping("/authority")
@RequiredArgsConstructor
public class AuthorityPageController {

    private final IncidentService incidentService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final StatusHistoryMapper statusHistoryMapper;

    private User getAuthority(User principal) {
        if (principal != null) {
            return principal;
        }

        return userRepository.findByEmail("admin@test.com")
                .orElseThrow(() -> new RuntimeException("No authority/admin user found. Start with demo profile or create an admin user."));
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal User principal) {
        User authority = getAuthority(principal);

        model.addAttribute("stats", incidentService.getDashboardStats());
        model.addAttribute("recentIncidents",
                incidentService.getAllForDashboard(PageRequest.of(0, 6)).getContent());
        model.addAttribute("notifications",
                notificationService.getUnreadNotifications(authority));

        return "authority-dashboard";
    }

    @GetMapping("/incidents")
    public String incidentsList(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) IncidentCategory category,
            @RequestParam(required = false) UrgencyLevel urgency,
            @RequestParam(required = false) Boolean overdue,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Page<IncidentResponse> incidents = incidentService.searchForAuthority(
                status,
                category,
                urgency,
                overdue,
                date,
                search,
                PageRequest.of(page, 20)
        );

        model.addAttribute("incidents", incidents);

        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedUrgency", urgency);
        model.addAttribute("selectedOverdue", overdue);
        model.addAttribute("selectedDate", date);
        model.addAttribute("search", search);

        model.addAttribute("statuses", IncidentStatus.values());
        model.addAttribute("categories", IncidentCategory.values());
        model.addAttribute("urgencies", UrgencyLevel.values());

        return "incidents-list";
    }

    @GetMapping("/incidents/{id}")
    public String incidentDetails(@PathVariable Long id, Model model) {
        IncidentResponse incident = incidentService.getById(id);

        List<StatusHistoryResponse> statusHistory =
                statusHistoryRepository.findByIncidentIdOrderByChangedAtAsc(id)
                        .stream()
                        .map(statusHistoryMapper::toResponse)
                        .toList();

        model.addAttribute("incident", incident);
        model.addAttribute("statusHistory", statusHistory);

        return "incident-details";
    }
    @GetMapping("/incidents/{id}/review")
    public String reviewIncident(@PathVariable Long id, Model model) {
        model.addAttribute("incident", incidentService.getById(id));
        return "review-incident";
    }

    @PostMapping("/incidents/{id}/review")
    public String submitReviewDecision(
            @PathVariable Long id,
            @RequestParam String decision,
            @RequestParam(required = false) String reviewNotes,
            @RequestParam(required = false) String rejectionReason,
            @AuthenticationPrincipal User principal,
            RedirectAttributes redirectAttributes
    ) {
        User authority = getAuthority(principal);

        try {
            if ("verify".equalsIgnoreCase(decision)) {
                incidentService.openForReview(id, authority, reviewNotes);
                redirectAttributes.addFlashAttribute("successMessage",
                        "The incident report has been reviewed successfully.");
            } else if ("reject".equalsIgnoreCase(decision)) {
                incidentService.rejectIncident(
                        id,
                        rejectionReason != null ? rejectionReason : "Report rejected",
                        authority,
                        reviewNotes
                );
                redirectAttributes.addFlashAttribute("successMessage",
                        "The incident report has been rejected successfully.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Invalid review decision.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/authority/incidents/" + id;
    }

    @GetMapping("/incidents/{id}/assign")
    public String assignIncident(@PathVariable Long id, Model model) {
        List<User> authorityOfficers =
                userRepository.findByRoleAndEnabledTrue(Role.AUTHORITY);

        model.addAttribute("incident", incidentService.getById(id));
        model.addAttribute("departments", Department.values());
        model.addAttribute("officers", authorityOfficers);
        return "assign-incident";
    }



    @PostMapping("/incidents/{id}/assign")
    public String submitAssignment(
            @PathVariable Long id,
            @RequestParam Department department,
            @RequestParam(required = false) Long officerId,
            @RequestParam(required = false) String note,
            @AuthenticationPrincipal User principal,
            RedirectAttributes redirectAttributes
    ) {
        User authority = getAuthority(principal);

        try {
            AssignIncidentRequest request = new AssignIncidentRequest();
            request.setDepartment(department);
            request.setOfficerId(officerId);
            request.setNote(note);

            incidentService.assignIncident(id, request, authority);

            redirectAttributes.addFlashAttribute("successMessage",
                    "The incident report has been assigned successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "The incident report failed to be assigned. " + e.getMessage());
        }

        return "redirect:/authority/incidents/" + id;
    }

    @PostMapping("/incidents/{id}/start")
    public String markInProgress(
            @PathVariable Long id,
            @AuthenticationPrincipal User principal,
            RedirectAttributes redirectAttributes
    ) {
        User authority = getAuthority(principal);

        try {
            incidentService.startProgress(id, authority);
            redirectAttributes.addFlashAttribute("successMessage",
                    "The incident report has been marked as In Progress successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/authority/incidents/" + id;
    }

    @GetMapping("/incidents/{id}/resolve")
    public String resolutionForm(@PathVariable Long id, Model model) {
        model.addAttribute("incident", incidentService.getById(id));
        return "resolution-form";
    }

    @PostMapping("/incidents/{id}/resolve")
    public String submitResolution(
            @PathVariable Long id,
            @RequestParam String resolutionNotes,
            @RequestParam MultipartFile afterPhoto,
            @RequestParam String responsibleAgent,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime resolutionTimestamp,
            @AuthenticationPrincipal User principal,
            RedirectAttributes redirectAttributes
    ) {
        User authority = getAuthority(principal);

        try {
            incidentService.resolveIncident(
                    id,
                    resolutionNotes,
                    afterPhoto,
                    responsibleAgent,
                    resolutionTimestamp,
                    authority
            );

            redirectAttributes.addFlashAttribute("successMessage",
                    "The incident has been marked as resolved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/authority/incidents/" + id;
    }

    @GetMapping("/escalations")
    public String escalationList(Model model) {
        List<IncidentResponse> incidents =
                incidentService.getAllForDashboard(PageRequest.of(0, 50)).getContent()
                        .stream()
                        .filter(i -> i.isEscalated() || i.isOverdue())
                        .toList();

        model.addAttribute("incidents", incidents);
        model.addAttribute("stats", incidentService.getDashboardStats());

        return "escalation-list";
    }
}
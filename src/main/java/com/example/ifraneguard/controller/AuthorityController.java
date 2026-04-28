package com.example.ifraneguard.controller;

import com.example.ifraneguard.dto.request.AssignIncidentRequest;
import com.example.ifraneguard.dto.response.ApiResponse;
import com.example.ifraneguard.dto.response.DashboardStatsResponse;
import com.example.ifraneguard.dto.response.IncidentResponse;
import com.example.ifraneguard.dto.response.NotificationResponse;
import com.example.ifraneguard.dto.response.UserResponse;
import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.enums.Department;
import com.example.ifraneguard.enums.IncidentStatus;
import com.example.ifraneguard.exceptions.InvalidWorkflowTransitionException;
import com.example.ifraneguard.service.IncidentService;
import com.example.ifraneguard.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Authority-only endpoints.
 *
 * All routes require AUTHORITY or ADMIN role.
 * Base URL: /api/authority
 */
@Slf4j
@RestController
@RequestMapping("/api/authority")
@PreAuthorize("hasAnyRole('AUTHORITY', 'ADMIN')")
@RequiredArgsConstructor
public class AuthorityController {

    private final IncidentService    incidentService;
    private final NotificationService notificationService;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    /**
     * GET /api/authority/dashboard/stats
     * Returns counts for each status — populates the stat cards.
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard statistics", incidentService.getDashboardStats()));
    }

    /**
     * GET /api/authority/incidents
     * Paginated list of all incidents, optionally filtered by status.
     */
    @GetMapping("/incidents")
    public ResponseEntity<ApiResponse<Page<IncidentResponse>>> getAllIncidents(
            @RequestParam(required = false) IncidentStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<IncidentResponse> page = (status != null)
                ? incidentService.getByStatus(status, pageable)
                : incidentService.getAllForDashboard(pageable);

        return ResponseEntity.ok(ApiResponse.success("Incidents retrieved", page));
    }

    // ── Workflow actions ──────────────────────────────────────────────────────

    /**
     * POST /api/authority/incidents/{id}/review
     * Opens an incident for review: SUBMITTED → UNDER_REVIEW
     */
    @PostMapping("/incidents/{id}/review")
    public ResponseEntity<ApiResponse<IncidentResponse>> openForReview(
            @PathVariable Long id,
            @AuthenticationPrincipal User authority) {

        IncidentResponse response = incidentService.openForReview(id, authority);
        return ResponseEntity.ok(ApiResponse.success("Incident is now under review", response));
    }

    /**
     * POST /api/authority/incidents/{id}/assign
     * Assigns incident to a department/officer: UNDER_REVIEW → ASSIGNED
     *
     * As per teammate's requirement:
     *  - Success → 200 with message "The incident report has been assigned successfully."
     *  - Failure → 400/422 with message "The incident report failed to be assigned."
     */
    @PostMapping("/incidents/{id}/assign")
    public ResponseEntity<ApiResponse<IncidentResponse>> assignIncident(
            @PathVariable Long id,
            @Valid @RequestBody AssignIncidentRequest request,
            @AuthenticationPrincipal User authority) {

        try {
            IncidentResponse response = incidentService.assignIncident(id, request, authority);
            return ResponseEntity.ok(
                    ApiResponse.success("The incident report has been assigned successfully.", response));

        } catch (InvalidWorkflowTransitionException e) {
            // The assignment failed (wrong status) — notify and return error message
            notificationService.sendError(authority, null,
                    "The incident report failed to be assigned. Reason: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("The incident report failed to be assigned."));
        }
    }

    /**
     * POST /api/authority/incidents/{id}/reject
     * Rejects an incident: UNDER_REVIEW → REJECTED
     */
    @PostMapping("/incidents/{id}/reject")
    public ResponseEntity<ApiResponse<IncidentResponse>> rejectIncident(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Report deemed invalid or duplicate") String reason,
            @AuthenticationPrincipal User authority) {

        IncidentResponse response = incidentService.rejectIncident(id, reason, authority);
        return ResponseEntity.ok(ApiResponse.success("Incident rejected", response));
    }

    /**
     * POST /api/authority/incidents/{id}/start
     * Marks work as started: ASSIGNED → IN_PROGRESS
     */
    @PostMapping("/incidents/{id}/start")
    public ResponseEntity<ApiResponse<IncidentResponse>> startProgress(
            @PathVariable Long id,
            @AuthenticationPrincipal User authority) {

        IncidentResponse response = incidentService.startProgress(id, authority);
        return ResponseEntity.ok(ApiResponse.success("Incident marked as IN_PROGRESS", response));
    }

    /**
     * POST /api/authority/incidents/{id}/resolve
     * Resolves an incident: IN_PROGRESS → RESOLVED
     */
    @PostMapping("/incidents/{id}/resolve")
    public ResponseEntity<ApiResponse<IncidentResponse>> resolveIncident(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Issue resolved by field team") String note,
            @AuthenticationPrincipal User authority) {

        IncidentResponse response = incidentService.resolveIncident(id, note, authority);
        return ResponseEntity.ok(ApiResponse.success("Incident resolved successfully", response));
    }

    // ── Officers dropdown (for assignment form) ───────────────────────────────

    /**
     * GET /api/authority/departments/{department}/officers
     * Returns the list of active officers in a given department.
     * Used to populate the officer dropdown in the assign form.
     */
    @GetMapping("/departments/{department}/officers")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getOfficersByDepartment(
            @PathVariable Department department) {

        List<UserResponse> officers = incidentService.getOfficersByDepartment(department);
        return ResponseEntity.ok(ApiResponse.success("Officers in " + department.getDisplayName(), officers));
    }

    // ── Notifications ─────────────────────────────────────────────────────────

    /**
     * GET /api/authority/notifications
     * Returns unread notifications for the dashboard notification area.
     */
    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal User authority) {
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(authority);
        return ResponseEntity.ok(ApiResponse.success("Notifications", notifications));
    }

    /**
     * POST /api/authority/notifications/read
     * Marks all notifications as read when user opens the panel.
     */
    @PostMapping("/notifications/read")
    public ResponseEntity<ApiResponse<Void>> markNotificationsRead(
            @AuthenticationPrincipal User authority) {
        notificationService.markAllAsRead(authority);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }
}

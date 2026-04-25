package com.example.ifraneguard.service;

import com.example.ifraneguard.dto.request.AssignIncidentRequest;
import com.example.ifraneguard.dto.request.IncidentSubmitRequest;
import com.example.ifraneguard.dto.response.DashboardStatsResponse;
import com.example.ifraneguard.dto.response.IncidentResponse;
import com.example.ifraneguard.Model.Assignment;
import com.example.ifraneguard.Model.Incident;
import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.dto.response.UserResponse;
import com.example.ifraneguard.enums.Department;
import com.example.ifraneguard.enums.IncidentStatus;
import com.example.ifraneguard.enums.Role;
import com.example.ifraneguard.enums.UrgencyLevel;
import com.example.ifraneguard.exceptions.IncidentNotFoundException;
import com.example.ifraneguard.exceptions.InvalidWorkflowTransitionException;
import com.example.ifraneguard.exceptions.UserNotFoundException;
import com.example.ifraneguard.repository.AssignmentRepository;
import com.example.ifraneguard.repository.IncidentRepository;
import com.example.ifraneguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncidentService {

    private final IncidentRepository       incidentRepository;
    private final UserRepository           userRepository;
    private final AssignmentRepository     assignmentRepository;
    private final UrgencyMappingService    urgencyMappingService;
    private final DuplicateDetectionService duplicateDetectionService;
    private final WorkflowService          workflowService;
    private final NotificationService      notificationService;

    // ✅ ADDED
    private final AuditService auditService;

    // ── CITIZEN OPERATIONS ────────────────────────────────────────────────────

    @Transactional
    public IncidentResponse submitIncident(IncidentSubmitRequest request, User reporter) {
        log.info("Citizen {} submitting incident: {}", reporter.getEmail(), request.getCategory());

        boolean isDuplicate = duplicateDetectionService.isDuplicate(
                request.getLatitude(), request.getLongitude(),
                request.getCategory(), null
        );

        if (isDuplicate) {
            log.warn("Duplicate incident detected near ({}, {})", request.getLatitude(), request.getLongitude());
        }

        UrgencyLevel urgency = urgencyMappingService.determineUrgency(request.getCategory());

        Incident incident = Incident.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .status(IncidentStatus.SUBMITTED)
                .urgencyLevel(urgency)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .locationDescription(request.getLocationDescription())
                .photoUrl(request.getPhotoUrl())
                .reporter(reporter)
                .build();

        incident = incidentRepository.save(incident);

        // ✅ ADDED
        auditService.logIncidentSubmitted(reporter, incident.getId(), incident.getTitle());

        String confirmationMsg = isDuplicate
                ? "Your report was submitted. Note: a similar report exists nearby."
                : "Your incident report has been submitted successfully. Reference #" + incident.getId();

        notificationService.sendSuccess(reporter, incident, confirmationMsg);

        log.info("Incident #{} created with urgency {}", incident.getId(), urgency);
        return IncidentResponse.from(incident);
    }

    // ── AUTHORITY OPERATIONS ─────────────────────────────────────────────────

    @Transactional
    public IncidentResponse openForReview(Long incidentId, User authority) {
        Incident incident = findIncidentOrThrow(incidentId);
        workflowService.transition(incident, IncidentStatus.UNDER_REVIEW, authority,
                "Opened for review by " + authority.getFullName());
        return IncidentResponse.from(incident);
    }

    @Transactional
    public IncidentResponse assignIncident(Long incidentId, AssignIncidentRequest request, User authority) {
        Incident incident = findIncidentOrThrow(incidentId);

        if (incident.getStatus() != IncidentStatus.UNDER_REVIEW) {
            throw new InvalidWorkflowTransitionException(
                    "Incident must be UNDER_REVIEW before it can be assigned. Current status: "
                            + incident.getStatus().getDisplayName()
            );
        }

        User assignedOfficer = resolveOfficer(request.getOfficerId(), request.getDepartment());

        Assignment assignment = Assignment.builder()
                .incident(incident)
                .department(request.getDepartment())
                .assignedOfficer(assignedOfficer)
                .assignedBy(authority)
                .note(request.getNote())
                .build();

        assignmentRepository.save(assignment);
        incident.setAssignment(assignment);

        // ✅ ADDED
        auditService.logIncidentAssigned(
                authority,
                incidentId,
                request.getDepartment().getDisplayName(),
                assignedOfficer != null ? assignedOfficer.getFullName() : null
        );

        workflowService.transition(incident, IncidentStatus.ASSIGNED, authority,
                "Assigned to " + request.getDepartment().getDisplayName());

        notificationService.sendSuccess(authority, incident,
                "The incident report has been assigned successfully.");

        notificationService.sendInfo(incident.getReporter(), incident,
                "Your incident report #" + incidentId + " has been assigned to " +
                        request.getDepartment().getDisplayName() + " and is being handled.");

        if (assignedOfficer != null) {
            notificationService.sendInfo(assignedOfficer, incident,
                    "You have been assigned to incident #" + incidentId +
                            ": " + incident.getTitle());
        }

        log.info("Incident #{} assigned to dept={} officer={} by={}",
                incidentId, request.getDepartment(), assignedOfficer, authority.getEmail());

        return IncidentResponse.from(incident);
    }

    @Transactional
    public IncidentResponse rejectIncident(Long incidentId, String reason, User authority) {
        Incident incident = findIncidentOrThrow(incidentId);
        workflowService.transition(incident, IncidentStatus.REJECTED, authority, reason);

        // ✅ ADDED
        auditService.logIncidentRejected(authority, incidentId, reason);

        notificationService.sendInfo(incident.getReporter(), incident,
                "Your incident report #" + incidentId + " has been reviewed. " +
                        "Unfortunately, it could not be processed at this time. Reason: " + reason);

        return IncidentResponse.from(incident);
    }

    @Transactional
    public IncidentResponse startProgress(Long incidentId, User officer) {
        Incident incident = findIncidentOrThrow(incidentId);
        workflowService.transition(incident, IncidentStatus.IN_PROGRESS, officer,
                "Field work started by " + officer.getFullName());

        notificationService.sendInfo(incident.getReporter(), incident,
                "Work has started on your incident report #" + incidentId + ".");

        return IncidentResponse.from(incident);
    }

    @Transactional
    public IncidentResponse resolveIncident(Long incidentId, String resolutionNote, User officer) {
        Incident incident = findIncidentOrThrow(incidentId);
        workflowService.transition(incident, IncidentStatus.RESOLVED, officer, resolutionNote);

        // ✅ ADDED
        auditService.logIncidentResolved(officer, incidentId);

        notificationService.sendSuccess(incident.getReporter(), incident,
                "Great news! Your incident report #" + incidentId +
                        " has been resolved. Thank you for helping keep Ifrane safe.");

        return IncidentResponse.from(incident);
    }

    // ── READ OPERATIONS ───────────────────────────────────────────────────────

    public IncidentResponse getById(Long id) {
        return IncidentResponse.from(findIncidentOrThrow(id));
    }

    public Page<IncidentResponse> getAllForDashboard(Pageable pageable) {
        return incidentRepository.findAll(pageable).map(IncidentResponse::from);
    }

    public Page<IncidentResponse> getByStatus(IncidentStatus status, Pageable pageable) {
        return incidentRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(IncidentResponse::from);
    }

    public Page<IncidentResponse> getMyIncidents(User citizen, Pageable pageable) {
        return incidentRepository.findByReporterOrderByCreatedAtDesc(citizen, pageable)
                .map(IncidentResponse::from);
    }

    public DashboardStatsResponse getDashboardStats() {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return DashboardStatsResponse.builder()
                .totalIncidents(incidentRepository.count())
                .submitted(incidentRepository.countByStatus(IncidentStatus.SUBMITTED))
                .underReview(incidentRepository.countByStatus(IncidentStatus.UNDER_REVIEW))
                .assigned(incidentRepository.countByStatus(IncidentStatus.ASSIGNED))
                .inProgress(incidentRepository.countByStatus(IncidentStatus.IN_PROGRESS))
                .resolved(incidentRepository.countByStatus(IncidentStatus.RESOLVED))
                .rejected(incidentRepository.countByStatus(IncidentStatus.REJECTED))
                .resolvedToday(incidentRepository.countByStatusAndCreatedAtAfter(
                        IncidentStatus.RESOLVED, todayStart))
                .submittedToday(incidentRepository.countByStatusAndCreatedAtAfter(
                        IncidentStatus.SUBMITTED, todayStart))
                .highUrgency(incidentRepository.findByUrgencyLevelAndStatusNot(
                        UrgencyLevel.HIGH, IncidentStatus.RESOLVED).size())
                .build();
    }

    public List<UserResponse> getOfficersByDepartment(Department department) {
        return userRepository.findByDepartmentAndRoleAndEnabledTrue(department, Role.AUTHORITY)
                .stream()
                .map(com.example.ifraneguard.dto.response.UserResponse::from)
                .collect(Collectors.toList());
    }

    private Incident findIncidentOrThrow(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException("Incident #" + id + " not found"));
    }

    private User resolveOfficer(Long officerId, Department department) {
        if (officerId != null) {
            return userRepository.findById(officerId)
                    .orElseThrow(() -> new UserNotFoundException("Officer #" + officerId + " not found"));
        }
        List<User> officers = userRepository.findLeastBusyOfficerInDepartment(department);
        return officers.isEmpty() ? null : officers.get(0);
    }
}
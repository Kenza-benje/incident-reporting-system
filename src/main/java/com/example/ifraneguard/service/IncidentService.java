package com.example.ifraneguard.service;

import com.example.ifraneguard.dto.request.AssignIncidentRequest;
import com.example.ifraneguard.dto.request.IncidentSubmitRequest;
import com.example.ifraneguard.dto.response.DashboardStatsResponse;
import com.example.ifraneguard.dto.response.IncidentResponse;
import com.example.ifraneguard.Model.Assignment;
import com.example.ifraneguard.Model.Incident;
import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.dto.response.UserResponse;
import com.example.ifraneguard.enums.*;
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
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import org.springframework.web.multipart.MultipartFile;
import com.example.ifraneguard.enums.PhotoType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    private final FileStorageService       fileStorageService;

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

        auditService.logIncidentSubmitted(reporter, incident.getId(), incident.getTitle());

        String confirmationMsg = isDuplicate
                ? "Your report was submitted. Note: a similar report exists nearby."
                : "Your incident report has been submitted successfully. Reference #" + incident.getId();

        notificationService.sendSuccess(reporter, incident, confirmationMsg);

        log.info("Incident #{} created with urgency {}", incident.getId(), urgency);
        return IncidentResponse.from(incident);

    }

    public Page<IncidentResponse> searchForAuthority(
            IncidentStatus status,
            IncidentCategory category,
            UrgencyLevel urgency,
            Boolean overdue,
            LocalDate date,
            String search,
            Pageable pageable
    ) {
        Specification<Incident> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            if (urgency != null) {
                predicates.add(cb.equal(root.get("urgencyLevel"), urgency));
            }

            if (date != null) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startOfDay));
                predicates.add(cb.lessThan(root.get("createdAt"), endOfDay));
            }

            if (search != null && !search.trim().isEmpty()) {
                String keyword = "%" + search.trim().toLowerCase() + "%";

                var titlePredicate = cb.like(cb.lower(root.get("title")), keyword);
                var descriptionPredicate = cb.like(cb.lower(root.get("description")), keyword);

                if (search.trim().matches("\\d+")) {
                    Long id = Long.parseLong(search.trim());
                    var idPredicate = cb.equal(root.get("id"), id);
                    predicates.add(cb.or(titlePredicate, descriptionPredicate, idPredicate));
                } else {
                    predicates.add(cb.or(titlePredicate, descriptionPredicate));
                }
            }

            if (overdue != null) {
                LocalDateTime now = LocalDateTime.now();

                var notResolved = cb.not(root.get("status").in(
                        IncidentStatus.RESOLVED,
                        IncidentStatus.REJECTED
                ));

                var highOverdue = cb.and(
                        cb.equal(root.get("urgencyLevel"), UrgencyLevel.HIGH),
                        cb.lessThan(root.get("createdAt"),
                                now.minusHours(UrgencyLevel.HIGH.getOverdueThresholdHours()))
                );

                var mediumOverdue = cb.and(
                        cb.equal(root.get("urgencyLevel"), UrgencyLevel.MEDIUM),
                        cb.lessThan(root.get("createdAt"),
                                now.minusHours(UrgencyLevel.MEDIUM.getOverdueThresholdHours()))
                );

                var lowOverdue = cb.and(
                        cb.equal(root.get("urgencyLevel"), UrgencyLevel.LOW),
                        cb.lessThan(root.get("createdAt"),
                                now.minusHours(UrgencyLevel.LOW.getOverdueThresholdHours()))
                );

                var overduePredicate = cb.and(
                        notResolved,
                        cb.or(highOverdue, mediumOverdue, lowOverdue)
                );

                if (overdue) {
                    predicates.add(overduePredicate);
                } else {
                    predicates.add(cb.not(overduePredicate));
                }
            }

            query.orderBy(cb.desc(root.get("createdAt")));

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return incidentRepository.findAll(spec, pageable)
                .map(IncidentResponse::from);
    }
    // ── AUTHORITY OPERATIONS ─────────────────────────────────────────────────

    @Transactional
    public IncidentResponse openForReview(Long incidentId, User authority, String reviewNotes) {
        Incident incident = findIncidentOrThrow(incidentId);

        incident.setReviewNotes(reviewNotes);

        workflowService.transition(
                incident,
                IncidentStatus.UNDER_REVIEW,
                authority,
                reviewNotes != null && !reviewNotes.isBlank()
                        ? reviewNotes
                        : "Opened for review by " + authority.getFullName()
        );

        return IncidentResponse.from(incident);
    }

    @Transactional
    public IncidentResponse openForReview(Long incidentId, User authority) {
        return openForReview(incidentId, authority, null);
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
    public IncidentResponse rejectIncident(
            Long incidentId,
            String reason,
            User authority,
            String reviewNotes
    ) {
        Incident incident = findIncidentOrThrow(incidentId);

        incident.setReviewNotes(reviewNotes);

        workflowService.transition(incident, IncidentStatus.REJECTED, authority, reason);

        auditService.logIncidentRejected(authority, incidentId, reason);

        notificationService.sendInfo(incident.getReporter(), incident,
                "Your incident report #" + incidentId + " has been reviewed. " +
                        "Unfortunately, it could not be processed at this time. Reason: " + reason);

        return IncidentResponse.from(incident);
    }

    @Transactional
    public IncidentResponse rejectIncident(Long incidentId, String reason, User authority) {
        return rejectIncident(incidentId, reason, authority, null);
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
    public IncidentResponse resolveIncident(
            Long incidentId,
            String resolutionNotes,
            MultipartFile afterPhoto,
            String responsibleAgent,
            LocalDateTime resolutionTimestamp,
            User officer
    ) {
        Incident incident = findIncidentOrThrow(incidentId);

        if (incident.getStatus() != IncidentStatus.IN_PROGRESS) {
            throw new InvalidWorkflowTransitionException(
                    "Incident must be IN_PROGRESS before it can be resolved. Current status: "
                            + incident.getStatus().getDisplayName()
            );
        }

        incident.setResolutionNotes(resolutionNotes);
        incident.setResponsibleAgent(responsibleAgent);
        incident.setResolutionTimestamp(
                resolutionTimestamp != null ? resolutionTimestamp : LocalDateTime.now()
        );

        if (afterPhoto != null && !afterPhoto.isEmpty()) {
            var storedPhoto = fileStorageService.storeIncidentPhoto(
                    afterPhoto,
                    incident,
                    officer,
                    PhotoType.RESOLUTION_PROOF
            );

            incident.setAfterPhotoUrl(storedPhoto.getFilePath());
        }

        workflowService.transition(
                incident,
                IncidentStatus.RESOLVED,
                officer,
                resolutionNotes
        );

        auditService.logIncidentResolved(officer, incidentId);

        notificationService.sendSuccess(
                incident.getReporter(),
                incident,
                "Great news! Your incident report #" + incidentId +
                        " has been resolved. Thank you for helping keep Ifrane safe."
        );

        return IncidentResponse.from(incident);
    }

    @Transactional
    public IncidentResponse resolveIncident(Long incidentId, String resolutionNotes, User officer) {
        return resolveIncident(
                incidentId,
                resolutionNotes,
                null,
                officer != null ? officer.getFullName() : "Authority Officer",
                LocalDateTime.now(),
                officer
        );
    }
    // ── READ OPERATIONS ───────────────────────────────────────────────────────

    public IncidentResponse getById(Long id) {
        // Use eager fetch so photos list is populated for the detail view
        Incident incident = incidentRepository.findByIdWithPhotos(id)
                .orElseThrow(() -> new IncidentNotFoundException("Incident #" + id + " not found"));
        return IncidentResponse.from(incident);
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

    public Page<IncidentResponse> getByCategory(IncidentCategory category, Pageable pageable) {
        return incidentRepository.findByCategoryOrderByCreatedAtDesc(category, pageable)
                .map(IncidentResponse::from);
    }

    public Page<IncidentResponse> getByStatusAndCategory(IncidentStatus status, IncidentCategory category, Pageable pageable) {
        return incidentRepository.findByCategoryAndStatusOrderByCreatedAtDesc(category, status, pageable)
                .map(IncidentResponse::from);
    }

    public DashboardStatsResponse getDashboardStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        long overdue = incidentRepository.findAll()
                .stream()
                .filter(Incident::isOverdue)
                .count();
        LocalDateTime now = LocalDateTime.now();

        long notReviewedOver12h =
                incidentRepository.countByStatusAndCreatedAtBefore(
                        IncidentStatus.SUBMITTED,
                        now.minusHours(12)
                );

        long highUrgencyUnassigned =
                incidentRepository.countHighUrgencyUnassignedBefore(
                        UrgencyLevel.HIGH,
                        now.minusMinutes(30)
                );

        long unresolvedOver7Days =
                incidentRepository.countByStatusNotAndCreatedAtBefore(
                        IncidentStatus.RESOLVED,
                        now.minusDays(7)
                );

        return DashboardStatsResponse.builder()
                .totalIncidents(incidentRepository.count())
                .submitted(incidentRepository.countByStatus(IncidentStatus.SUBMITTED))
                .underReview(incidentRepository.countByStatus(IncidentStatus.UNDER_REVIEW))
                .assigned(incidentRepository.countByStatus(IncidentStatus.ASSIGNED))
                .inProgress(incidentRepository.countByStatus(IncidentStatus.IN_PROGRESS))
                .resolved(incidentRepository.countByStatus(IncidentStatus.RESOLVED))
                .rejected(incidentRepository.countByStatus(IncidentStatus.REJECTED))
                .resolvedToday(incidentRepository.countByStatusAndResolvedAtAfter(
                        IncidentStatus.RESOLVED, todayStart))
                .submittedToday(incidentRepository.countByStatusAndCreatedAtAfter(
                        IncidentStatus.SUBMITTED, todayStart))
                .highUrgency(incidentRepository.countByUrgencyLevelAndStatusNot(
                        UrgencyLevel.HIGH, IncidentStatus.RESOLVED))
                .escalated(incidentRepository.countByEscalatedTrue())
                .overdue(overdue)
                .notReviewedOver12h(notReviewedOver12h)
                .highUrgencyUnassigned(highUrgencyUnassigned)
                .unresolvedOver7Days(unresolvedOver7Days)
                .build();
    }

    public List<UserResponse> getOfficersByDepartment(Department department) {
        return userRepository.findByDepartmentAndRoleAndEnabledTrue(department, Role.AUTHORITY)
                .stream()
                .map(UserResponse::from)
                .toList();
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
        List<User> officers = userRepository.findLeastBusyOfficerInDepartment(department, Role.AUTHORITY);
        return officers.isEmpty() ? null : officers.get(0);
    }
}
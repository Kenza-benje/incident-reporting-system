package com.example.ifraneguard.service;

import com.example.ifraneguard.Model.Incident;
import com.example.ifraneguard.Model.StatusHistory;
import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.enums.IncidentStatus;
import com.example.ifraneguard.exceptions.InvalidWorkflowTransitionException;
import com.example.ifraneguard.repository.IncidentRepository;
import com.example.ifraneguard.repository.StatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Enforces the strict incident status workflow.
 *
 * ALL status changes MUST go through this service.
 * No controller or other service should directly set incident.setStatus().
 *
 * Workflow:
 *   SUBMITTED → UNDER_REVIEW → ASSIGNED → IN_PROGRESS → RESOLVED
 *                           ↘ REJECTED
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final IncidentRepository incidentRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    /**
     * Attempts to transition an incident to a new status.
     * Throws InvalidWorkflowTransitionException if the transition is not allowed.
     *
     * @param incident  The incident to update
     * @param newStatus The desired new status
     * @param changedBy The user performing this action
     * @param reason    Optional explanation
     */
    @Transactional
    public void transition(Incident incident, IncidentStatus newStatus,
                           User changedBy, String reason) {

        IncidentStatus currentStatus = incident.getStatus();

        // Guard: check if this transition is allowed
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidWorkflowTransitionException(
                    String.format("Cannot transition incident #%d from %s to %s",
                            incident.getId(), currentStatus, newStatus)
            );
        }

        // Record the old status for the audit trail
        StatusHistory history = StatusHistory.builder()
                .incident(incident)
                .fromStatus(currentStatus)
                .toStatus(newStatus)
                .changedBy(changedBy)
                .reason(reason)
                .build();
        statusHistoryRepository.save(history);

        // Apply the transition
        incident.setStatus(newStatus);

        // If resolved or rejected, record the timestamp
        if (newStatus == IncidentStatus.RESOLVED || newStatus == IncidentStatus.REJECTED) {
            incident.setResolvedAt(LocalDateTime.now());
        }

        incidentRepository.save(incident);
        log.info("Incident #{} transitioned: {} → {} by user {}",
                incident.getId(), currentStatus, newStatus, changedBy.getEmail());
    }
}
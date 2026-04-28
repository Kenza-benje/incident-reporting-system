package com.example.ifraneguard.service;

import com.example.ifraneguard.Model.AuditLog;
import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates audit log entries for significant system events.
 *
 * DESIGN DECISIONS:
 *
 * 1. @Async — audit logging runs in a BACKGROUND THREAD.
 *    → The main request doesn't wait for the audit write to finish.
 *    → If audit logging fails, the actual operation still succeeds.
 *    → This keeps API response times fast.
 *
 * 2. Propagation.REQUIRES_NEW — runs in its own separate transaction.
 *    → If the main transaction rolls back (e.g., assignment fails),
 *      the audit log STILL gets written — we want to record attempted actions too.
 *
 * 3. All public log*() methods follow the same pattern:
 *    actor, action, entityType, entityId, description, optional metadata.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    // ── Incident Events ───────────────────────────────────────────────────────

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logIncidentSubmitted(User actor, Long incidentId, String title) {
        save(actor, "INCIDENT_SUBMITTED", "Incident", incidentId,
                "New incident submitted: \"" + title + "\"", null);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logIncidentAssigned(User actor, Long incidentId,
                                    String department, String officerName) {
        save(actor, "INCIDENT_ASSIGNED", "Incident", incidentId,
                "Incident #" + incidentId + " assigned to " + department +
                        (officerName != null ? " (officer: " + officerName + ")" : ""),
                "{\"department\": \"" + department + "\"}");
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logIncidentRejected(User actor, Long incidentId, String reason) {
        save(actor, "INCIDENT_REJECTED", "Incident", incidentId,
                "Incident #" + incidentId + " rejected. Reason: " + reason, null);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logIncidentResolved(User actor, Long incidentId) {
        save(actor, "INCIDENT_RESOLVED", "Incident", incidentId,
                "Incident #" + incidentId + " marked as resolved.", null);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logStatusChanged(User actor, Long incidentId,
                                 String fromStatus, String toStatus) {
        save(actor, "STATUS_CHANGED", "Incident", incidentId,
                "Status changed: " + fromStatus + " → " + toStatus,
                "{\"from\": \"" + fromStatus + "\", \"to\": \"" + toStatus + "\"}");
    }

    // ── User Events ───────────────────────────────────────────────────────────

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUserRegistered(User newUser) {
        save(newUser, "USER_REGISTERED", "User", newUser.getId(),
                "New user registered: " + newUser.getEmail() +
                        " (role: " + newUser.getRole() + ")", null);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUserLogin(User user, String ipAddress) {
        try {
            AuditLog entry = AuditLog.builder()
                    .actor(user)
                    .actorEmail(user.getEmail())
                    .action("USER_LOGIN")
                    .entityType("User")
                    .entityId(user.getId())
                    .description("User logged in from IP: " + ipAddress)
                    .ipAddress(ipAddress)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Audit log write failed for action USER_LOGIN: {}", e.getMessage());
        }
    }

    // ── System Events ─────────────────────────────────────────────────────────

    /**
     * Logs system-triggered events (no human actor, e.g., escalation scheduler).
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSystemEvent(String action, String entityType,
                               Long entityId, String description) {
        save(null, action, entityType, entityId, description, null);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void save(User actor, String action, String entityType,
                      Long entityId, String description, String metadata) {
        try {
            AuditLog entry = AuditLog.builder()
                    .actor(actor)
                    .actorEmail(actor != null ? actor.getEmail() : "SYSTEM")
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .metadata(metadata)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            // NEVER let audit logging crash the application
            log.error("Audit log write failed for action {}: {}", action, e.getMessage());
        }
    }
}

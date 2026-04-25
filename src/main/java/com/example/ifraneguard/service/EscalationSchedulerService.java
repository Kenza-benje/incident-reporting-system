package com.example.ifraneguard.service;

import com.example.ifraneguard.Model.Incident;
import com.example.ifraneguard.enums.EscalationType;
import com.example.ifraneguard.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Background job that periodically checks for overdue incidents
 * and marks them as escalated so authorities get alerted.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EscalationSchedulerService {

    private final IncidentRepository incidentRepository;
    private final NotificationService notificationService;

    /**
     * Runs every 30 minutes.
     * Finds incidents that have been open longer than their urgency threshold
     * and flags them as escalated if not already flagged.
     */
    @Scheduled(fixedRate = 30 * 60 * 1000)
    @Transactional
    public void checkAndEscalateOverdueIncidents() {
        log.info("Running escalation check at {}", LocalDateTime.now());

        LocalDateTime cutoff = LocalDateTime.now().minusHours(12);

        List<Incident> candidateIncidents =
                incidentRepository.findNonEscalatedOlderThan(cutoff);

        int escalatedCount = 0;

        for (Incident incident : candidateIncidents) {

            if (incident.isOverdue()) {
                incident.setEscalated(true);

                // Member 4: store escalation metadata for escalation-list UI
                if (incident.getEscalationTriggeredAt() == null) {
                    incident.setEscalationTriggeredAt(LocalDateTime.now());
                }

                if (incident.getEscalationType() == null) {
                    incident.setEscalationType(EscalationType.NOT_REVIEWED_IN_TIME);
                }

                incidentRepository.save(incident);

                if (incident.getAssignment() != null
                        && incident.getAssignment().getAssignedOfficer() != null) {
                    notificationService.sendWarning(
                            incident.getAssignment().getAssignedOfficer(),
                            incident,
                            "⚠️ OVERDUE: Incident #" + incident.getId()
                                    + " (" + incident.getTitle()
                                    + ") has exceeded its response time limit."
                    );
                }

                escalatedCount++;
            }
        }

        if (escalatedCount > 0) {
            log.warn("Escalated {} overdue incident(s)", escalatedCount);
        } else {
            log.info("No overdue incidents found in this check.");
        }
    }
}
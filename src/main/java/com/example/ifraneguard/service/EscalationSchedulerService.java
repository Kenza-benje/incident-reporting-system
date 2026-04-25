package com.example.ifraneguard.service;

import com.example.ifraneguard.Model.Incident;
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
 *
 * @Scheduled runs automatically on a fixed schedule — no HTTP request needed.
 * fixedRate = every 30 minutes (30 * 60 * 1000 ms)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EscalationSchedulerService {

    private final IncidentRepository  incidentRepository;
    private final NotificationService notificationService;

    /**
     * Runs every 30 minutes.
     * Finds incidents that have been open longer than their urgency threshold
     * and flags them as escalated (if not already flagged).
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30 minutes in milliseconds
    @Transactional
    public void checkAndEscalateOverdueIncidents() {
        log.info("Running escalation check at {}", LocalDateTime.now());

        // The 12-hour cutoff covers most MEDIUM urgency incidents.
        // HIGH urgency uses 6h — the isOverdue() method handles per-incident logic.
        LocalDateTime cutoff = LocalDateTime.now().minusHours(12);

        List<Incident> candidateIncidents = incidentRepository.findNonEscalatedOlderThan(cutoff);

        int escalatedCount = 0;
        for (Incident incident : candidateIncidents) {
            if (incident.isOverdue()) { // Use the per-urgency threshold
                incident.setEscalated(true);
                incidentRepository.save(incident);

                // Notify all AUTHORITY users (in a real system, filter by department)
                // For now, notify the assigned officer or general authority notification
                if (incident.getAssignment() != null
                        && incident.getAssignment().getAssignedOfficer() != null) {
                    notificationService.sendWarning(
                            incident.getAssignment().getAssignedOfficer(),
                            incident,
                            "⚠️ OVERDUE: Incident #" + incident.getId() +
                                    " (" + incident.getTitle() + ") has exceeded its response time limit."
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
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

        // Use minimum urgency threshold (HIGH = 6h) so we don't miss recently-overdue incidents
        LocalDateTime cutoff = LocalDateTime.now().minusHours(6);

        List<Incident> candidateIncidents =
                incidentRepository.findNonEscalatedOlderThan(cutoff);

        int escalatedCount = 0;

        for (Incident incident : candidateIncidents) {

            if (incident.isOverdue()) {
                incident.setEscalated(true);

                if (incident.getEscalationTriggeredAt() == null) {
                    incident.setEscalationTriggeredAt(LocalDateTime.now());
                }

                if (incident.getEscalationType() == null) {
                    incident.setEscalationType(EscalationType.NOT_REVIEWED_OVER_12H);
                }

                incidentRepository.save(incident);

                if (incident.getAssignment() != null
                        && incident.getAssignment().getAssignedOfficer() != null) {
                    notificationService.sendWarning(
                            incident.getAssignment().getAssignedOfficer(),
                            incident,
                            "⚠️ OVERDUE: Incident #" + incident.getId()
                                    + " (" + incident.getTitle()
                                    + "package com.example.ifraneguard.service;\n" +
                                    "\n" +
                                    "import com.example.ifraneguard.Model.Incident;\n" +
                                    "import com.example.ifraneguard.enums.EscalationType;\n" +
                                    "import com.example.ifraneguard.repository.IncidentRepository;\n" +
                                    "import lombok.RequiredArgsConstructor;\n" +
                                    "import lombok.extern.slf4j.Slf4j;\n" +
                                    "import org.springframework.scheduling.annotation.Scheduled;\n" +
                                    "import org.springframework.stereotype.Service;\n" +
                                    "import org.springframework.transaction.annotation.Transactional;\n" +
                                    "\n" +
                                    "import java.time.LocalDateTime;\n" +
                                    "import java.util.List;\n" +
                                    "\n" +
                                    "@Slf4j\n" +
                                    "@Service\n" +
                                    "@RequiredArgsConstructor\n" +
                                    "public class EscalationSchedulerService {\n" +
                                    "\n" +
                                    "    private final IncidentRepository incidentRepository;\n" +
                                    "    private final NotificationService notificationService;\n" +
                                    "\n" +
                                    "    @Scheduled(fixedRate = 30 * 60 * 1000)\n" +
                                    "    @Transactional\n" +
                                    "    public void checkAndEscalateOverdueIncidents() {\n" +
                                    "        log.info(\"Running escalation check at {}\", LocalDateTime.now());\n" +
                                    "\n" +
                                    "        LocalDateTime cutoff = LocalDateTime.now().minusHours(6);\n" +
                                    "\n" +
                                    "        List<Incident> candidateIncidents =\n" +
                                    "                incidentRepository.findNonEscalatedOlderThan(cutoff);\n" +
                                    "\n" +
                                    "        int escalatedCount = 0;\n" +
                                    "\n" +
                                    "        for (Incident incident : candidateIncidents) {\n" +
                                    "\n" +
                                    "            if (incident.isOverdue()) {\n" +
                                    "                incident.setEscalated(true);\n" +
                                    "\n" +
                                    "                if (incident.getEscalationTriggeredAt() == null) {\n" +
                                    "                    incident.setEscalationTriggeredAt(LocalDateTime.now());\n" +
                                    "                }\n" +
                                    "\n" +
                                    "                // ✅ FIXED HERE\n" +
                                    "                if (incident.getEscalationType() == null) {\n" +
                                    "                    incident.setEscalationType(EscalationType.NOT_REVIEWED_OVER_12H);\n" +
                                    "                }\n" +
                                    "\n" +
                                    "                incidentRepository.save(incident);\n" +
                                    "\n" +
                                    "                if (incident.getAssignment() != null\n" +
                                    "                        && incident.getAssignment().getAssignedOfficer() != null) {\n" +
                                    "                    notificationService.sendWarning(\n" +
                                    "                            incident.getAssignment().getAssignedOfficer(),\n" +
                                    "                            incident,\n" +
                                    "                            \"⚠\uFE0F OVERDUE: Incident #\" + incident.getId()\n" +
                                    "                                    + \" (\" + incident.getTitle()\n" +
                                    "                                    + \") has exceeded its response time limit.\"\n" +
                                    "                    );\n" +
                                    "                }\n" +
                                    "\n" +
                                    "                escalatedCount++;\n" +
                                    "            }\n" +
                                    "        }\n" +
                                    "\n" +
                                    "        if (escalatedCount > 0) {\n" +
                                    "            log.warn(\"Escalated {} overdue incident(s)\", escalatedCount);\n" +
                                    "        } else {\n" +
                                    "            log.info(\"No overdue incidents found in this check.\");\n" +
                                    "        }\n" +
                                    "    }\n" +
                                    "}) has exceeded its response time limit."
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
package com.example.ifraneguard.service;


import com.example.ifraneguard.Model.Incident;
import com.example.ifraneguard.enums.IncidentCategory;
import com.example.ifraneguard.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Handles duplicate detection logic.
 *
 * A report is considered a duplicate if:
 *   1. Same category
 *   2. Within ~300 meters (approximated as 0.003° lat/lon difference)
 *   3. Submitted within the last 24 hours
 *   4. Not already rejected
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicateDetectionService {

    private final IncidentRepository incidentRepository;

    /**
     * Returns true if this incident appears to be a duplicate of an existing open report.
     *
     * @param latitude  Where the incident occurred
     * @param longitude Where the incident occurred
     * @param category  What type of incident
     * @param excludeId The ID of the current incident (to exclude it from results)
     */
    public boolean isDuplicate(Double latitude, Double longitude,
                               IncidentCategory category, Long excludeId) {

        LocalDateTime since = LocalDateTime.now().minusHours(24);

        List<Incident> nearby = incidentRepository.findNearbyDuplicates(
                latitude, longitude, category, since, excludeId != null ? excludeId : -1L
        );

        if (!nearby.isEmpty()) {
            log.info("Duplicate detected: {} similar reports within 300m/24h for category {}",
                    nearby.size(), category);
        }

        return !nearby.isEmpty();
    }

    /**
     * Returns the list of nearby similar incidents (for showing to citizen as "similar reports").
     */
    public List<Incident> findSimilarIncidents(Double latitude, Double longitude,
                                               IncidentCategory category) {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return incidentRepository.findNearbyDuplicates(latitude, longitude, category, since, -1L);
    }
}
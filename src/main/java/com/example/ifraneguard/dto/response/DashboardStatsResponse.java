package com.example.ifraneguard.dto.response;


import lombok.Builder;
import lombok.Data;

/**
 * Aggregated statistics for the authority dashboard summary cards.
 * e.g. "42 Submitted | 5 Overdue | 3 Resolved Today"
 */
@Data
@Builder
public class DashboardStatsResponse {

    private long totalIncidents;
    private long submitted;
    private long underReview;
    private long assigned;
    private long inProgress;
    private long resolved;
    private long rejected;
    private long resolvedToday;
    private long submittedToday;
    private long highUrgency;
}
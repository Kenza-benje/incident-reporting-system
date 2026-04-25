package com.example.ifraneguard.Model;


import com.example.ifraneguard.enums.IncidentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Immutable audit record of every status transition for an incident.
 * Allows authorities to see the full lifecycle of any incident.
 * Example:
 *   SUBMITTED → UNDER_REVIEW  (changed by: officer Jane, at: 09:12)
 *   UNDER_REVIEW → ASSIGNED   (changed by: officer Jane, at: 09:45)
 */
@Entity
@Table(name = "status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Incident incident;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus toStatus;

    /**
     * The user who triggered this status change.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User changedBy;

    @Column(columnDefinition = "TEXT")
    private String reason; // Optional reason/note for the change

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;
}
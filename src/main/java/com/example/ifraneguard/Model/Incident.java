package com.example.ifraneguard.Model;


import com.example.ifraneguard.enums.IncidentCategory;
import com.example.ifraneguard.enums.IncidentStatus;
import com.example.ifraneguard.enums.UrgencyLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The core entity of the system. Represents a reported incident.
 *
 * Key design decisions:
 * - latitude/longitude stored as double for geo queries
 * - isOverdue() is a computed method, NOT a database column — avoids stale data
 * - status transitions are enforced in the service layer, not here
 */
@Entity
@Table(name = "incidents", indexes = {
        // Index on status for fast dashboard queries
        @Index(name = "idx_incident_status", columnList = "status"),
        // Index on category for filtering
        @Index(name = "idx_incident_category", columnList = "category"),
        // Composite index for geospatial duplicate detection queries
        @Index(name = "idx_incident_location_time", columnList = "latitude, longitude, createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Description is required")
    @Column(nullable = false, columnDefinition = "TEXT") // TEXT allows long descriptions
    private String description;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private IncidentStatus status = IncidentStatus.SUBMITTED; // Default on creation

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UrgencyLevel urgencyLevel; // Auto-set by service based on category

    // --- Location ---
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    @Column(nullable = false)
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    @Column(nullable = false)
    private Double longitude;

    @Column // Optional human-readable address
    private String locationDescription;

    // --- Media ---
    @Column // Path to uploaded image, e.g. "/uploads/incidents/uuid.jpg"
    private String photoUrl;

    // --- Overdue flag ---
    /**
     * Whether the authority explicitly flagged this as requiring escalation.
     * The real overdue check is done via isOverdue() computed method below.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean escalated = false;

    // --- Relationships ---

    /**
     * The citizen who submitted this incident.
     * LAZY loading — don't fetch user from DB unless we actually access it.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User reporter;

    /**
     * The assignment record (if any). One incident → one assignment.
     * mappedBy = "incident" means the foreign key lives on the Assignment table.
     */
    @OneToOne(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Assignment assignment;

    /**
     * All status change history for this incident (audit trail).
     */
    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<StatusHistory> statusHistory;

    /**
     * In-app notifications related to this incident.
     */
    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Notification> notifications;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Timestamp of when the incident was officially resolved or rejected.
     */
    @Column
    private LocalDateTime resolvedAt;

    // -------------------------------------------------------------------------
    // Computed helpers — these run in memory, no extra DB query needed
    // -------------------------------------------------------------------------

    /**
     * An incident is overdue if it has NOT been resolved/rejected AND
     * it has been open longer than its urgency threshold.
     * This is computed on-the-fly so it's always accurate —> no stale DB value.
     */
    public boolean isOverdue() {
        if (status == IncidentStatus.RESOLVED || status == IncidentStatus.REJECTED) {
            return false; // Closed incidents can't be overdue
        }
        if (urgencyLevel == null || createdAt == null) return false;

        LocalDateTime overdueTime = createdAt.plusHours(urgencyLevel.getOverdueThresholdHours());
        return LocalDateTime.now().isAfter(overdueTime);
    }

    /**
     * Helper to get the assigned department display name safely.
     */
    public String getAssignedDepartmentName() {
        if (assignment == null || assignment.getDepartment() == null) return "Unassigned";
        return assignment.getDepartment().getDisplayName();
    }
}

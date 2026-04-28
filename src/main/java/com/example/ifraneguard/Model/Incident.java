package com.example.ifraneguard.Model;

import com.example.ifraneguard.enums.EscalationType;
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
import jakarta.persistence.Lob;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

import java.util.List;

@Entity
@Table(name = "incidents", indexes = {
        @Index(name = "idx_incident_status", columnList = "status"),
        @Index(name = "idx_incident_category", columnList = "category"),
        @Index(name = "idx_incident_location_time", columnList = "latitude, longitude, created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class Incident {

    @Lob
    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    private String responsibleAgent;

    private LocalDateTime resolutionTimestamp;

    private String afterPhotoUrl;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String reviewNotes;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Description is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private IncidentStatus status = IncidentStatus.SUBMITTED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UrgencyLevel urgencyLevel;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    @Column(nullable = false)
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    @Column(nullable = false)
    private Double longitude;

    private String locationDescription;

    private String photoUrl;

    @Column(nullable = false)
    @Builder.Default
    private boolean escalated = false;

    @Enumerated(EnumType.STRING)
    private EscalationType escalationType;

    private LocalDateTime escalationTriggeredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User reporter;

    @OneToOne(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Assignment assignment;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<StatusHistory> statusHistory;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Notification> notifications;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<IncidentPhoto> photos;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<InternalNote> internalNotes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;

    public boolean isOverdue() {
        if (status == IncidentStatus.RESOLVED || status == IncidentStatus.REJECTED) {
            return false;
        }

        if (urgencyLevel == null || createdAt == null) {
            return false;
        }

        LocalDateTime overdueTime = createdAt.plusHours(urgencyLevel.getOverdueThresholdHours());
        return LocalDateTime.now().isAfter(overdueTime);
    }

    public String getAssignedDepartmentName() {
        if (assignment == null || assignment.getDepartment() == null) {
            return "Unassigned";
        }

        return assignment.getDepartment().getDisplayName();
    }
}
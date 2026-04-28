package com.example.ifraneguard.Model;


import com.example.ifraneguard.enums.Department;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Records which department and officer were assigned to handle an incident.
 * Separated from Incident to keep the Incident entity clean and follow SRP.
 *
 * This also creates a clean audit record of every assignment action.
 */
@Entity
@Table(name = "assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The incident being assigned. OneToOne — each incident has at most one assignment record.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @NotNull
    private Incident incident;

    /**
     * The department responsible for handling this incident.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Department is required")
    private Department department;

    /**
     * The specific officer assigned. Can be null if only dept-level assignment is made.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User assignedOfficer;

    /**
     * The authority user who performed this assignment action.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User assignedBy;

    /**
     * Optional note from the authority explaining context or instructions.
     */
    @Column(columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime assignedAt;
}

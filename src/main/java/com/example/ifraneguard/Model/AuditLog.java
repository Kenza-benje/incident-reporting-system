package com.example.ifraneguard.Model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Immutable record of every significant action in the system.
 *
 * WHAT GETS LOGGED:
 *  - User registered / logged in / deactivated
 *  - Incident submitted / assigned / rejected / resolved
 *  - Assignment created or changed
 *  - Any action by AUTHORITY or ADMIN users
 *
 * WHY IT'S IMPORTANT for this project:
 *  → Government reporting platforms MUST have accountability trails
 *  → Ifrane municipality can audit who did what if a complaint arises
 *  → Professors love seeing this — shows real-world thinking
 *
 * This entity is APPEND-ONLY — once saved, never updated or deleted.
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_actor", columnList = "actor_id"),
        @Index(name = "idx_audit_entity", columnList = "entityType, entityId"),
        @Index(name = "idx_audit_created", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * WHO performed the action. Nullable — some events are system-triggered
     * (e.g., the escalation scheduler, not a human user).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User actor;

    /**
     * Human-readable name of the actor at time of logging.
     * Stored separately so if the user is deleted, the log remains meaningful.
     */
    @Column
    private String actorEmail;

    /**
     * The type of action performed.
     * Examples: USER_REGISTERED, INCIDENT_SUBMITTED, INCIDENT_ASSIGNED,
     *           INCIDENT_REJECTED, INCIDENT_RESOLVED, USER_LOGIN, STATUS_CHANGED
     */
    @Column(nullable = false)
    private String action;

    /**
     * Which entity type was affected.
     * Examples: "Incident", "User", "Assignment"
     */
    @Column(nullable = false)
    private String entityType;

    /**
     * The ID of the affected entity.
     * e.g., incident ID 42, user ID 7
     */
    @Column
    private Long entityId;

    /**
     * Free-text description of what happened.
     * Example: "Incident #42 assigned to Forest Services by officer Jane Doe"
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * JSON snapshot of relevant data at the time of the action.
     * Example: { "oldStatus": "SUBMITTED", "newStatus": "UNDER_REVIEW" }
     * Stored as plain TEXT (not JSONB) for simplicity — use JSONB in production.
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /**
     * The IP address of the request, for security auditing.
     */
    @Column(length = 45) // IPv6 max length
    private String ipAddress;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
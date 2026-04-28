package com.example.ifraneguard.Model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * In-app notification sent to a user about an incident update.
 * Used to populate the notification area in the authority dashboard UI.
 *
 * Example messages:
 *  - "The incident report has been assigned successfully."
 *  - "Incident #42 is now UNDER_REVIEW."
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_user_read", columnList = "recipient_id, is_read")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Who receives this notification.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User recipient;

    /**
     * The incident this notification is about (nullable — some are system-wide).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Incident incident;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String type; // e.g. "SUCCESS", "ERROR", "INFO", "WARNING"

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

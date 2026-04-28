package com.example.ifraneguard.repository;

import com.example.ifraneguard.Model.AuditLog;
import com.example.ifraneguard.Model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Read-only access to audit records.
 * NOTE: No delete methods — audit logs are legally immutable.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** All actions performed by a specific user (for user activity report). */
    Page<AuditLog> findByActorOrderByCreatedAtDesc(User actor, Pageable pageable);

    /** All actions affecting a specific entity (e.g., full history of incident #42). */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, Long entityId);

    /** Filter by action type (e.g., all INCIDENT_ASSIGNED events). */
    Page<AuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    /** Audit logs within a time window (for compliance reports). */
    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime from, LocalDateTime to);

    /** Recent activity for admin dashboard. */
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
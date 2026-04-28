package com.example.ifraneguard.repository;

import com.example.ifraneguard.Model.Incident;
import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.enums.IncidentCategory;
import com.example.ifraneguard.enums.IncidentStatus;
import com.example.ifraneguard.enums.UrgencyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;

/**
 * Data access layer for Incident entity.
 *
 * JpaSpecificationExecutor allows dynamic filtering (used in the authority dashboard
 * when filtering by status, category, date range, etc.)
 */
@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long>,
        JpaSpecificationExecutor<Incident> {

    // ── Citizen queries ──────────────────────────────────────────────────────

    /** All incidents submitted by a specific citizen (for their "My Reports" page). */
    Page<Incident> findByReporterOrderByCreatedAtDesc(User reporter, Pageable pageable);

    List<Incident> findByReporterAndStatus(User reporter, IncidentStatus status);

    // ── Authority dashboard queries ──────────────────────────────────────────

    Page<Incident> findByStatusOrderByCreatedAtDesc(IncidentStatus status, Pageable pageable);

    Page<Incident> findByCategoryOrderByCreatedAtDesc(
            IncidentCategory category, Pageable pageable);

    Page<Incident> findByCategoryAndStatusOrderByCreatedAtDesc(
            IncidentCategory category, IncidentStatus status, Pageable pageable);

    List<Incident> findByUrgencyLevelAndStatusNot(UrgencyLevel level, IncidentStatus status);

    /** Count by status — used for dashboard statistics cards. */
    long countByStatus(IncidentStatus status);

    long countByStatusAndCreatedAtAfter(IncidentStatus status, LocalDateTime since);

    long countByStatusAndResolvedAtAfter(IncidentStatus status, LocalDateTime since);

    long countByEscalatedTrue();

    // ── Duplicate detection ──────────────────────────────────────────────────

    /**
     * Detects duplicate reports within ~300m and 24 hours of the given location/time.
     *
     * How the 300m distance works:
     * ~0.003 degrees of latitude ≈ 300 meters at Ifrane's latitude (~33°N).
     * This is an approximation — for production, use PostGIS st_dwithin() instead.
     * The formula used is the bounding-box approach: fast and good enough for ~300m.
     *
     * @param latitude  Center latitude
     * @param longitude Center longitude
     * @param category  Must match the same category (wildfire ≠ road damage)
     * @param since     Only look 24 hours back
     * @param excludeId Exclude the current incident itself (for re-checks)
     */
    @Query("""
        SELECT i FROM Incident i
        WHERE i.category = :category
          AND i.id != :excludeId
          AND i.createdAt >= :since
          AND ABS(i.latitude - :latitude) < 0.003
          AND ABS(i.longitude - :longitude) < 0.003
          AND i.status NOT IN ('REJECTED')
        """)
    List<Incident> findNearbyDuplicates(
            @Param("latitude")  Double latitude,
            @Param("longitude") Double longitude,
            @Param("category")  IncidentCategory category,
            @Param("since")     LocalDateTime since,
            @Param("excludeId") Long excludeId
    );

    // ── Escalation scheduler query ───────────────────────────────────────────

    /**
     * Finds all incidents that are still open (not resolved/rejected)
     * and were created before the given cutoff time.
     * The scheduler uses this to flag overdue incidents.
     */
    @Query("""
        SELECT i FROM Incident i
        WHERE i.status NOT IN ('RESOLVED', 'REJECTED')
          AND i.createdAt < :cutoff
          AND i.escalated = false
        """)
    List<Incident> findNonEscalatedOlderThan(@Param("cutoff") LocalDateTime cutoff);

    @Query("""
        SELECT i.category, COUNT(i) FROM Incident i
        GROUP BY i.category
        ORDER BY COUNT(i) DESC
        """)
    List<Object[]> countByCategory();

    @Query("""
        SELECT i.status, COUNT(i) FROM Incident i
        GROUP BY i.status
        """)
    List<Object[]> countGroupedByStatus();

    /** Count high-urgency incidents that are not resolved — used for dashboard stats card. */
    long countByUrgencyLevelAndStatusNot(UrgencyLevel level, IncidentStatus status);

    long countByStatusAndCreatedAtBefore(IncidentStatus status, LocalDateTime time);

    @Query("""
    SELECT COUNT(i) FROM Incident i
    WHERE i.urgencyLevel = :urgencyLevel
      AND i.assignment IS NULL
      AND i.createdAt < :time
      AND i.status NOT IN ('RESOLVED', 'REJECTED')
""")
    long countHighUrgencyUnassignedBefore(
            @Param("urgencyLevel") UrgencyLevel urgencyLevel,
            @Param("time") LocalDateTime time
    );

    long countByStatusNotAndCreatedAtBefore(
            IncidentStatus status,
            LocalDateTime time
    );

    /**
     * Fetches an incident with its photos eagerly loaded — used for the detail view
     * so photos are available without a LazyInitializationException.
     */
    @Query("""
        SELECT DISTINCT i FROM Incident i
        LEFT JOIN FETCH i.photos
        LEFT JOIN FETCH i.reporter
        LEFT JOIN FETCH i.assignment a
        LEFT JOIN FETCH a.assignedOfficer
        WHERE i.id = :id
        """)
    java.util.Optional<Incident> findByIdWithPhotos(@Param("id") Long id);
}

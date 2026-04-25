package com.example.ifraneguard.repository;

import com.example.ifraneguard.Model.Assignment;
import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.enums.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    Optional<Assignment> findByIncidentId(Long incidentId);

    List<Assignment> findByAssignedOfficer(User officer);

    List<Assignment> findByDepartment(Department department);

    /** How many active (non-resolved) incidents is this officer handling? */
    @Query("""
        SELECT COUNT(a) FROM Assignment a
        WHERE a.assignedOfficer = :officer
          AND a.incident.status NOT IN ('RESOLVED', 'REJECTED')
        """)
    long countActiveAssignmentsForOfficer(@Param("officer") User officer);
}
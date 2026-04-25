package com.example.ifraneguard.repository;

import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.enums.Department;
import com.example.ifraneguard.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access layer for User entity.
 * Spring Data JPA auto-generates SQL from method names — no manual SQL needed.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Find all officers in a specific department.
     */
    List<User> findByDepartmentAndRoleAndEnabledTrue(Department department, Role role);

    /**
     * Find all active authority users (for admin dashboards).
     */
    List<User> findByRoleAndEnabledTrue(Role role);

    /**
     * Custom query: find officers by department who have the fewest active assignments.
     * Useful for load-balanced auto-assignment.
     */
    @Query("""
        SELECT u FROM User u
        WHERE u.department = :department
          AND u.role = 'AUTHORITY'
          AND u.enabled = true
        ORDER BY SIZE(u.handledAssignments) ASC
        """)
    List<User> findLeastBusyOfficerInDepartment(@Param("department") Department department);
}
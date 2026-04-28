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

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByOfficerCodeIgnoreCase(String officerCode);

    boolean existsByEmail(String email);

    List<User> findByDepartmentAndRoleAndEnabledTrue(Department department, Role role);

    List<User> findByRoleAndEnabledTrue(Role role);

    /**
     * Finds the least-busy authority officer in a department.
     * Used for auto-assignment when no specific officer is chosen.
     * Uses :#{#role.name()} to safely pass enum as string in JPQL.
     */
    @Query("""
        SELECT u FROM User u
        WHERE u.department = :department
          AND u.role = :role
          AND u.enabled = true
        ORDER BY SIZE(u.handledAssignments) ASC
        """)
    List<User> findLeastBusyOfficerInDepartment(
            @Param("department") Department department,
            @Param("role") Role role);
}

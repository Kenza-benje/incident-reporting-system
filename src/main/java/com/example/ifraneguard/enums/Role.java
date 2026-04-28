package com.example.ifraneguard.enums;

/**
 * System roles.
 *
 * CITIZEN   → can submit and track their own incidents
 * AUTHORITY → any authority officer who can review/assign/manage incidents
 *             (replaces the per-department roles for Spring Security simplicity)
 * ADMIN     → full system access, user management
 *
 * NOTE: We intentionally use a single AUTHORITY role instead of per-department roles
 * because Spring Security role checks use @PreAuthorize("hasRole('AUTHORITY')").
 * The actual department a user belongs to is stored on the User.department field.
 */
public enum Role {
    CITIZEN,
    AUTHORITY,
    ADMIN
}

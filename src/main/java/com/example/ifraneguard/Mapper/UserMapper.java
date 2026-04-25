package com.example.ifraneguard.Mapper;

import com.example.ifraneguard.dto.request.RegisterRequest;
import com.example.ifraneguard.dto.response.UserResponse;
import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .departmentDisplayName(
                        user.getDepartment() != null
                                ? user.getDepartment().getDisplayName() : null)
                .enabled(user.isEnabled())
                .build();
    }

    /**
     * Converts a RegisterRequest DTO → User entity (pre-save).
     * Password must be encoded BEFORE calling this, or encoded after.
     * We pass the already-encoded password in so this mapper stays pure.
     *
     * @param request         The registration form data
     * @param encodedPassword BCrypt-hashed password from UserService
     */
    public User toEntity(RegisterRequest request, String encodedPassword) {
        return User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(encodedPassword)
                .role(request.getRole() != null ? request.getRole() : Role.CITIZEN)
                .department(request.getDepartment())
                .enabled(true)
                .build();
    }
}
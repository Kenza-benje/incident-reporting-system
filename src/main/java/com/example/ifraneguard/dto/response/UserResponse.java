package com.example.ifraneguard.dto.response;

import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.enums.Department;
import com.example.ifraneguard.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private Department department;
    private String departmentDisplayName;
    private boolean enabled;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .departmentDisplayName(user.getDepartment() != null
                        ? user.getDepartment().getDisplayName() : null)
                .enabled(user.isEnabled())
                .build();
    }
}
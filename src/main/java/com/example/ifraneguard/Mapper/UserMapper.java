package com.example.ifraneguard.Mapper;

import com.example.ifraneguard.dto.response.UserResponse;
import com.example.ifraneguard.Model.User;
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

}
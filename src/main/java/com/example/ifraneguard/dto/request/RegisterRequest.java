package com.example.ifraneguard.dto.request;


import com.example.ifraneguard.enums.Department;
import com.example.ifraneguard.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Must be a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private Role role; // Default CITIZEN if not provided

    private Department department; // Only for AUTHORITY users
}

package com.example.ifraneguard.service;

import com.example.ifraneguard.dto.request.RegisterRequest;
import com.example.ifraneguard.dto.response.UserResponse;
import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.enums.Role;
import com.example.ifraneguard.exceptions.EmailAlreadyExistsException;
import com.example.ifraneguard.exceptions.UserNotFoundException;
import com.example.ifraneguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user registration and profile operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService    auditService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        // Public /api/auth/register must never allow self-registration as ADMIN or AUTHORITY.
        // Role is clamped to CITIZEN; privileged accounts must be created via a protected admin interface.
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CITIZEN)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {} ({})", saved.getEmail(), saved.getRole());
        auditService.logUserRegistered(saved);
        return UserResponse.from(saved);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
    }

    public UserResponse getProfile(User user) {
        return UserResponse.from(user);
    }
}

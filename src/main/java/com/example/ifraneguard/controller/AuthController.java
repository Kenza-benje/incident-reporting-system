package com.example.ifraneguard.controller;

import com.example.ifraneguard.dto.request.LoginRequest;
import com.example.ifraneguard.dto.request.RegisterRequest;
import com.example.ifraneguard.dto.response.ApiResponse;
import com.example.ifraneguard.dto.response.AuthResponse;
import com.example.ifraneguard.dto.response.UserResponse;
import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.Mapper.UserMapper;
import com.example.ifraneguard.service.JwtService;
import com.example.ifraneguard.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Handles registration and JWT-based login.
 * Base URL: /api/auth
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService           userService;
    private final UserMapper            userMapper;
    private final JwtService            jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService    userDetailsService;

    /**
     * POST /api/auth/register
     * Anyone can register. Returns a JWT immediately so they don't need to log in again.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse userResponse = userService.register(request);

        // Issue a JWT right away so the frontend can start using the API immediately
        UserDetails userDetails = userDetailsService.loadUserByUsername(userResponse.getEmail());
        String token = jwtService.generateToken(
                Map.of("role", userResponse.getRole().name()),
                userDetails
        );

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(userResponse)
                .expiresIn(jwtService.getExpirationDuration() / 1000) // ms → seconds
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", authResponse));
    }

    /**
     * POST /api/auth/login
     * Authenticates email + password, returns a signed JWT.
     *
     * FLOW:
     *  1. AuthenticationManager verifies credentials against DB.
     *  2. If valid, generate JWT with the user's role embedded.
     *  3. Return token to frontend — frontend stores it and sends it on every future request.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        try {
            // This throws BadCredentialsException if email/password don't match
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid email or password"));
        }

        // Credentials are valid — load the full user for token generation
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        User user = userService.findByEmail(request.getEmail());

        // Embed the role in the JWT payload for quick role checks without DB
        String token = jwtService.generateToken(
                Map.of("role", user.getRole().name(), "userId", user.getId()),
                userDetails
        );

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .expiresIn(jwtService.getExpirationDuration() / 1000)
                .build();

        log.info("User logged in: {}", request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }
}
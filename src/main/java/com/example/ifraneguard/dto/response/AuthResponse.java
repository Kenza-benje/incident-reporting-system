package com.example.ifraneguard.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Returned after successful login or registration.
 * Contains the JWT token the frontend must store and send with every future request.
 */
@Data
@Builder
public class AuthResponse {
    private String      token;       // JWT bearer token
    private String      tokenType;   // Always "Bearer"
    private UserResponse user;       // Basic profile info
    private long        expiresIn;   // Seconds until expiry (e.g. 86400 = 24h)
}

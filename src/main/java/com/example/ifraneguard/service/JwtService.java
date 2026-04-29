package com.example.ifraneguard.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles all JWT operations: generation, validation, and claim extraction.
 *
 * HOW JWT WORKS (for a beginner):
 *   1. User logs in with email + password.
 *   2. Server verifies credentials, then calls generateToken() to create a signed JWT.
 *   3. JWT is returned to the client (mobile app, browser).
 *   4. Client stores it (localStorage, memory, etc.).
 *   5. On every subsequent request, client sends: "Authorization: Bearer <token>"
 *   6. Our JwtAuthFilter intercepts the request, calls isTokenValid() — no DB needed.
 *   7. If valid, Spring Security marks the user as authenticated for this request.
 *
 * STRUCTURE OF A JWT:
 *   header.payload.signature
 *   - header: algorithm used (HS256)
 *   - payload: claims (email, role, expiry) — readable but NOT editable without secret
 *   - signature: HMAC hash of header+payload using our secret key
 */
@Service
public class JwtService {

    /**
     * Secret key used to sign JWTs. Loaded from application.properties.
     * Must be at least 256 bits (32 chars) for HS256.
     * NEVER hardcode this in source code — use environment variables in production.
     */
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    /**
     * Token validity in milliseconds. Default: 24 hours.
     */
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    // ── Token Generation ─────────────────────────────────────────────────────

    /**
     * Generates a JWT for the given user with no extra claims.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a JWT with extra custom claims (e.g., user role, user ID).
     *
     * @param extraClaims Additional data to embed in the token payload
     * @param userDetails The authenticated user
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())      // "username" = email in our system
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // ── Token Validation ─────────────────────────────────────────────────────

    /**
     * Returns true if the token belongs to this user AND hasn't expired.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ── Claim Extraction ─────────────────────────────────────────────────────

    /**
     * Extracts the email (subject) from a token.
     * Used by JwtAuthFilter to know WHICH user is making the request.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic claim extractor — pass a function to pull any field from the JWT payload.
     * Example: extractClaim(token, Claims::getSubject) → email
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses and verifies the token signature, returning all embedded claims.
     * Throws JwtException if the token is tampered with, expired, or malformed.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Converts the Base64-encoded secret string into a cryptographic Key object.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * How many milliseconds until this token expires.
     * Useful for telling the frontend when to refresh.
     */
    public long getExpirationDuration() {
        return jwtExpiration;
    }
}

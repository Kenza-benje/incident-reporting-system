package com.example.ifraneguard.config;

import com.example.ifraneguard.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService        jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest  request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain         filterChain
    ) throws ServletException, IOException {

        // Step 1: Read the Authorization header
        final String authHeader = request.getHeader("Authorization");

        // Step 2: If there's no Bearer token, skip this filter entirely
        // (Spring Security will handle unauthenticated access downstream)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract the token (remove "Bearer " prefix)
        final String jwt   = authHeader.substring(7);
        final String email;

        try {
            // Step 4: Extract email from token — throws if token is malformed/tampered
            email = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            filterChain.doFilter(request, response); // Let Spring Security reject it
            return;
        }

        // Step 5: Only authenticate if not already authenticated in this request
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user from DB (to verify they still exist and are enabled)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

            // Step 6: Validate token against this user's current details
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Step 7: Create an authentication object and register it with Spring Security
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                        // no credentials needed (already authenticated)
                                userDetails.getAuthorities() // ROLE_CITIZEN, ROLE_AUTHORITY, etc.
                        );

                // Attach request metadata (IP address, session ID) for auditing
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // THIS is what makes @AuthenticationPrincipal work in controllers
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("JWT authenticated: {} → {}", email, userDetails.getAuthorities());
            }
        }

        // Continue the filter chain regardless
        filterChain.doFilter(request, response);
    }
}
package com.example.ifraneguard.config;

import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Bridges our User entity with Spring Security.
 *
 * Spring Security calls loadUserByUsername() during login.
 * We load the user from the database and return a UserDetails object.
 *
 * We use email as the "username" in this system.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Convert our Role enum to Spring Security's GrantedAuthority format
        // Spring Security expects "ROLE_CITIZEN", "ROLE_AUTHORITY" etc.
        String springRole = "ROLE_" + user.getRole().name();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword()) // Already BCrypt hashed
                .authorities(List.of(new SimpleGrantedAuthority(springRole)))
                .accountLocked(!user.isEnabled())
                .build();
    }
}
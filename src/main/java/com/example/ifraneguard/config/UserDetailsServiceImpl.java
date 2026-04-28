package com.example.ifraneguard.config;

import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Bridges our User entity with Spring Security.
 *
 * Returns our custom User entity directly (User implements UserDetails) so that
 * @AuthenticationPrincipal User in controllers receives the actual entity,
 * not a Spring Security User wrapper — no cast required, no extra DB call.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}

package com.example.ifraneguard.controller;

import com.example.ifraneguard.Model.User;
import com.example.ifraneguard.dto.request.RegisterRequest;
import com.example.ifraneguard.enums.Role;
import com.example.ifraneguard.repository.UserRepository;
import com.example.ifraneguard.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles Thymeleaf login/signup/logout pages with session-based auth.
 * The REST API (/api/auth/**) still uses JWT.
 * This controller provides the UI-level authentication experience.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthPageController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository        userRepository;
    private final UserService           userService;

    // ── LOGIN ─────────────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "success", required = false) String success,
            @RequestParam(value = "selectedRole", required = false, defaultValue = "CITIZEN") String selectedRole,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("error", "Invalid credentials. Please check your email and password.");
        }
        if (success != null) {
            model.addAttribute("success", success);
        }
        model.addAttribute("selectedRole", selectedRole);
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(
            @RequestParam String identifier,
            @RequestParam String password,
            @RequestParam(defaultValue = "CITIZEN") String role,
            @RequestParam(defaultValue = "EMAIL") String loginMethod,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Resolve login identifier to the email Spring Security uses internally.
            // Citizens can log in by email. Authority users can log in by officerCode from DB.
            String email = identifier.trim();
            if ("ID".equalsIgnoreCase(loginMethod)) {
                if ("AUTHORITY".equalsIgnoreCase(role)) {
                    email = userRepository.findByOfficerCodeIgnoreCase(identifier.trim())
                            .map(User::getEmail)
                            .orElse(identifier.trim()); // fallback so authentication fails normally
                } else {
                    email = userRepository.findAll().stream()
                            .filter(u -> identifier.equalsIgnoreCase(u.getFullName())
                                    || identifier.equals(String.valueOf(u.getId())))
                            .map(User::getEmail)
                            .findFirst()
                            .orElse(identifier.trim());
                }
            }

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            User user = (User) auth.getPrincipal();

            // Role check: enforce the role they selected
            if ("AUTHORITY".equalsIgnoreCase(role)) {
                if (user.getRole() != Role.AUTHORITY && user.getRole() != Role.ADMIN) {
                    redirectAttributes.addFlashAttribute("error",
                            "Your account does not have authority access. Please log in as a citizen.");
                    return "redirect:/login?error";
                }
            } else {
                if (user.getRole() == Role.AUTHORITY || user.getRole() == Role.ADMIN) {
                    // Authority staff selected citizen — still allow, redirect to dashboard
                }
            }

            // Store authentication in the HTTP session
            SecurityContextHolder.getContext().setAuthentication(auth);
            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );

            log.info("User {} logged in via web UI (role: {})", user.getEmail(), user.getRole());

            // Redirect based on role
            if (user.getRole() == Role.AUTHORITY || user.getRole() == Role.ADMIN) {
                return "redirect:/authority/dashboard";
            } else {
                return "redirect:/";
            }

        } catch (BadCredentialsException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid email or password. Please try again.");
            return "redirect:/login?error";
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Login failed. Please try again.");
            return "redirect:/login?error";
        }
    }

    // ── SIGNUP ────────────────────────────────────────────────────────────────

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String doSignup(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam(required = false) String citizenId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/signup";
        }
        if (password.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters.");
            return "redirect:/signup";
        }
        if (userRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "An account with that email already exists.");
            return "redirect:/signup";
        }

        try {
            RegisterRequest reg = new RegisterRequest();
            reg.setFullName(fullName);
            reg.setEmail(email);
            reg.setPassword(password);
            userService.register(reg);

            log.info("New citizen registered via UI: {}", email);

            // Auto-login after registration
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );

            return "redirect:/?registered=true";

        } catch (Exception e) {
            log.error("Signup error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/signup";
        }
    }

    // ── LOGOUT ────────────────────────────────────────────────────────────────

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return "redirect:/login?success=You+have+been+signed+out+successfully.";
    }
}

package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class AuthController {
    private final Map<String, String> citizenPasswordsByEmail = new ConcurrentHashMap<>();

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(
            @RequestParam("fullName") String fullName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes
    ) {
        if (fullName == null || fullName.isBlank() ||
                email == null || email.isBlank() ||
                password == null || password.isBlank() ||
                confirmPassword == null || confirmPassword.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "All fields are required.");
            return "redirect:/signup";
        }
        if (password.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters.");
            return "redirect:/signup";
        }
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/signup";
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (citizenPasswordsByEmail.containsKey(normalizedEmail)) {
            redirectAttributes.addFlashAttribute("error", "An account already exists with this email.");
            return "redirect:/signup";
        }

        // Placeholder in-memory registration for frontend flow demo.
        // Member 2 can replace this with persistent user service + secure password hashing.
        citizenPasswordsByEmail.put(normalizedEmail, password);
        redirectAttributes.addFlashAttribute("success", "Account created successfully. Please sign in.");
        redirectAttributes.addFlashAttribute("selectedRole", "CITIZEN");
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "role", required = false, defaultValue = "CITIZEN") String role,
            Model model
    ) {
        model.addAttribute("selectedRole", role);
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam("role") String role,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            RedirectAttributes redirectAttributes
    ) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Email and password are required.");
            redirectAttributes.addFlashAttribute("selectedRole", role);
            return "redirect:/login";
        }

        // Placeholder login flow for team integration:
        // Member 2 can replace this with real authentication service checks.
        if ("AUTHORITY".equalsIgnoreCase(role)) {
            redirectAttributes.addFlashAttribute("success", "Welcome back, authority user.");
            return "redirect:/authority/dashboard";
        }

        String normalizedEmail = email.trim().toLowerCase();
        String registeredPassword = citizenPasswordsByEmail.get(normalizedEmail);
        if (registeredPassword != null && !registeredPassword.equals(password)) {
            redirectAttributes.addFlashAttribute("error", "Invalid citizen credentials.");
            redirectAttributes.addFlashAttribute("selectedRole", "CITIZEN");
            return "redirect:/login";
        }

        String userId = normalizedEmail.replace("@", "_").replace(".", "_");
        redirectAttributes.addFlashAttribute("success", "Welcome back, citizen user.");
        return "redirect:/citizen/my-reports?userId=" + userId;
    }
}

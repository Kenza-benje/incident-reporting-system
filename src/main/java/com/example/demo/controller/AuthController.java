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
    private final Map<String, CitizenAccount> citizenAccountsByEmail = new ConcurrentHashMap<>();
    private final Map<String, CitizenAccount> citizenAccountsById = new ConcurrentHashMap<>();

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(
            @RequestParam("fullName") String fullName,
            @RequestParam("citizenId") String citizenId,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes
    ) {
        if (fullName == null || fullName.isBlank() ||
                citizenId == null || citizenId.isBlank() ||
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

        String normalizedId = citizenId.trim().toUpperCase();
        if (citizenAccountsById.containsKey(normalizedId)) {
            redirectAttributes.addFlashAttribute("error", "This citizen ID is already in use.");
            return "redirect:/signup";
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (citizenAccountsByEmail.containsKey(normalizedEmail)) {
            redirectAttributes.addFlashAttribute("error", "An account already exists with this email.");
            return "redirect:/signup";
        }

        // Placeholder in-memory registration for frontend flow demo.
        // Member 2 can replace this with persistent user service + secure password hashing.
        CitizenAccount account = new CitizenAccount(
                fullName.trim(),
                normalizedId,
                normalizedEmail,
                password
        );
        citizenAccountsById.put(normalizedId, account);
        citizenAccountsByEmail.put(normalizedEmail, account);
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
            @RequestParam(value = "loginMethod", required = false, defaultValue = "EMAIL") String loginMethod,
            @RequestParam("identifier") String identifier,
            @RequestParam("password") String password,
            RedirectAttributes redirectAttributes
    ) {
        if (identifier == null || identifier.isBlank() || password == null || password.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Identifier and password are required.");
            redirectAttributes.addFlashAttribute("selectedRole", role);
            return "redirect:/login";
        }

        // Placeholder login flow for team integration:
        // Member 2 can replace this with real authentication service checks.
        if ("AUTHORITY".equalsIgnoreCase(role)) {
            redirectAttributes.addFlashAttribute("success", "Welcome back, authority user.");
            return "redirect:/authority/dashboard";
        }

        CitizenAccount account;
        if ("ID".equalsIgnoreCase(loginMethod)) {
            account = citizenAccountsById.get(identifier.trim().toUpperCase());
        } else {
            account = citizenAccountsByEmail.get(identifier.trim().toLowerCase());
        }

        if (account == null || !account.password().equals(password)) {
            redirectAttributes.addFlashAttribute("error", "Invalid citizen credentials.");
            redirectAttributes.addFlashAttribute("selectedRole", "CITIZEN");
            return "redirect:/login";
        }

        String userId = account.citizenId().toLowerCase();
        redirectAttributes.addFlashAttribute("success", "Welcome back, citizen user.");
        // Continue to citizen reporting flow after successful login.
        return "redirect:/citizen/report?userId=" + userId;
    }

    private record CitizenAccount(
            String fullName,
            String citizenId,
            String email,
            String password
    ) {
    }
}

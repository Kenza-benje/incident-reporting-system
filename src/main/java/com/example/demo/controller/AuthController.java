package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

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

        String userId = email.trim().toLowerCase().replace("@", "_").replace(".", "_");
        redirectAttributes.addFlashAttribute("success", "Welcome back, citizen user.");
        return "redirect:/citizen/my-reports?userId=" + userId;
    }
}

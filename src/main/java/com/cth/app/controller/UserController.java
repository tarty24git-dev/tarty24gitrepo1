package com.cth.app.controller;

import com.cth.app.model.User;
import com.cth.app.service.AppConfigService;
import com.cth.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users")
@Tag(name = "User Management", description = "Endpoints for user maintenance, lock/unlock, password reset, and registration")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AppConfigService appConfigService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("appName", appConfigService.getAppName());
        model.addAttribute("users", userService.getAllUsers());
        return "users";
    }

    @GetMapping("/new")
    @Operation(summary = "Get user creation form page")
    public String createUserForm(Model model) {
        model.addAttribute("appName", appConfigService.getAppName());
        model.addAttribute("mfaEnabled", appConfigService.isMfaEnabled());
        model.addAttribute("user", new User());
        return "user-create";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User " + user.getUsername() + " created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating user: " + e.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/toggle-lock")
    public String toggleLock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.toggleLockStatus(id);
            String status = user.isAccountNonLocked() ? "unlocked" : "locked";
            redirectAttributes.addFlashAttribute("successMessage", "User " + user.getUsername() + " is now " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, @RequestParam String newPassword, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.resetPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password reset successfully for " + user.getUsername());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    // REST endpoints for Swagger UI / API consumers
    @ResponseBody
    @GetMapping("/api/list")
    @Operation(summary = "Get list of all users")
    public List<User> apiListUsers() {
        return userService.getAllUsers();
    }
}

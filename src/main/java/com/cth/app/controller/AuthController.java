package com.cth.app.controller;

import com.cth.app.model.User;
import com.cth.app.service.AppConfigService;
import com.cth.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        model.addAttribute("appName", appConfigService.getAppName());
        model.addAttribute("authMode", appConfigService.getProperty("app.auth.mode", "LOCAL"));
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password, or account locked/expired.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("appName", appConfigService.getAppName());
        model.addAttribute("mfaEnabled", appConfigService.isMfaEnabled());
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Account created successfully! You can now log in.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }
}

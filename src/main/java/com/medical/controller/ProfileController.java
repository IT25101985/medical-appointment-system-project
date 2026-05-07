package com.medical.controller;

import com.medical.entity.User;
import com.medical.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@Controller
@RequestMapping("/patient")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "patient/dashboard";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName()).orElse(null);
        model.addAttribute("user", user);
        return "patient/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("user") User updatedUser, Authentication authentication) {
        User currentUser = userService.findByUsername(authentication.getName()).orElse(null);
        if (currentUser != null) {
            userService.updateUserProfile(currentUser, updatedUser);
        }
        return "redirect:/patient/dashboard";
    }
}
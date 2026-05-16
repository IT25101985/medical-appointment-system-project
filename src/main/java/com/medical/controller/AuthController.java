package com.medical.controller;

import com.medical.entity.User;
import com.medical.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.core.Authentication;
import java.security.Principal;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private com.medical.service.DoctorService doctorService;

    @GetMapping("/")
    public String index(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        model.addAttribute("doctors", doctorService.getAllDoctors());
        return "index";
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new Patient());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") Patient user) {
        // Log to console for debugging
        System.out.println("Registering user: " + user.getUsername());

        user.setRole("ROLE_PATIENT"); // Default role for portal registration
        userService.saveUser(user);

        return "redirect:/login?registered";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@org.springframework.web.bind.annotation.RequestParam String username,
                                        @org.springframework.web.bind.annotation.RequestParam String newPassword) {
        java.util.Optional<User> optUser = userService.findByUsername(username);
        if (optUser.isPresent()) {
            User user = optUser.get();
            user.setPassword(newPassword);
            userService.saveUser(user);
            return "redirect:/forgot-password?success";
        }
        return "redirect:/forgot-password?error";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"))) {
            return "redirect:/doctor/dashboard";
        } else {
            return "redirect:/patient/dashboard";
        }
    }
}

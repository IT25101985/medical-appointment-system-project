package com.medical.controller;

import com.medical.entity.Patient;
import com.medical.entity.User;
import com.medical.repository.UserRepository;
import com.medical.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/templates/patient")
public class PatientProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    public String viewProfile() {
        return "redirect:/patient/dashboard?section=profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("user") Patient updatedUser, Principal principal) {
        if (principal != null) {
            Optional<User> optUser = userService.findByUsername(principal.getName());
            if (optUser.isPresent()) {
                User currentUser = optUser.get();
                userService.updateUserProfile(currentUser, updatedUser);

                // Updates Patient specific fields
                if (currentUser instanceof Patient) {
                    Patient p = (Patient) currentUser;
                    p.setBloodGroup(updatedUser.getBloodGroup());
                    p.setBloodPressure(updatedUser.getBloodPressure());
                    p.setHeartRate(updatedUser.getHeartRate());
                    p.setEmergencyContact(updatedUser.getEmergencyContact());
                    userRepository.save(p);
                }

                if(updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
                    currentUser.setPassword(updatedUser.getPassword());
                    userService.saveUser(currentUser);
                }
            }
        }
        return "redirect:/patient/dashboard?section=profile&success";
    }


    @PostMapping("/delete-account")
    public String deleteAccount(Principal principal) {
        if (principal != null) {
            Optional<User> optUser = userService.findByUsername(principal.getName());
            optUser.ifPresent(user -> userService.deleteUser(user));
        }
        return "redirect:/logout";
    }

}
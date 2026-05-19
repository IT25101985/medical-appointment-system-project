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
@RequestMapping("/patient")
public class PatientProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    public String viewProfile() {
        return "redirect:/patient/dashboard?section=profile";
    }

}
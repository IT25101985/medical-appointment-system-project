package com.medical.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private com.medical.service.UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(java.security.Principal principal, Model model) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
            java.util.Optional<com.medical.entity.User> optUser = userService.findByUsername(principal.getName());
            if (optUser.isPresent()) {
                model.addAttribute("profileImage", optUser.get().getProfileImage());
            }
        }
        return "doctor/dashboard";
    }

    @PostMapping("/update-picture")
    public String updatePicture(@org.springframework.web.bind.annotation.RequestParam("imageUrl") String imageUrl, java.security.Principal principal) {
        if (principal != null) {
            java.util.Optional<com.medical.entity.User> optUser = userService.findByUsername(principal.getName());
            if (optUser.isPresent()) {
                com.medical.entity.User user = optUser.get();
                user.setProfileImage(imageUrl);
                userService.saveUser(user);
            }
        }
        return "redirect:/doctor/dashboard";
    }

    @GetMapping("/appointments")
    public String viewAppointments(Model model, java.security.Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            // Filter appointments for the currently logged in doctor
            java.util.List<com.medical.entity.Appointment> doctorAppointments = appointmentService.getAllAppointments().stream()
                .filter(a -> a.getDoctor() != null && a.getDoctor().getUser() != null && username.equals(a.getDoctor().getUser().getUsername()))
                .collect(java.util.stream.Collectors.toList());
            model.addAttribute("appointments", doctorAppointments);
        } else {
            model.addAttribute("appointments", new java.util.ArrayList<>());
        }
        return "doctor/view-appointments";
    }
    @GetMapping("/records")
    public String patientRecords(Model model, java.security.Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "doctor/records";
    }

    @GetMapping("/consultations")
    public String consultations(Model model, java.security.Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "doctor/consultations";
    }
}

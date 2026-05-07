package com.medical.controller;

import com.medical.entity.User;
import com.medical.entity.Appointment; // Ensure this import exists
import com.medical.service.AppointmentService;
import com.medical.service.DoctorService;
import com.medical.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Optional;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "patient/dashboard";
    }

    @GetMapping("/book-appointment")
    public String bookAppointmentForm(Model model, Authentication authentication) {
        User patient = null;
        Optional<User> userOptional = userService.findByUsername(authentication.getName());

        if (userOptional.isPresent()) {
            patient = userOptional.get();
        }

        Appointment appointment = new Appointment();

        if (patient != null) {
            appointment.setContactEmail(patient.getEmail());
            appointment.setContactPhone(patient.getPhoneNo());
        }

        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("appointment", appointment);
        return "patient/book-appointment";
    }

    @PostMapping("/book-appointment")
    public String bookAppointment(@ModelAttribute("appointment") Appointment appointment,
                                  @RequestParam(value = "medicalRecord", required = false) MultipartFile medicalRecord,
                                  Authentication authentication) {

        User patient = null;
        Optional<User> userOptional = userService.findByUsername(authentication.getName());

        if (userOptional.isPresent()) {
            patient = userOptional.get();
        }

        appointment.setPatient(patient);
        appointment.setStatus("SCHEDULED");
        appointmentService.saveAppointment(appointment);

        // Handle File Upload if present
        if (medicalRecord != null && !medicalRecord.isEmpty()) {
            try {
                String fileName = medicalRecord.getOriginalFilename();
                // Logic to save the file to a directory could go here
                System.out.println("Uploaded medical record: " + fileName);
            } catch (Exception e) {
                System.err.println("File upload failed: " + e.getMessage());
            }
        }

        return "redirect:/patient/history";
    }

    @GetMapping("/history")
    public String history(Model model, Authentication authentication) {
        User patient = null;
        Optional<User> userOptional = userService.findByUsername(authentication.getName());
        if (userOptional.isPresent()) {
            patient = userOptional.get();
        }

        model.addAttribute("appointments", appointmentService.getAppointmentsForPatient(patient));
        return "patient/history";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Authentication authentication) {
        User user = null;
        Optional<User> userOpt = userService.findByUsername(authentication.getName());
        if (userOpt.isPresent()) {
            user = userOpt.get();
        }

        model.addAttribute("user", user);
        return "patient/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("user") User updatedUser, Authentication authentication) {
        User currentUser = null;
        Optional<User> userOpt = userService.findByUsername(authentication.getName());

        if (userOpt.isPresent()) {
            currentUser = userOpt.get();
        }

        if (currentUser != null) {
            userService.updateUserProfile(currentUser, updatedUser);
        }
        return "redirect:/patient/dashboard";
    }
}
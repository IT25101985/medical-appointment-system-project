package com.medical.controller;

import com.medical.entity.Appointment;
import com.medical.entity.User;
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
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
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

    @Autowired
    private com.medical.service.PdfService pdfService;

    @Autowired
    private com.medical.service.NotificationService notificationService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "patient/dashboard";
    }

    @GetMapping("/book-appointment")
    public String bookAppointmentForm(Model model, Authentication authentication) {
        // Find the currently logged-in user
        User patient = null;
        Optional<User> userOptional = userService.findByUsername(authentication.getName());

        if (userOptional.isPresent()) {
            patient = userOptional.get();
        }

        // Create an empty appointment object to bind to the form
        Appointment appointment = new Appointment();

        // Auto-fill form details if the patient exists
        if (patient != null) {
            appointment.setContactEmail(patient.getEmail());
            appointment.setContactPhone(patient.getPhoneNo());
        }

        // Pass data to the HTML view
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("appointment", appointment);
        return "patient/book-appointment"; // Returns the HTML page

    }

    @PostMapping("/book-appointment")
    public String bookAppointment(@ModelAttribute("appointment") Appointment appointment,
                                  @RequestParam(value = "medicalRecord", required = false) MultipartFile medicalRecord,
                                  Authentication authentication) {

        // Get the current logged-in patient
        User patient = null;
        Optional<User> userOptional = userService.findByUsername(authentication.getName());

        if (userOptional.isPresent()) {
            patient = userOptional.get();
        }

        //  Set the patient to the appointment and save to database
        appointment.setPatient(patient);
        appointment.setStatus("SCHEDULED");
        appointmentService.saveAppointment(appointment);

        // Handle File Upload if present
        if (medicalRecord != null && !medicalRecord.isEmpty()) {
            try {
                // Save file logic here
                String fileName = medicalRecord.getOriginalFilename();
                System.out.println("Uploaded medical record: " + fileName + " for Patient: " + (patient != null ? patient.getUsername() : "Unknown"));
            } catch (Exception e) {
                System.err.println("File upload failed: " + e.getMessage());
            }
        }

        // Send Notifications
        if (patient != null && appointment.getAppointmentDate() != null) {
            String dateStr = appointment.getAppointmentDate().toString();
            notificationService.sendBookingConfirmationEmail(appointment.getContactEmail(), patient.getFullName(), dateStr);
            notificationService.sendSmsReminder(appointment.getContactPhone(), patient.getFullName(), dateStr);
        }
        return "redirect:/patient/history";
    }

    @GetMapping("/history")
    public String history(Model model, Authentication authentication) {
        // Find patient
        User patient = null;
        Optional<User> userOptional = userService.findByUsername(authentication.getName());
        if (userOptional.isPresent()) {
            patient = userOptional.get();
        }

        // Send patient's appointments to the HTML page
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

    @GetMapping("/appointment/{id}/pdf")
    public ResponseEntity<InputStreamResource> downloadSummary(@PathVariable Long id, Authentication authentication) {
        // Find current patient
        User patient = null;
        Optional<User> userOpt = userService.findByUsername(authentication.getName());

        if (userOpt.isPresent()) {
            patient = userOpt.get();
        }

        // Basic FOR-LOOP instead of complex Streams (Easy for beginners!)
        Appointment foundAppointment = null;
        for (Appointment a : appointmentService.getAllAppointments()) {

            if (a.getId().equals(id)) {
                // Ensure the patient only downloads their own appointment
                if (patient == null || patient.getId().equals(a.getPatient().getId())) {
                    foundAppointment = a;
                    break; // Found it, exit loop
                }
            }
        }

        if (foundAppointment != null) {
            // Generate the PDF file
            ByteArrayInputStream bis = pdfService.generateAppointmentSummary(foundAppointment);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=booking_summary_" + id + ".pdf");
            // Return file to user
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(bis));
        } else {
            // If appointment not found, return 404 Error
            return ResponseEntity.notFound().build();
        }
    }
}

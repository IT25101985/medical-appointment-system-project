package com.medical.controller;

import com.medical.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;

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

        User patient = userService.findByUsername(authentication.getName())
                .orElse(null);

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

        User patient = userService.findByUsername(authentication.getName())
                .orElse(null);

        appointment.setPatient(patient);
        appointment.setStatus("SCHEDULED");

        appointmentService.saveAppointment(appointment);

        if (medicalRecord != null && !medicalRecord.isEmpty()) {
            try {
                String fileName = medicalRecord.getOriginalFilename();
                System.out.println("Uploaded medical record: " + fileName);
            } catch (Exception e) {
                System.err.println("File upload failed: " + e.getMessage());
            }
        }

        if (patient != null && appointment.getAppointmentDate() != null) {
            String dateStr = appointment.getAppointmentDate().toString();

            notificationService.sendBookingConfirmationEmail(
                    appointment.getContactEmail(),
                    patient.getFullName(),
                    dateStr
            );

            notificationService.sendSmsReminder(
                    appointment.getContactPhone(),
                    patient.getFullName(),
                    dateStr
            );
        }

        return "redirect:/patient/history";
    }

    @GetMapping("/history")
    public String history(Model model, Authentication authentication) {

        User patient = userService.findByUsername(authentication.getName())
                .orElse(null);

        model.addAttribute("appointments",
                appointmentService.getAppointmentsForPatient(patient));

        return "patient/history";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Authentication authentication) {

        User user = userService.findByUsername(authentication.getName())
                .orElse(null);

        model.addAttribute("user", user);
        return "patient/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("user") User updatedUser,
                                Authentication authentication) {

        User currentUser = userService.findByUsername(authentication.getName())
                .orElse(null);

        if (currentUser != null) {
            userService.updateUserProfile(currentUser, updatedUser);
        }

        return "redirect:/patient/dashboard";
    }

    @GetMapping("/appointment/{id}/pdf")
    public ResponseEntity<InputStreamResource> downloadSummary(
            @PathVariable Long id,
            Authentication authentication) {

        User patient = userService.findByUsername(authentication.getName())
                .orElse(null);

        Appointment foundAppointment = null;

        for (Appointment a : appointmentService.getAllAppointments()) {
            if (a.getId().equals(id)) {
                if (patient == null || patient.getId().equals(a.getPatient().getId())) {
                    foundAppointment = a;
                    break;
                }
            }
        }

        if (foundAppointment != null) {
            ByteArrayInputStream bis =
                    pdfService.generateAppointmentSummary(foundAppointment);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition",
                    "attachment; filename=booking_summary_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new InputStreamResource(bis));
        }

        return ResponseEntity.notFound().build();
    }
}
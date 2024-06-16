package com.medical.controller;

import com.medical.entity.Appointment;
import com.medical.entity.User;
import com.medical.service.AppointmentService;
import com.medical.service.DoctorService;
import com.medical.service.UserService;
import com.medical.service.PdfService;
import com.medical.service.NotificationService;
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
import java.util.Optional;

@Controller
@RequestMapping("/patient")
public class BookingController {

    @Autowired private DoctorService doctorService;
    @Autowired private AppointmentService appointmentService;
    @Autowired private UserService userService;
    @Autowired private PdfService pdfService;
    @Autowired private NotificationService notificationService;

    // 1. Book Appointment Form
    @GetMapping("/book-appointment")
    public String bookAppointmentForm(Model model, Authentication authentication) {
        User patient = userService.findByUsername(authentication.getName()).orElse(null);
        Appointment appointment = new Appointment();
        if (patient != null) {
            appointment.setContactEmail(patient.getEmail());
            appointment.setContactPhone(patient.getPhoneNo());
        }
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("appointment", appointment);
        return "patient/book-appointment";
    }

    // 2. Submit Booking
    @PostMapping("/book-appointment")
    public String bookAppointment(@ModelAttribute("appointment") Appointment appointment,
                                  @RequestParam(value = "medicalRecord", required = false) MultipartFile medicalRecord,
                                  Authentication authentication) {
        User patient = userService.findByUsername(authentication.getName()).orElse(null);
        appointment.setPatient(patient);
        appointment.setStatus("SCHEDULED");
        appointmentService.saveAppointment(appointment);

        // Notifications
        if (patient != null && appointment.getAppointmentDate() != null) {
            String dateStr = appointment.getAppointmentDate().toString();
            notificationService.sendBookingConfirmationEmail(appointment.getContactEmail(), patient.getFullName(), dateStr);
        }
        return "redirect:/patient/history";
    }

    // 3. History Page
    @GetMapping("/history")
    public String history(Model model, Authentication authentication) {
        User patient = userService.findByUsername(authentication.getName()).orElse(null);
        model.addAttribute("appointments", appointmentService.getAppointmentsForPatient(patient));
        return "patient/history";
    }

    // 4. Download PDF
    @GetMapping("/appointment/{id}/pdf")
    public ResponseEntity<InputStreamResource> downloadSummary(@PathVariable Long id, Authentication authentication) {
        // ... (Neenga kudutha PDF download logic inga varum)
        return null; // Logic short-ah irukka ippo null kuduthuruken
    }
}
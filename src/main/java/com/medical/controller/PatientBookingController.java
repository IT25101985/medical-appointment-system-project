package com.medical.controller;

import com.medical.entity.*;
import com.medical.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/patient")
public class PatientBookingController {

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        Optional<User> optUser = userService.findByUsername(principal.getName());
        if (optUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = optUser.get();

        // Build a patient object for display in the template
        Patient patient;
        if (user instanceof Patient) {
            patient = (Patient) user;
        } else {
            // User registered but no Patient record yet - create a display-only object
            patient = new Patient();
            patient.setId(user.getId());
            patient.setUsername(user.getUsername());
            patient.setFullName(user.getFullName());
            patient.setEmail(user.getEmail());
            patient.setPhoneNo(user.getPhoneNo());
            patient.setAddress(user.getAddress());
            patient.setProfileImage(user.getProfileImage());
        }

        model.addAttribute("patient", patient);
        model.addAttribute("user", patient);
        model.addAttribute("username", principal.getName());

        // Booking form
        Appointment app = new Appointment();
        app.setContactEmail(user.getEmail());
        app.setContactPhone(user.getPhoneNo());
        model.addAttribute("appointment", app);

        // Doctors list
        List<Doctor> doctors = doctorService.getAllDoctors();
        model.addAttribute("doctors", doctors != null ? doctors : new ArrayList<>());

        // Appointments & maps - use the real DB user for querying
        List<Appointment> appointments;
        try {
            appointments = appointmentService.getAppointmentsForPatient(user);
            if (appointments == null) appointments = new ArrayList<>();
        } catch (Exception e) {
            appointments = new ArrayList<>();
        }

        Map<Long, Invoice> invoiceMap = new HashMap<>();
        Map<Long, MedicalRecord> recordMap = new HashMap<>();
        for (Appointment a : appointments) {
            try {
                invoiceService.getInvoiceByAppointment(a).ifPresent(inv -> invoiceMap.put(a.getId(), inv));
                medicalRecordService.getRecordByAppointment(a).ifPresent(rec -> recordMap.put(a.getId(), rec));
            } catch (Exception ignored) {}
        }

        model.addAttribute("appointments", appointments);
        model.addAttribute("invoiceMap", invoiceMap);
        model.addAttribute("recordMap", recordMap);

        // Latest prescription for dashboard overview
        try {
            List<MedicalRecord> recentRecords = medicalRecordService.getRecentRecordsByPatient(user);
            model.addAttribute("latestRecord", (recentRecords != null && !recentRecords.isEmpty()) ? recentRecords.get(0) : null);
        } catch (Exception e) {
            model.addAttribute("latestRecord", null);
        }

        return "patient/dashboard";
    }

    @GetMapping("/book-appointment")
    public String bookAppointmentForm() {
        return "redirect:/patient/dashboard?section=booking";
    }

    @PostMapping("/book-appointment")
    public String saveAppointment(@ModelAttribute("appointment") Appointment appointment,
                                  @RequestParam(value = "medicalRecord", required = false) org.springframework.web.multipart.MultipartFile file,
                                  Principal principal) {
        if (principal != null) {
            Optional<User> optUser = userService.findByUsername(principal.getName());
            if (optUser.isPresent()) {
                appointment.setPatient(optUser.get());
                appointment.setStatus("SCHEDULED");

                if (appointment.getDoctor() != null && appointment.getDoctor().getId() != null) {
                    doctorService.getDoctorById(appointment.getDoctor().getId()).ifPresent(appointment::setDoctor);
                }

                appointmentService.saveAppointment(appointment);
                return "redirect:/patient/dashboard?section=history&success";
            }
        }
        return "redirect:/login";
    }

    @GetMapping("/history")
    public String viewHistory() {
        return "redirect:/patient/dashboard?section=history";
    }

    @PostMapping("/appointment/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id, Principal principal) {
        if (principal != null) {
            Optional<Appointment> optApp = appointmentService.getAppointmentById(id);
            if (optApp.isPresent() && optApp.get().getPatient().getUsername().equals(principal.getName())) {
                Appointment app = optApp.get();
                app.setStatus("CANCELLED");
                appointmentService.saveAppointment(app);
            }
        }
        return "redirect:/patient/dashboard?section=history&cancelled";
    }

    @PostMapping("/appointment/{id}/delete")
    public String deleteAppointment(@PathVariable Long id, Principal principal) {
        if (principal != null) {
            Optional<Appointment> optApp = appointmentService.getAppointmentById(id);
            if (optApp.isPresent() && optApp.get().getPatient().getUsername().equals(principal.getName())) {
                appointmentService.deleteAppointment(optApp.get());
            }
        }
        return "redirect:/patient/dashboard?section=history&deleted";
    }

    @GetMapping("/appointment/{id}/reschedule")
    public String rescheduleForm(@PathVariable Long id) {
        return "redirect:/patient/dashboard?section=booking&reschedule=" + id;
    }

    @GetMapping("/appointment/{id}/record")
    public String viewPrescription(@PathVariable Long id, Model model, Principal principal) {
        if (principal != null) {
            Optional<Appointment> optApp = appointmentService.getAppointmentById(id);
            if (optApp.isPresent() && optApp.get().getPatient().getUsername().equals(principal.getName())) {
                Optional<MedicalRecord> optRecord = medicalRecordService.getRecordByAppointment(optApp.get());
                if (optRecord.isPresent()) {
                    model.addAttribute("record", optRecord.get());
                    return "patient/view-record";
                }
            }
        }
        return "redirect:/patient/dashboard?section=history&error=no_record";
    }
}

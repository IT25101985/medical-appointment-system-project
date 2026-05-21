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

    // Dashboard mapping has been moved to PatientProfileController for workload distribution and advanced analytics integration.

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
                if (appointment.getId() != null) {
                    Optional<Appointment> existingOpt = appointmentService.getAppointmentById(appointment.getId());
                    if (existingOpt.isPresent() && existingOpt.get().getPatient().getId().equals(optUser.get().getId())) {
                        Appointment existing = existingOpt.get();
                        existing.setAppointmentDate(appointment.getAppointmentDate());
                        existing.setContactEmail(appointment.getContactEmail());
                        existing.setContactPhone(appointment.getContactPhone());
                        if (appointment.getDoctor() != null && appointment.getDoctor().getId() != null) {
                            doctorService.getDoctorById(appointment.getDoctor().getId()).ifPresent(existing::setDoctor);
                        }
                        existing.setStatus("SCHEDULED");
                        appointmentService.saveAppointment(existing);
                        return "redirect:/patient/dashboard?section=history&success";
                    }
                }

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

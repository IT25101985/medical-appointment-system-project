package com.medical.controller;

import com.medical.entity.Appointment;
import com.medical.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/doctor")
public class DoctorScheduleController {

    @Autowired
    private AppointmentService appointmentService;

    // 1. View Appointments (Doctor-oda schedule-ai mattum filter panni kaatta)
    @GetMapping("/appointments")
    public String viewAppointments(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();

            // Logged-in doctor-oda username-ku match aagura appointments mattum edukkirom
            List<Appointment> doctorAppointments = appointmentService.getAllAppointments().stream()
                    .filter(a -> a.getDoctor() != null &&
                            a.getDoctor().getUser() != null &&
                            username.equals(a.getDoctor().getUser().getUsername()))
                    .collect(Collectors.toList());

            model.addAttribute("appointments", doctorAppointments);
        } else {
            model.addAttribute("appointments", new ArrayList<>());
        }
        return "doctor/view-appointments";
    }

    // 2. Patient Records (Schedules kooda vara clinical data)
    @GetMapping("/records")
    public String patientRecords(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "doctor/records";
    }

    // 3. Consultations (Doctor-oda schedule timing-la nadakkura consultations)
    @GetMapping("/consultations")
    public String consultations(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "doctor/consultations";
    }
}
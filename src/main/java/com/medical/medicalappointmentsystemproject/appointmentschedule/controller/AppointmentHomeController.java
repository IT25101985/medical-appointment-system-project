package com.medical.medicalappointmentsystemproject.appointmentschedule.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppointmentHomeController {

    @GetMapping("/appointmentschedule")
    public String home() {
        return "redirect:/appointmentschedule" +
                "/appointments/dashboard";
    }
}
package com.medical.controller;

import com.medical.entity.Doctor;
import com.medical.entity.User;
import com.medical.service.AppointmentService;
import com.medical.service.DoctorService;
import com.medical.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.medical.entity.Appointment;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private com.medical.service.NotificationService notificationService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard"; // Usually admin dashboard view
    }

    @GetMapping("/doctors")
    public String doctorManagement(Model model) {
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("newDoctor", new Doctor());
        model.addAttribute("newUser", new User());
        return "admin/doctor-management";
    }

    @PostMapping("/doctors/add")
    public String addDoctor(@ModelAttribute("newDoctor") Doctor doctor, @ModelAttribute("newUser") User user) {
        user.setRole("ROLE_DOCTOR");
        User savedUser = userService.saveUser(user);
        doctor.setUser(savedUser);
        doctorService.saveDoctor(doctor);
        return "redirect:/admin/doctors";
    }

    @PostMapping("/doctors/{id}/delete")
    public String deleteDoctor(@PathVariable Long id) {
        Optional<Doctor> d = doctorService.getDoctorById(id);
        if(d.isPresent()){
            Doctor doc = d.get();
            for(Appointment a : appointmentService.getAppointmentsForDoctor(doc)) {
                appointmentService.deleteAppointment(a);
            }
            User u = doc.getUser();
            doctorService.deleteDoctor(id);
            if(u != null) {
                userService.deleteUser(u);
            }
        }
        return "redirect:/admin/doctors";
    }

    @GetMapping("/appointments")
    public String appointments(Model model) {
        model.addAttribute("appointments", appointmentService.getAllAppointments());
        model.addAttribute("doctors", doctorService.getAllDoctors());
        return "admin/appointments";
    }

    @PostMapping("/appointments/{id}/checkin")
    public String checkInAppointment(@PathVariable Long id) {
        
        // Loop through all appointments to find the matching ID (Beginner approach)
        Appointment foundAppointment = null;
        for (Appointment a : appointmentService.getAllAppointments()) {
            if (a.getId().equals(id)) {
                foundAppointment = a;
                break; // We found the appointment, exit the loop early
            }
        }
        
        // If we successfully found the appointment, change its status
        if (foundAppointment != null) {
            foundAppointment.setStatus("COMPLETED");
            appointmentService.saveAppointment(foundAppointment); // Save to database
        }
        return "redirect:/admin/appointments"; // Reload page
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id) {
        
        // Find appointment using a traditional for-loop
        Appointment foundAppointment = null;
        for (Appointment a : appointmentService.getAllAppointments()) {
            if (a.getId().equals(id)) {
                foundAppointment = a;
                break;
            }
        }
        
        // Check if the appointment exists
        if (foundAppointment != null) {
            // 1. Update Database Status
            foundAppointment.setStatus("CANCELLED");
            appointmentService.saveAppointment(foundAppointment);
            
            // 2. Send cancellation email
            try {
                notificationService.sendCancellationEmail(
                    foundAppointment.getContactEmail(), 
                    foundAppointment.getPatient().getFullName(), 
                    foundAppointment.getAppointmentDate().toString()
                );
            } catch (Exception e) {
                // Ignore failure so the system doesn't crash if the email server is offline
                System.out.println("Email ignored");
            }
        }
        return "redirect:/admin/appointments";
    }
}

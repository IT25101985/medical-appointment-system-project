package com.medical.controller;

import com.medical.entity.Patient;
import com.medical.entity.User;
import com.medical.entity.Appointment;
import com.medical.entity.Invoice;
import com.medical.entity.MedicalRecord;
import com.medical.entity.Doctor;
import com.medical.repository.UserRepository;
import com.medical.service.UserService;
import com.medical.service.AppointmentService;
import com.medical.service.MedicalRecordService;
import com.medical.service.InvoiceService;
import com.medical.service.DoctorService;
import com.medical.repository.PatientRepository;

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
public class PatientProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/profile")
    public String viewProfile() {
        return "redirect:/patient/dashboard?section=profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("user") Patient updatedUser, Principal principal) {
        if (principal != null) {
            Optional<User> optUser = userService.findByUsername(principal.getName());
            if (optUser.isPresent()) {
                User currentUser = optUser.get();
                userService.updateUserProfile(currentUser, updatedUser);

                // Updates Patient specific fields
                if (currentUser instanceof Patient) {
                    Patient p = (Patient) currentUser;
                    p.setBloodGroup(updatedUser.getBloodGroup());
                    p.setBloodPressure(updatedUser.getBloodPressure());
                    p.setHeartRate(updatedUser.getHeartRate());
                    p.setEmergencyContact(updatedUser.getEmergencyContact());
                    userRepository.save(p);
                }

                if(updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
                    currentUser.setPassword(updatedUser.getPassword());
                    userService.saveUser(currentUser);
                }
            }
        }
        return "redirect:/patient/dashboard?section=profile&success";
    }


    @PostMapping("/delete-account")
    public String deleteAccount(Principal principal) {
        if (principal != null) {
            Optional<User> optUser = userService.findByUsername(principal.getName());
            optUser.ifPresent(user -> userService.deleteUser(user));
        }
        return "redirect:/logout";
    }

    // Overriding / extended handling of Dashboard to add complex tracking analytics pipelines
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

        Patient patient;
        Optional<Patient> optPatient = patientRepository.findById(user.getId());
        if (optPatient.isPresent()) {
            patient = optPatient.get();
        } else {
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

    }

    Appointment app = new Appointment();
        app.setContactEmail(user.getEmail());
        app.setContactPhone(user.getPhoneNo());
        model.addAttribute("appointment", app);

    List<Doctor> doctors = doctorService.getAllDoctors();
        model.addAttribute("doctors", doctors != null ? doctors : new ArrayList<>());

    List<Appointment> appointments;
        try {
        appointments = appointmentService.getAppointmentsForPatient(user);
        if (appointments == null) appointments = new ArrayList<>();
    } catch (Exception e) {
        appointments = new ArrayList<>();


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

        try {
            List<MedicalRecord> recentRecords = medicalRecordService.getRecentRecordsByPatient(user);
            model.addAttribute("latestRecord", (recentRecords != null && !recentRecords.isEmpty()) ? recentRecords.get(0) : null);
        } catch (Exception e) {
            model.addAttribute("latestRecord", null);
        }

        return "patient/dashboard";
    }


}
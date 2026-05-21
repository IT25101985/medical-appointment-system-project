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
import org.springframework.jdbc.core.JdbcTemplate;

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
    private JdbcTemplate jdbcTemplate;

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


                String bp = updatedUser.getBloodPressure();
                if (bp != null) {
                    bp = bp.trim();
                }
                if (bp != null && !bp.isEmpty()) {
                    if (!bp.matches("\\s*\\d{2,3}\\s*/\\s*\\d{2,3}\\s*")) {
                        return "redirect:/patient/dashboard?section=profile&error=invalid_bp";
                    }
                    bp = bp.replaceAll("\\s+", "");
                }


                String emergencyContact = updatedUser.getEmergencyContact();
                if (emergencyContact != null) {
                    emergencyContact = emergencyContact.trim();
                }
                if (emergencyContact != null && !emergencyContact.isEmpty()) {
                    String cleanPhone = emergencyContact.replaceAll("[\\s\\-()]", "");
                    if (!cleanPhone.matches("\\d{10}")) {
                        return "redirect:/patient/dashboard?section=profile&error=invalid_phone";
                    }
                    emergencyContact = cleanPhone;
                }


                String rawPassword = null;
                if (updatedUser.getPassword() != null && !updatedUser.getPassword().trim().isEmpty()) {
                    rawPassword = updatedUser.getPassword().trim();
                    if (rawPassword.length() < 8 || !rawPassword.matches(".*\\d.*") || !rawPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",.<>/?].*")) {
                        return "redirect:/patient/dashboard?section=profile&error=weak_password";
                    }
                }


                Optional<Patient> optPatient = patientRepository.findById(currentUser.getId());
                if (optPatient.isPresent()) {
                    Patient p = optPatient.get();
                    p.setFullName(updatedUser.getFullName());
                    p.setPhoneNo(updatedUser.getPhoneNo());
                    p.setAddress(updatedUser.getAddress());
                    p.setProfileImage(updatedUser.getProfileImage());
                    if (updatedUser.getBloodGroup() != null) p.setBloodGroup(updatedUser.getBloodGroup());
                    if (bp != null) p.setBloodPressure(bp.isEmpty() ? null : bp);
                    if (updatedUser.getHeartRate() != null) {
                        String hr = updatedUser.getHeartRate().trim();
                        p.setHeartRate(hr.isEmpty() ? null : hr);
                    }
                    if (emergencyContact != null) p.setEmergencyContact(emergencyContact.isEmpty() ? null : emergencyContact);

                    if (rawPassword != null) {
                        p.setPassword(rawPassword);
                        userService.saveUser(p);
                    } else {
                        patientRepository.save(p);
                    }
                } else {
                    currentUser.setFullName(updatedUser.getFullName());
                    currentUser.setPhoneNo(updatedUser.getPhoneNo());
                    currentUser.setAddress(updatedUser.getAddress());
                    currentUser.setProfileImage(updatedUser.getProfileImage());
                    if (rawPassword != null) {
                        currentUser.setPassword(rawPassword);
                    }
                    userService.saveUser(currentUser);

                    String newBg = updatedUser.getBloodGroup();
                    String newBp = (bp != null && !bp.isEmpty()) ? bp : null;
                    String newHr = null;
                    if (updatedUser.getHeartRate() != null) {
                        newHr = updatedUser.getHeartRate().trim().isEmpty() ? null : updatedUser.getHeartRate().trim();
                    }
                    String newEc = (emergencyContact != null && !emergencyContact.isEmpty()) ? emergencyContact : null;

                    jdbcTemplate.update("UPDATE users SET user_type = 'PATIENT', blood_group = ?, blood_pressure = ?, heart_rate = ?, emergency_contact = ? WHERE id = ?",
                            newBg, newBp, newHr, newEc, currentUser.getId());
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


    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model, @RequestParam(value = "reschedule", required = false) Long rescheduleId) {
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

        Appointment app = new Appointment();
        if (rescheduleId != null) {
            Optional<Appointment> optApp = appointmentService.getAppointmentById(rescheduleId);
            if (optApp.isPresent() && optApp.get().getPatient().getId().equals(user.getId())) {
                app = optApp.get();
            }
        }
        if (app.getContactEmail() == null) {
            app.setContactEmail(user.getEmail());
        }
        if (app.getContactPhone() == null) {
            app.setContactPhone(user.getPhoneNo());
        }
        model.addAttribute("appointment", app);

        List<Doctor> doctors = doctorService.getAllDoctors();
        model.addAttribute("doctors", doctors != null ? doctors : new ArrayList<>());

        List<Appointment> appointments;
        try {
            appointments = appointmentService.getAppointmentsForPatient(user);
            if (appointments == null) appointments = new ArrayList<>();
        } catch (Exception e) {
            appointments = new ArrayList<>();
        }


        long totalVisits = appointments.stream()
                .filter(a -> "COMPLETED".equals(a.getStatus()))
                .count();

        long cancelledVisits = appointments.stream()
                .filter(a -> "CANCELLED".equals(a.getStatus()))
                .count();


        double attendanceRate = (totalVisits + cancelledVisits > 0)
                ? ((double) totalVisits / (totalVisits + cancelledVisits)) * 100
                : 100.0;


        model.addAttribute("totalVisits", totalVisits);
        model.addAttribute("attendanceRate", String.format("%.1f%%", attendanceRate));


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

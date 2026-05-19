package com.medical.controller;

import com.medical.entity.Appointment;
import com.medical.entity.Doctor;
import com.medical.entity.User;
import com.medical.entity.Feedback;
import com.medical.service.AppointmentService;
import com.medical.service.DoctorService;
import com.medical.service.UserService;
import com.medical.service.MedicalRecordService;
import com.medical.service.FeedbackService;
import com.medical.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private MedicalRecordService medicalRecordService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private UserRepository userRepository;

    // --- OOP Helper Method (Encapsulated Reusable Logic) ---
    // This helper method finds an appointment by its ID using a clean, readable loop.
    private Appointment findAppointmentById(Long id) {
        for (Appointment a : appointmentService.getAllAppointments()) {
            if (a.getId().equals(id)) {
                return a; // Return the object immediately when found
            }
        }
        return null; // Return null if no matching appointment is found
    }

    @GetMapping("/export")
    public void exportData(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"users_export.csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("ID,Username,Full Name,Role,Email,Phone");

        for (User u : userService.getAllUsers()) {
            String email = (u.getEmail() != null) ? u.getEmail() : "";
            String phone = (u.getPhoneNo() != null) ? u.getPhoneNo() : "";

            writer.printf("%d,%s,%s,%s,%s,%s\n",
                    u.getId(), u.getUsername(), u.getFullName(), u.getRole(), email, phone);
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Appointment> allAppointments = appointmentService.getAllAppointments();
        List<User> allUsers = userService.getAllUsers();
        List<Doctor> allDoctors = doctorService.getAllDoctors();

        // Status counters
        long total = allAppointments.size();
        long cancelled = allAppointments.stream().filter(a -> "CANCELLED".equals(a.getStatus())).count();
        long completed = allAppointments.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();
        long scheduled = allAppointments.stream().filter(a -> "SCHEDULED".equals(a.getStatus())).count();

        // Today's appointments
        LocalDate today = LocalDate.now();
        long todayCount = allAppointments.stream()
                .filter(a -> a.getAppointmentDate() != null && a.getAppointmentDate().toLocalDate().equals(today))
                .count();

        // Revenue calculation (completed * average fee)
        double averageFee = allDoctors.stream()
                .mapToDouble(d -> d.getConsultationFee() != null ? d.getConsultationFee() : 500.0)
                .average().orElse(500.0);
        double revenue = averageFee * completed;

        // Peak booking hours statistics
        Map<Integer, Long> hourlyStats = allAppointments.stream()
                .filter(a -> a.getAppointmentDate() != null)
                .collect(Collectors.groupingBy(a -> a.getAppointmentDate().getHour(), Collectors.counting()));

        long patientsCount = allUsers.stream().filter(u -> "ROLE_PATIENT".equals(u.getRole())).count();
        long doctorsCount = allDoctors.size();

        // Fetch recent 5 appointments
        List<Appointment> recentAppointments = allAppointments.stream()
                .sorted((a, b) -> {
                    if (a.getId() == null || b.getId() == null) return 0;
                    return b.getId().compareTo(a.getId());
                })
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("totalAppointments", total);
        model.addAttribute("cancelledCount", cancelled);
        model.addAttribute("completedCount", completed);
        model.addAttribute("scheduledCount", scheduled);
        model.addAttribute("todayCount", todayCount);
        model.addAttribute("estimatedRevenue", String.format("%.0f", revenue));
        model.addAttribute("patientsCount", patientsCount);
        model.addAttribute("doctorsCount", doctorsCount);
        model.addAttribute("hourlyStats", hourlyStats);
        model.addAttribute("recentAppointments", recentAppointments);
        model.addAttribute("allDoctors", allDoctors);

        return "admin/dashboard";
    }

    @GetMapping("/doctors")
    public String doctorManagement(Model model) {
        model.addAttribute("doctors", doctorService.getAllDoctors());
        model.addAttribute("newDoctor", new Doctor());
        model.addAttribute("newUser", new User());
        return "admin/doctor-management";
    }

    @PostMapping("/doctors/add")
    public String addDoctor(@ModelAttribute("newDoctor") Doctor doctor,
                            @ModelAttribute("newUser") User user,
                            @RequestParam(required = false) String profileImage) {
        try {
            user.setRole("ROLE_DOCTOR");
            if (profileImage != null && !profileImage.isEmpty()) {
                user.setProfileImage(profileImage);
            }

            User savedUser = userService.saveUser(user);
            doctor.setUser(savedUser);
            doctor.setName(user.getFullName());

            doctorService.saveDoctor(doctor);
            return "redirect:/admin/doctors?add_success";
        } catch (Exception e) {
            return "redirect:/admin/doctors?error=Registration failed: " + e.getMessage();
        }
    }

    @PostMapping("/doctors/{id}/delete")
    public String deleteDoctor(@PathVariable Long id) {
        Optional<Doctor> d = doctorService.getDoctorById(id);

        if (d.isPresent()) {
            Doctor doc = d.get();

            // 1. Manually remove doctor's appointments to prevent Foreign Key constraints
            for (Appointment a : appointmentService.getAppointmentsForDoctor(doc)) {
                appointmentService.deleteAppointment(a);
            }

            // 2. Manually remove doctor's feedbacks
            for (Feedback f : feedbackService.getFeedbackByDoctor(doc)) {
                feedbackService.deleteFeedback(f.getId());
            }

            User u = doc.getUser();
            doctorService.deleteDoctor(id);

            // 3. Delete the associated user account and patient side feedbacks
            if (u != null) {
                for (Feedback f : feedbackService.getFeedbackByPatient(u)) {
                    feedbackService.deleteFeedback(f.getId());
                }
                userService.deleteUser(u);
            }
        }
        return "redirect:/admin/doctors";
    }

    @PostMapping("/doctors/{id}/update")
    public String updateDoctor(@PathVariable Long id,
                               @RequestParam(required = false) String name,
                               @RequestParam(required = false) String phoneNo,
                               @RequestParam(required = false) String specialization,
                               @RequestParam(required = false) String experience,
                               @RequestParam(required = false) String clinicHours,
                               @RequestParam(required = false) Double consultationFee,
                               @RequestParam(required = false) String profileImage) {
        try {
            Optional<Doctor> optDoc = doctorService.getDoctorById(id);
            if (optDoc.isPresent()) {
                Doctor doc = optDoc.get();

                if (name != null && !name.trim().isEmpty()) {
                    doc.setName(name.trim());
                }
                doc.setSpecialization(specialization);
                doc.setExperience(experience);
                doc.setClinicHours(clinicHours);
                if (consultationFee != null) {
                    doc.setConsultationFee(consultationFee);
                }

                if (doc.getUser() != null) {
                    User u = doc.getUser();
                    if (phoneNo != null) u.setPhoneNo(phoneNo);
                    if (name != null && !name.trim().isEmpty()) u.setFullName(name.trim());
                    if (profileImage != null && !profileImage.isEmpty()) u.setProfileImage(profileImage);
                    userRepository.save(u);
                }
                doctorService.saveDoctor(doc);
            }
            return "redirect:/admin/doctors?update_success";
        } catch (Exception e) {
            return "redirect:/admin/doctors?error";
        }
    }

    @PostMapping("/doctors/{id}/reset-password")
    public String resetDoctorPassword(@PathVariable Long id) {
        Optional<Doctor> optDoc = doctorService.getDoctorById(id);
        if (optDoc.isPresent() && optDoc.get().getUser() != null) {
            User u = optDoc.get().getUser();
            u.setPassword("123"); // Resets to default password '123'
            userService.saveUser(u);
        }
        return "redirect:/admin/doctors?reset_success";
    }

    @GetMapping("/appointments")
    public String appointments(Model model) {
        model.addAttribute("appointments", appointmentService.getAllAppointments());
        model.addAttribute("doctors", doctorService.getAllDoctors());
        return "admin/appointments";
    }

    @PostMapping("/appointments/{id}/checkin")
    public String checkInAppointment(@PathVariable Long id) {
        // Reusing our custom OOP helper method to locate the appointment smoothly
        Appointment foundAppointment = findAppointmentById(id);

        if (foundAppointment != null) {
            foundAppointment.setStatus("COMPLETED");
            appointmentService.saveAppointment(foundAppointment);
        }
        return "redirect:/admin/appointments";
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id) {
        // Reusing our custom OOP helper method to locate the appointment smoothly
        Appointment foundAppointment = findAppointmentById(id);

        if (foundAppointment != null) {
            foundAppointment.setStatus("CANCELLED");
            appointmentService.saveAppointment(foundAppointment);
            // Notification logic successfully removed from here
        }
        return "redirect:/admin/appointments";
    }

    @GetMapping("/users")
    public String userManagement(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        Optional<User> optUser = userService.getUserById(id);
        if (optUser.isPresent()) {
            User user = optUser.get();

            // Clean up patient-side relational records first
            for (Appointment a : appointmentService.getAppointmentsForPatient(user)) {
                appointmentService.deleteAppointment(a);
            }
            for (Feedback f : feedbackService.getFeedbackByPatient(user)) {
                feedbackService.deleteFeedback(f.getId());
            }

            // If the user happens to be a doctor, handle the Doctor data dependencies safely
            Optional<Doctor> optDoc = doctorService.getDoctorByUser(user);
            if (optDoc.isPresent()) {
                Doctor doc = optDoc.get();
                for (Appointment a : appointmentService.getAppointmentsForDoctor(doc)) {
                    appointmentService.deleteAppointment(a);
                }
                for (Feedback f : feedbackService.getFeedbackByDoctor(doc)) {
                    feedbackService.deleteFeedback(f.getId());
                }
                doctorService.deleteDoctor(doc.getId());
            }

            userService.deleteUser(user);
        }
        return "redirect:/admin/users";
    }
}
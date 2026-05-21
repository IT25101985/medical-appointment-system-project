package com.medical.controller;

import com.medical.entity.User;
import com.medical.service.AppointmentService;
import com.medical.service.DoctorService;
import com.medical.service.UserService;
import com.medical.service.MedicalRecordService;
import com.medical.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;
import java.util.List;

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
    private com.medical.service.NotificationService notificationService;

    @Autowired
    private com.medical.repository.UserRepository userRepository;

    @GetMapping("/export")
    public void exportData(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"users_export.csv\"");
        java.io.PrintWriter writer = response.getWriter();
        writer.println("ID,Username,Full Name,Role,Email,Phone");
        for (User u : userService.getAllUsers()) {
            writer.printf("%d,%s,%s,%s,%s,%s\n",
                    u.getId(), u.getUsername(), u.getFullName(), u.getRole(),
                    u.getEmail() != null ? u.getEmail() : "",
                    u.getPhoneNo() != null ? u.getPhoneNo() : "");
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Appointment> allAppointments = appointmentService.getAllAppointments();
        List<User> allUsers = userService.getAllUsers();
        List<Doctor> allDoctors = doctorService.getAllDoctors();

        // Status counts
        long total = allAppointments.size();
        long cancelled = allAppointments.stream().filter(a -> "CANCELLED".equals(a.getStatus())).count();
        long completed = allAppointments.stream().filter(a -> "COMPLETED".equals(a.getStatus())).count();
        long scheduled = allAppointments.stream().filter(a -> "SCHEDULED".equals(a.getStatus())).count();

        // Today's appointments
        java.time.LocalDate today = java.time.LocalDate.now();
        long todayCount = allAppointments.stream()
                .filter(a -> a.getAppointmentDate() != null && a.getAppointmentDate().toLocalDate().equals(today))
                .count();

        // Revenue estimate (completed × avg fee)
        double revenue = allDoctors.stream()
                .mapToDouble(d -> d.getConsultationFee() != null ? d.getConsultationFee() : 500.0)
                .average().orElse(500.0) * completed;

        // Peak booking hours
        java.util.Map<Integer, Long> hourlyStats = allAppointments.stream()
                .filter(a -> a.getAppointmentDate() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        a -> a.getAppointmentDate().getHour(), java.util.stream.Collectors.counting()));

        // User demographics
        long patientsCount = allUsers.stream().filter(u -> "ROLE_PATIENT".equals(u.getRole())).count();
        long doctorsCount = allDoctors.size();

        // Recent 5 appointments
        List<Appointment> recentAppointments = allAppointments.stream()
                .sorted((a, b) -> { if(a.getId()==null||b.getId()==null) return 0; return b.getId().compareTo(a.getId()); })
                .limit(5)
                .collect(java.util.stream.Collectors.toList());

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
            doctor.setName(user.getFullName()); // Fix null name issue
            doctorService.saveDoctor(doctor);
            return "redirect:/admin/doctors?add_success";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/doctors?error=Registration failed: " + e.getMessage();
        }
    }

    @PostMapping("/doctors/{id}/delete")
    public String deleteDoctor(@PathVariable Long id) {
        Optional<Doctor> d = doctorService.getDoctorById(id);
        if(d.isPresent()){
            Doctor doc = d.get();
            // Delete related appointments manually to prevent FK constraint issues
            for(Appointment a : appointmentService.getAppointmentsForDoctor(doc)) {
                appointmentService.deleteAppointment(a);
            }
            // Delete related feedbacks manually
            for(com.medical.entity.Feedback f : feedbackService.getFeedbackByDoctor(doc)) {
                feedbackService.deleteFeedback(f.getId()); // assuming this exists, if not we'll need to add it or ignore
            }
            User u = doc.getUser();
            doctorService.deleteDoctor(id);
            if(u != null) {
                // Delete patient feedbacks if any
                for(com.medical.entity.Feedback f : feedbackService.getFeedbackByPatient(u)) {
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
                if (name != null && !name.trim().isEmpty()) doc.setName(name.trim());
                doc.setSpecialization(specialization);
                doc.setExperience(experience);
                doc.setClinicHours(clinicHours);
                if (consultationFee != null) doc.setConsultationFee(consultationFee);

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
            e.printStackTrace();
            return "redirect:/admin/doctors?error";
        }
    }

    @PostMapping("/doctors/{id}/reset-password")
    public String resetDoctorPassword(@PathVariable Long id) {
        Optional<Doctor> optDoc = doctorService.getDoctorById(id);
        if (optDoc.isPresent() && optDoc.get().getUser() != null) {
            User u = optDoc.get().getUser();
            // Reset to default password '123'
            u.setPassword("123");
            userService.saveUser(u); // saveUser will encode '123'
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

    // --- User Management (Member 1) ---
    @GetMapping("/users")
    public String userManagement(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users"; // You'll need to create this HTML file
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        Optional<User> optUser = userService.getUserById(id);
        if (optUser.isPresent()) {
            User user = optUser.get();

            // Clean up patient side relationships first
            for(Appointment a : appointmentService.getAppointmentsForPatient(user)) {
                appointmentService.deleteAppointment(a);
            }
            for(com.medical.entity.Feedback f : feedbackService.getFeedbackByPatient(user)) {
                feedbackService.deleteFeedback(f.getId());
            }

            // If user is a doctor, we need to handle the Doctor entity relationship
            Optional<Doctor> optDoc = doctorService.getDoctorByUser(user);
            if (optDoc.isPresent()) {
                Doctor doc = optDoc.get();
                for(Appointment a : appointmentService.getAppointmentsForDoctor(doc)) {
                    appointmentService.deleteAppointment(a);
                }
                for(com.medical.entity.Feedback f : feedbackService.getFeedbackByDoctor(doc)) {
                    feedbackService.deleteFeedback(f.getId());
                }
                doctorService.deleteDoctor(doc.getId());
            }

            userService.deleteUser(user);
        }
        return "redirect:/admin/users";
    }
}

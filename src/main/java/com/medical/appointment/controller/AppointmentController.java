package com.medical.appointment.controller;

import com.medical.appointment.entity.Appointment;
import com.medical.appointment.entity.Schedule;
import com.medical.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // =========================================
    // ===== DASHBOARD =====
    // =========================================
    @GetMapping
    public String dashboard(Model model) {
        try {
            long total = appointmentService
                    .getAllAppointments().size();
            long pending = appointmentService
                    .countByStatus("PENDING");
            long confirmed = appointmentService
                    .countByStatus("CONFIRMED");
            long cancelled = appointmentService
                    .countByStatus("CANCELLED");
            List<Appointment> upcoming = appointmentService
                    .getUpcomingAppointments();

            model.addAttribute("totalAppointments", total);
            model.addAttribute("pendingCount", pending);
            model.addAttribute("confirmedCount", confirmed);
            model.addAttribute("cancelledCount", cancelled);
            model.addAttribute("upcomingAppointments", upcoming);

            System.out.println("Dashboard loaded - Total: " + total);

        } catch (Exception e) {
            System.err.println("Dashboard error: " + e.getMessage());
            model.addAttribute("totalAppointments", 0);
            model.addAttribute("pendingCount", 0);
            model.addAttribute("confirmedCount", 0);
            model.addAttribute("cancelledCount", 0);
            model.addAttribute("upcomingAppointments",
                    new ArrayList<>());
        }
        return "appointments/dashboard";
    }

    // =========================================
    // ===== CREATE: Show Booking Form =====
    // =========================================
    @GetMapping("/book")
    public String showBookingForm(Model model) {
        model.addAttribute("appointment", new Appointment());
        try {
            model.addAttribute("availableSchedules",
                    appointmentService.getAvailableSchedules());
        } catch (Exception e) {
            model.addAttribute("availableSchedules",
                    new ArrayList<>());
        }
        return "appointments/book-appointment";
    }

    // =========================================
    // ===== CREATE: Process Booking =====
    // =========================================
    @PostMapping("/book")
    public String bookAppointment(
            @Valid @ModelAttribute("appointment")
            Appointment appointment,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== BOOK APPOINTMENT ===");
        System.out.println("Patient: "
                + appointment.getPatientName());
        System.out.println("Doctor: "
                + appointment.getDoctorName());
        System.out.println("Date: "
                + appointment.getAppointmentDate());

        if (result.hasErrors()) {
            System.out.println("Validation errors: "
                    + result.getErrorCount());
            try {
                model.addAttribute("availableSchedules",
                        appointmentService.getAvailableSchedules());
            } catch (Exception e) {
                model.addAttribute("availableSchedules",
                        new ArrayList<>());
            }
            return "appointments/book-appointment";
        }

        try {
            Appointment saved = appointmentService
                    .bookAppointment(appointment);
            System.out.println("Booked OK: ID=" + saved.getId());
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "✅ Appointment booked! ID: #" + saved.getId());
            return "redirect:/appointments/all";

        } catch (Exception e) {
            System.err.println("Book error: " + e.getMessage());
            model.addAttribute("errorMessage",
                    "❌ Booking failed: " + e.getMessage());
            try {
                model.addAttribute("availableSchedules",
                        appointmentService.getAvailableSchedules());
            } catch (Exception ex) {
                model.addAttribute("availableSchedules",
                        new ArrayList<>());
            }
            return "appointments/book-appointment";
        }
    }

    // =========================================
    // ===== READ: All Appointments =====
    // =========================================
    @GetMapping("/all")
    public String viewAllAppointments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String doctor,
            Model model) {

        System.out.println("=== VIEW ALL ===");
        System.out.println("Status filter: " + status);

        List<Appointment> appointments = new ArrayList<>();

        try {
            if (status != null && !status.trim().isEmpty()) {
                appointments = appointmentService
                        .getAppointmentsByStatus(status.trim());
            } else if (doctor != null && !doctor.trim().isEmpty()) {
                appointments = appointmentService
                        .getDoctorAppointments(doctor.trim());
            } else {
                appointments = appointmentService
                        .getAllAppointments();
            }
            System.out.println("Found: " + appointments.size()
                    + " appointments");

        } catch (Exception e) {
            System.err.println("ViewAll error: " + e.getMessage());
            appointments = new ArrayList<>();
        }

        model.addAttribute("appointments", appointments);
        model.addAttribute("selectedStatus", status);
        return "appointments/appointments-list";
    }

    // =========================================
    // ===== READ: My Appointments =====
    // =========================================
    @GetMapping("/my-appointments")
    public String myAppointments(
            @RequestParam(required = false) String email,
            Model model) {

        if (email != null && !email.trim().isEmpty()) {
            try {
                List<Appointment> list = appointmentService
                        .getPatientAppointments(email.trim());
                model.addAttribute("appointments", list);
                System.out.println("Found " + list.size()
                        + " for: " + email);
            } catch (Exception e) {
                System.err.println("MyApt error: " + e.getMessage());
                model.addAttribute("appointments", new ArrayList<>());
            }
            model.addAttribute("searchEmail", email);
        }
        return "appointments/my-appointments";
    }

    // =========================================
    // ===== READ: View Single =====
    // =========================================
    @GetMapping("/view/{id}")
    public String viewAppointment(
            @PathVariable Long id, Model model) {

        System.out.println("=== VIEW ID: " + id + " ===");

        try {
            Optional<Appointment> appointment =
                    appointmentService.getAppointmentById(id);

            if (appointment.isPresent()) {
                model.addAttribute("appointment",
                        appointment.get());
                return "appointments/view-appointment";
            }

            System.out.println("Not found: " + id);

        } catch (Exception e) {
            System.err.println("View error: " + e.getMessage());
        }

        return "redirect:/appointments/all";
    }

    // =========================================
    // ===== UPDATE: Show Reschedule Form =====
    // =========================================
    @GetMapping("/reschedule/{id}")
    public String showRescheduleForm(
            @PathVariable Long id, Model model) {

        System.out.println("=== SHOW RESCHEDULE: " + id + " ===");

        try {
            Optional<Appointment> appointment =
                    appointmentService.getAppointmentById(id);

            if (appointment.isPresent()) {
                model.addAttribute("appointment",
                        appointment.get());
                return "appointments/reschedule";
            }

        } catch (Exception e) {
            System.err.println("ShowReschedule error: "
                    + e.getMessage());
        }

        return "redirect:/appointments/all";
    }

    // =========================================
    // ===== UPDATE: Process Reschedule =====
    // =========================================
    @PostMapping("/reschedule/{id}")
    public String rescheduleAppointment(
            @PathVariable Long id,
            @RequestParam(name = "newDate", required = false)
            String newDate,
            @RequestParam(name = "newTime", required = false)
            String newTime,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== RESCHEDULE POST ===");
        System.out.println("ID       : " + id);
        System.out.println("New Date : " + newDate);
        System.out.println("New Time : " + newTime);

        try {
            // ===== VALIDATE DATE =====
            if (newDate == null || newDate.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "❌ Please select a new date!");
                return "redirect:/appointments/reschedule/" + id;
            }

            // ===== VALIDATE TIME =====
            if (newTime == null || newTime.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "❌ Please select a new time!");
                return "redirect:/appointments/reschedule/" + id;
            }

            // ===== PARSE =====
            LocalDate parsedDate;
            LocalTime parsedTime;

            try {
                parsedDate = LocalDate.parse(newDate.trim());
            } catch (Exception pe) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "❌ Invalid date format: " + newDate);
                return "redirect:/appointments/reschedule/" + id;
            }

            try {
                parsedTime = LocalTime.parse(newTime.trim());
            } catch (Exception pe) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "❌ Invalid time format: " + newTime);
                return "redirect:/appointments/reschedule/" + id;
            }

            // ===== FUTURE DATE CHECK =====
            if (parsedDate.isBefore(LocalDate.now())) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "❌ Please select a future date!");
                return "redirect:/appointments/reschedule/" + id;
            }

            // ===== DO RESCHEDULE =====
            appointmentService.rescheduleAppointment(
                    id, parsedDate, parsedTime);

            System.out.println("Reschedule SUCCESS: " + id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "✅ Appointment #" + id
                            + " rescheduled to "
                            + parsedDate + " at " + parsedTime);

        } catch (Exception e) {
            System.err.println("Reschedule ERROR: "
                    + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "❌ Reschedule failed: " + e.getMessage());
        }

        return "redirect:/appointments/all";
    }

    // =========================================
    // ===== DELETE: Cancel Appointment =====
    // =========================================
    @PostMapping("/cancel/{id}")
    public String cancelAppointment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== CANCEL POST: " + id + " ===");

        try {
            // ===== CHECK EXISTS =====
            Optional<Appointment> opt =
                    appointmentService.getAppointmentById(id);

            if (!opt.isPresent()) {
                System.out.println("Not found: " + id);
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "❌ Appointment #" + id + " not found!");
                return "redirect:/appointments/all";
            }

            Appointment apt = opt.get();
            System.out.println("Current Status: "
                    + apt.getStatus());

            // ===== ALREADY CANCELLED =====
            if ("CANCELLED".equals(apt.getStatus())) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "⚠️ Already cancelled!");
                return "redirect:/appointments/all";
            }

            // ===== DO CANCEL =====
            appointmentService.cancelAppointment(id);

            System.out.println("Cancel SUCCESS: " + id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "✅ Appointment #" + id
                            + " cancelled successfully!");

        } catch (Exception e) {
            System.err.println("Cancel ERROR: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "❌ Cancel failed: " + e.getMessage());
        }

        return "redirect:/appointments/all";
    }

    // =========================================
    // ===== DELETE: Permanent Delete =====
    // =========================================
    @PostMapping("/delete/{id}")
    public String deleteAppointment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== DELETE POST: " + id + " ===");

        try {
            // ===== CHECK EXISTS =====
            if (!appointmentService.getAppointmentById(id)
                    .isPresent()) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "❌ Appointment #" + id + " not found!");
                return "redirect:/appointments/all";
            }

            // ===== DO DELETE =====
            appointmentService.deleteAppointment(id);

            System.out.println("Delete SUCCESS: " + id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "✅ Appointment #" + id
                            + " deleted permanently.");

        } catch (Exception e) {
            System.err.println("Delete ERROR: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "❌ Delete failed: " + e.getMessage());
        }

        return "redirect:/appointments/all";
    }

    // =========================================
    // ===== SCHEDULE: View All =====
    // =========================================
    @GetMapping("/schedules")
    public String viewSchedules(Model model) {
        System.out.println("=== VIEW SCHEDULES ===");
        try {
            List<Schedule> schedules = appointmentService
                    .getAllSchedules();
            model.addAttribute("schedules", schedules);
            System.out.println("Schedules: " + schedules.size());
        } catch (Exception e) {
            System.err.println("Schedules error: " + e.getMessage());
            model.addAttribute("schedules", new ArrayList<>());
        }
        model.addAttribute("newSchedule", new Schedule());
        return "appointments/schedules";
    }

    // =========================================
    // ===== SCHEDULE: Add New =====
    // =========================================
    @PostMapping("/schedules/add")
    public String addSchedule(
            @ModelAttribute("newSchedule") Schedule schedule,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== ADD SCHEDULE ===");
        System.out.println("Doctor: " + schedule.getDoctorName());
        System.out.println("Day: " + schedule.getDayOfWeek());

        try {
            // ===== SET DEFAULTS =====
            if (schedule.getAvailableDate() == null) {
                schedule.setAvailableDate(LocalDate.now());
            }
            schedule.setAvailable(true);

            // ===== VALIDATE =====
            if (schedule.getDoctorName() == null
                    || schedule.getDoctorName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "❌ Doctor name is required!");
                return "redirect:/appointments/schedules";
            }

            if (schedule.getStartTime() == null
                    || schedule.getEndTime() == null) {
                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "❌ Start and end time are required!");
                return "redirect:/appointments/schedules";
            }

            // ===== SAVE =====
            Schedule saved = appointmentService
                    .addSchedule(schedule);
            System.out.println("Schedule saved: ID="
                    + saved.getId());
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "✅ Schedule added for Dr. "
                            + schedule.getDoctorName());

        } catch (Exception e) {
            System.err.println("AddSchedule error: "
                    + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "❌ Add failed: " + e.getMessage());
        }

        return "redirect:/appointments/schedules";
    }

    // =========================================
    // ===== SCHEDULE: Delete =====
    // =========================================
    @PostMapping("/schedules/delete/{id}")
    public String deleteSchedule(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== DELETE SCHEDULE: " + id + " ===");

        try {
            appointmentService.deleteSchedule(id);
            System.out.println("Schedule deleted: " + id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "✅ Schedule removed successfully.");
        } catch (Exception e) {
            System.err.println("DeleteSchedule error: "
                    + e.getMessage());
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "❌ Delete failed: " + e.getMessage());
        }

        return "redirect:/appointments/schedules";
    }

    // =========================================
    // ===== SCHEDULE: Update Status =====
    // =========================================
    @PostMapping("/schedules/toggle/{id}")
    public String toggleSchedule(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== TOGGLE SCHEDULE: " + id + " ===");

        try {
            List<Schedule> all = appointmentService.getAllSchedules();
            Schedule target = all.stream()
                    .filter(s -> s.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "Schedule not found: " + id));

            target.setAvailable(!target.isAvailable());
            appointmentService.updateSchedule(id, target);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "✅ Schedule status updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "❌ Toggle failed: " + e.getMessage());
        }

        return "redirect:/appointments/schedules";
    }
}
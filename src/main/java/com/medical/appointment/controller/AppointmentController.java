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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // Constant lists
    private static final List<String> DOCTORS = Arrays.asList(
            "Dr. Silva", "Dr. Perera", "Dr. Fernando",
            "Dr. Gunawardena", "Dr. Rajapaksa", "Dr. Wickrama"
    );

    private static final List<String> SPECIALIZATIONS = Arrays.asList(
            "Cardiology", "Neurology", "Orthopedics",
            "Pediatrics", "General Medicine", "Dentistry"
    );

    // ==================== DASHBOARD ====================

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("appointments",
                appointmentService.getUpcomingAppointments());
        model.addAttribute("todaysAppointments",
                appointmentService.getTodaysAppointments());
        model.addAttribute("totalBooked",
                appointmentService.countBooked());
        model.addAttribute("totalCompleted",
                appointmentService.countCompleted());
        model.addAttribute("totalCancelled",
                appointmentService.countCancelled());
        model.addAttribute("totalRescheduled",
                appointmentService.countRescheduled());
        model.addAttribute("todayCount",
                appointmentService.countTodaysAppointments());
        return "appointments";
    }

    // ==================== CREATE ====================

    @GetMapping("/book")
    public String showBookingForm(Model model) {
        model.addAttribute("appointment", new Appointment());
        model.addAttribute("doctors", DOCTORS);
        model.addAttribute("specializations", SPECIALIZATIONS);
        model.addAttribute("schedules",
                appointmentService.getAvailableSchedules());
        return "book-appointment";
    }

    @PostMapping("/book")
    public String bookAppointment(
            @Valid @ModelAttribute("appointment")
            Appointment appointment,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("doctors", DOCTORS);
            model.addAttribute("specializations", SPECIALIZATIONS);
            model.addAttribute("schedules",
                    appointmentService.getAvailableSchedules());
            return "book-appointment";
        }

        appointmentService.bookAppointment(appointment);
        redirectAttributes.addFlashAttribute("successMessage",
                "✅ Appointment booked successfully!");
        return "redirect:/appointments";
    }

    // ==================== READ ====================

    @GetMapping("/all")
    public String viewAll(
            Model model,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        List<Appointment> appointments;

        if (search != null && !search.trim().isEmpty()) {
            appointments = appointmentService.searchAppointments(search);
        } else if (status != null && !status.isEmpty()) {
            appointments = appointmentService.getByStatus(status);
        } else {
            appointments = appointmentService.getAllAppointments();
        }

        model.addAttribute("appointments", appointments);
        model.addAttribute("search", search);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("totalBooked",
                appointmentService.countBooked());
        model.addAttribute("totalCompleted",
                appointmentService.countCompleted());
        model.addAttribute("totalCancelled",
                appointmentService.countCancelled());
        return "appointments";
    }

    @GetMapping("/{id}")
    public String viewAppointment(
            @PathVariable Long id, Model model) {
        Optional<Appointment> appointment =
                appointmentService.getAppointmentById(id);

        if (appointment.isPresent()) {
            model.addAttribute("appointment", appointment.get());
            return "appointment-detail";
        }
        return "redirect:/appointments";
    }

    // ==================== UPDATE ====================

    @GetMapping("/reschedule/{id}")
    public String showRescheduleForm(
            @PathVariable Long id, Model model) {
        Optional<Appointment> appointment =
                appointmentService.getAppointmentById(id);

        if (appointment.isPresent()) {
            model.addAttribute("appointment", appointment.get());
            return "reschedule";
        }
        return "redirect:/appointments";
    }

    @PostMapping("/reschedule/{id}")
    public String reschedule(
            @PathVariable Long id,
            @RequestParam String newDate,
            @RequestParam String newTime,
            RedirectAttributes redirectAttributes) {

        LocalDate date = LocalDate.parse(newDate);
        Appointment updated = appointmentService.rescheduleAppointment(
                id, date, newTime);

        if (updated != null) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ Appointment rescheduled to " + newDate +
                            " at " + newTime);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "❌ Appointment not found!");
        }
        return "redirect:/appointments";
    }

    @PostMapping("/status/{id}")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {

        appointmentService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("successMessage",
                "✅ Status updated to: " + status);
        return "redirect:/appointments";
    }

    // ==================== DELETE ====================

    @PostMapping("/cancel/{id}")
    public String cancelAppointment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        boolean result = appointmentService.cancelAppointment(id);
        if (result) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ Appointment cancelled successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "❌ Appointment not found!");
        }
        return "redirect:/appointments";
    }

    @PostMapping("/delete/{id}")
    public String deleteAppointment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        appointmentService.deleteAppointment(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "🗑️ Appointment deleted permanently!");
        return "redirect:/appointments";
    }

    // ==================== SCHEDULE ====================

    @GetMapping("/schedule")
    public String viewSchedule(Model model) {
        model.addAttribute("schedules",
                appointmentService.getAllSchedules());
        model.addAttribute("schedule", new Schedule());
        model.addAttribute("doctors", DOCTORS);
        model.addAttribute("specializations", SPECIALIZATIONS);
        return "reschedule";
    }

    @PostMapping("/schedule/add")
    public String addSchedule(
            @Valid @ModelAttribute("schedule") Schedule schedule,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("schedules",
                    appointmentService.getAllSchedules());
            model.addAttribute("doctors", DOCTORS);
            model.addAttribute("specializations", SPECIALIZATIONS);
            return "reschedule";
        }

        appointmentService.createSchedule(schedule);
        redirectAttributes.addFlashAttribute("successMessage",
                "✅ Schedule added successfully!");
        return "redirect:/appointments/schedule";
    }

    @PostMapping("/schedule/delete/{id}")
    public String deleteSchedule(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        appointmentService.deleteSchedule(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "🗑️ Schedule deleted!");
        return "redirect:/appointments/schedule";
    }

    // File content view
    @GetMapping("/file-log")
    public String viewFileLog(Model model) {
        model.addAttribute("fileContent",
                appointmentService.readAppointmentsFromFile());
        return "file-log";
    }
}
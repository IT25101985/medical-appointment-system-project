package com.medical.medicalappointmentsystemproject.appointmentschedule.controller;

import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Appointment;
import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Appointment.AppointmentStatus;
import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Appointment.AppointmentType;
import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Schedule;
import com.medical.medicalappointmentsystemproject.appointmentschedule.service.AppointmentService;
import com.medical.medicalappointmentsystemproject.appointmentschedule.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/appointmentschedule/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final ScheduleService scheduleService;

    // ============ DASHBOARD ============
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Map<String, Object> stats =
                appointmentService.getDashboardStats();
        List<Appointment> todayList =
                appointmentService.getByDate(LocalDate.now());
        List<Appointment> pendingList =
                appointmentService.getByStatus(
                        AppointmentStatus.PENDING);

        model.addAttribute("stats", stats);
        model.addAttribute("todayAppointments", todayList);
        model.addAttribute("pendingAppointments", pendingList);
        model.addAttribute("pageTitle",
                "Appointment Dashboard");
        return "appointmentschedule/appointment-dashboard";
    }

    // ============ LIST ============
    @GetMapping
    public String listAppointments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false) String search,
            Model model) {

        List<Appointment> appointments;

        if (search != null && !search.trim().isEmpty()) {
            appointments = appointmentService
                    .searchAppointments(search.trim());
            model.addAttribute("filterInfo",
                    "Search: \"" + search + "\"");
            model.addAttribute("searchKeyword", search);
        } else if (status != null && !status.isEmpty()) {
            AppointmentStatus apptStatus =
                    AppointmentStatus.valueOf(status.toUpperCase());
            appointments =
                    appointmentService.getByStatus(apptStatus);
            model.addAttribute("filterInfo",
                    "Status: " + status);
            model.addAttribute("selectedStatus", status);
        } else if (date != null) {
            appointments = appointmentService.getByDate(date);
            model.addAttribute("filterInfo", "Date: " + date);
            model.addAttribute("selectedDate", date);
        } else {
            appointments = appointmentService.getAllAppointments();
        }

        model.addAttribute("appointments", appointments);
        model.addAttribute("statuses",
                AppointmentStatus.values());
        model.addAttribute("pageTitle", "All Appointments");
        return "appointmentschedule/appointment-list";
    }

    // ============ BOOK FORM ============
    @GetMapping("/book")
    public String showBookingForm(
            @RequestParam(required = false) Long scheduleId,
            Model model) {

        Appointment appointment = new Appointment();

        if (scheduleId != null) {
            scheduleService.getScheduleById(scheduleId)
                    .ifPresent(schedule -> {
                        appointment.setDoctorId(
                                schedule.getDoctorId());
                        appointment.setDoctorName(
                                schedule.getDoctorName());
                        appointment.setSpecialization(
                                schedule.getSpecialization());
                        appointment.setAppointmentDate(
                                schedule.getScheduleDate());
                        appointment.setAppointmentTime(
                                schedule.getStartTime());
                        appointment.setSchedule(schedule);
                        model.addAttribute("selectedSchedule",
                                schedule);
                    });
        }

        model.addAttribute("appointment", appointment);
        model.addAttribute("availableSchedules",
                scheduleService.getAvailableSchedules());
        model.addAttribute("appointmentTypes",
                AppointmentType.values());
        model.addAttribute("pageTitle", "Book Appointment");
        return "appointmentschedule/book-appointment";
    }

    // ============ BOOK PROCESS ============
    @PostMapping("/book")
    public String processBooking(
            @Valid @ModelAttribute("appointment")
            Appointment appointment,
            BindingResult result,
            @RequestParam(required = false) Long scheduleId,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("availableSchedules",
                    scheduleService.getAvailableSchedules());
            model.addAttribute("appointmentTypes",
                    AppointmentType.values());
            model.addAttribute("pageTitle",
                    "Book Appointment");
            return "appointmentschedule/book-appointment";
        }

        try {
            if (scheduleId != null) {
                scheduleService.getScheduleById(scheduleId)
                        .ifPresent(appointment::setSchedule);
            }

            Appointment booked =
                    appointmentService.bookAppointment(appointment);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "🎉 Appointment booked! Ref: " +
                            booked.getAppointmentRef() +
                            (booked.getTokenNumber() != null ?
                                    " | Token: #" + booked.getTokenNumber() : ""));

            return "redirect:/appointmentschedule/appointments" +
                    "/confirmation/" + booked.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "❌ " + e.getMessage());
            return "redirect:/appointmentschedule" +
                    "/appointments/book";
        }
    }

    // ============ CONFIRMATION ============
    @GetMapping("/confirmation/{id}")
    public String showConfirmation(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        return appointmentService.getAppointmentById(id)
                .map(appointment -> {
                    model.addAttribute("appointment", appointment);
                    model.addAttribute("pageTitle",
                            "Booking Confirmed");
                    return "appointmentschedule/" +
                            "appointment-confirmation";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute(
                            "errorMessage", "❌ Not found!");
                    return "redirect:/appointmentschedule" +
                            "/appointments";
                });
    }

    // ============ VIEW DETAIL ============
    @GetMapping("/view/{id}")
    public String viewAppointment(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        return appointmentService.getAppointmentById(id)
                .map(appointment -> {
                    model.addAttribute("appointment", appointment);
                    model.addAttribute("pageTitle",
                            "Appointment Details");
                    return "appointmentschedule/appointment-detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute(
                            "errorMessage", "❌ Not found!");
                    return "redirect:/appointmentschedule" +
                            "/appointments";
                });
    }

    // ============ RESCHEDULE FORM ============
    @GetMapping("/reschedule/{id}")
    public String showRescheduleForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        return appointmentService.getAppointmentById(id)
                .map(appointment -> {
                    if (!appointment.isReschedulable()) {
                        redirectAttributes.addFlashAttribute(
                                "errorMessage",
                                "❌ Cannot reschedule. Status: " +
                                        appointment.getStatus());
                        return "redirect:/appointmentschedule" +
                                "/appointments";
                    }
                    model.addAttribute("appointment", appointment);
                    model.addAttribute("availableSchedules",
                            scheduleService.getAvailableSchedules());
                    model.addAttribute("pageTitle",
                            "Reschedule Appointment");
                    return "appointmentschedule/" +
                            "reschedule-appointment";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute(
                            "errorMessage", "❌ Not found!");
                    return "redirect:/appointmentschedule" +
                            "/appointments";
                });
    }

    // ============ RESCHEDULE PROCESS ============
    @PostMapping("/reschedule/{id}")
    public String processReschedule(
            @PathVariable Long id,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate newDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime newTime,
            @RequestParam(required = false) Long newScheduleId,
            RedirectAttributes redirectAttributes) {

        try {
            Appointment rescheduled =
                    appointmentService.rescheduleAppointment(
                            id, newDate, newTime, newScheduleId);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "✅ Rescheduled! New date: " +
                            rescheduled.getFormattedDate());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "❌ " + e.getMessage());
        }
        return "redirect:/appointmentschedule" +
                "/appointments/view/" + id;
    }

    // ============ STATUS UPDATE ============
    @GetMapping("/status/{id}/{status}")
    public String changeStatus(
            @PathVariable Long id,
            @PathVariable String status,
            RedirectAttributes redirectAttributes) {

        try {
            AppointmentStatus newStatus =
                    AppointmentStatus.valueOf(status.toUpperCase());
            appointmentService.updateStatus(id, newStatus);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "✅ Status updated to " + newStatus);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "❌ " + e.getMessage());
        }
        return "redirect:/appointmentschedule/appointments";
    }

    // ============ CANCEL ============
    @GetMapping("/cancel/{id}")
    public String cancelAppointment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            Appointment cancelled =
                    appointmentService.cancelAppointment(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "✅ Appointment " +
                            cancelled.getAppointmentRef() +
                            " cancelled.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "❌ " + e.getMessage());
        }
        return "redirect:/appointmentschedule/appointments";
    }

    // ============ DELETE ============
    @GetMapping("/delete/{id}")
    public String deleteAppointment(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            appointmentService.deleteAppointment(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "✅ Appointment deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "❌ " + e.getMessage());
        }
        return "redirect:/appointmentschedule/appointments";
    }

    // ============ HOME REDIRECT ============
    @GetMapping("/home")
    public String home() {
        return "redirect:/appointmentschedule" +
                "/appointments/dashboard";
    }
}
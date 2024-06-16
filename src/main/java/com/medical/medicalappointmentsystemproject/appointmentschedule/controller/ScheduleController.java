package com.medical.medicalappointmentsystemproject.appointmentschedule.controller;

import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Schedule;
import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Schedule.ScheduleStatus;
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
import java.util.List;

@Controller
@RequestMapping("/appointmentschedule/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // ============ LIST ============
    @GetMapping
    public String listSchedules(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false) String search,
            Model model) {

        List<Schedule> schedules;

        if (doctorId != null) {
            schedules = scheduleService.getSchedulesByDoctor(doctorId);
            model.addAttribute("filterInfo",
                    "Doctor ID: " + doctorId);
        } else if (date != null) {
            schedules = scheduleService.getSchedulesByDate(date);
            model.addAttribute("filterInfo", "Date: " + date);
        } else if (search != null && !search.trim().isEmpty()) {
            schedules = scheduleService.searchByDoctorName(search);
            model.addAttribute("filterInfo",
                    "Search: " + search);
            model.addAttribute("searchKeyword", search);
        } else {
            schedules = scheduleService.getAllSchedules();
        }

        model.addAttribute("schedules", schedules);
        model.addAttribute("todaySchedules",
                scheduleService.getTodaySchedules());
        model.addAttribute("pageTitle", "Schedule Management");
        return "appointmentschedule/schedule-manage";
    }

    // ============ NEW FORM ============
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("schedule", new Schedule());
        model.addAttribute("pageTitle", "Create New Schedule");
        model.addAttribute("isEdit", false);
        return "appointmentschedule/schedule-form";
    }

    // ============ SAVE ============
    @PostMapping("/save")
    public String saveSchedule(
            @Valid @ModelAttribute("schedule") Schedule schedule,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("pageTitle",
                    schedule.getId() == null ?
                            "Create New Schedule" : "Edit Schedule");
            model.addAttribute("isEdit",
                    schedule.getId() != null);
            return "appointmentschedule/schedule-form";
        }

        try {
            if (schedule.getId() == null) {
                scheduleService.createSchedule(schedule);
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "✅ Schedule created successfully!");
            } else {
                scheduleService.updateSchedule(
                        schedule.getId(), schedule);
                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "✅ Schedule updated successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "❌ " + e.getMessage());
        }

        return "redirect:/appointmentschedule/schedules";
    }

    // ============ EDIT FORM ============
    @GetMapping("/edit/{id}")
    public String showEditForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        return scheduleService.getScheduleById(id)
                .map(schedule -> {
                    model.addAttribute("schedule", schedule);
                    model.addAttribute("pageTitle", "Edit Schedule");
                    model.addAttribute("isEdit", true);
                    return "appointmentschedule/schedule-form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute(
                            "errorMessage", "❌ Schedule not found!");
                    return "redirect:/appointmentschedule/schedules";
                });
    }

    // ============ VIEW ============
    @GetMapping("/view/{id}")
    public String viewSchedule(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        return scheduleService.getScheduleById(id)
                .map(schedule -> {
                    model.addAttribute("schedule", schedule);
                    model.addAttribute("pageTitle",
                            "Schedule Details");
                    return "appointmentschedule/schedule-detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute(
                            "errorMessage", "❌ Schedule not found!");
                    return "redirect:/appointmentschedule/schedules";
                });
    }

    // ============ DELETE ============
    @GetMapping("/delete/{id}")
    public String deleteSchedule(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            scheduleService.deleteSchedule(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "✅ Schedule deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "❌ " + e.getMessage());
        }
        return "redirect:/appointmentschedule/schedules";
    }

    // ============ STATUS CHANGE ============
    @GetMapping("/status/{id}/{status}")
    public String changeStatus(
            @PathVariable Long id,
            @PathVariable String status,
            RedirectAttributes redirectAttributes) {

        try {
            ScheduleStatus newStatus =
                    ScheduleStatus.valueOf(status.toUpperCase());
            scheduleService.updateScheduleStatus(id, newStatus);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "✅ Status updated to " + newStatus);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "❌ " + e.getMessage());
        }
        return "redirect:/appointmentschedule/schedules";
    }
}
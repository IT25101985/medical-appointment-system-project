package com.medical.medicalappointmentsystemproject.appointmentschedule.service;

import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Schedule;
import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Schedule.ScheduleStatus;
import com.medical.medicalappointmentsystemproject.appointmentschedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    // ============ CREATE ============

    public Schedule createSchedule(Schedule schedule) {
        log.info("Creating schedule for Doctor: {}",
                schedule.getDoctorName());

        if (schedule.getEndTime().isBefore(schedule.getStartTime()) ||
                schedule.getEndTime().equals(schedule.getStartTime())) {
            throw new IllegalArgumentException(
                    "End time must be after start time");
        }

        if (schedule.getScheduleDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Schedule date cannot be in the past");
        }

        if (schedule.getBookedSlots() == null) {
            schedule.setBookedSlots(0);
        }
        if (schedule.getStatus() == null) {
            schedule.setStatus(ScheduleStatus.AVAILABLE);
        }

        Schedule saved = scheduleRepository.save(schedule);
        log.info("Schedule created with ID: {}", saved.getId());
        return saved;
    }

    // ============ READ ============

    @Transactional(readOnly = true)
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Schedule> getScheduleById(Long id) {
        return scheduleRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getAvailableSchedules() {
        return scheduleRepository.findAvailableSchedules(
                LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByDoctor(Long doctorId) {
        return scheduleRepository.findByDoctorId(doctorId);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByDate(LocalDate date) {
        return scheduleRepository.findByScheduleDate(date);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getAvailableBySpecialization(
            String specialization) {
        return scheduleRepository.findAvailableBySpecialization(
                specialization, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Schedule> getTodaySchedules() {
        return scheduleRepository.findTodaySchedules();
    }

    @Transactional(readOnly = true)
    public List<Schedule> searchByDoctorName(String name) {
        return scheduleRepository.searchByDoctorName(name);
    }

    // ============ UPDATE ============

    public Schedule updateSchedule(Long id, Schedule updated) {
        log.info("Updating schedule ID: {}", id);

        Schedule existing = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Schedule not found: " + id));

        existing.setDoctorId(updated.getDoctorId());
        existing.setDoctorName(updated.getDoctorName());
        existing.setSpecialization(updated.getSpecialization());
        existing.setScheduleDate(updated.getScheduleDate());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setMaxSlots(updated.getMaxSlots());
        existing.setLocation(updated.getLocation());
        existing.setNotes(updated.getNotes());

        if (existing.getBookedSlots() >= existing.getMaxSlots()) {
            existing.setStatus(ScheduleStatus.FULL);
        } else {
            existing.setStatus(ScheduleStatus.AVAILABLE);
        }

        return scheduleRepository.save(existing);
    }

    public Schedule updateScheduleStatus(
            Long id, ScheduleStatus status) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Schedule not found: " + id));
        schedule.setStatus(status);
        return scheduleRepository.save(schedule);
    }

    // ============ DELETE ============

    public void deleteSchedule(Long id) {
        log.info("Deleting schedule ID: {}", id);

        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Schedule not found: " + id));

        if (schedule.getBookedSlots() > 0) {
            throw new IllegalStateException(
                    "Cannot delete schedule with existing bookings");
        }

        scheduleRepository.delete(schedule);
    }

    // ============ SLOT MANAGEMENT ============

    public Schedule bookSlot(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException(
                        "Schedule not found"));

        if (!schedule.hasAvailableSlots()) {
            throw new IllegalStateException(
                    "No available slots");
        }

        schedule.bookSlot();
        return scheduleRepository.save(schedule);
    }

    public Schedule releaseSlot(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException(
                        "Schedule not found"));

        schedule.releaseSlot();
        return scheduleRepository.save(schedule);
    }
}
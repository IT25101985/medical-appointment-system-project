package com.medical.medicalappointmentsystemproject.appointmentschedule.service;

import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Appointment;
import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Appointment.AppointmentStatus;
import com.medical.medicalappointmentsystemproject.appointmentschedule.model.Schedule;
import com.medical.medicalappointmentsystemproject.appointmentschedule.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ScheduleService scheduleService;

    // ============ CREATE ============

    public Appointment bookAppointment(Appointment appointment) {
        log.info("Booking appointment for: {}",
                appointment.getPatientName());

        if (appointmentRepository.existsDuplicate(
                appointment.getPatientId(),
                appointment.getDoctorId(),
                appointment.getAppointmentDate())) {
            throw new IllegalStateException(
                    "You already have an appointment with " +
                            "this doctor on " +
                            appointment.getAppointmentDate());
        }

        if (appointment.getSchedule() != null &&
                appointment.getSchedule().getId() != null) {

            Long scheduleId = appointment.getSchedule().getId();
            Schedule schedule = scheduleService.bookSlot(scheduleId);
            appointment.setSchedule(schedule);

            Integer token = appointmentRepository
                    .getNextTokenNumber(scheduleId);
            appointment.setTokenNumber(token);
        }

        if (appointment.getStatus() == null) {
            appointment.setStatus(AppointmentStatus.PENDING);
        }

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment booked. Ref: {}",
                saved.getAppointmentRef());
        return saved;
    }

    // ============ READ ============

    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Appointment> getByReference(String ref) {
        return appointmentRepository.findByAppointmentRef(ref);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getByPatient(Long patientId) {
        return appointmentRepository
                .findByPatientIdOrderByAppointmentDateDesc(patientId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getByDoctor(Long doctorId) {
        return appointmentRepository
                .findByDoctorIdOrderByAppointmentDateDesc(doctorId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getUpcomingByPatient(Long patientId) {
        return appointmentRepository.findUpcomingByPatient(
                patientId, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Appointment> getByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getByDate(LocalDate date) {
        return appointmentRepository
                .findByAppointmentDateOrderByAppointmentTimeAsc(date);
    }

    @Transactional(readOnly = true)
    public List<Appointment> searchAppointments(String keyword) {
        return appointmentRepository.searchAppointments(keyword);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getByDateRange(
            LocalDate start, LocalDate end) {
        return appointmentRepository.findByDateRange(start, end);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAppointments",
                appointmentRepository.count());
        stats.put("todayAppointments",
                appointmentRepository.countTodayAppointments());
        stats.put("pendingCount",
                appointmentRepository.countByStatus(
                        AppointmentStatus.PENDING));
        stats.put("confirmedCount",
                appointmentRepository.countByStatus(
                        AppointmentStatus.CONFIRMED));
        stats.put("completedCount",
                appointmentRepository.countByStatus(
                        AppointmentStatus.COMPLETED));
        stats.put("cancelledCount",
                appointmentRepository.countByStatus(
                        AppointmentStatus.CANCELLED));
        return stats;
    }

    // ============ UPDATE ============

    public Appointment rescheduleAppointment(
            Long id,
            LocalDate newDate,
            LocalTime newTime,
            Long newScheduleId) {

        log.info("Rescheduling appointment ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Appointment not found: " + id));

        if (!appointment.isReschedulable()) {
            throw new IllegalStateException(
                    "Cannot reschedule. Status: " +
                            appointment.getStatus());
        }

        if (appointment.getSchedule() != null) {
            scheduleService.releaseSlot(
                    appointment.getSchedule().getId());
        }

        if (newScheduleId != null) {
            Schedule newSchedule =
                    scheduleService.bookSlot(newScheduleId);
            appointment.setSchedule(newSchedule);
            Integer newToken = appointmentRepository
                    .getNextTokenNumber(newScheduleId);
            appointment.setTokenNumber(newToken);
        }

        appointment.setAppointmentDate(newDate);
        appointment.setAppointmentTime(newTime);
        appointment.setStatus(AppointmentStatus.RESCHEDULED);

        return appointmentRepository.save(appointment);
    }

    public Appointment updateStatus(
            Long id, AppointmentStatus newStatus) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Appointment not found"));

        appointment.setStatus(newStatus);

        if (newStatus == AppointmentStatus.CANCELLED &&
                appointment.getSchedule() != null) {
            scheduleService.releaseSlot(
                    appointment.getSchedule().getId());
        }

        return appointmentRepository.save(appointment);
    }

    public Appointment updateAppointment(
            Long id, Appointment updated) {

        Appointment existing = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Appointment not found"));

        existing.setPatientName(updated.getPatientName());
        existing.setPatientPhone(updated.getPatientPhone());
        existing.setPatientEmail(updated.getPatientEmail());
        existing.setReason(updated.getReason());
        existing.setNotes(updated.getNotes());
        existing.setAppointmentType(updated.getAppointmentType());

        return appointmentRepository.save(existing);
    }

    // ============ DELETE ============

    public Appointment cancelAppointment(Long id) {
        log.info("Cancelling appointment ID: {}", id);
        return updateStatus(id, AppointmentStatus.CANCELLED);
    }

    public void deleteAppointment(Long id) {
        log.info("Deleting appointment ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Appointment not found"));

        if (appointment.getSchedule() != null &&
                appointment.getStatus() != AppointmentStatus.CANCELLED) {
            scheduleService.releaseSlot(
                    appointment.getSchedule().getId());
        }

        appointmentRepository.delete(appointment);
    }
}
package com.medical.appointment.service;

import com.medical.appointment.entity.Appointment;
import com.medical.appointment.entity.Schedule;
import com.medical.appointment.repository.AppointmentRepository;
import com.medical.appointment.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AppointmentService {

    private static final String FILE_PATH =
            "src/main/resources/data/appointments.txt";

    private static final String LOG_PATH =
            "src/main/resources/data/appointment_log.txt";

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    // ==================== CREATE ====================

    public Appointment bookAppointment(Appointment appointment) {
        appointment.setStatus("BOOKED");
        Appointment saved = appointmentRepository.save(appointment);
        writeToFile(saved);
        logAction("BOOKED", saved);
        return saved;
    }

    public Schedule createSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    // ==================== READ ====================

    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getUpcomingAppointments() {
        return appointmentRepository
                .findByAppointmentDateGreaterThanEqualOrderByAppointmentDateAsc(
                        LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Appointment> getTodaysAppointments() {
        return appointmentRepository
                .findTodaysAppointments(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Appointment> getByStatus(String status) {
        return appointmentRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Appointment> searchAppointments(String keyword) {
        return appointmentRepository.searchByKeyword(keyword);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Schedule> getAvailableSchedules() {
        return scheduleRepository.findByIsAvailableTrue();
    }

    public String readAppointmentsFromFile() {
        StringBuilder sb = new StringBuilder();
        try {
            Path path = Paths.get(FILE_PATH);
            if (Files.exists(path)) {
                Files.readAllLines(path)
                        .forEach(line -> sb.append(line).append("\n"));
            } else {
                sb.append("No records yet. Book an appointment first!");
            }
        } catch (IOException e) {
            sb.append("Error: ").append(e.getMessage());
        }
        return sb.toString();
    }

    // ==================== UPDATE ====================

    public Appointment rescheduleAppointment(Long id,
                                             LocalDate newDate,
                                             String newTime) {
        return appointmentRepository.findById(id).map(apt -> {
            apt.setAppointmentDate(newDate);
            apt.setAppointmentTime(LocalTime.parse(newTime));
            apt.setStatus("RESCHEDULED");
            Appointment updated = appointmentRepository.save(apt);
            logAction("RESCHEDULED", updated);
            return updated;
        }).orElse(null);
    }

    public Appointment updateStatus(Long id, String newStatus) {
        return appointmentRepository.findById(id).map(apt -> {
            apt.setStatus(newStatus);
            Appointment updated = appointmentRepository.save(apt);
            logAction("STATUS->" + newStatus, updated);
            return updated;
        }).orElse(null);
    }

    public Schedule updateSchedule(Long id, Schedule data) {
        return scheduleRepository.findById(id).map(sch -> {
            sch.setDoctorName(data.getDoctorName());
            sch.setAvailableDate(data.getAvailableDate());
            sch.setStartTime(data.getStartTime());
            sch.setEndTime(data.getEndTime());
            sch.setMaxPatients(data.getMaxPatients());
            sch.setAvailable(data.isAvailable());
            sch.setSpecialization(data.getSpecialization());
            return scheduleRepository.save(sch);
        }).orElse(null);
    }

    // ==================== DELETE ====================

    public boolean cancelAppointment(Long id) {
        return appointmentRepository.findById(id).map(apt -> {
            apt.setStatus("CANCELLED");
            appointmentRepository.save(apt);
            logAction("CANCELLED", apt);
            return true;
        }).orElse(false);
    }

    public boolean deleteAppointment(Long id) {
        if (appointmentRepository.existsById(id)) {
            appointmentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean deleteSchedule(Long id) {
        if (scheduleRepository.existsById(id)) {
            scheduleRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ==================== FILE HANDLING ====================

    private void writeToFile(Appointment a) {
        try {
            ensureDir(FILE_PATH);
            try (BufferedWriter bw = new BufferedWriter(
                    new FileWriter(FILE_PATH, true))) {
                bw.write(String.format(
                        "ID:%-4s | Patient:%-20s | Doctor:%-20s | " +
                                "Date:%-12s | Time:%-8s | Status:%s",
                        a.getId(), a.getPatientName(),
                        a.getDoctorName(), a.getAppointmentDate(),
                        a.getAppointmentTime(), a.getStatus()
                ));
                bw.newLine();
                bw.write("-".repeat(95));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Write error: " + e.getMessage());
        }
    }

    private void logAction(String action, Appointment a) {
        try {
            ensureDir(LOG_PATH);
            try (BufferedWriter bw = new BufferedWriter(
                    new FileWriter(LOG_PATH, true))) {
                String ts = LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );
                bw.write(String.format(
                        "[%s] %-20s | ID:%-4s | Patient:%s",
                        ts, action, a.getId(), a.getPatientName()
                ));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Log error: " + e.getMessage());
        }
    }

    private void ensureDir(String filePath) throws IOException {
        Path parent = Paths.get(filePath).getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }

    // ==================== STATS ====================

    public long countBooked() {
        return appointmentRepository.countByStatus("BOOKED");
    }

    public long countCompleted() {
        return appointmentRepository.countByStatus("COMPLETED");
    }

    public long countCancelled() {
        return appointmentRepository.countByStatus("CANCELLED");
    }

    public long countRescheduled() {
        return appointmentRepository.countByStatus("RESCHEDULED");
    }

    public long countTodaysAppointments() {
        return getTodaysAppointments().size();
    }
}
package com.medical.appointment.service;

import com.medical.appointment.entity.Appointment;
import com.medical.appointment.entity.Schedule;
import com.medical.appointment.repository.AppointmentRepository;
import com.medical.appointment.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

// ✅ OOP: ABSTRACTION - hides complex logic from controller
@Service
@Transactional
public class AppointmentService {

    // ✅ FILE HANDLING - inject paths from properties
    @Value("${app.file.appointments:" +
            "src/main/resources/data/appointments.txt}")
    private String appointmentFilePath;

    @Value("${app.file.logs:" +
            "src/main/resources/data/appointment_log.txt}")
    private String logFilePath;

    // ✅ OOP: DEPENDENCY INJECTION
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    // ============================================
    // CREATE OPERATIONS
    // ============================================

    // ✅ C: Book new appointment
    public Appointment bookAppointment(Appointment appointment) {
        appointment.setStatus("BOOKED");
        Appointment saved = appointmentRepository.save(appointment);

        // ✅ FILE HANDLING: Write to file
        writeAppointmentToFile(saved);
        logAction("BOOKED", saved);

        return saved;
    }

    // ✅ C: Create schedule slot
    public Schedule createSchedule(Schedule schedule) {
        Schedule saved = scheduleRepository.save(schedule);
        logScheduleAction("CREATED", saved);
        return saved;
    }

    // ============================================
    // READ OPERATIONS
    // ============================================

    // ✅ R: Get all appointments
    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    // ✅ R: Get by ID
    @Transactional(readOnly = true)
    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    // ✅ R: Get upcoming appointments
    @Transactional(readOnly = true)
    public List<Appointment> getUpcomingAppointments() {
        return appointmentRepository
                .findByAppointmentDateGreaterThanEqualOrderByAppointmentDateAsc(
                        LocalDate.now());
    }

    // ✅ R: Get today's appointments
    @Transactional(readOnly = true)
    public List<Appointment> getTodaysAppointments() {
        return appointmentRepository
                .findTodaysAppointments(LocalDate.now());
    }

    // ✅ R: Get by status
    @Transactional(readOnly = true)
    public List<Appointment> getByStatus(String status) {
        return appointmentRepository.findByStatus(status);
    }

    // ✅ R: Search by keyword
    @Transactional(readOnly = true)
    public List<Appointment> searchAppointments(String keyword) {
        return appointmentRepository.searchByKeyword(keyword);
    }

    // ✅ R: Get all schedules
    @Transactional(readOnly = true)
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    // ✅ R: Get available schedules
    @Transactional(readOnly = true)
    public List<Schedule> getAvailableSchedules() {
        return scheduleRepository.findByIsAvailableTrue();
    }

    // ✅ R: Get schedule by ID
    @Transactional(readOnly = true)
    public Optional<Schedule> getScheduleById(Long id) {
        return scheduleRepository.findById(id);
    }

    // ✅ R: Read file content
    public String readAppointmentsFromFile() {
        StringBuilder content = new StringBuilder();
        try {
            Path path = Paths.get(appointmentFilePath);
            if (Files.exists(path)) {
                List<String> lines = Files.readAllLines(path);
                for (String line : lines) {
                    content.append(line).append("\n");
                }
            } else {
                content.append("No appointment file found.");
            }
        } catch (IOException e) {
            content.append("Error reading file: ").append(e.getMessage());
        }
        return content.toString();
    }

    // ============================================
    // UPDATE OPERATIONS
    // ============================================

    // ✅ U: Reschedule appointment
    public Appointment rescheduleAppointment(Long id,
                                             LocalDate newDate,
                                             String newTime) {
        Optional<Appointment> optional =
                appointmentRepository.findById(id);

        if (optional.isPresent()) {
            Appointment appointment = optional.get();
            appointment.setAppointmentDate(newDate);
            appointment.setAppointmentTime(LocalTime.parse(newTime));
            appointment.setStatus("RESCHEDULED");

            Appointment updated = appointmentRepository.save(appointment);
            logAction("RESCHEDULED", updated);
            updateFileRecord(updated);

            return updated;
        }
        return null;
    }

    // ✅ U: Update appointment status
    public Appointment updateStatus(Long id, String newStatus) {
        Optional<Appointment> optional =
                appointmentRepository.findById(id);

        if (optional.isPresent()) {
            Appointment appointment = optional.get();
            appointment.setStatus(newStatus);
            Appointment updated = appointmentRepository.save(appointment);
            logAction("STATUS -> " + newStatus, updated);
            return updated;
        }
        return null;
    }

    // ✅ U: Update full appointment
    public Appointment updateAppointment(Long id,
                                         Appointment updatedData) {
        Optional<Appointment> optional =
                appointmentRepository.findById(id);

        if (optional.isPresent()) {
            Appointment existing = optional.get();
            existing.setPatientName(updatedData.getPatientName());
            existing.setDoctorName(updatedData.getDoctorName());
            existing.setAppointmentDate(updatedData.getAppointmentDate());
            existing.setAppointmentTime(updatedData.getAppointmentTime());
            existing.setSpecialization(updatedData.getSpecialization());
            existing.setNotes(updatedData.getNotes());
            existing.setPatientEmail(updatedData.getPatientEmail());
            existing.setPatientPhone(updatedData.getPatientPhone());

            Appointment updated = appointmentRepository.save(existing);
            logAction("UPDATED", updated);
            return updated;
        }
        return null;
    }

    // ✅ U: Update schedule
    public Schedule updateSchedule(Long id, Schedule updatedData) {
        Optional<Schedule> optional = scheduleRepository.findById(id);
        if (optional.isPresent()) {
            Schedule existing = optional.get();
            existing.setDoctorName(updatedData.getDoctorName());
            existing.setAvailableDate(updatedData.getAvailableDate());
            existing.setStartTime(updatedData.getStartTime());
            existing.setEndTime(updatedData.getEndTime());
            existing.setMaxPatients(updatedData.getMaxPatients());
            existing.setAvailable(updatedData.isAvailable());
            existing.setSpecialization(updatedData.getSpecialization());
            return scheduleRepository.save(existing);
        }
        return null;
    }

    // ============================================
    // DELETE OPERATIONS
    // ============================================

    // ✅ D: Cancel appointment (soft delete)
    public boolean cancelAppointment(Long id) {
        Optional<Appointment> optional =
                appointmentRepository.findById(id);

        if (optional.isPresent()) {
            Appointment appointment = optional.get();
            appointment.setStatus("CANCELLED");
            appointmentRepository.save(appointment);
            logAction("CANCELLED", appointment);
            return true;
        }
        return false;
    }

    // ✅ D: Hard delete appointment
    public boolean deleteAppointment(Long id) {
        if (appointmentRepository.existsById(id)) {
            appointmentRepository.deleteById(id);
            logAction("HARD_DELETED",
                    new Appointment() {{ setId(id); }});
            return true;
        }
        return false;
    }

    // ✅ D: Delete schedule
    public boolean deleteSchedule(Long id) {
        if (scheduleRepository.existsById(id)) {
            scheduleRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ============================================
    // FILE HANDLING METHODS
    // ============================================

    // ✅ FILE: Write new appointment to file
    private void writeAppointmentToFile(Appointment a) {
        try {
            ensureDirectoryExists(appointmentFilePath);

            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(appointmentFilePath, true))) {

                writer.write(String.format(
                        "ID:%-5s | Patient:%-20s | Doctor:%-20s | " +
                                "Date:%-12s | Time:%-8s | Status:%-12s",
                        a.getId(),
                        a.getPatientName(),
                        a.getDoctorName(),
                        a.getAppointmentDate(),
                        a.getAppointmentTime(),
                        a.getStatus()
                ));
                writer.newLine();
                writer.write("-".repeat(100));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("File write error: " + e.getMessage());
        }
    }

    // ✅ FILE: Log every action
    private void logAction(String action, Appointment a) {
        try {
            ensureDirectoryExists(logFilePath);

            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(logFilePath, true))) {

                String timestamp = LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );

                writer.write(String.format(
                        "[%s] ACTION:%-20s | ID:%-5s | Patient:%s",
                        timestamp, action, a.getId(), a.getPatientName()
                ));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Log error: " + e.getMessage());
        }
    }

    // ✅ FILE: Log schedule action
    private void logScheduleAction(String action, Schedule s) {
        try {
            ensureDirectoryExists(logFilePath);

            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(logFilePath, true))) {

                String timestamp = LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );
                writer.write(String.format(
                        "[%s] SCHEDULE_%s | Doctor:%s | Date:%s",
                        timestamp, action,
                        s.getDoctorName(), s.getAvailableDate()
                ));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Log error: " + e.getMessage());
        }
    }

    // ✅ FILE: Update existing record in file
    private void updateFileRecord(Appointment a) {
        try {
            Path path = Paths.get(appointmentFilePath);
            if (!Files.exists(path)) return;

            List<String> lines = Files.readAllLines(path);
            StringBuilder updated = new StringBuilder();

            for (String line : lines) {
                if (line.startsWith(
                        String.format("ID:%-5s", a.getId()))) {
                    updated.append(String.format(
                            "ID:%-5s | Patient:%-20s | Doctor:%-20s | " +
                                    "Date:%-12s | Time:%-8s | Status:%-12s",
                            a.getId(), a.getPatientName(),
                            a.getDoctorName(), a.getAppointmentDate(),
                            a.getAppointmentTime(), a.getStatus()
                    ));
                } else {
                    updated.append(line);
                }
                updated.append(System.lineSeparator());
            }

            Files.write(path, updated.toString().getBytes());

        } catch (IOException e) {
            System.err.println("File update error: " + e.getMessage());
        }
    }

    // ✅ FILE: Helper - create directory if not exists
    private void ensureDirectoryExists(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (path.getParent() != null &&
                !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
    }

    // ============================================
    // DASHBOARD STATISTICS
    // ============================================

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
package com.medical.appointment.service;

import com.medical.appointment.entity.Appointment;
import com.medical.appointment.entity.Schedule;
import com.medical.appointment.repository.AppointmentRepository;
import com.medical.appointment.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    private static final String BACKUP_FILE = "appointments_backup.txt";

    // ===== CREATE =====
    @Transactional
    public Appointment bookAppointment(Appointment appointment) {
        try {
            if (appointment.getStatus() == null
                    || appointment.getStatus().isEmpty()) {
                appointment.setStatus("PENDING");
            }
            if (appointment.getAppointmentType() == null
                    || appointment.getAppointmentType().isEmpty()) {
                appointment.setAppointmentType("IN_PERSON");
            }

            Appointment saved = appointmentRepository
                    .save(appointment);
            System.out.println("Booked OK: ID=" + saved.getId());

            saveToFile(saved);
            return saved;

        } catch (Exception e) {
            System.err.println("Book error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(
                    "Booking failed: " + e.getMessage());
        }
    }

    // ===== READ =====
    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        try {
            return appointmentRepository.findAll();
        } catch (Exception e) {
            System.err.println("GetAll error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getUpcomingAppointments() {
        try {
            return appointmentRepository
                    .findByAppointmentDateAfterOrderByAppointmentDateAsc(
                            LocalDate.now());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByStatus(String status) {
        try {
            return appointmentRepository.findByStatus(status);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public List<Appointment> getPatientAppointments(String email) {
        try {
            return appointmentRepository.findByPatientEmail(email);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public List<Appointment> getDoctorAppointments(String doctorName) {
        try {
            return appointmentRepository.findByDoctorName(doctorName);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // ===== UPDATE: Reschedule =====
    // Using Direct @Query - bypasses JPA cache issues
    @Transactional
    public Appointment rescheduleAppointment(Long id,
                                             LocalDate newDate,
                                             LocalTime newTime) {
        try {
            System.out.println("=== RESCHEDULE SERVICE ===");
            System.out.println("ID: " + id);
            System.out.println("Date: " + newDate);
            System.out.println("Time: " + newTime);

            // Check exists
            Appointment appointment = appointmentRepository
                    .findById(id)
                    .orElseThrow(() -> new RuntimeException(
                            "Appointment #" + id + " not found"));

            System.out.println("Found: " + appointment.getStatus());

            // Direct query update
            int rows = appointmentRepository
                    .updateDateTimeById(id, newDate, newTime);

            System.out.println("Updated rows: " + rows);

            if (rows == 0) {
                throw new RuntimeException(
                        "Update failed - 0 rows affected");
            }

            // Fetch updated
            Appointment updated = appointmentRepository
                    .findById(id)
                    .orElse(appointment);

            // File update
            try {
                updateFileRecord(updated);
            } catch (Exception fe) {
                System.err.println("File warn: " + fe.getMessage());
            }

            System.out.println("Reschedule OK!");
            return updated;

        } catch (Exception e) {
            System.err.println("Reschedule ERROR: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    // ===== UPDATE: Status =====
    @Transactional
    public Appointment updateStatus(Long id, String status) {
        try {
            int rows = appointmentRepository
                    .updateStatusById(id, status);
            System.out.println("Status updated: " + rows + " rows");

            return appointmentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException(
                            "Not found: " + id));
        } catch (Exception e) {
            throw new RuntimeException(
                    "Status update failed: " + e.getMessage());
        }
    }

    // ===== DELETE: Cancel =====
    // Using Direct @Query
    @Transactional
    public void cancelAppointment(Long id) {
        try {
            System.out.println("=== CANCEL SERVICE ===");
            System.out.println("ID: " + id);

            // Check exists
            Appointment appointment = appointmentRepository
                    .findById(id)
                    .orElseThrow(() -> new RuntimeException(
                            "Appointment #" + id + " not found"));

            System.out.println("Current status: "
                    + appointment.getStatus());

            if ("CANCELLED".equals(appointment.getStatus())) {
                System.out.println("Already cancelled!");
                return;
            }

            // Direct query update
            int rows = appointmentRepository
                    .updateStatusById(id, "CANCELLED");

            System.out.println("Cancelled rows: " + rows);

            if (rows == 0) {
                throw new RuntimeException(
                        "Cancel failed - 0 rows affected");
            }

            System.out.println("Cancel OK!");

        } catch (Exception e) {
            System.err.println("Cancel ERROR: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    // ===== DELETE: Permanent =====
    @Transactional
    public void deleteAppointment(Long id) {
        try {
            System.out.println("=== DELETE SERVICE ID: " + id + " ===");

            if (!appointmentRepository.existsById(id)) {
                throw new RuntimeException(
                        "Appointment #" + id + " not found");
            }

            appointmentRepository.deleteById(id);
            System.out.println("Delete OK!");

        } catch (Exception e) {
            System.err.println("Delete ERROR: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    // ===== SCHEDULE CRUD =====

    @Transactional
    public Schedule addSchedule(Schedule schedule) {
        try {
            if (schedule.getAvailableDate() == null) {
                schedule.setAvailableDate(LocalDate.now());
            }
            schedule.setAvailable(true);
            Schedule saved = scheduleRepository.save(schedule);
            System.out.println("Schedule added: " + saved.getId());
            return saved;
        } catch (Exception e) {
            System.err.println("Schedule add error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(
                    "Schedule add failed: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Schedule> getAllSchedules() {
        try {
            return scheduleRepository.findAll();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public List<Schedule> getAvailableSchedules() {
        try {
            return scheduleRepository.findByIsAvailableTrue();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public List<Schedule> getDoctorSchedule(String doctorName) {
        try {
            return scheduleRepository.findByDoctorName(doctorName);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Transactional
    public Schedule updateSchedule(Long id,
                                   Schedule updatedSchedule) {
        Schedule schedule = scheduleRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Schedule not found: " + id));

        schedule.setDoctorName(updatedSchedule.getDoctorName());
        schedule.setSpecialization(
                updatedSchedule.getSpecialization());
        schedule.setDayOfWeek(updatedSchedule.getDayOfWeek());
        schedule.setStartTime(updatedSchedule.getStartTime());
        schedule.setEndTime(updatedSchedule.getEndTime());
        schedule.setMaxPatients(updatedSchedule.getMaxPatients());
        schedule.setAvailable(updatedSchedule.isAvailable());
        schedule.setRoomNumber(updatedSchedule.getRoomNumber());

        if (updatedSchedule.getAvailableDate() != null) {
            schedule.setAvailableDate(
                    updatedSchedule.getAvailableDate());
        }

        return scheduleRepository.save(schedule);
    }

    @Transactional
    public void deleteSchedule(Long id) {
        try {
            scheduleRepository.deleteById(id);
            System.out.println("Schedule deleted: " + id);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Schedule delete failed: " + e.getMessage());
        }
    }

    // ===== STATISTICS =====
    @Transactional(readOnly = true)
    public long countByStatus(String status) {
        try {
            return appointmentRepository.countByStatus(status);
        } catch (Exception e) {
            return 0L;
        }
    }

    // ===== FILE HANDLING =====
    public void saveToFile(Appointment appointment) {
        try (FileWriter fw = new FileWriter(BACKUP_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            out.println(appointment.toString());

        } catch (IOException e) {
            System.err.println("File save error: " + e.getMessage());
        }
    }

    public void readFromFile() {
        File file = new File(BACKUP_FILE);
        if (!file.exists()) {
            System.out.println("No backup file.");
            return;
        }
        try (BufferedReader br = new BufferedReader(
                new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("Record: " + line);
            }
        } catch (IOException e) {
            System.err.println("File read error: " + e.getMessage());
        }
    }

    public void updateFileRecord(Appointment appointment) {
        File file = new File(BACKUP_FILE);
        if (!file.exists()) {
            saveToFile(appointment);
            return;
        }

        File tempFile = new File("appointments_temp.txt");

        try (BufferedReader reader = new BufferedReader(
                new FileReader(file));
             BufferedWriter writer = new BufferedWriter(
                     new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (appointment.getId() != null
                        && line.startsWith(
                        appointment.getId() + ",")) {
                    writer.write(appointment.toString());
                } else {
                    writer.write(line);
                }
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("File update error: " + e.getMessage());
            return;
        }

        if (file.delete()) {
            tempFile.renameTo(file);
        }
    }
}
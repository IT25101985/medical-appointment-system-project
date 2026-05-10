package com.medical.appointment.repository;

import com.medical.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

// ✅ @Repository annotation is important
@Repository
public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    // Find by patient name - case insensitive
    List<Appointment> findByPatientNameContainingIgnoreCase(
            String patientName);

    // Find by doctor
    List<Appointment> findByDoctorName(String doctorName);

    // Find by status
    List<Appointment> findByStatus(String status);

    // Find by date
    List<Appointment> findByAppointmentDate(LocalDate date);

    // Find upcoming - sorted by date
    List<Appointment>
    findByAppointmentDateGreaterThanEqualOrderByAppointmentDateAsc(
            LocalDate date);

    // Find by specialization
    List<Appointment> findBySpecialization(String specialization);

    // Count by status
    @Query("SELECT COUNT(a) FROM Appointment a " +
            "WHERE a.status = :status")
    long countByStatus(@Param("status") String status);

    // Find today's appointments
    @Query("SELECT a FROM Appointment a " +
            "WHERE a.appointmentDate = :today " +
            "ORDER BY a.appointmentTime ASC")
    List<Appointment> findTodaysAppointments(
            @Param("today") LocalDate today);

    // Search by name or email
    @Query("SELECT a FROM Appointment a WHERE " +
            "LOWER(a.patientName) LIKE " +
            "LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.patientEmail) LIKE " +
            "LOWER(CONCAT('%', :keyword, '%'))")
    List<Appointment> searchByKeyword(
            @Param("keyword") String keyword);
}
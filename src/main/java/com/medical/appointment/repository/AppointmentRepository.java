package com.medical.appointment.repository;

import com.medical.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository
        extends JpaRepository<Appointment, Long> {

    // Find by patient name (case insensitive search)
    List<Appointment> findByPatientNameContainingIgnoreCase(
            String patientName);

    // Find by doctor name
    List<Appointment> findByDoctorName(String doctorName);

    // Find by status
    List<Appointment> findByStatus(String status);

    // Find by date
    List<Appointment> findByAppointmentDate(LocalDate date);

    // Find upcoming - date >= today, ordered by date
    List<Appointment>
    findByAppointmentDateGreaterThanEqualOrderByAppointmentDateAsc(
            LocalDate date);

    // Find by doctor and date
    List<Appointment> findByDoctorNameAndAppointmentDate(
            String doctorName, LocalDate date);

    // Find by specialization
    List<Appointment> findBySpecialization(String specialization);

    // Custom JPQL - count by status
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
    long countByStatus(@Param("status") String status);

    // Custom JPQL - find today's appointments
    @Query("SELECT a FROM Appointment a " +
            "WHERE a.appointmentDate = :today " +
            "ORDER BY a.appointmentTime ASC")
    List<Appointment> findTodaysAppointments(
            @Param("today") LocalDate today);

    // Custom JPQL - search by name or email
    @Query("SELECT a FROM Appointment a WHERE " +
            "LOWER(a.patientName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.patientEmail) LIKE " +
            "LOWER(CONCAT('%', :keyword, '%'))")
    List<Appointment> searchByKeyword(
            @Param("keyword") String keyword);

    // Native MySQL Query - get appointments this month
    @Query(value = "SELECT * FROM appointments " +
            "WHERE MONTH(appointment_date) = MONTH(CURDATE()) " +
            "AND YEAR(appointment_date) = YEAR(CURDATE())",
            nativeQuery = true)
    List<Appointment> findAppointmentsThisMonth();
}